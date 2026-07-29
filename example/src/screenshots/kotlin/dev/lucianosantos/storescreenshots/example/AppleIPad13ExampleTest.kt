package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/** The 13" iPad slot App Store Connect requires for any app that runs on iPad. */
class AppleIPad13ExampleTest : StoreScreenshotsTest(FormFactor.AppleIPad13) {

    @Test
    fun counter() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_apple_title,
        descriptionRes = R.string.screenshot_ipad13_desc,
    ) { CounterScreen(count = 42) }
}
