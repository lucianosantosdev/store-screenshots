package dev.lucianosantos.storescreenshots.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotPreview
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class PhoneExampleTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test
    fun counter() = capture(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_counter_title,
        descriptionRes = R.string.screenshot_counter_desc,
    ) { CounterScreen(count = 42) }
}

@Preview(widthDp = 411, heightDp = 914)
@Composable
private fun PhonePreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Count anything, anywhere",
    description = "A focused tap counter that gets out of your way",
) { CounterScreen(count = 42) }
