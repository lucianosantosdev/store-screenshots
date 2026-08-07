package dev.lucianosantos.storescreenshots.frames

import androidx.compose.ui.graphics.Color

/**
 * Everything [IPhoneBezel] and [IosStatusBar] need to draw one particular iPhone, in that device's
 * own points.
 *
 * There is one implementation per modelled device — [IPhone17Metrics] and [IPhone17ProMaxMetrics] —
 * and the drawing code is shared: it divides its laid-out body width by [BodyWidth] to get one
 * "reference point" in dp and multiplies every figure below by it. That keeps the bezel, the Dynamic
 * Island and the status bar in real-device proportion at any size — a 1284x2778 App Store shot, a
 * thumbnail in a feature graphic, or an IDE preview.
 *
 * Property names are PascalCase to read as the measurements they are (`BodyWidth`, `CellularX`)
 * rather than as ordinary Kotlin properties.
 */
internal interface IPhoneMetrics {

    // ---- Screen and body ------------------------------------------------------------------

    /** Logical screen width in points. */
    val ScreenWidth: Float

    /** Logical screen height in points. */
    val ScreenHeight: Float

    /** Distance from the outer edge of the body to the edge of the display, on every side. */
    val Bezel: Float

    /** Outer width of the enclosure. */
    val BodyWidth: Float

    /** Outer height of the enclosure. */
    val BodyHeight: Float

    /**
     * Corner radius of the body. iOS corners are continuous ("squircle") curves rather than circular
     * arcs, so this is the circular arc that tracks the real profile most closely.
     */
    val BodyCorner: Float

    /** Corner radius of the display itself. */
    val ScreenCorner: Float

    /** Width of the machined rail that runs around the outside of the body. */
    val Rail: Float

    /** The highlight line along the very outer edge of the rail. */
    val Rim: Float

    /** The rail's face. */
    val RailColor: Color

    /** The highlight along the outer edge. */
    val RimColor: Color

    /** The black bezel between the rail and the display. */
    val BezelColor: Color

    // ---- Side buttons ---------------------------------------------------------------------

    /** How far the volume and Action buttons stand proud of the left edge. */
    val LeftButtonProtrusion: Float

    /** How far the side (power) button stands proud of the right edge. */
    val RightButtonProtrusion: Float

    /** Action button, then volume up, then volume down: (top edge from the body's top, height). */
    val LeftButtons: List<Pair<Float, Float>>

    /** Side (power) button: (top edge from the body's top, height). */
    val RightButtons: List<Pair<Float, Float>>

    /** Rounding on the outer end of a side button. */
    val ButtonCorner: Float

    /** The button's face darkens to this where it tucks under the enclosure. */
    val ButtonShadowColor: Color

    /** How much of the visible protrusion that contact shadow covers. */
    val ButtonShadowFraction: Float

    /**
     * A button is milled out of the same aluminium as the rail, and it is the chamfer around its
     * edge that catches light — not its face, which stays as flat as the Simulator draws it. So the
     * highlight is a hairline along the button's outline: this is its width, and how bright it is at
     * the top of the button versus the bottom, with the light above the device.
     */
    val ButtonRim: Float
    val ButtonRimTopAlpha: Float
    val ButtonRimBottomAlpha: Float

    // ---- Dynamic Island -------------------------------------------------------------------

    val IslandWidth: Float
    val IslandHeight: Float

    /** Gap between the top of the display and the top of the Dynamic Island. */
    val IslandTop: Float

    // ---- Status bar -----------------------------------------------------------------------

    /**
     * Top safe-area inset — what a non-edge-to-edge screen has to leave clear so its own top bar is
     * not drawn under the status bar and the Dynamic Island.
     */
    val SafeAreaTop: Float

    /** Centre of the clock's digits, measured from the left edge of the display. */
    val ClockCenterX: Float

    /** Vertical centre of the clock's digits, measured from the top of the display. */
    val ClockCenterY: Float

    /** Cap height of the clock's digits. The frame sizes its font to match this, not the em size. */
    val ClockCapHeight: Float

    /** Left edge of the shortest cellular bar. */
    val CellularX: Float

    /** Baseline all four cellular bars sit on. */
    val CellularBottom: Float

    val CellularBarWidth: Float

    /** Left-edge to left-edge distance between neighbouring bars. */
    val CellularBarPitch: Float

    val CellularBarCorner: Float

    /** Heights of the four bars, shortest first. */
    val CellularBarHeights: List<Float>

    val WifiX: Float
    val WifiTop: Float

    /** Width the Wi-Fi glyph is drawn at, which [Wifi]'s proportions are scaled to. */
    val WifiWidth: Float

    /** The Wi-Fi glyph's shape. */
    val Wifi: WifiGlyph

    val BatteryX: Float
    val BatteryTop: Float
    val BatteryWidth: Float
}

/**
 * The shape of the Wi-Fi glyph: two concentric arc bands plus a dot that tapers to a point. Both
 * bands are [Stroke] thick with round caps, centred [ArcCenterY] below the glyph's top edge, and
 * sweep ±[HalfSweep]° either side of straight up. The dot reads as a third, tiny band whose two
 * round caps have run into each other, so it is a shallow dome [DotHalfWidth] wide sitting on
 * [DotCenterY], tapering to a point on the glyph's baseline.
 *
 * SF Symbols is not redistributable, so these are fitted rather than copied: candidate glyphs are
 * rasterised and scored against a capture's pixels by how much of their combined area they share.
 * iOS does not draw one Wi-Fi glyph at every size — the Pro Max's is a little squarer than the
 * iPhone 17's, not just larger — so each device carries its own fit, and [Width] is the size that
 * fit was made at. [drawWifiGlyph] scales everything by the width it is asked for, so one shape
 * still serves an App Store frame, a thumbnail and the iPad, which reuses the iPhone 17's.
 */
internal class WifiGlyph(
    val Width: Float,
    val Height: Float,
    val Stroke: Float,
    val OuterRadius: Float,
    val InnerRadius: Float,
    val HalfSweep: Float,
    val DotHalfWidth: Float,
    val DotCenterY: Float,
) {
    /**
     * Not a free parameter — it is `OuterRadius + Stroke / 2`, which is what puts the outer band's
     * crown on the glyph's top edge.
     */
    val ArcCenterY: Float get() = OuterRadius + Stroke / 2
}

/**
 * Geometry of a real iPhone 17, in the device's own points, measured off an iPhone 17 running
 * iOS 26.5 in the Xcode 26.6 Simulator ("Show Device Bezels" on).
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
 * [IPhone17ProMaxMetrics] was measured a second way, off the Simulator's own vector artwork; that
 * pass reproduces the figures below to within half a point, which is as close as reading them off a
 * screenshot gets.
 *
 * Why this matters beyond looks: App Store Review guideline 2.3.10 rejects screenshots that show
 * non-iOS status bar imagery, so the status bar an iPhone frame draws has to be an iOS status bar,
 * not a Material one.
 */
internal object IPhone17Metrics : IPhoneMetrics {

    // ---- Screen and body ------------------------------------------------------------------

    /** Logical screen width in points (1206 px @3x). */
    override val ScreenWidth = 402f

    /** Logical screen height in points (2622 px @3x). */
    override val ScreenHeight = 874f

    /**
     * Distance from the outer edge of the body to the edge of the display, on every side. Measured
     * 15.61 / 15.05 / 15.61 / 16.17 pt (left / right / top / bottom) — the frame draws all four
     * uniformly at the mean of the two sides.
     */
    override val Bezel = 15.33f

    /** Outer width of the aluminium body. Derived, and lands on the measured 432.67 pt. */
    override val BodyWidth = ScreenWidth + 2 * Bezel

    /**
     * Outer height of the aluminium body, measured rather than derived: the top and bottom bezels
     * are a fraction thicker than the sides, so `ScreenHeight + 2 * Bezel` would come out about a
     * point short and skew the body's proportions.
     */
    override val BodyHeight = 905.5f

    /**
     * Corner radius of the body. iOS corners are continuous ("squircle") curves rather than
     * circular arcs, but a circular arc of this radius tracks the measured corner profile to
     * within half a point everywhere below the very top row, which is closer than the frame is
     * ever drawn at.
     */
    override val BodyCorner = 76.9f

    /** Corner radius of the display itself, fitted the same way to the framebuffer mask. */
    override val ScreenCorner = 63.6f

    /**
     * Width of the machined rail that runs around the outside of the body, taken from where the
     * rail's grey gives way to the black bezel in the capture (10.39 px into a 776 px-wide body).
     */
    override val Rail = 5.79f

    /** The highlight line along the very outer edge of the rail — 1.4 px in the same capture. */
    override val Rim = 0.78f

    /** The rail's face — (44, 44, 44) in the capture. */
    override val RailColor = Color(0xFF2C2C2C)

    /** The highlight along the outer edge — (126, 126, 126). */
    override val RimColor = Color(0xFF7E7E7E)

    /** The black bezel between the rail and the display — (0, 0, 0). */
    override val BezelColor = Color(0xFF000000)

    // ---- Side buttons ---------------------------------------------------------------------

    /** How far the volume and Action buttons stand proud of the left edge. */
    override val LeftButtonProtrusion = 4.5f

    /** How far the side (power) button stands proud of the right edge. */
    override val RightButtonProtrusion = 3.9f

    /** Action button, then volume up, then volume down: (top edge from the body's top, height). */
    override val LeftButtons = listOf(159.5f to 31.2f, 220.3f to 61.3f, 299.4f to 61.3f)

    /** Side (power) button: (top edge from the body's top, height). */
    override val RightButtons = listOf(261.5f to 98.1f)

    /** Rounding on the outer end of a side button. */
    override val ButtonCorner = 1.8f

    /**
     * The button's face darkens to this over the last quarter of its protrusion, where it tucks
     * under the enclosure — (38, 38, 38) against the rail's (44, 44, 44) in the capture.
     */
    override val ButtonShadowColor = Color(0xFF262626)

    /** How much of the visible protrusion that contact shadow covers. */
    override val ButtonShadowFraction = 0.28f

    override val ButtonRim = 0.6f
    override val ButtonRimTopAlpha = 0.5f
    override val ButtonRimBottomAlpha = 0.12f

    // ---- Dynamic Island -------------------------------------------------------------------

    override val IslandWidth = 125.45f
    override val IslandHeight = 36.8f

    /** Gap between the top of the display and the top of the Dynamic Island. */
    override val IslandTop = 13.94f

    // ---- Status bar -----------------------------------------------------------------------

    /**
     * Top safe-area inset — what a non-edge-to-edge screen has to leave clear so its own top bar
     * is not drawn under the status bar and the Dynamic Island.
     */
    override val SafeAreaTop = 62f

    /** Centre of the clock's digits, measured from the left edge of the display. */
    override val ClockCenterX = 73.67f

    /** Vertical centre of the clock's digits, measured from the top of the display. */
    override val ClockCenterY = 32.67f

    /** Cap height of the clock's digits. The frame sizes its font to match this, not the em size. */
    override val ClockCapHeight = 12.67f

    /** Vertical centre of the signal / Wi-Fi / battery cluster. */
    const val IconCenterY = 32.5f

    /** Left edge of the shortest cellular bar. */
    override val CellularX = 288.33f

    /** Baseline all four cellular bars sit on. */
    override val CellularBottom = 38.67f

    override val CellularBarWidth = 3.33f

    /** Left-edge to left-edge distance between neighbouring bars (a 2.0 pt gap). */
    override val CellularBarPitch = 5.33f

    override val CellularBarCorner = 1.0f

    /** Heights of the four bars, shortest first. */
    override val CellularBarHeights = listOf(4.67f, 7.0f, 9.67f, 12.33f)

    override val WifiX = 315f
    override val WifiTop = 26.33f
    override val WifiWidth = 17f

    /** Overlaps iOS's own glyph by 94% of their combined area. The iPad reuses this shape. */
    override val Wifi = WifiGlyph(
        Width = 17f,
        Height = 12.33f,
        Stroke = 2.5f,
        OuterRadius = 10.63f,
        InnerRadius = 6.38f,
        HalfSweep = 43f,
        DotHalfWidth = 2.6f,
        DotCenterY = 9.6f,
    )

    override val BatteryX = 339.33f
    override val BatteryTop = 26f
    override val BatteryWidth = 25f

    /**
     * The battery's proportions, as fractions of [BatteryWidth]. Unlike the Wi-Fi glyph, this one
     * does scale cleanly across the range: [drawBatteryGlyph] stretches these to the width it is
     * given, and on the Pro Max, where iOS draws a 28.33 pt battery, that lands within half a point
     * of the measured outline everywhere — 0.96 IoU against the capture, so there is nothing for a
     * per-device fit to buy.
     */
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
