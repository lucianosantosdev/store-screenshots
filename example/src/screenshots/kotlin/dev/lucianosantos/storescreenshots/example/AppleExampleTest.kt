package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class AppleExampleTest : StoreScreenshotsTest(FormFactor.AppleIPhone67) {

    @Test
    @Screenshot(
        title = "Ship cross-store",
        description = "App Store Connect 6.7\" size, ready to upload",
    )
    fun counter() = capture { CounterScreen(count = 42) }
}
