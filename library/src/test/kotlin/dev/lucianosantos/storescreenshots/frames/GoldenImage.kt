package dev.lucianosantos.storescreenshots.frames

import dev.lucianosantos.storescreenshots.ScreenshotRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

/**
 * Holds a rendered frame against a committed capture of what it is supposed to look like.
 *
 * A golden here *is* our own output, unlike the Apple comparison tests, which hold the frame against
 * a real Simulator capture. That is the point: a golden does not say the layout is right, it says
 * the layout has not moved. Regenerate one deliberately — delete the reference, re-run, look at the
 * image, commit it — whenever a change is meant to alter the frame.
 *
 * Compared with tolerance rather than pixel for pixel. Both sides come out of the same renderer, but
 * text antialiasing shifts by a hair across JDK and OS, and an exact match would fail on a machine
 * other than the one that recorded it while catching nothing extra: a device that changed size or
 * moved shifts a percentage of the canvas, not a fringe of a few thousand edge pixels. Canvas
 * dimensions, on the other hand, are asserted exactly.
 */
internal object GoldenImage {

    /** A pixel counts as different when a channel is off by more than this, out of 255. */
    const val ChannelTolerance = 8

    /** At most this fraction of the canvas may differ — 0.2% of 1242x2484 is about 6,200 px. */
    const val DifferingPixelTolerance = 0.002f

    /**
     * The same, for a tilted mockup. A solid device's silhouette is a long diagonal outline rather
     * than four axis-aligned edges, so its antialiased fringe covers several times as many pixels
     * as an upright frame's — on a phone canvas the outline alone is some 6,000 px long. The budget
     * is raised to cover that fringe and no more; a device that actually moved still shifts whole
     * percent of the canvas.
     */
    const val TiltedDifferingPixelTolerance = 0.006f

    /** Mean absolute channel error across the whole canvas. Antialiasing noise sits far below 1. */
    const val MeanErrorTolerance = 1.0f

    /**
     * Asserts the PNG written under [writtenName] matches the committed reference [referenceName].
     * [reportDir] is where the rendered, reference and diff images are written for inspection.
     */
    fun assertMatches(
        writtenName: String,
        referenceName: String,
        reportDir: String = "build/reports/frame-goldens",
        differingPixelTolerance: Float = DifferingPixelTolerance,
        meanErrorTolerance: Float = MeanErrorTolerance,
    ) {
        val rendered = StatusBarReference.opaque(StatusBarReference.read(written(writtenName)))
        val reference = loadReference(referenceName, rendered)

        assertEquals(
            "$referenceName is ${reference.width}x${reference.height} but the frame rendered " +
                "${rendered.width}x${rendered.height} — the canvas size changed",
            reference.width to reference.height,
            rendered.width to rendered.height,
        )

        var differing = 0
        var totalError = 0L
        var worst = 0
        val diff = BufferedImage(rendered.width, rendered.height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until rendered.height) {
            for (x in 0 until rendered.width) {
                val (ar, ag, ab) = StatusBarReference.rgb(reference, x, y)
                val (br, bg, bb) = StatusBarReference.rgb(rendered, x, y)
                val delta = maxOf(abs(ar - br), abs(ag - bg), abs(ab - bb))
                totalError += (abs(ar - br) + abs(ag - bg) + abs(ab - bb)).toLong()
                worst = maxOf(worst, delta)
                if (delta > ChannelTolerance) {
                    differing++
                    diff.setRGB(x, y, 0xFFFF0000.toInt())
                } else {
                    diff.setRGB(x, y, rendered.getRGB(x, y))
                }
            }
        }

        val pixels = rendered.width * rendered.height
        val differingFraction = differing.toFloat() / pixels
        val meanError = totalError.toFloat() / (pixels * 3)

        val dir = File(reportDir).apply { mkdirs() }
        ImageIO.write(rendered, "png", File(dir, "$writtenName-rendered.png"))
        ImageIO.write(reference, "png", File(dir, "$writtenName-reference.png"))
        ImageIO.write(diff, "png", File(dir, "$writtenName-diff.png"))

        val report = buildString {
            append("$referenceName vs the frame as rendered now\n")
            append("  differing pixels   %,d of %,d (%.4f%%, max %.4f%%)\n"
                .format(differing, pixels, differingFraction * 100, differingPixelTolerance * 100))
            append("  mean channel error %.4f (max %.4f)\n".format(meanError, meanErrorTolerance))
            append("  worst pixel delta  %d of 255\n".format(worst))
            append("  images written to  $dir\n")
        }
        println(report)

        val failures = buildList {
            if (differingFraction > differingPixelTolerance) {
                add("%.4f%% of the canvas differs, past the %.4f%% allowed — look at $writtenName-diff.png"
                    .format(differingFraction * 100, differingPixelTolerance * 100))
            }
            if (meanError > meanErrorTolerance) {
                add("mean channel error %.4f is past the %.4f allowed".format(meanError, meanErrorTolerance))
            }
        }
        assertTrue("\n$report\n" + failures.joinToString("\n"), failures.isEmpty())
    }

    private fun written(name: String): File {
        val root = ScreenshotRule.defaultOutputRoot()
        return root.walkTopDown().firstOrNull { it.name == "$name.png" }
            ?: error("no $name.png written under $root")
    }

    /**
     * Loads the committed golden, or writes the current render out as one and fails, so recording a
     * new golden is a matter of deleting the file and running the test — never something that
     * happens silently on a run that was meant to be checking.
     */
    private fun loadReference(name: String, rendered: BufferedImage): BufferedImage {
        val stream = javaClass.getResourceAsStream("/reference/$name")
        if (stream != null) return stream.use { StatusBarReference.opaque(ImageIO.read(it)) }

        val recorded = File("src/test/resources/reference/$name").apply { parentFile.mkdirs() }
        ImageIO.write(rendered, "png", recorded)
        error(
            "No golden at /reference/$name, so the current render was written to $recorded. " +
                "Look at it, and commit it if it is what the frame should look like."
        )
    }
}
