package dev.lucianosantos.storescreenshots.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.ScreenshotPreview
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class WearExampleTest : StoreScreenshotsTest(FormFactor.Wear) {

    @Test
    @Screenshot(locales = ["en-US", "pt-BR"], backgroundColor = 0xFF000000)
    fun counter() = capture { WearCounterScreen(count = 42) }
}

@Preview(widthDp = 227, heightDp = 227)
@Composable
private fun WearPreview() = ScreenshotPreview(
    formFactor = FormFactor.Wear,
    backgroundColor = Color.Black,
) { WearCounterScreen(count = 42) }
