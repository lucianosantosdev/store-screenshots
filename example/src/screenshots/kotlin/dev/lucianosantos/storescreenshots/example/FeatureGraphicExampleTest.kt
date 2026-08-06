package dev.lucianosantos.storescreenshots.example

import androidx.compose.ui.res.stringResource
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
        // The form factor defaults to subdir "." (a real app's single asset lands at
        // images/featureGraphic.png). This sample emits a gallery of demo banners, so group them
        // under images/featureGraphic/ instead.
        subdir = "featureGraphic",
    ) {
        FeatureGraphicBanner(
            title = stringResource(R.string.screenshot_feature_title),
            description = stringResource(R.string.screenshot_feature_desc),
        )
    }
}
