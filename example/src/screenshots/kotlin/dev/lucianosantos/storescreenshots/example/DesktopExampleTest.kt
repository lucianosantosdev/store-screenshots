package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class DesktopExampleTest : StoreScreenshotsTest(FormFactor.Desktop) {

    @Test fun counter() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_counter_title,
        descriptionRes = R.string.screenshot_counter_desc,
    ) { CounterScreen(count = 42) }
}
