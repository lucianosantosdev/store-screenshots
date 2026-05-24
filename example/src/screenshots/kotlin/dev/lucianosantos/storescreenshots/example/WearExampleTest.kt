package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test
import org.robolectric.annotation.Config

@Config(application = StubApplication::class)
class WearExampleTest : StoreScreenshotsTest(FormFactor.Wear) {

    @Test
    @Screenshot(backgroundColor = 0xFF000000)
    fun counter() = capture { WearCounterScreen(count = 42) }
}
