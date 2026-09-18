package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import dev.lucianosantos.storescreenshots.ScaledMockup
import dev.lucianosantos.storescreenshots.StoreScreenshotsStubApplication
import dev.lucianosantos.storescreenshots.mockup3dRotation
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

/**
 * The gate the whole solid renderer stands on: its front face has to land exactly where the old
 * flat `graphicsLayer` tilt put it.
 *
 * A solid device is extruded *backwards* from z = 0, so its front face occupies the same plane the
 * flat path rotates. Give the body no thickness and the two should therefore produce the same
 * image — which makes the old path an exact oracle for the new one. If [DeviceProjection]'s camera
 * distance, its rotation order, or its homography were wrong, the device would land somewhere else
 * and this would fail with an unmistakable diff.
 *
 * That matters beyond the front face: the side rails are projected through the same model, so a
 * front face that lands correctly is also the proof that the rails drawn against it line up. It is
 * also what lets the library promise that turning the solid renderer on does not move a device
 * that was already being tilted.
 *
 * The content is a fine checkerboard on purpose. A uniform fill would hide a projection that was
 * slightly wrong inside a correct silhouette; a checkerboard turns that into moiré.
 *
 * Both renders are written to `build/reports/solid-front-face/`, so a failure can be looked at
 * rather than guessed at. The two halves run as separate tests because a compose rule accepts
 * `setContent` once; [MethodSorters.NAME_ASCENDING] and the `a`/`b`/`c` prefixes order them.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    application = StoreScreenshotsStubApplication::class,
    qualifiers = "w400dp-h800dp-xxhdpi",
)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class SolidFrontFaceMatchesFlatTiltTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun a_flatPath() {
        render("flat") {
            ScaledMockup(
                nativeWidth = NativeWidth,
                nativeHeight = NativeHeight,
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(Footprint)
                    .mockup3dRotation(RotationX, RotationY, RotationZ, CameraDistance),
            ) { Checkerboard() }
        }
    }

    @Test
    fun b_solidPath() {
        render("solid") {
            SolidDeviceMockup(
                nativeWidth = NativeWidth,
                nativeHeight = NativeHeight,
                modifier = Modifier.align(Alignment.Center).width(Footprint),
                // No thickness, so there is no side wall to draw and nothing but the front face is
                // left to compare. `DeviceProjectionTest` pins that a body with no thickness has no
                // visible rail, so this really is the front face on its own.
                body = FlatBody,
                tilt = MockupTilt(RotationX, RotationY, RotationZ, CameraDistance),
                // Everything the renderer adds *on top of* the face is turned off, so what is left
                // to compare is the face itself: no thickness means no rails, no elevation means no
                // cast shadow, and no specular means no sheen on the glass.
                lighting = MockupLighting(specular = 0f),
                elevation = 0.dp,
            ) { Checkerboard() }
        }
    }

    @Test
    fun c_theTwoAgree() {
        val flat = ImageIO.read(output("flat")) ?: error("the flat render was not written")
        val solid = ImageIO.read(output("solid")) ?: error("the solid render was not written")
        assertTrue(
            "the two renders are different sizes: ${flat.width}x${flat.height} and ${solid.width}x${solid.height}",
            flat.width == solid.width && flat.height == solid.height,
        )

        // Ignore a fringe around the silhouette. Both sides antialias the device's edge, but one
        // reaches it through a layer transform and the other through a canvas homography, so the
        // partially covered pixels along the boundary are allowed to disagree. Everything inside —
        // which is all of the checkerboard — is not.
        val edge = nearEdge(flat, solid)

        var differing = 0
        var compared = 0
        var totalError = 0L
        var worst = 0
        val diff = BufferedImage(flat.width, flat.height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until flat.height) for (x in 0 until flat.width) {
            if (edge[y * flat.width + x]) {
                diff.setRGB(x, y, 0xFF202020.toInt())
                continue
            }
            val a = flat.getRGB(x, y)
            val b = solid.getRGB(x, y)
            val delta = maxOf(
                abs(((a shr 16) and 0xFF) - ((b shr 16) and 0xFF)),
                abs(((a shr 8) and 0xFF) - ((b shr 8) and 0xFF)),
                abs((a and 0xFF) - (b and 0xFF)),
            )
            totalError += delta.toLong()
            worst = maxOf(worst, delta)
            compared++
            if (delta > ChannelTolerance) {
                differing++
                diff.setRGB(x, y, 0xFFFF0000.toInt())
            } else {
                diff.setRGB(x, y, b)
            }
        }

        val dir = File("build/reports/solid-front-face").apply { mkdirs() }
        ImageIO.write(diff, "png", File(dir, "diff.png"))

        val differingFraction = differing.toFloat() / compared
        val meanError = totalError.toFloat() / compared
        val report = buildString {
            append("solid front face vs the flat graphicsLayer tilt\n")
            append("  rotX=$RotationX rotY=$RotationY rotZ=$RotationZ cameraDistance=$CameraDistance\n")
            append("  compared         %,d px (%,d skipped as silhouette fringe)\n"
                .format(compared, flat.width * flat.height - compared))
            append("  differing pixels %,d (%.4f%%, max %.4f%%)\n"
                .format(differing, differingFraction * 100, DifferingFraction * 100))
            append("  mean error       %.4f (max %.4f)\n".format(meanError, MeanError))
            append("  worst delta      %d of 255\n".format(worst))
            append("  images in        $dir\n")
        }
        println(report)

        assertTrue(
            "$report\nThe solid renderer's front face does not land where the flat tilt put it. " +
                "Look at diff.png: a shifted or sheared device means DeviceProjection's camera " +
                "model is wrong; moiré across the face means the homography is.",
            differingFraction <= DifferingFraction && meanError <= MeanError,
        )
    }

    /** True for pixels within [FringeRadius] of a silhouette edge in either image. */
    private fun nearEdge(a: BufferedImage, b: BufferedImage): BooleanArray {
        val w = a.width
        val h = a.height
        val seed = BooleanArray(w * h)
        fun isBackground(image: BufferedImage, x: Int, y: Int): Boolean =
            (image.getRGB(x, y) and 0xFFFFFF) == 0
        for (y in 0 until h) for (x in 0 until w) {
            val bg = isBackground(a, x, y)
            if (bg != isBackground(b, x, y)) { seed[y * w + x] = true; continue }
            // A pixel with a neighbour of the other kind is on the boundary.
            for (dy in -1..1) for (dx in -1..1) {
                val nx = x + dx
                val ny = y + dy
                if (nx in 0 until w && ny in 0 until h && isBackground(a, nx, ny) != bg) {
                    seed[y * w + x] = true
                }
            }
        }
        // Grow the seed by the fringe radius.
        var current = seed
        repeat(FringeRadius) {
            val next = BooleanArray(w * h)
            for (y in 0 until h) for (x in 0 until w) {
                if (current[y * w + x]) { next[y * w + x] = true; continue }
                for (dy in -1..1) for (dx in -1..1) {
                    val nx = x + dx
                    val ny = y + dy
                    if (nx in 0 until w && ny in 0 until h && current[ny * w + nx]) next[y * w + x] = true
                }
            }
            current = next
        }
        return current
    }

    private fun render(name: String, content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
        compose.setContent {
            Box(Modifier.fillMaxSize().background(Color.Black)) { content() }
        }
        compose.waitForIdle()
        val file = output(name).apply { parentFile.mkdirs() }
        compose.onRoot().captureRoboImage(filePath = file.absolutePath, roborazziOptions = RoborazziOptions())
    }

    private fun output(name: String) = File("build/reports/solid-front-face/$name.png")

    /**
     * A fine checkerboard with a border and diagonals. Every square is a whole number of native
     * pixels so the pattern itself is identical in both renders, leaving the projection as the only
     * thing that can differ.
     */
    @Composable
    private fun Checkerboard() {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color(0xFF101828))
            val cell = 16.dp.toPx()
            var row = 0
            var y = 0f
            while (y < size.height) {
                var column = 0
                var x = 0f
                while (x < size.width) {
                    if ((row + column) % 2 == 0) {
                        drawRect(
                            color = Color(0xFFE5E7EB),
                            topLeft = Offset(x, y),
                            size = Size(minOf(cell, size.width - x), minOf(cell, size.height - y)),
                        )
                    }
                    x += cell
                    column++
                }
                y += cell
                row++
            }
            drawLine(Color(0xFFEF4444), Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = 4.dp.toPx())
            drawLine(Color(0xFF22D3EE), Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = 4.dp.toPx())
        }
    }

    private companion object {
        val NativeWidth = 411.dp
        val NativeHeight = 822.dp
        val Footprint = 300.dp

        const val RotationX = 8f
        const val RotationY = -26f
        const val RotationZ = -6f
        const val CameraDistance = 12f

        /** A body with no thickness: a front face and nothing else. */
        val FlatBody = DeviceBody(
            width = NativeWidth,
            height = NativeHeight,
            cornerRadius = 0.dp,
            thickness = 0.dp,
            screen = ScreenSpec(inset = 0.dp, corner = 0.dp),
            railColor = Color.Black,
            rimColor = Color.Black,
            backColor = Color.Black,
        )

        const val ChannelTolerance = 8
        const val DifferingFraction = 0.005f
        const val MeanError = 1.0f
        const val FringeRadius = 3
    }
}
