package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class Tablet10PositionTest : StoreScreenshotsTest(FormFactor.Tablet10) {

    @Test fun tablet10_top() = screenshot(
        title = "Mockup at top",
        description = "10-inch tablet with device above text",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Top),
    ) { CounterScreen(count = 42) }

    @Test fun tablet10_middle() = screenshot(
        title = "Mockup centered",
        description = "10-inch tablet with device in the middle",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Middle),
    ) { CounterScreen(count = 42) }

    @Test fun tablet10_bottom() = screenshot(
        title = "Mockup at bottom",
        description = "10-inch tablet with device below text",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Bottom),
    ) { CounterScreen(count = 42) }
}
