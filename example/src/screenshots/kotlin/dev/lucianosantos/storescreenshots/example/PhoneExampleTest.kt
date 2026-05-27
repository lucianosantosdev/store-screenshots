package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class PhoneExampleTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test
    fun counter() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_counter_title,
        descriptionRes = R.string.screenshot_counter_desc,
    ) { CounterScreen(count = 42) }
}
