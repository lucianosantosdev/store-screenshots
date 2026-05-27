package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class PhoneCustomExampleTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test fun custom_layout() = customScreenshot {
        CustomScreenshotLayout {
            Mockup { CounterScreen(count = 42) }
        }
    }
}
