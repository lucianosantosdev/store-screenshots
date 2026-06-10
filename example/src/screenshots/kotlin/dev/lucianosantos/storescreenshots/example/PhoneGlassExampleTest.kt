package dev.lucianosantos.storescreenshots.example

import androidx.compose.ui.Modifier
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.GlassEffect
import dev.lucianosantos.storescreenshots.GlassReflexStyle
import dev.lucianosantos.storescreenshots.GlassShadow
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import dev.lucianosantos.storescreenshots.screenGlass
import org.junit.Test

class PhoneGlassExampleTest : StoreScreenshotsTest(FormFactor.Phone) {

    /** Default glass: a soft diagonal reflex plus a shadow falling into the bottom-right corner. */
    @Test
    fun glass_default() = screenshot(
        titleRes = R.string.screenshot_counter_title,
        descriptionRes = R.string.screenshot_counter_desc,
    ) { CounterScreen(count = 42, modifier = Modifier.screenGlass()) }

    /** Steeper reflex pushed to the top, with the shadow gradient anchored to the left edge. */
    @Test
    fun glass_custom() = screenshot(
        titleRes = R.string.screenshot_counter_title,
        descriptionRes = R.string.screenshot_counter_desc,
    ) {
        CounterScreen(
            count = 42,
            modifier = Modifier.screenGlass(
                reflexAngle = 40f,
                reflexPosition = 0.2f,
                reflexWidth = 0.22f,
                reflexAlpha = 0.24f,
                shadow = GlassShadow.Left,
                shadowAlpha = 0.18f,
            ),
        )
    }

    /** Same default glass, applied via the style so the screen needs no per-call modifier. */
    @Test
    fun glass_via_style() = screenshot(
        titleRes = R.string.screenshot_counter_title,
        descriptionRes = R.string.screenshot_counter_desc,
        style = ScreenshotStyle(screenGlass = GlassEffect()),
    ) { CounterScreen(count = 42) }

    /**
     * Wedge reflex: instead of a centered band, the sheen fills from a diagonal line near the
     * center out to the border. Bright upper-right gloss + a bottom-left shadow for contrast.
     */
    @Test
    fun glass_wedge_a() = screenshot(
        titleRes = R.string.screenshot_counter_title,
        descriptionRes = R.string.screenshot_counter_desc,
    ) {
        CounterScreen(
            count = 42,
            modifier = Modifier.screenGlass(
                reflexAngle = -32f,
                reflexPosition = 0.55f,
                reflexWidth = 0.5f,
                reflexStyle = GlassReflexStyle.Wedge,
                reflexAlpha = 0.20f,
                shadow = GlassShadow.BottomLeft,
                shadowAlpha = 0.20f,
            ),
        )
    }

    /** Wedge with a sharper diagonal edge through the center and no shadow — a clean two-tone split. */
    @Test
    fun glass_wedge_b() = screenshot(
        titleRes = R.string.screenshot_counter_title,
        descriptionRes = R.string.screenshot_counter_desc,
    ) {
        CounterScreen(
            count = 42,
            modifier = Modifier.screenGlass(
                reflexAngle = -28f,
                reflexPosition = 0.5f,
                reflexWidth = 0.22f,
                reflexStyle = GlassReflexStyle.Wedge,
                reflexAlpha = 0.22f,
                shadow = GlassShadow.None,
            ),
        )
    }

    /** Broader, softer wedge from a steeper diagonal with a faint bottom-left shadow. */
    @Test
    fun glass_wedge_c() = screenshot(
        titleRes = R.string.screenshot_counter_title,
        descriptionRes = R.string.screenshot_counter_desc,
    ) {
        CounterScreen(
            count = 42,
            modifier = Modifier.screenGlass(
                reflexAngle = -40f,
                reflexPosition = 0.6f,
                reflexWidth = 0.8f,
                reflexStyle = GlassReflexStyle.Wedge,
                reflexAlpha = 0.18f,
                shadow = GlassShadow.BottomLeft,
                shadowAlpha = 0.16f,
            ),
        )
    }
}
