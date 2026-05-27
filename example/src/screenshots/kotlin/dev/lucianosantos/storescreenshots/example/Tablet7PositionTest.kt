package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class Tablet7PositionTest : StoreScreenshotsTest(FormFactor.Tablet7) {

    @Test fun tablet7_top() = screenshot(
        title = "Mockup at top",
        description = "7-inch tablet with device above text",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Top),
    ) { CounterScreen(count = 42) }

    @Test fun tablet7_middle() = screenshot(
        title = "Mockup centered",
        description = "7-inch tablet with device in the middle",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Middle),
    ) { CounterScreen(count = 42) }

    @Test fun tablet7_bottom() = screenshot(
        title = "Mockup at bottom",
        description = "7-inch tablet with device below text",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Bottom),
    ) { CounterScreen(count = 42) }
}
