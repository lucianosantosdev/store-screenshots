package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class Tablet7ExampleTest : StoreScreenshotsTest(FormFactor.Tablet7) {

    @Test
    fun counter() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_tablet7_title,
        descriptionRes = R.string.screenshot_tablet7_desc,
    ) { CounterScreen(count = 42) }
}
