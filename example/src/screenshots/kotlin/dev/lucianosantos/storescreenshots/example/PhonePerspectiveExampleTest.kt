package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class PhonePerspectiveExampleTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test
    fun counter_perspective() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_perspective_title,
        descriptionRes = R.string.screenshot_perspective_desc,
        style = perspectiveScreenshotStyle,
    ) { CounterScreen(count = 42) }
}
