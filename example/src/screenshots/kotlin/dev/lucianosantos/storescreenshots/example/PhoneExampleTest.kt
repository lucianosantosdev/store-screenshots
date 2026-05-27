package dev.lucianosantos.storescreenshots.example

import androidx.compose.runtime.Composable
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotPreview
import dev.lucianosantos.storescreenshots.PhoneScreenshotPreview
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

@PhoneScreenshotPreview
@Composable
private fun PhonePreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Count anything, anywhere",
    description = "A focused tap counter that gets out of your way",
) { CounterScreen(count = 42) }
