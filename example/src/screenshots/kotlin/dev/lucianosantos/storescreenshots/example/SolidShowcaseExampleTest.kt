package dev.lucianosantos.storescreenshots.example

import androidx.compose.ui.res.stringResource
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotCanvas
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/**
 * Nine devices laid out as a compass, flat in the middle — the showcase for the solid renderer.
 * See [SolidShowcaseBanner] for why these particular angles.
 *
 * On a tall canvas rather than the feature graphic's own 1024x500. A square grid of phones has the
 * shape of a single phone whatever its size — three across are half as tall as they are wide, and
 * three rows of them are twice as tall as they are wide — so it wants roughly 1:2, which is the one
 * shape a 2:1 banner cannot hold. The form factor is still the feature graphic, so the shot lands
 * with the other banners; only its size is overridden.
 */
class SolidShowcaseExampleTest : StoreScreenshotsTest(
    FormFactor.GooglePlayFeatureGraphic,
    canvas = ScreenshotCanvas.px(1200, 2200),
) {

    @Test
    fun solid_showcase() = customScreenshot(
        locales = listOf("en-US", "pt-BR"),
        fileName = "solid_showcase",
        subdir = "featureGraphic",
    ) {
        SolidShowcaseBanner(
            title = stringResource(R.string.screenshot_showcase_title),
            description = stringResource(R.string.screenshot_showcase_desc),
        )
    }
}
