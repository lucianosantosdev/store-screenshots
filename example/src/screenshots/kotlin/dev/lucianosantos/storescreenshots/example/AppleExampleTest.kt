package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class AppleExampleTest : StoreScreenshotsTest(FormFactor.AppleIPhone67) {

    @Test
    fun counter() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_apple_title,
        descriptionRes = R.string.screenshot_apple_desc,
    ) { CounterScreen(count = 42) }
}
