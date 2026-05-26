package dev.lucianosantos.storescreenshots

import androidx.compose.ui.text.font.FontFamily

/**
 * String-safe enum for font families usable inside annotations (which can't hold [FontFamily]
 * instances directly). The [ScreenshotRule] maps each value to its Compose counterpart.
 *
 * [Inherit] means "use whatever the class-level [ScreenshotStyle.fontFamily] says."
 */
enum class FontFamilyName {
    Inherit,
    Default,
    SansSerif,
    Serif,
    Monospace,
    Cursive;

    fun toFontFamily(): FontFamily = when (this) {
        Inherit -> FontFamily.Default
        Default -> FontFamily.Default
        SansSerif -> FontFamily.SansSerif
        Serif -> FontFamily.Serif
        Monospace -> FontFamily.Monospace
        Cursive -> FontFamily.Cursive
    }
}
