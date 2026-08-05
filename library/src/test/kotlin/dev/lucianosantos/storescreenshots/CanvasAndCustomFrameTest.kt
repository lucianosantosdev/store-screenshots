package dev.lucianosantos.storescreenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.frames.StatusBarReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.abs

/** Solid, unmistakable fill so the custom frame can be found in the PNG by colour alone. */
private val FrameFill = Color(0xFFFF00FF)

/**
 * A custom frame written the way the docs suggest and the example does: width from the layout,
 * height from an aspect ratio. This is the shape that overflows — `fillMaxWidth` fixes the width
 * first, so on a canvas with less height than `width × 2` there is no size satisfying both the
 * ratio and the constraints, and `aspectRatio` passes the constraints through unchanged.
 */
private val widthDrivenFrame = ScreenshotStyle(
    mockupFrame = { content ->
        // Padded so the fill survives as a border the content does not cover — the bezel of a real
        // custom frame, and what makes the frame's own bounds measurable in the output.
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 18f)
                .background(FrameFill)
                .padding(16.dp)
        ) { content() }
    },
)

@Composable
private fun PlainContent() {
    Box(Modifier.fillMaxSize().background(Color(0xFF6A1B9A)))
}

/**
 * A custom `mockupFrame` too tall for its canvas is scaled down, not clipped.
 *
 * This is the regression that shipped: the phone canvas moved from 1233x2742 to a shorter shape,
 * the example's jewelled frame kept asking for `width × 2` of height, and the extra ran off the
 * bottom of the image and over the title. Nothing failed — a clipped frame still renders, and the
 * existing goldens covered layouts that happened to fit.
 *
 * Asserted through the frame's *aspect ratio in the output* rather than a golden. Scaling preserves
 * it; clipping does not, and the difference between the two is what the bug was. A golden would
 * have to be re-recorded on the next legitimate canvas change and would go on passing if the new
 * render were clipped in the same way, which is exactly how this got through the first time.
 *
 * Run on a canvas deliberately shorter than any the library ships. The default 1:2 canvas is not
 * enough to prove anything here: a `width × 2` frame overflows the frame's *padding* there but
 * still lands inside the image, so the test passed with the fix reverted. The gate has to be a
 * canvas where the overflow genuinely leaves the PNG.
 */
class CustomFrameFitTest : StoreScreenshotsTest(
    FormFactor.Phone,
    widthDrivenFrame,
    canvas = ScreenshotCanvas.px(1242, 1800),
) {

    @Test
    fun aCustomFrameTallerThanTheCanvasIsScaledRatherThanClipped() {
        screenshot(
            fileName = "fit_custom_frame",
            title = "Custom frame",
            description = "Taller than the space it is given",
        ) { PlainContent() }

        val bounds = frameBounds(readWritten("fit_custom_frame"))
        val ratio = bounds.height.toFloat() / bounds.width

        assertTrue(
            "the frame reaches the bottom edge of the canvas, so it was clipped: $bounds",
            bounds.bottom < bounds.canvasHeight - 1,
        )
        assertTrue(
            "the frame is $ratio:1 in the output but was declared 2:1 — a uniform scale keeps the " +
                "ratio, clipping shortens it, so this is the frame losing its bottom edge: $bounds",
            abs(ratio - 2f) < 0.02f,
        )
        assertTrue(
            "the frame should have been scaled down to fit, but it is still the full content " +
                "width — nothing shrank it: $bounds",
            bounds.width < bounds.canvasWidth - 120,
        )
    }
}

/**
 * The canvas override reaches the PNG. `ScreenshotCanvas` is only worth having if the file on disk
 * comes out the size that was asked for, so this renders at 9:16 — the shape the default 1:2 canvas
 * deliberately is not — and measures the result.
 */
class CanvasOverrideTest : StoreScreenshotsTest(
    FormFactor.Phone,
    canvas = ScreenshotCanvas.px(1080, 1920),
) {

    @Test
    fun theOverriddenCanvasIsTheSizeWrittenToDisk() {
        screenshot(fileName = "canvas_override", title = "Nine by sixteen") { PlainContent() }

        val image = readWritten("canvas_override")
        assertEquals(
            "ScreenshotCanvas.px(1080, 1920) should write a 1080x1920 PNG",
            1080 to 1920,
            image.width to image.height,
        )
    }
}

private class FrameBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val canvasWidth: Int,
    val canvasHeight: Int,
) {
    val width get() = right - left + 1
    val height get() = bottom - top + 1
    override fun toString() =
        "${width}x$height at ($left,$top)-($right,$bottom) on ${canvasWidth}x$canvasHeight"
}

/** The bounding box of [FrameFill] in [image] — where the custom frame actually landed. */
private fun frameBounds(image: BufferedImage): FrameBounds {
    var left = Int.MAX_VALUE
    var top = Int.MAX_VALUE
    var right = -1
    var bottom = -1
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val (r, g, b) = StatusBarReference.rgb(image, x, y)
            if (r > 200 && g < 60 && b > 200) {
                if (x < left) left = x
                if (x > right) right = x
                if (y < top) top = y
                if (y > bottom) bottom = y
            }
        }
    }
    check(right >= 0) { "the custom frame's fill colour is nowhere in the ${image.width}x${image.height} capture" }
    return FrameBounds(left, top, right, bottom, image.width, image.height)
}

private fun readWritten(name: String): BufferedImage {
    val root = ScreenshotRule.defaultOutputRoot()
    val file: File = root.walkTopDown().firstOrNull { it.name == "$name.png" }
        ?: error("no $name.png written under $root")
    return StatusBarReference.opaque(StatusBarReference.read(file))
}
