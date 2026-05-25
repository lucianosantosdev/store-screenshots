package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class PhoneExampleTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test
    @Screenshot(
        title = "Count anything, anywhere",
        description = "A focused tap counter that gets out of your way",
    )
    fun counter() = capture { CounterScreen(count = 42) }
}
