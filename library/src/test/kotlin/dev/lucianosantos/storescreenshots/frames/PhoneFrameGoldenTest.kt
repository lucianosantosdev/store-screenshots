package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotRule
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/**
 * Golden images for the phone frame, and for a `Middle`-positioned frame.
 *
 * The frame rewrite that moved [PhoneFrame] onto `DeviceMockup`, and the `MockupPosition.Middle`
 * change that bounded the mockup's height, were both argued to leave the composition alone. That
 * claim was only ever checked by looking at the output. These are what make it survive the next
 * refactor: the whole pipeline runs — frame, layout, flatten, write — and the PNG is held against
 * a committed capture of what it is supposed to look like.
 *
 * Unlike [IosStatusBarComparisonTest], which deliberately compares against a real iOS capture
 * rather than our own output, a golden here *is* our own output. That is the point — it does not
 * say the layout is right, it says the layout has not moved. Regenerate deliberately (delete the
 * reference and re-run, then look at the image) whenever a change is meant to alter the frame.
 *
 * The comparison itself, and why it is tolerant rather than exact, lives in [GoldenImage].
 */
class PhoneFrameGoldenTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test
    fun phoneFrameMatchesItsGolden() {
        screenshot(
            fileName = "golden_phone_frame",
            title = "Count anything",
            description = "A focused tap counter that gets out of your way",
        ) { GoldenContent() }

        assertMatchesGolden("golden_phone_frame", "phone_frame.png")
    }

    /**
     * `Middle` is the position the layout change actually rewrote — it used to place an unbounded
     * mockup between two weighted spacers, and now sits in a weighted `Column`. Every form factor
     * goes through that branch, so it is the one most worth pinning.
     */
    @Test
    fun middlePositionedFrameMatchesItsGolden() {
        screenshot(
            fileName = "golden_phone_middle",
            title = "Mockup centered",
            description = "Title above, description below",
            style = ScreenshotStyle(mockupPosition = MockupPosition.Middle),
        ) { GoldenContent() }

        assertMatchesGolden("golden_phone_middle", "phone_frame_middle.png")
    }

    /**
     * Deliberately plain: flat colours and one line of text, so a diff points at the frame and the
     * layout rather than at whatever the sample app happened to draw.
     */
    @Composable
    private fun GoldenContent() {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF6A1B9A)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "42", color = Color.White, fontSize = 96.sp, fontWeight = FontWeight.Bold)
        }
    }

    private fun assertMatchesGolden(writtenName: String, referenceName: String) =
        GoldenImage.assertMatches(writtenName, referenceName)
}
