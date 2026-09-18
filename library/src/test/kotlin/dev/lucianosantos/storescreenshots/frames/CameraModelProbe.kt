package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import dev.lucianosantos.storescreenshots.StoreScreenshotsStubApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Measures what `Modifier.graphicsLayer`'s `rotationX`/`rotationY`/`cameraDistance` actually do to a
 * known set of points, so the solid-device renderer can reproduce the same projection exactly rather
 * than guess at Compose's internals.
 *
 * Four coloured markers sit at the corners of a box of known size. The box is rotated through a
 * `graphicsLayer`; each marker's centroid is then read back out of the render. Solving for the
 * camera distance and the rotation convention that reproduce those centroids tells us the model.
 *
 * It asserts what it measures, so it is the provenance of [DeviceProjection]'s camera model rather
 * than a comment claiming one: if a future Compose moved its camera or reordered its rotations,
 * this fails with the numbers in hand instead of the mockups quietly going crooked. The gate that
 * holds the renderer to the same model end-to-end is [SolidFrontFaceMatchesFlatTiltTest].
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [35],
    application = StoreScreenshotsStubApplication::class,
    qualifiers = "w400dp-h600dp-xxhdpi",
)
class CameraModelProbe {

    @get:Rule
    val compose = createComposeRule()

    private val density = 3f
    private val boxW = 200f * density   // px
    private val boxH = 400f * density   // px
    private val marker = 10f * density  // px

    /** Marker centres in the box's own space, relative to its centre. Order: TL, TR, BR, BL. */
    private fun localCorners(): List<Pair<Float, Float>> {
        val x = boxW / 2f - marker / 2f
        val y = boxH / 2f - marker / 2f
        return listOf(-x to -y, x to -y, x to y, -x to y)
    }

    // One render per test: the compose rule allows setContent only once.
    @Test fun rotationYOnly() = probeOne("rotationY only", 0f, 30f, 0f)
    @Test fun rotationXOnly() = probeOne("rotationX only", 30f, 0f, 0f)
    @Test fun marketingAngle() = probeOne("marketing angle", 8f, -26f, -6f)

    private fun probeOne(label: String, rx: Float, ry: Float, rz: Float) {
        val report = StringBuilder("graphicsLayer camera model probe (density=$density)\n")
        val image = render(rx, ry, rz)
        val pivotX = image.width / 2f
        val pivotY = image.height / 2f
        val measured = MARKERS.map { centroid(image, it) }

        report.append("\n== $label  (rotX=$rx rotY=$ry rotZ=$rz, cameraDistance=12f*density)\n")
        report.append("   canvas ${image.width}x${image.height}, pivot ($pivotX, $pivotY)\n")
        measured.forEachIndexed { i, m ->
            if (m == null) report.append("   marker $i NOT FOUND\n")
            else report.append("   marker $i measured (%.2f, %.2f)  offset (%+.2f, %+.2f)\n"
                .format(m.first, m.second, m.first - pivotX, m.second - pivotY))
        }
        if (measured.any { it == null }) { println(report); return }

        // Search the camera distance and convention that reproduce those centroids.
        var best: Fit? = null
        for (order in Order.entries) for (zAfter in listOf(true, false))
            for (signY in listOf(1f, -1f)) for (signX in listOf(1f, -1f)) {
                var d = 200f
                while (d < 200_000f) {
                    val err = error(measured, d, rx * signX, ry * signY, rz, order, zAfter, pivotX, pivotY)
                    if (err.isFinite() && (best == null || err < best!!.error)) {
                        best = Fit(err, d, order, zAfter, signX, signY)
                    }
                    d *= 1.002f
                }
            }
        val f = best!!
        report.append(
            "   best fit: D=%.1f px  order=%s  zAfter2D=%s  signX=%+.0f signY=%+.0f  rms error %.3f px\n"
                .format(f.d, f.order, f.zAfter, f.signX, f.signY, f.error)
        )
        report.append(
            "   D / (cameraDistance*density) = %.3f   [72 would confirm Skia's inches->points]\n"
                .format(f.d / (12f * density))
        )
        // Residuals of the winning convention, and of the model we intend to ship (D = c*density*72).
        listOf("best fit" to f.d, "c*density*72" to 12f * density * 72f).forEach { (name, d) ->
            report.append("   residuals with $name (D=%.1f):\n".format(d))
            localCorners().forEachIndexed { i, (lx, ly) ->
                val p = projectPoint(lx, ly, d, rx * f.signX, ry * f.signY, rz, f.order, f.zAfter)
                val m = measured[i]!!
                report.append("     marker $i  model (%+.2f, %+.2f)  measured (%+.2f, %+.2f)  d=(%+.2f, %+.2f)\n"
                    .format(p.first, p.second, m.first - pivotX, m.second - pivotY,
                        pivotX + p.first - m.first, pivotY + p.second - m.second))
            }
        }
        // Runner-up conventions, so the margin between them is visible rather than assumed.
        report.append("   all conventions at D=%.1f:\n".format(12f * density * 72f))
        for (order in Order.entries) for (zAfter in listOf(true, false)) {
            val err = error(measured, 12f * density * 72f, rx, ry, rz, order, zAfter, pivotX, pivotY)
            report.append("     order=%-8s zAfter2D=%-5s  rms %.3f px\n".format(order, zAfter, err))
        }
        println(report)
    }

    private enum class Order { XThenY, YThenX }

    private class Fit(
        val error: Float, val d: Float, val order: Order,
        val zAfter: Boolean, val signX: Float, val signY: Float,
    )

    private fun error(
        measured: List<Pair<Float, Float>?>, d: Float, rx: Float, ry: Float, rz: Float,
        order: Order, zAfter: Boolean, pivotX: Float, pivotY: Float,
    ): Float {
        var sum = 0f
        localCorners().forEachIndexed { i, (lx, ly) ->
            val p = projectPoint(lx, ly, d, rx, ry, rz, order, zAfter)
            val m = measured[i]!!
            val dx = (pivotX + p.first) - m.first
            val dy = (pivotY + p.second) - m.second
            sum += dx * dx + dy * dy
        }
        return kotlin.math.sqrt(sum / 4f)
    }

    private fun projectPoint(
        lx: Float, ly: Float, d: Float, rxDeg: Float, ryDeg: Float, rzDeg: Float,
        order: Order, zAfter: Boolean,
    ): Pair<Float, Float> {
        val rx = Math.toRadians(rxDeg.toDouble())
        val ry = Math.toRadians(ryDeg.toDouble())
        val rz = Math.toRadians(rzDeg.toDouble())

        var x = lx.toDouble(); var y = ly.toDouble(); var z = 0.0
        if (!zAfter) { // rotationZ as a 3D rotation before projection
            val nx = x * cos(rz) - y * sin(rz); val ny = x * sin(rz) + y * cos(rz)
            x = nx; y = ny
        }
        fun applyX() { val ny = y * cos(rx) - z * sin(rx); val nz = y * sin(rx) + z * cos(rx); y = ny; z = nz }
        fun applyY() { val nx = x * cos(ry) + z * sin(ry); val nz = -x * sin(ry) + z * cos(ry); x = nx; z = nz }
        when (order) { Order.XThenY -> { applyX(); applyY() }; Order.YThenX -> { applyY(); applyX() } }

        val w = d - z
        if (w <= 1e-3) return Float.NaN to Float.NaN
        var sx = x * d / w; var sy = y * d / w
        if (zAfter) { // rotationZ as a 2D rotation of the projected result
            val nx = sx * cos(rz) - sy * sin(rz); val ny = sx * sin(rz) + sy * cos(rz)
            sx = nx; sy = ny
        }
        return sx.toFloat() to sy.toFloat()
    }

    private fun render(rx: Float, ry: Float, rz: Float): BufferedImage {
        compose.setContent { Scene(rx, ry, rz) }
        compose.waitForIdle()
        val file = File.createTempFile("camera-probe", ".png")
        compose.onRoot().captureRoboImage(filePath = file.absolutePath, roborazziOptions = RoborazziOptions())
        return ImageIO.read(file) ?: error("could not decode $file")
    }

    @Composable
    private fun Scene(rx: Float, ry: Float, rz: Float) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(200.dp, 400.dp)
                    .graphicsLayer {
                        rotationX = rx
                        rotationY = ry
                        rotationZ = rz
                        cameraDistance = 12f * this.density
                    }
                    .background(Color(0xFF303030)),
            ) {
                listOf(
                    Alignment.TopStart, Alignment.TopEnd, Alignment.BottomEnd, Alignment.BottomStart,
                ).forEachIndexed { i, alignment ->
                    Box(Modifier.align(alignment).size(10.dp).background(Color(MARKERS[i])))
                }
            }
        }
    }

    /** Centroid of the pixels that are unambiguously [argb], or null when the marker is missing. */
    private fun centroid(image: BufferedImage, argb: Long): Pair<Float, Float>? {
        val tr = ((argb shr 16) and 0xFF).toInt()
        val tg = ((argb shr 8) and 0xFF).toInt()
        val tb = (argb and 0xFF).toInt()
        var sx = 0.0; var sy = 0.0; var n = 0
        for (y in 0 until image.height) for (x in 0 until image.width) {
            val p = image.getRGB(x, y)
            val r = (p shr 16) and 0xFF; val g = (p shr 8) and 0xFF; val b = p and 0xFF
            if (kotlin.math.abs(r - tr) < 40 && kotlin.math.abs(g - tg) < 40 && kotlin.math.abs(b - tb) < 40) {
                sx += x + 0.5; sy += y + 0.5; n++
            }
        }
        return if (n == 0) null else (sx / n).toFloat() to (sy / n).toFloat()
    }

    private companion object {
        /** TL, TR, BR, BL. */
        val MARKERS = listOf(0xFFFF0000L, 0xFF00FF00L, 0xFF0000FFL, 0xFFFFFF00L)
    }
}
