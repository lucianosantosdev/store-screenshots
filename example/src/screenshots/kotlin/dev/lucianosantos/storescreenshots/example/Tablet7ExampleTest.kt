package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class Tablet7ExampleTest : StoreScreenshotsTest(FormFactor.Tablet7) {

    @Test
    @Screenshot(
        title = "Built for every screen",
        description = "The same Compose UI, framed for 7-inch tablets",
    )
    fun counter() = capture { CounterScreen(count = 42) }
}
