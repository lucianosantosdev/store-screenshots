package dev.lucianosantos.storescreenshots.frames

import androidx.compose.ui.graphics.Color

/**
 * Geometry of a real iPhone, in the device's own points, measured off an iPhone 17 running iOS 26.5
 * in the Xcode 26.6 Simulator ("Show Device Bezels" on).
 *
 * Every number here was read out of two captures. Only the status bar figures are guarded by a
 * test — `IosStatusBarComparisonTest` holds the drawn glyphs against a committed capture — so if
 * you change one of the bezel figures, re-measure rather than guess. This is how the captures were
 * taken:
 *
 * ```
 * xcrun simctl boot "iPhone 17"    # any iOS 26 runtime
 * open -a Simulator                # Window > Show Device Bezels must be on
 * xcrun simctl status_bar booted override --time 9:41 \
 *     --dataNetwork wifi --wifiMode active --wifiBars 3 \
 *     --cellularMode active --cellularBars 4 \
 *     --batteryState discharging --batteryLevel 100
 * ```
 *
 * The bezel figures come from the Simulator's own window, captured with
 * `screencapture -x -o -l <window id>` and cropped to the device (side buttons included). The
 * status bar figures come from the raw framebuffer at @3x — `xcrun simctl io booted screenshot` —
 * whose top strip is 1206x210 px, i.e. 402 pt wide, matching [ScreenWidth].
 *
 * Window scale does not matter: everything below is in device points, and the frame works in
 * fractions of the enclosure's width.
 *
 * The frame is drawn as a *scale model*: [IPhoneBezel] divides its laid-out body width by
 * [BodyWidth] to get one "reference point" in dp and multiplies every constant below by it. That
 * keeps the bezel, the Dynamic Island, and the status bar in real-device proportion at any size —
 * a 1290x2796 App Store shot, a thumbnail in a feature graphic, or an IDE preview.
 *
 * Why this matters beyond looks: App Store Review guideline 2.3.10 rejects screenshots that show
 * non-iOS status bar imagery, so the status bar an iPhone frame draws has to be an iOS status bar,
 * not a Material one.
 */
internal object IPhone17Metrics {

    // ---- Screen and body ------------------------------------------------------------------

    /** Logical screen width in points (1206 px @3x). */
    const val ScreenWidth = 402f

    /** Logical screen height in points (2622 px @3x). */
    const val ScreenHeight = 874f

    /**
     * Distance from the outer edge of the body to the edge of the display, on every side. Measured
     * 15.61 / 15.05 / 15.61 / 16.17 pt (left / right / top / bottom) — the frame draws all four
     * uniformly at the mean of the two sides.
     */
    const val Bezel = 15.33f

    /** Outer width of the aluminium body. Derived, and lands on the measured 432.67 pt. */
    const val BodyWidth = ScreenWidth + 2 * Bezel

    /**
     * Outer height of the aluminium body, measured rather than derived: the top and bottom bezels
     * are a fraction thicker than the sides, so `ScreenHeight + 2 * Bezel` would come out about a
     * point short and skew the body's proportions.
     */
    const val BodyHeight = 905.5f

    /**
     * Corner radius of the body. iOS corners are continuous ("squircle") curves rather than
     * circular arcs, but a circular arc of this radius tracks the measured corner profile to
     * within half a point everywhere below the very top row, which is closer than the frame is
     * ever drawn at.
     */
    const val BodyCorner = 76.9f

    /** Corner radius of the display itself, fitted the same way to the framebuffer mask. */
    const val ScreenCorner = 63.6f

    /**
     * Width of the machined rail that runs around the outside of the body, taken from where the
     * rail's grey gives way to the black bezel in the capture (10.39 px into a 776 px-wide body).
     */
    const val Rail = 5.79f

    /** The highlight line along the very outer edge of the rail — 1.4 px in the same capture. */
    const val Rim = 0.78f

    /** The rail's face — (44, 44, 44) in the capture. */
    val RailColor = Color(0xFF2C2C2C)

    /** The highlight along the outer edge — (126, 126, 126). */
    val RimColor = Color(0xFF7E7E7E)

    /** The black bezel between the rail and the display — (0, 0, 0). */
    val BezelColor = Color(0xFF000000)

    // ---- Side buttons ---------------------------------------------------------------------

    /** How far the volume and Action buttons stand proud of the left edge. */
    const val LeftButtonProtrusion = 4.5f

    /** How far the side (power) button stands proud of the right edge. */
    const val RightButtonProtrusion = 3.9f

    /** Action button, then volume up, then volume down: (top edge from the body's top, height). */
    val LeftButtons = listOf(159.5f to 31.2f, 220.3f to 61.3f, 299.4f to 61.3f)

    /** Side (power) button: (top edge from the body's top, height). */
    val RightButtons = listOf(261.5f to 98.1f)

    /** Rounding on the outer end of a side button. */
    const val ButtonCorner = 1.8f

    /**
     * The button's face darkens to this over the last quarter of its protrusion, where it tucks
     * under the enclosure — (38, 38, 38) against the rail's (44, 44, 44) in the capture.
     */
    val ButtonShadowColor = Color(0xFF262626)

    /** How much of the visible protrusion that contact shadow covers. */
    const val ButtonShadowFraction = 0.28f

    /**
     * A button is milled out of the same aluminium as the rail, and it is the chamfer around its
     * edge that catches light — not its face, which stays as flat as the Simulator draws it. So the
     * highlight is a hairline along the button's outline: this is its width, and how bright it is
     * at the top of the button versus the bottom, with the light above the device.
     *
     * Only the outer part of that outline is ever seen; the rest of the button is behind the
     * enclosure.
     */
    const val ButtonRim = 0.6f
    const val ButtonRimTopAlpha = 0.5f
    const val ButtonRimBottomAlpha = 0.12f

    // ---- Dynamic Island -------------------------------------------------------------------

    const val IslandWidth = 125.45f
    const val IslandHeight = 36.8f

    /** Gap between the top of the display and the top of the Dynamic Island. */
    const val IslandTop = 13.94f

    // ---- Status bar -----------------------------------------------------------------------

    /**
     * Top safe-area inset — what a non-edge-to-edge screen has to leave clear so its own top bar
     * is not drawn under the status bar and the Dynamic Island.
     */
    const val SafeAreaTop = 62f

    /** Centre of the clock's digits, measured from the left edge of the display. */
    const val ClockCenterX = 73.67f

    /** Vertical centre of the clock's digits, measured from the top of the display. */
    const val ClockCenterY = 32.67f

    /** Cap height of the clock's digits. The frame sizes its font to match this, not the em size. */
    const val ClockCapHeight = 12.67f

    /** Vertical centre of the signal / Wi-Fi / battery cluster. */
    const val IconCenterY = 32.5f

    /** Left edge of the shortest cellular bar. */
    const val CellularX = 288.33f

    /** Baseline all four cellular bars sit on. */
    const val CellularBottom = 38.67f

    const val CellularBarWidth = 3.33f

    /** Left-edge to left-edge distance between neighbouring bars (a 2.0 pt gap). */
    const val CellularBarPitch = 5.33f

    const val CellularBarCorner = 1.0f

    /** Heights of the four bars, shortest first. */
    val CellularBarHeights = listOf(4.67f, 7.0f, 9.67f, 12.33f)

    const val WifiX = 315f
    const val WifiTop = 26.33f
    const val WifiWidth = 17f
    const val WifiHeight = 12.33f

    /**
     * The Wi-Fi glyph is two concentric arc bands plus a pointed dot. Both bands are [WifiStroke]
     * thick with round caps, centred [WifiArcCenterY] below the glyph's top edge, and sweep
     * ±[WifiHalfSweep]° either side of straight up.
     *
     * SF Symbols is not redistributable, so these were fitted rather than copied: candidate glyphs
     * were rasterised and scored against the reference's pixels, and this set overlaps iOS's own by
     * 94% of their combined area. [WifiArcCenterY] is not free — it is
     * `WifiOuterRadius + WifiStroke / 2`, which is what puts the outer band's crown on the glyph's
     * top edge.
     */
    const val WifiStroke = 2.5f
    const val WifiArcCenterY = 11.88f
    const val WifiOuterRadius = 10.63f
    const val WifiInnerRadius = 6.38f
    const val WifiHalfSweep = 43f

    /**
     * The dot below the arcs. It reads as a third, tiny band whose two round caps have run into
     * each other, so it is drawn as a shallow dome [WifiDotHalfWidth] wide sitting on
     * [WifiDotCenterY], tapering to a point on the glyph's baseline.
     */
    const val WifiDotHalfWidth = 2.6f
    const val WifiDotCenterY = 9.6f

    const val BatteryX = 339.33f
    const val BatteryTop = 26f
    const val BatteryWidth = 25f
    const val BatteryHeight = 13f
    const val BatteryCorner = 4.3f
    const val BatteryStroke = 1f

    /** Inset from the outline to the charge level — one point of stroke plus one point of gap. */
    const val BatteryFillInset = 2f
    const val BatteryFillCorner = 2.6f

    /** The terminal on the right-hand end: gap from the outline, then its size. */
    const val BatteryNubGap = 1f
    const val BatteryNubWidth = 1.33f
    const val BatteryNubHeight = 4f

    /** The outline is drawn at 40% of the content colour; the charge level is solid. */
    const val BatteryOutlineAlpha = 0.4f

    /** The terminal is drawn slightly stronger than the outline. */
    const val BatteryNubAlpha = 0.5f
}
