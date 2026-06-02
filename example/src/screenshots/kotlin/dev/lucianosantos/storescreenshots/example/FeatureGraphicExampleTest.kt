package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/**
 * A feature graphic has no built-in title/description frame — it is a promotional banner you
 * compose yourself with [customScreenshot], dropping a `DeviceMockup` for each form factor your
 * app supports. See [FeatureGraphicBanner].
 */
class FeatureGraphicExampleTest : StoreScreenshotsTest(FormFactor.GooglePlayFeatureGraphic) {

    @Test
    fun feature_graphic() = customScreenshot(
        locales = listOf("en-US", "pt-BR"),
    ) { FeatureGraphicBanner() }
}
