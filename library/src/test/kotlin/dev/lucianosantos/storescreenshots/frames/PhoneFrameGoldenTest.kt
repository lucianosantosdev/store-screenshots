package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotRule
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

/**
 * Golden images for the phone frame, and for a `Middle`-positioned frame.
 *
 * The frame rewrite that moved [PhoneFrame] onto `DeviceMockup`, and the `MockupPosition.Middle`
 * change that bounded the mockup's height, were both argued to leave the composition alone. That
 * claim was only ever checked by looking at the output. These are what make it survive the next
 * refactor: the whole pipeline runs — frame, layout, flatten, write — and the PNG is held against
 * a committed capture of what it is supposed to look like.
 *
 * Unlike [IosStatusBarComparisonTest], which deliberately compares against a real iOS capture
 * rather than our own output, a golden here *is* our own output. That is the point — it does not
 * say the layout is right, it says the layout has not moved. Regenerate deliberately (delete the
 * reference and re-run, then look at the image) whenever a change is meant to alter the frame.
 *
 * Compared with tolerance rather than pixel-for-pixel. Both sides come out of the same renderer,
 * but text antialiasing shifts by a hair across JDK and OS, and an exact match would fail on a
 * machine other than the one that recorded it while catching nothing extra: a device that changed
 * size or moved shifts a percentage of the canvas, not a fringe of a few thousand edge pixels.
 * Canvas dimensions, on the other hand, are asserted exactly — that is the regression this branch
 * exists to prevent.
 */
class PhoneFrameGoldenTest : StoreScreenshotsTest(FormFactor.Phone) {

    /** A pixel counts as different when a channel is off by more than this, out of 255. */
    private val channelTolerance = 8

    /** At most this fraction of the canvas may differ — 0.2% of 1242x2208 is about 5,500 px. */
    private val differingPixelTolerance = 0.002f

    /** Mean absolute channel error across the whole canvas. Antialiasing noise sits far below 1. */
    private val meanErrorTolerance = 1.0f

    @Test
    fun phoneFrameMatchesItsGolden() {
        screenshot(
            fileName = "golden_phone_frame",
            title = "Count anything",
            description = "A focused tap counter that gets out of your way",
        ) { GoldenContent() }

        assertMatchesGolden("golden_phone_frame", "phone_frame.png")
    }

    /**
     * `Middle` is the position the layout change actually rewrote — it used to place an unbounded
     * mockup between two weighted spacers, and now sits in a weighted `Column`. Every form factor
     * goes through that branch, so it is the one most worth pinning.
     */
    @Test
    fun middlePositionedFrameMatchesItsGolden() {
        screenshot(
            fileName = "golden_phone_middle",
            title = "Mockup centered",
            description = "Title above, description below",
            style = ScreenshotStyle(mockupPosition = MockupPosition.Middle),
        ) { GoldenContent() }

        assertMatchesGolden("golden_phone_middle", "phone_frame_middle.png")
    }

    /**
     * Deliberately plain: flat colours and one line of text, so a diff points at the frame and the
     * layout rather than at whatever the sample app happened to draw.
     */
    @Composable
    private fun GoldenContent() {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF6A1B9A)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "42", color = Color.White, fontSize = 96.sp, fontWeight = FontWeight.Bold)
        }
    }

    private fun assertMatchesGolden(writtenName: String, referenceName: String) {
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
                if (delta > channelTolerance) {
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

        val dir = File("build/reports/frame-goldens").apply { mkdirs() }
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
