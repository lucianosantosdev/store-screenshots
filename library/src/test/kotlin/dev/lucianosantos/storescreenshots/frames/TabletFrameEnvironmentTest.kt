package dev.lucianosantos.storescreenshots.frames

import androidx.compose.ui.graphics.Color
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.RecordingContent
import dev.lucianosantos.storescreenshots.Seen
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import dev.lucianosantos.storescreenshots.assertLogicalSize
import dev.lucianosantos.storescreenshots.assertReportedSizeMatchesMeasured
import org.junit.Test

/**
 * [TabletFrame] measures content at the slot's logical size — 600x960dp for the 7-inch slot,
 * 800x1280dp for the 10-inch — the same contract [PhoneFrame] and [AppleFrame] hold.
 *
 * It used to place content straight into the bezel's on-canvas footprint and set no device
 * environment at all: on the 600x960dp 7-inch canvas the banner's 96dp of padding and the bezel
 * each took their share first, so the app was measured at ~484dp while `Configuration` and
 * `LocalWindowInfo` went on reporting the canvas's own 600x960. Content was a fifth narrower than
 * the number it was handed, and every sp and dp inside it rendered oversized to match — the
 * identical defect the Apple frames were fixed for, left behind on the Android tablets.
 */
class TabletFrameEnvironmentTest {

    class Tablet7 : StoreScreenshotsTest(FormFactor.Tablet7) {
        @Test
        fun contentIsMeasuredAtTheSlotsLogicalSize() {
            val seen = Seen()
            screenshot(
                locales = listOf("en-US"),
                title = "probe",
                description = "probe",
                backgroundColor = Color.Black,
                fileName = "env_tablet7",
            ) { RecordingContent(seen) }
            seen.assertLogicalSize(width = 600, height = 960, density = 2)
            seen.assertReportedSizeMatchesMeasured(density = 2)
        }
    }

    class Tablet10 : StoreScreenshotsTest(FormFactor.Tablet10) {
        @Test
        fun contentIsMeasuredAtTheSlotsLogicalSize() {
            val seen = Seen()
            screenshot(
                locales = listOf("en-US"),
                title = "probe",
                description = "probe",
                backgroundColor = Color.Black,
                fileName = "env_tablet10",
            ) { RecordingContent(seen) }
            seen.assertLogicalSize(width = 800, height = 1280, density = 2)
            seen.assertReportedSizeMatchesMeasured(density = 2)
        }
    }
}
