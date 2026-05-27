package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class PhonePositionTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test fun phone_top() = screenshot(
        title = "Mockup at top",
        description = "Title and description below the device",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Top),
    ) { CounterScreen(count = 42) }

    @Test fun phone_middle() = screenshot(
        title = "Mockup centered",
        description = "Title above, description below",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Middle),
    ) { CounterScreen(count = 42) }

    @Test fun phone_bottom() = screenshot(
        title = "Mockup at bottom",
        description = "Title and description above the device",
        style = ScreenshotStyle(mockupPosition = MockupPosition.Bottom),
    ) { CounterScreen(count = 42) }
}
