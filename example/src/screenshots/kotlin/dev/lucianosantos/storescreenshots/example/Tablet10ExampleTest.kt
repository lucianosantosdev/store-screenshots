package dev.lucianosantos.storescreenshots.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.ScreenshotPreview
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class Tablet10ExampleTest : StoreScreenshotsTest(FormFactor.Tablet10) {

    @Test
    @Screenshot(locales = ["en-US", "pt-BR"])
    fun counter() = capture(
        titleRes = R.string.screenshot_tablet10_title,
        descriptionRes = R.string.screenshot_tablet10_desc,
    ) { CounterScreen(count = 42) }
}

@Preview(widthDp = 800, heightDp = 1280)
@Composable
private fun Tablet10Preview() = ScreenshotPreview(
    formFactor = FormFactor.Tablet10,
    title = "Big screen, same code",
    description = "10-inch layout uses identical Compose UI",
) { CounterScreen(count = 42) }
