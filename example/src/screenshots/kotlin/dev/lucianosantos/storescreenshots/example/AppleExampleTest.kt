package dev.lucianosantos.storescreenshots.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.ScreenshotPreview
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class AppleExampleTest : StoreScreenshotsTest(FormFactor.AppleIPhone67) {

    @Test
    @Screenshot(locales = ["en-US", "pt-BR"])
    fun counter() = capture(
        titleRes = R.string.screenshot_apple_title,
        descriptionRes = R.string.screenshot_apple_desc,
    ) { CounterScreen(count = 42) }
}

@Preview(widthDp = 430, heightDp = 932)
@Composable
private fun ApplePreview() = ScreenshotPreview(
    formFactor = FormFactor.AppleIPhone67,
    title = "Ship cross-store",
    description = "App Store Connect 6.7\" size, ready to upload",
) { CounterScreen(count = 42) }
