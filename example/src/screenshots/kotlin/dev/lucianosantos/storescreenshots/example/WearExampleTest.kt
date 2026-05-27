package dev.lucianosantos.storescreenshots.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotPreview
import dev.lucianosantos.storescreenshots.WearScreenshotPreview
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class WearExampleTest : StoreScreenshotsTest(FormFactor.Wear) {

    @Test
    fun counter() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        backgroundColor = Color.Black,
    ) { WearCounterScreen(count = 42) }
}

@WearScreenshotPreview
@Composable
private fun WearPreview() = ScreenshotPreview(
    formFactor = FormFactor.Wear,
    backgroundColor = Color.Black,
) { WearCounterScreen(count = 42) }
