package dev.lucianosantos.storescreenshots

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What a screen inside the mockup believed about the device it was on. Recorded during composition
 * rather than asserted there, so a failure reports the values instead of dying inside Compose.
 */
private class Seen {
    var containerSize: IntSize? = null
    var orientation: Int? = null
}

@Composable
private fun RecordingContent(seen: Seen) {
    seen.containerSize = LocalWindowInfo.current.containerSize
    seen.orientation = LocalConfiguration.current.orientation
    Box(Modifier.fillMaxSize().background(Color(0xFF6A1B9A)))
}

/**
 * A portrait mockup makes its content read portrait, on both of the locals a screen might ask.
 *
 * Run on the feature graphic canvas because that is the case that breaks: 512dp x 250dp is
 * landscape and much shorter than a phone, so anything that leaks through from the canvas is
 * unmistakably landscape rather than merely a bit off.
 *
 * [LocalWindowInfo] is the half that regressed. `DeviceMockup` scoped [LocalConfiguration] from the
 * start, but Compose Multiplatform screens cannot read `Configuration` — it is Android-only — so
 * they derive their layout from `LocalWindowInfo.containerSize`, which fell through to the canvas.
 * A CMP app's phone screens rendered their landscape arrangement inside a portrait phone mockup:
 * side rails where a top bar belonged, squeezed into a ~100dp-wide device. Asserting only the
 * Configuration would go on passing through exactly that bug.
 */
class MockupDeviceEnvironmentTest : StoreScreenshotsTest(FormFactor.GooglePlayFeatureGraphic) {

    @Test
    fun aPortraitPhoneMockupOnALandscapeCanvasReadsPortrait() {
        val seen = Seen()

        customScreenshot(fileName = "device_environment") {
            DeviceMockup(FormFactor.Phone, Modifier.fillMaxSize()) { RecordingContent(seen) }
        }

        val size = requireNotNull(seen.containerSize) { "content never composed" }
        assertTrue(
            "LocalWindowInfo.containerSize is $size — wider than it is tall, so the content is " +
                "reading the landscape canvas rather than the portrait phone it was drawn into",
            size.height > size.width,
        )
        assertTrue(
            "LocalConfiguration.orientation should be portrait inside a portrait mockup",
            seen.orientation == Configuration.ORIENTATION_PORTRAIT,
        )
    }

    /** The same for a mockup asked for landscape: the flag has to reach both locals, not just one. */
    @Test
    fun aLandscapeMockupReadsLandscape() {
        val seen = Seen()

        customScreenshot(fileName = "device_environment_landscape") {
            DeviceMockup(
                FormFactor.Phone,
                Modifier.fillMaxSize(),
                orientation = MockupOrientation.Landscape,
            ) { RecordingContent(seen) }
        }

        val size = requireNotNull(seen.containerSize) { "content never composed" }
        assertTrue(
            "MockupOrientation.Landscape should make containerSize landscape too, but it is $size",
            size.width > size.height,
        )
        assertTrue(
            "LocalConfiguration.orientation should be landscape inside a landscape mockup",
            seen.orientation == Configuration.ORIENTATION_LANDSCAPE,
        )
    }
}
