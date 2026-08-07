package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import dev.lucianosantos.storescreenshots.StoreScreenshotsStubApplication
import dev.lucianosantos.storescreenshots.frames.IPhone17ProMaxMetrics as M
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
 * Holds the enclosure [IPhoneBezel] draws for an iPhone 17 Pro Max against the one the Simulator
 * draws.
 *
 * The status bar test guards the glyphs a reviewer reads; this guards the hardware around them. It
 * exists because the pair of them is what a submission is dismissed over: a frame that depicts a
 * phone Apple no longer sells reads as a stale screenshot no matter how right the icons are.
 *
 * The capture is `src/test/resources/reference/iphone17promax_bezel.png` — the Simulator's own
 * window with "Show Device Bezels" on, over a plain white app, taken with
 * `screencapture -x -o -l <window id>`. It is normalised so the *body* measures exactly
 * [IPhone17ProMaxMetrics.BodyWidth] x [IPhone17ProMaxMetrics.BodyHeight] at 3 px per point, with
 * [IPhone17ProMaxMetrics.LeftButtonProtrusion] points of room for the side buttons on each side:
 * the Simulator sizes its 9-slice chrome to a whole number of window pixels, which leaves the drawn
 * body about a fifth of a percent short of the artwork it is stretching, and normalising takes that
 * out rather than baking it into a fixture. Every geometry it is normalised to comes from the vector
 * artwork itself — see [IPhone17ProMaxMetrics].
 *
 * The test renders at `xxhdpi` so a dp is 3 px, matching the capture with no rescaling in between,
 * and every measurement is printed before anything is asserted, so a failure says which feature
 * moved and by how much. Both images are written to `build/reports/iphone17promax/`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    application = StoreScreenshotsStubApplication::class,
    qualifiers = "w478dp-h986dp-xxhdpi",
)
class IPhone17ProMaxBezelComparisonTest {

    @get:Rule
    val compose = createComposeRule()

    /** Pixels per device point, in both images. */
    private val scale = 3f

    /**
     * How much of the two silhouettes' combined area has to be shared. What keeps this off 1.0 is
     * the anti-aliased pixel along every edge — a capture blurs where a Compose fill does not — plus
     * the Simulator's soft outer shadow, which the frame does not draw at all.
     */
    private val silhouetteTolerance = 0.99f

    /**
     * How far any one measured feature may sit from the Simulator's, in device points.
     *
     * Two points rather than one: the capture is a rasterised photo-real chrome with a soft edge and
     * a shadow under every side button, where the frame draws hard-edged fills, so each measurement
     * carries a consistent point or so of bias in whichever direction the softer image spreads. That
     * is well short of what a wrong constant costs — the notch this frame replaced sat 30 points from
     * the Dynamic Island, and every enclosure figure is tens of points — so the check still bites.
     */
    private val tolerancePt = 2f

    /** The corner radii compare two different curve families, so they get more room. */
    private val cornerTolerancePt = 4f

    @Test
    fun bezelMatchesSimulator() {
        val reference = BezelReference.onKey(StatusBarReference.load("iphone17promax_bezel.png"))
        val rendered = render {
            Box(Modifier.fillMaxSize().background(Color(BezelReference.Key))) {
                IPhoneBezel(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(M.BodyWidth.dp, M.BodyHeight.dp),
                    // The status bar has its own comparison; white-on-white would only confuse the
                    // measurement of where the lit display begins.
                    showStatusBar = false,
                    clock = "09:41",
                    statusBarContentDark = false,
                    // Edge to edge so the content lights the whole display, the way the app the
                    // capture was taken over does.
                    edgeToEdge = true,
                    metrics = M,
                ) {
                    Box(Modifier.fillMaxSize().background(Color.White))
                }
            }
        }

        val report = StringBuilder(
            "Simulator iPhone 17 Pro Max vs store-screenshots bezel  (%.0f px per point)\n"
                .format(scale)
        )
        val failures = mutableListOf<String>()

        fun compare(name: String, expectedPx: Number, actualPx: Number, tolerance: Float = tolerancePt) {
            val expected = expectedPx.toFloat() / scale
            val actual = actualPx.toFloat() / scale
            val delta = actual - expected
            val ok = abs(delta) <= tolerance
            report.append(
                "  %-22s simulator %8.2f  drawn %8.2f  delta %+6.2f pt (max %.1f)  %s\n"
                    .format(name, expected, actual, delta, tolerance, if (ok) "ok" else "FAIL")
            )
            if (!ok) failures += "$name is %+.2f pt off the Simulator's %.2f pt".format(delta, expected)
        }

        val iou = BezelReference.silhouetteIou(reference, rendered)
        val iouOk = iou >= silhouetteTolerance
        report.append(
            "  %-22s %.4f (min %.4f)  %s\n".format("silhouette IoU", iou, silhouetteTolerance, if (iouOk) "ok" else "FAIL")
        )
        if (!iouOk) {
            failures += "the drawn body overlaps the Simulator's by only %.2f%% of their combined area".format(iou * 100)
        }

        val refBody = BezelReference.bodyBounds(reference)
        val ownBody = BezelReference.bodyBounds(rendered)
        compare("body width", refBody.width, ownBody.width)
        compare("body height", refBody.height, ownBody.height)
        compare("body left edge", refBody.left, ownBody.left)
        compare("body top edge", refBody.top, ownBody.top)

        val (refLeft, refRight) = BezelReference.protrusions(reference, refBody)
        val (ownLeft, ownRight) = BezelReference.protrusions(rendered, ownBody)
        compare("left button reach", refLeft, ownLeft)
        compare("right button reach", refRight, ownRight)

        val refScreen = requireNotNull(BezelReference.screenBounds(reference)) { "No lit display in the capture" }
        val ownScreen = requireNotNull(BezelReference.screenBounds(rendered)) { "The frame drew no lit display" }
        compare("screen width", refScreen.width, ownScreen.width)
        compare("screen height", refScreen.height, ownScreen.height)
        compare("bezel left", refScreen.left - refBody.left, ownScreen.left - ownBody.left)
        compare("bezel right", refBody.right - refScreen.right, ownBody.right - ownScreen.right)
        compare("bezel top", refScreen.top - refBody.top, ownScreen.top - ownBody.top)
        compare("bezel bottom", refBody.bottom - refScreen.bottom, ownBody.bottom - ownScreen.bottom)

        compare(
            "body corner radius",
            BezelReference.cornerRadius(reference, refBody, BezelReference::isDevice),
            BezelReference.cornerRadius(rendered, ownBody, BezelReference::isDevice),
            cornerTolerancePt,
        )
        compare(
            "screen corner radius",
            BezelReference.cornerRadius(reference, refScreen, BezelReference::isScreen),
            BezelReference.cornerRadius(rendered, ownScreen, BezelReference::isScreen),
            cornerTolerancePt,
        )

        val refIsland = BezelReference.islandBounds(reference, refScreen)
        val ownIsland = BezelReference.islandBounds(rendered, ownScreen)
        if (refIsland == null || ownIsland == null) {
            failures += "the Dynamic Island is missing from ${if (refIsland == null) "the capture" else "the frame"}"
        } else {
            compare("island width", refIsland.width, ownIsland.width)
            compare("island height", refIsland.height, ownIsland.height)
            compare("island top", refIsland.top - refScreen.top, ownIsland.top - ownScreen.top)
            compare("island centre x", refIsland.centerX - refBody.left, ownIsland.centerX - ownBody.left)
        }

        // Each side button on its own, so a failure names the one that moved.
        listOf(true to "left", false to "right").forEach { (onLeft, side) ->
            val refRuns = BezelReference.buttonRuns(reference, refBody, onLeft)
            val ownRuns = BezelReference.buttonRuns(rendered, ownBody, onLeft)
            if (refRuns.size != ownRuns.size) {
                report.append("  %-22s simulator %d  drawn %d  FAIL\n".format("$side button count", refRuns.size, ownRuns.size))
                failures += "the frame draws ${ownRuns.size} $side-hand buttons where the Simulator draws ${refRuns.size}"
                return@forEach
            }
            refRuns.zip(ownRuns).forEachIndexed { index, (expected, actual) ->
                compare("$side button $index top", expected.first, actual.first)
                compare("$side button $index height", expected.second, actual.second)
            }
        }

        val dir = File("build/reports/iphone17promax").apply { mkdirs() }
        ImageIO.write(rendered, "png", File(dir, "bezel-rendered.png"))
        ImageIO.write(reference, "png", File(dir, "bezel-reference.png"))

        println(report)
        assertTrue("\n$report\n" + failures.joinToString("\n"), failures.isEmpty())
    }

    private fun render(content: @Composable () -> Unit): BufferedImage {
        compose.setContent(content)
        compose.waitForIdle()
        val file = File.createTempFile("iphone17promax-bezel", ".png")
        compose.onRoot().captureRoboImage(filePath = file.absolutePath, roborazziOptions = RoborazziOptions())
        return BezelReference.onKey(StatusBarReference.read(file))
    }
}
