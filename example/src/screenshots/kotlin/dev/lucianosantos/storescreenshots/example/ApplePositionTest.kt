package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class ApplePositionTest : StoreScreenshotsTest(FormFactor.AppleIPhone67) {

    @Test fun apple_top() = screenshot(
        title = "Mockup at top",
        description = "iPhone 6.7\" with device above text",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Top),
    ) { CounterScreen(count = 42) }

    @Test fun apple_middle() = screenshot(
        title = "Mockup centered",
        description = "iPhone 6.7\" with device in the middle",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Middle),
    ) { CounterScreen(count = 42) }

    @Test fun apple_bottom() = screenshot(
        title = "Mockup at bottom",
        description = "iPhone 6.7\" with device below text",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Bottom),
    ) { CounterScreen(count = 42) }
}
