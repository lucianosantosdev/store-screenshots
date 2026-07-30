package dev.lucianosantos.storescreenshots.frames

import androidx.compose.ui.graphics.Color

/**
 * Geometry of a real iPad, in the device's own points, measured off a 13-inch iPad Air (M4) running
 * iPadOS 26.5 in the Xcode 26.6 Simulator ("Show Device Bezels" on).
 *
 * That model was chosen because its display is exactly 2048x2732 px — the size
 * [dev.lucianosantos.storescreenshots.FormFactor.AppleIPad13] writes — so the capture maps onto the
 * output without rescaling.
 *
 * Only the status bar figures are guarded by a test (`IPadOsStatusBarComparisonTest` holds the drawn
 * glyphs against a committed capture), so if you change one of the bezel figures, re-measure rather
 * than guess. This is how the captures were taken:
 *
 * ```
 * xcrun simctl boot "iPad Air 13-inch (M4)"
 * open -a Simulator                     # Window > Show Device Bezels must be on
 * xcrun simctl ui booted appearance dark
 * xcrun simctl status_bar booted override --time 9:41 \
 *     --dataNetwork wifi --wifiMode active --wifiBars 3 \
 *     --batteryState discharging --batteryLevel 100
 * ```
 *
 * The bezel figures come from the Simulator's own window, captured with
 * `screencapture -x -o -l <window id>` — in *light* appearance, so the lit display is easy to tell
 * from the black bezel. The status bar figures come from the raw framebuffer at @2x in dark
 * appearance, `xcrun simctl io booted screenshot`, whose 2048 px width is 1024 pt: [ScreenWidth].
 *
 * The same drawing code serves both this and [IPhone17Metrics]; what differs is the numbers.
 */
internal object IPadAir13Metrics {

    // ---- Screen and body ------------------------------------------------------------------

    /** Logical screen width in points (2048 px @2x). */
    const val ScreenWidth = 1024f

    /** Logical screen height in points (2732 px @2x). */
    const val ScreenHeight = 1366f

    /**
     * Distance from the outer edge of the body to the edge of the display. Measured 57.21 / 57.21 /
     * 60.72 / 58.38 pt (left / right / top / bottom); the frame draws all four at their mean.
     */
    const val Bezel = 58.4f

    /** Outer width of the aluminium body. Derived, and lands on the measured 1138.4 pt. */
    const val BodyWidth = ScreenWidth + 2 * Bezel

    /**
     * Outer height of the body, measured rather than derived — the top bezel is a couple of points
     * deeper than the sides, and deriving it would skew the body's proportions.
     */
    const val BodyHeight = 1484f

    /**
     * Corner radius of the body. Fitted to the measured corner profile, which a circular arc tracks
     * to within a point or so — iPad corners are far squarer than an iPhone's.
     */
    const val BodyCorner = 83f

    /** Corner radius of the display itself. Small: an iPad's screen is nearly a rectangle. */
    const val ScreenCorner = 12f

    /** Width of the machined rail around the outside of the body. */
    const val Rail = 5.25f

    /** The highlight line along the very outer edge of the rail. */
    const val Rim = 1.2f

    /** The rail's face — (39, 39, 39) in the capture. */
    val RailColor = Color(0xFF272727)

    /** The highlight along the outer edge — (91, 91, 91). */
    val RimColor = Color(0xFF5B5B5B)

    /** The bezel between the rail and the display — (2, 2, 2). */
    val BezelColor = Color(0xFF020202)

    // ---- Buttons --------------------------------------------------------------------------

    /** Volume up and volume down, on the right edge: (top edge from the body's top, height). */
    val RightButtons = listOf(123.8f to 47.9f, 185.7f to 49f)

    /** How far the volume buttons stand proud of the right edge. */
    const val RightButtonProtrusion = 2.3f

    /**
     * The top (power) button, which on an iPad lies along the top edge rather than a side:
     * (left edge from the body's left, width).
     */
    val TopButton = 967.9f to 85.3f

    /** How far the top button stands proud of the top edge. */
    const val TopButtonProtrusion = 1.2f

    /** Rounding on the outer end of a button. */
    const val ButtonCorner = 1.8f

    /** Buttons are milled from the same aluminium as the rail, so they share its treatment. */
    val ButtonShadowColor = Color(0xFF1E1E1E)
    const val ButtonShadowFraction = 0.28f
    const val ButtonRim = 0.9f
    const val ButtonRimTopAlpha = 0.5f
    const val ButtonRimBottomAlpha = 0.12f

    // ---- Status bar -----------------------------------------------------------------------

    /** Top safe-area inset — an iPad has no cutout, so its status bar is much shallower. */
    const val SafeAreaTop = 24f

    /**
     * iPadOS puts the clock at the leading edge rather than centring it the way iOS does on a
     * phone, and follows it with the date. This is where its ink starts.
     */
    const val ClockLeft = 17f

    /** Vertical centre of the clock's digits, measured from the top of the display. */
    const val ClockCenterY = 16.25f

    /** Cap height of the clock's digits. The frame sizes its font to match this. */
    const val ClockCapHeight = 10.5f

    /** Vertical centre of the Wi-Fi and battery cluster. */
    const val IconCenterY = 16f

    /**
     * No cellular bars: a Wi-Fi iPad shows none, and it is the model whose screenshots go in the
     * iPad slot. The Wi-Fi glyph and the battery are the same shapes iOS draws on a phone — see
     * [IPhone17Metrics] — just smaller and placed here.
     */
    const val WifiX = 963f
    const val WifiTop = 11f
    const val WifiWidth = 14f
    const val WifiHeight = 10f

    const val BatteryX = 981.5f
    const val BatteryTop = 9.5f
    const val BatteryWidth = 25f
    const val BatteryHeight = 13f
}
