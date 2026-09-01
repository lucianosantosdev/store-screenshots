package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * What a screen inside an Apple frame believed about the device it was on. Recorded during
 * composition rather than asserted there, so a failure reports the values instead of dying
 * inside Compose.
 */
private class Seen {
    var containerSize: IntSize? = null
    var screenWidthDp: Int? = null
}

@Composable
private fun RecordingContent(seen: Seen) {
    seen.containerSize = LocalWindowInfo.current.containerSize
    seen.screenWidthDp = LocalConfiguration.current.screenWidthDp
    Box(Modifier.fillMaxSize().background(Color(0xFF6A1B9A)))
}

/**
 * The Apple frames measure content at the slot's logical size — 428x926dp for the 6.5" iPhone,
 * 1024x1366dp for the 13" iPad — and scale the mockup into the banner, the way [PhoneFrame] has
 * measured at 411x822dp since 1.5.5.
 *
 * They used to place content straight into the bezel's on-canvas footprint instead: the banner's
 * padding and the bezel each took their share of the slot's width first, and a 6.5" screenshot
 * measured its app at ~348dp — 80dp narrower than the 428dp the canvas qualifiers promise. Every
 * sp and dp then rendered ~20% larger than on a real device, and width-sensitive layouts broke:
 * the first symptom in the wild was a search field's placeholder folding onto two lines in a
 * store listing, on a screen that renders one line on every real iPhone.
 *
 * Asserting exact equality with the slot's logical size, on both locals a screen can ask, pins
 * the contract: the qualifiers, the Configuration and the WindowInfo all name the same device.
 */
class AppleFrameEnvironmentTest {

    class IPhone65 : StoreScreenshotsTest(FormFactor.AppleIPhone65) {
        @Test
        fun contentIsMeasuredAtTheSlotsLogicalSize() {
            val seen = Seen()
            screenshot(
                locales = listOf("en-US"),
                title = "probe",
                description = "probe",
                backgroundColor = Color.Black,
                fileName = "env_iphone65",
            ) { RecordingContent(seen) }
            seen.assertLogicalSize(width = 428, height = 926)
        }
    }

    class IPhone67 : StoreScreenshotsTest(FormFactor.AppleIPhone67) {
        @Test
        fun contentIsMeasuredAtTheSlotsLogicalSize() {
            val seen = Seen()
            screenshot(
                locales = listOf("en-US"),
                title = "probe",
                description = "probe",
                backgroundColor = Color.Black,
                fileName = "env_iphone67",
            ) { RecordingContent(seen) }
            seen.assertLogicalSize(width = 430, height = 932)
        }
    }

    class IPad13 : StoreScreenshotsTest(FormFactor.AppleIPad13) {
        @Test
        fun contentIsMeasuredAtTheSlotsLogicalSize() {
            val seen = Seen()
            screenshot(
                locales = listOf("en-US"),
                title = "probe",
                description = "probe",
                backgroundColor = Color.Black,
                fileName = "env_ipad13",
            ) { RecordingContent(seen) }
            seen.assertLogicalSize(width = 1024, height = 1366)
        }
    }
}

private fun Seen.assertLogicalSize(width: Int, height: Int) {
    assertEquals("Configuration.screenWidthDp", width, requireNotNull(screenWidthDp) { "content never composed" })
    val container = requireNotNull(containerSize) { "content never composed" }
    // The canvases render at 3x (iPhones) and 2x (iPad); containerSize is in pixels.
    val density = if (width >= 1024) 2 else 3
    assertEquals("containerSize.width", width * density, container.width)
    assertEquals("containerSize.height", height * density, container.height)
}
