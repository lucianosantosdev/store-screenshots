package dev.lucianosantos.storescreenshots.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupMaterial
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/**
 * The same iPhone frame, finished in silver rather than the black one it models by default.
 *
 * Nothing here is a second device: `mockupMaterial` recolours the solid body a tilt makes visible —
 * the rails, the buttons milled out of them, and the machined edge along them — while the device's
 * own measurements, its Dynamic Island and its iOS status bar stay exactly as they were. The front
 * bezel stays black on purpose, because it is black on every iPhone whatever the body is finished
 * in; only the metal around the outside changes.
 *
 * Turned toward its left rail, which is the side the action and volume buttons are on.
 */
class AppleIPhoneSilverExampleTest : StoreScreenshotsTest(FormFactor.AppleIPhone67) {

    @Test
    fun counter_silver() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_silver_title,
        descriptionRes = R.string.screenshot_silver_desc,
        style = silverScreenshotStyle,
    ) { CounterScreen(count = 42) }
}
