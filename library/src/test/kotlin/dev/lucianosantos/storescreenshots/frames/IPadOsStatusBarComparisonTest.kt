package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import dev.lucianosantos.storescreenshots.StoreScreenshotsStubApplication
import dev.lucianosantos.storescreenshots.frames.IPadAir13Metrics as M
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
 * Holds the status bar [IPadOsStatusBar] draws against the one iPadOS draws — the iPad counterpart
 * of [IosStatusBarComparisonTest], and there for the same reason.
 *
 * App Store Connect requires a 13-inch iPad screenshot from any app that lists on iPad, so this slot
 * is as exposed to guideline 2.3.10 as the iPhone ones. It used to render the Android tablet frame,
 * Material status bar and all.
 *
 * Two things differ from the phone and are worth stating, because they are easy to mistake for bugs:
 * a Wi-Fi iPad shows **no cellular bars**, and its clock sits at the **leading edge** followed by the
 * date rather than being centred. Both are what the capture shows.
 *
 * The capture is `src/test/resources/reference/ipad13_statusbar.png` at @2x, 1024 pt wide, so the
 * test renders at `xhdpi` / 1024 dp and the two line up with no rescaling.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    application = StoreScreenshotsStubApplication::class,
    qualifiers = "w1024dp-h1366dp-xhdpi",
)
class IPadOsStatusBarComparisonTest {

    @get:Rule
    val compose = createComposeRule()

    /** Minimum overlap between our glyph and iPadOS's, as intersection over union of their ink. */
    private val glyphIouTolerance = 0.9f

    /** The clock may land within this fraction of the screen's width — 5 px on the capture. */
    private val clockTolerance = 0.005f

    @Test
    fun statusBarMatchesIpadOs() {
        val reference = StatusBarReference.opaque(StatusBarReference.load("ipad13_statusbar.png"))
        // The app the capture was taken over, sampled well clear of any glyph.
        val (r, g, b) = StatusBarReference.rgb(reference, 1024, 100)

        val rendered = render {
            Box(Modifier.fillMaxSize().background(Color(r, g, b))) {
                // The same string the capture shows: iPadOS puts the date after the time.
                IPadOsStatusBar(clock = "09:41  Wed 29 Jul", screenWidth = M.ScreenWidth.dp, contentColor = Color.White)
            }
        }

        val report = StringBuilder("iPadOS status bar vs store-screenshots status bar\n")
        val failures = mutableListOf<String>()
        // Two pixels per reference point, matching the capture's @2x framebuffer.
        val scale = reference.width / M.ScreenWidth
        fun region(fromPt: Float, toPt: Float) =
            Rect((fromPt * scale).toInt(), 0, (toPt * scale).toInt(), reference.height - 1)

        listOf(
            "wifi" to region(M.WifiX - 3, M.WifiX + M.WifiWidth + 3),
            "battery" to region(M.BatteryX - 3, M.BatteryX + M.BatteryWidth + 4),
        ).forEach { (name, box) ->
            val iou = StatusBarReference.inkIou(reference, rendered, box)
            val ok = iou >= glyphIouTolerance
            report.append("  %-18s ink IoU %.4f (min %.4f)  %s\n".format(name, iou, glyphIouTolerance, if (ok) "ok" else "FAIL"))
            if (!ok) failures += "the $name glyph overlaps iPadOS's by only %.1f%% of their combined area".format(iou * 100)
        }

        // Nothing may be drawn where a phone puts its cellular bars — a Wi-Fi iPad shows none, and
        // drawing them here would be inventing hardware the screenshot does not depict.
        val cellularBox = region(M.WifiX - 60, M.WifiX - 5)
        val strayInk = StatusBarReference.inkBounds(rendered, cellularBox)
        report.append("  %-18s %s\n".format("no cellular bars", if (strayInk == null) "ok" else "FAIL — drew something at $strayInk"))
        if (strayInk != null) failures += "the iPad status bar drew cellular bars, which a Wi-Fi iPad does not show"

        // Only the time, not the date that follows it: the date's width depends on the font, and
        // iPadOS sets it in SF Pro, which is not redistributable.
        val timeBox = region(0f, 60f)
        val expected = requireNotNull(StatusBarReference.inkBounds(reference, timeBox)) { "No clock in the capture" }
        val actual = requireNotNull(StatusBarReference.inkBounds(rendered, timeBox)) { "The status bar drew no clock" }
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
        compare("clock left edge", expected.left, actual.left)
        compare("clock centre y", expected.centerY, actual.centerY)
        compare("clock cap height", expected.height, actual.height)

        val dir = File("build/reports/iphone17").apply { mkdirs() }
        ImageIO.write(rendered, "png", File(dir, "ipad-statusbar-rendered.png"))
        ImageIO.write(reference, "png", File(dir, "ipad-statusbar-reference.png"))

        println(report)
        assertTrue("\n$report\n" + failures.joinToString("\n"), failures.isEmpty())
    }

    private fun render(content: @Composable () -> Unit): BufferedImage {
        compose.setContent(content)
        compose.waitForIdle()
        val file = File.createTempFile("ipad-status-bar", ".png")
        compose.onRoot().captureRoboImage(filePath = file.absolutePath, roborazziOptions = RoborazziOptions())
        return StatusBarReference.opaque(StatusBarReference.read(file))
    }
}
