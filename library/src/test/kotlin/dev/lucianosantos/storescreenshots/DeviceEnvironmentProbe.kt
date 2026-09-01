package dev.lucianosantos.storescreenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlin.math.abs

/**
 * What a screen inside a mockup believed about the device it was on, and the box it was actually
 * laid out in. Recorded during composition rather than asserted there, so a failure reports the
 * values instead of dying inside Compose.
 *
 * Shared by every test that probes the device environment — the phone/tablet frames, the Apple
 * frames, and [DeviceMockup] itself — so the probe cannot drift between them.
 */
internal class Seen {
    var containerSize: IntSize? = null
    var measuredSize: IntSize? = null
    var screenWidthDp: Int? = null
    var screenHeightDp: Int? = null
    var orientation: Int? = null
}

@Composable
internal fun RecordingContent(seen: Seen) {
    val configuration = LocalConfiguration.current
    seen.containerSize = LocalWindowInfo.current.containerSize
    seen.screenWidthDp = configuration.screenWidthDp
    seen.screenHeightDp = configuration.screenHeightDp
    seen.orientation = configuration.orientation
    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { seen.measuredSize = it }
            .background(Color(0xFF6A1B9A))
    )
}

/**
 * The frame reported the size it actually measured content in. This is the invariant every frame
 * exists to hold: [ProvideDeviceEnvironment] is only worth setting if the number it hands content
 * is the number content is laid out at.
 *
 * A dp of tolerance, because the two sides reach pixels by different routes — the reported size
 * rounds one dp figure, while the measured size falls out of a chain of dp paddings that each
 * round on their own.
 */
internal fun Seen.assertReportedSizeMatchesMeasured(density: Int) {
    val container = requireNotNull(containerSize) { "content never composed" }
    val measured = requireNotNull(measuredSize) { "content was never measured" }
    assertTrue(
        "content was told it had $container and was measured in $measured",
        abs(container.width - measured.width) <= density &&
            abs(container.height - measured.height) <= density,
    )
}

/** Both locals a screen can ask name the [width] x [height] dp device the slot's qualifiers do. */
internal fun Seen.assertLogicalSize(width: Int, height: Int, density: Int) {
    assertEquals("Configuration.screenWidthDp", width, requireNotNull(screenWidthDp) { "content never composed" })
    assertEquals("Configuration.screenHeightDp", height, screenHeightDp)
    val container = requireNotNull(containerSize) { "content never composed" }
    // containerSize is in pixels.
    assertEquals("containerSize.width", width * density, container.width)
    assertEquals("containerSize.height", height * density, container.height)
}
