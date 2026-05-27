package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class DesktopPositionTest : StoreScreenshotsTest(FormFactor.Desktop) {

    @Test fun desktop_top() = screenshot(
        title = "Mockup at top",
        description = "Desktop with monitor above text",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Top),
    ) { CounterScreen(count = 42) }

    @Test fun desktop_middle() = screenshot(
        title = "Mockup centered",
        description = "Desktop with monitor in the middle",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Middle),
    ) { CounterScreen(count = 42) }

    @Test fun desktop_bottom() = screenshot(
        title = "Mockup at bottom",
        description = "Desktop with monitor below text",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Bottom),
    ) { CounterScreen(count = 42) }
}
