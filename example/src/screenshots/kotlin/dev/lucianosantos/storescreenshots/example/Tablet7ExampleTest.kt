package dev.lucianosantos.storescreenshots.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.ScreenshotPreview
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class Tablet7ExampleTest : StoreScreenshotsTest(FormFactor.Tablet7) {

    @Test
    @Screenshot(locales = ["en-US", "pt-BR"])
    fun counter() = capture(
        titleRes = R.string.screenshot_tablet7_title,
        descriptionRes = R.string.screenshot_tablet7_desc,
    ) { CounterScreen(count = 42) }
}

@Preview(widthDp = 600, heightDp = 960)
@Composable
private fun Tablet7Preview() = ScreenshotPreview(
    formFactor = FormFactor.Tablet7,
    title = "Built for every screen",
    description = "The same Compose UI, framed for 7-inch tablets",
) { CounterScreen(count = 42) }
