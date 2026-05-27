package dev.lucianosantos.storescreenshots.example

import androidx.compose.ui.graphics.Color
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class WearExampleTest : StoreScreenshotsTest(FormFactor.Wear) {

    @Test
    fun counter() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        backgroundColor = Color.Black,
    ) { WearCounterScreen(count = 42) }
}
