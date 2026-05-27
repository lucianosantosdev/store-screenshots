package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class PhoneStyledExampleTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test
    fun counter_styled() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_styled_title,
        descriptionRes = R.string.screenshot_styled_desc,
        style = styledScreenshotStyle,
    ) { CounterScreen(count = 42) }
}
