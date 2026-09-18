package dev.lucianosantos.storescreenshots.example

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/**
 * An iPhone turned far enough to put its whole right rail on show, which is what makes the point
 * the Android example cannot: the side button is not painted on the front of the device, it is
 * standing on the rail, and it turns with it.
 */
class AppleIPhonePerspectiveExampleTest : StoreScreenshotsTest(FormFactor.AppleIPhone67) {

    @Test
    fun counter_perspective() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_apple_perspective_title,
        descriptionRes = R.string.screenshot_apple_perspective_desc,
        style = ScreenshotStyle(
            mockupPosition = MockupPosition.Middle,
            mockupOffset = DpOffset(x = 16.dp, y = 0.dp),
            mockupRotationY = -30f,
            mockupRotationX = 6f,
            mockupRotation = -4f,
            mockupElevation = 20.dp,
            background = { MarketingBackground() },
            title = { text -> StyledTitle(text) },
            description = { text -> StyledDescription(text) },
        ),
    ) { CounterScreen(count = 42) }
}
