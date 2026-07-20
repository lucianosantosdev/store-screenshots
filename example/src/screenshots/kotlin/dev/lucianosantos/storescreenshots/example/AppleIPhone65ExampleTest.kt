package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/** The 6.5" slot App Store Connect selects by default. */
class AppleIPhone65ExampleTest : StoreScreenshotsTest(FormFactor.AppleIPhone65) {

    @Test
    fun counter() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_apple_title,
        descriptionRes = R.string.screenshot_apple65_desc,
    ) { CounterScreen(count = 42) }
}
