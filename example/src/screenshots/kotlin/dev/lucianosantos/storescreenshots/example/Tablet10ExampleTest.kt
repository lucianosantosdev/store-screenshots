package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class Tablet10ExampleTest : StoreScreenshotsTest(FormFactor.Tablet10) {

    @Test
    @Screenshot(
        title = "Big screen, same code",
        description = "10-inch layout uses identical Compose UI",
    )
    fun counter() = capture { CounterScreen(count = 42) }
}
