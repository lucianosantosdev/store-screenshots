package dev.lucianosantos.storescreenshots.example

import androidx.compose.runtime.Composable
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotPreview
import dev.lucianosantos.storescreenshots.AppleIPhone67ScreenshotPreview
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

@AppleIPhone67ScreenshotPreview
@Composable
private fun ApplePreview() = ScreenshotPreview(
    formFactor = FormFactor.AppleIPhone67,
    title = "Ship cross-store",
    description = "App Store Connect 6.7\" size, ready to upload",
) { CounterScreen(count = 42) }
