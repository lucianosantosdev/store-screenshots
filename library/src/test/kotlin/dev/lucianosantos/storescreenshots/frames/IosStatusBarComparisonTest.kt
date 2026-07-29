package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import dev.lucianosantos.storescreenshots.StoreScreenshotsStubApplication
import dev.lucianosantos.storescreenshots.frames.IPhone17Metrics as M
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

/**
 * Holds the status bar [IosStatusBar] draws against the one iOS draws.
 *
 * This is the check that matters for App Store Review: guideline 2.3.10 rejects screenshots showing
 * "non-iOS status bar images", and the Apple frames used to draw Material icons. A regression here
 * is not a cosmetic drift, it is a rejected submission — so the glyphs are compared against a real
 * capture rather than against a golden image of our own output, which would happily lock in
 * whatever we last drew.
 *
 * The capture is `src/test/resources/reference/iphone17_statusbar.png`; [StatusBarReference]
 * documents how it was taken. Because it is at @3x and 402 pt wide, the test renders at
 * `xxhdpi` / 402 dp so the two line up pixel for pixel with no rescaling in between.
 *
 * Every measurement is printed before anything is asserted, so a failure says which glyph moved and
 * by how much rather than just "images differ", and both images are written to
 * `build/reports/iphone17/` to be looked at.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    application = StoreScreenshotsStubApplication::class,
    qualifiers = "w402dp-h874dp-xxhdpi",
)
class IosStatusBarComparisonTest {

    @get:Rule
    val compose = createComposeRule()

    /**
     * Minimum overlap between our glyph and iOS's, as intersection over union of their ink. The
     * glyphs are vector paths on both sides, so what keeps this off 1.0 is the last half-pixel of
     * each outline rather than anything a reader would see.
     */
    private val glyphIouTolerance = 0.9f

    /**
     * The clock may land within this fraction of the screen's width. At the capture's 1206 px that
     * is 6 px — a fifth of a point on the device.
     */
    private val clockTolerance = 0.005f

    @Test
    fun statusBarMatchesIos() {
        val reference = StatusBarReference.opaque(StatusBarReference.load())
        // The app the capture was taken over, sampled well clear of any glyph.
        val (r, g, b) = StatusBarReference.rgb(reference, 600, 190)

        val rendered = render {
            Box(Modifier.fillMaxSize().background(Color(r, g, b))) {
                IosStatusBar(clock = "09:41", screenWidth = M.ScreenWidth.dp, contentColor = Color.White)
            }
        }

        val report = StringBuilder("iOS status bar vs store-screenshots status bar\n")
        val failures = mutableListOf<String>()
        // Three pixels per reference point, matching the capture's @3x framebuffer.
        val scale = reference.width / M.ScreenWidth
        fun region(fromPt: Float, toPt: Float) =
            Rect((fromPt * scale).toInt(), 0, (toPt * scale).toInt(), reference.height - 1)

        // Each glyph on its own, so a failure names the one that moved. A couple of points of
        // margin either side keeps a glyph that drifted inside its own window.
        listOf(
            "cellular" to region(M.CellularX - 3, M.CellularX + 3 * M.CellularBarPitch + M.CellularBarWidth + 3),
            "wifi" to region(M.WifiX - 3, M.WifiX + M.WifiWidth + 3),
            "battery" to region(M.BatteryX - 3, M.BatteryX + M.BatteryWidth + M.BatteryNubGap + M.BatteryNubWidth + 3),
        ).forEach { (name, box) ->
            val iou = StatusBarReference.inkIou(reference, rendered, box)
            val ok = iou >= glyphIouTolerance
            report.append("  %-18s ink IoU %.4f (min %.4f)  %s\n".format(name, iou, glyphIouTolerance, if (ok) "ok" else "FAIL"))
            if (!ok) failures += "the $name glyph overlaps iOS's by only %.1f%% of their combined area".format(iou * 100)
        }

        // The clock is compared by where its digits land and how tall they are, not pixel for pixel:
        // iOS sets it in SF Pro, which is not redistributable, so the glyph shapes cannot coincide.
        val clockBox = region(0f, M.ClockCenterX * 2)
        val expected = requireNotNull(StatusBarReference.inkBounds(reference, clockBox)) { "No clock in the capture" }
        val actual = requireNotNull(StatusBarReference.inkBounds(rendered, clockBox)) { "The status bar drew no clock" }
        fun compare(name: String, expectedPx: Int, actualPx: Int) {
            val delta = (actualPx - expectedPx).toFloat()
            val relative = abs(delta) / reference.width
            val ok = relative <= clockTolerance
            report.append(
                "  %-18s expected %5d  actual %5d  delta %+5.0f px (%.3f%% of screen)  %s\n"
                    .format(name, expectedPx, actualPx, delta, relative * 100, if (ok) "ok" else "FAIL")
            )
            if (!ok) failures += "the clock's $name is off by %+.0f px (%.3f%% of screen width)".format(delta, relative * 100)
        }
        compare("clock centre x", expected.centerX, actual.centerX)
        compare("clock centre y", expected.centerY, actual.centerY)
        compare("clock cap height", expected.height, actual.height)

        val dir = File("build/reports/iphone17").apply { mkdirs() }
        ImageIO.write(rendered, "png", File(dir, "statusbar-rendered.png"))
        ImageIO.write(reference, "png", File(dir, "statusbar-reference.png"))

        println(report)
        assertTrue("\n$report\n" + failures.joinToString("\n"), failures.isEmpty())
    }

    private fun render(content: @androidx.compose.runtime.Composable () -> Unit): BufferedImage {
        compose.setContent(content)
        compose.waitForIdle()
        val file = File.createTempFile("ios-status-bar", ".png")
        compose.onRoot().captureRoboImage(filePath = file.absolutePath, roborazziOptions = RoborazziOptions())
        return StatusBarReference.opaque(StatusBarReference.read(file))
    }
}
