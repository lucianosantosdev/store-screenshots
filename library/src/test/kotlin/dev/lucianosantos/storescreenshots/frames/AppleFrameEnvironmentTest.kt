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
            seen.assertLogicalSize(width = 428, height = 926, density = 3)
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
            seen.assertLogicalSize(width = 430, height = 932, density = 3)
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
            seen.assertLogicalSize(width = 1024, height = 1366, density = 2)
        }
    }

    /**
     * An overridden `aspectRatio` reshapes the body, and the frame reports the screen that reshaping
     * leaves rather than the slot's logical height.
     *
     * The override branch used to keep reporting the logical 932dp while laying content out in the
     * taller box the new body opened up — the frame lying about the one number it exists to make
     * honest. 0.42 is well clear of the device's own 0.478, so a frame that ignored the override
     * (as the old float-equality default did) or went on reporting 932dp fails loudly here.
     */
    class OverriddenBodyRatio : StoreScreenshotsTest(FormFactor.AppleIPhone67) {
        @Test
        fun theFrameReportsTheScreenTheOverrideLeaves() {
            val seen = Seen()
            customScreenshot(fileName = "env_iphone67_squat") {
                AppleFrame(
                    title = "probe",
                    description = "probe",
                    backgroundColor = Color.Black,
                    formFactor = FormFactor.AppleIPhone67,
                    aspectRatio = 0.42f,
                ) { RecordingContent(seen) }
            }
            seen.assertReportedSizeMatchesMeasured(density = 3)
            val height = requireNotNull(seen.screenHeightDp)
            assert(height > 932) {
                "a body stretched from 0.478 to 0.42 leaves a taller screen than the logical " +
                    "932dp, but the frame reported ${height}dp"
            }
        }
    }
}
