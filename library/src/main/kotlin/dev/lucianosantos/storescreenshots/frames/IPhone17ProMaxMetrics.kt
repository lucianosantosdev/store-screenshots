package dev.lucianosantos.storescreenshots.frames

import androidx.compose.ui.graphics.Color

/**
 * Geometry of an iPhone 17 Pro Max, in the device's own points.
 *
 * This is the device the Apple App Store slots are drawn as. Both iPhone slots are Pro Max-class
 * sizes — 428x926 pt for 6.5" and 430x932 pt for 6.7" — and App Store Review expects the screenshots
 * filling them to depict a current iPhone. A frame modelled on a 6.3" iPhone 17, and especially one
 * carrying the notch of a phone Apple stopped selling in 2022, is what gets a submission dismissed
 * under guideline 2.3.10.
 *
 * Unlike [IPhone17Metrics], which was read off screenshots, the enclosure figures below come from
 * the Simulator's own *vector* artwork, so they are exact rather than measured:
 *
 * ```
 * # Which chrome the device uses, and its screen in pixels:
 * plutil -p "/Library/Developer/CoreSimulator/Profiles/DeviceTypes/iPhone 17 Pro Max.simdevicetype/Contents/Resources/profile.plist"
 * #   chromeIdentifier => com.apple.dt.devicekit.chrome.phone12
 * #   mainScreenWidth => 1320, mainScreenHeight => 2868, mainScreenScale => 3
 *
 * # The artwork and its layout, one PDF point per device point:
 * ls  /Library/Developer/DeviceKit/Chrome/phone12.devicechrome/Contents/Resources/
 * cat /Library/Developer/DeviceKit/Chrome/phone12.devicechrome/Contents/Resources/chrome.json
 * ```
 *
 * `PhoneComposite.pdf` rasterises to a body of exactly 470 x 986 pt around a 440 x 956 pt screen —
 * a uniform 15 pt bezel — with the rail and rim colours below readable straight out of its pixels.
 * `chrome.json` gives each side button's offset and the button PDFs give their sizes. Running the
 * same extraction against `phone11`, the chrome the plain iPhone 17 uses, reproduces
 * [IPhone17Metrics]'s screenshot-measured figures to within half a point, which is what makes the
 * two sets comparable.
 *
 * [SafeAreaTop] and [ScreenCorner] are not in the artwork; they came from the running device, via a
 * throwaway app reporting `view.safeAreaInsets` and `UIScreen.main._displayCornerRadius`.
 *
 * The status bar and Dynamic Island figures were measured off the framebuffer at @3x, the same way
 * [IPhone17Metrics]'s were, and are guarded by `IPhone17ProMaxStatusBarComparisonTest`. The
 * enclosure is guarded by `IPhone17ProMaxBezelComparisonTest`. Re-measure rather than guess if you
 * change either set.
 *
 * ```
 * xcrun simctl boot "iPhone 17 Pro Max"
 * xcrun simctl ui booted appearance dark
 * xcrun simctl status_bar booted override --time 9:41 \
 *     --dataNetwork wifi --wifiMode active --wifiBars 3 \
 *     --cellularMode active --cellularBars 4 \
 *     --batteryState discharging --batteryLevel 100
 * xcrun simctl io booted screenshot screen.png    # top 1320x210 strip is the committed capture
 * ```
 */
internal object IPhone17ProMaxMetrics : IPhoneMetrics {

    // ---- Screen and body ------------------------------------------------------------------

    /** Logical screen width in points (1320 px @3x). */
    override val ScreenWidth = 440f

    /** Logical screen height in points (2868 px @3x). */
    override val ScreenHeight = 956f

    /**
     * Uniform on all four sides, unlike the iPhone 17's: the artwork's body is exactly 30 pt wider
     * and 30 pt taller than the screen it wraps.
     */
    override val Bezel = 15f

    /** Outer width of the titanium body — 470.000 pt in the artwork. */
    override val BodyWidth = ScreenWidth + 2 * Bezel

    /** Outer height of the body — 986.000 pt in the artwork. */
    override val BodyHeight = ScreenHeight + 2 * Bezel

    /**
     * Corner radius of the body, from a least-squares circle fitted to the artwork's corner. iOS
     * corners are continuous ("squircle") curves, so the fit carries about half a point of residual;
     * that is the same order as the difference between this and the 77 pt a corner concentric with
     * the display would have.
     */
    override val BodyCorner = 78.5f

    /** Corner radius of the display, as the device itself reports it. */
    override val ScreenCorner = 62f

    /** Width of the machined rail, from where the rail's grey gives way to black in the artwork. */
    override val Rail = 6.33f

    /** The highlight line along the very outer edge of the rail. */
    override val Rim = 1f

    /** The rail's face — (44, 44, 44) in the artwork. */
    override val RailColor = Color(0xFF2C2C2C)

    /** The highlight along the outer edge — (126, 126, 126). */
    override val RimColor = Color(0xFF7E7E7E)

    /** The black bezel between the rail and the display. */
    override val BezelColor = Color(0xFF000000)

    // ---- Side buttons ---------------------------------------------------------------------

    /**
     * Both sides protrude equally, measured at 3.98 pt off a window capture. The iPhone 17's
     * figures come out lopsided (4.5 left, 3.9 right) only because that capture was taken at a
     * zoom where the Simulator rounded each side to a different whole pixel.
     */
    override val LeftButtonProtrusion = 4f
    override val RightButtonProtrusion = 4f

    /**
     * Action button, then volume up, then volume down: (top edge from the body's top, height).
     * Tops are `chrome.json`'s button offsets; each height is its PDF's, less the 1 pt of soft
     * shadow the artwork carries on every edge.
     */
    override val LeftButtons = listOf(180f to 32f, 268f to 62f, 354f to 62f)

    /** Side (power) button: (top edge from the body's top, height). */
    override val RightButtons = listOf(293f to 99f)

    /** Rounding on the outer end of a side button, fitted to the button artwork's corner. */
    override val ButtonCorner = 1f

    /** The button's face darkens to this where it tucks under the enclosure. */
    override val ButtonShadowColor = Color(0xFF262626)

    /** How much of the visible protrusion that contact shadow covers. */
    override val ButtonShadowFraction = 0.28f

    override val ButtonRim = 0.6f
    override val ButtonRimTopAlpha = 0.5f
    override val ButtonRimBottomAlpha = 0.12f

    // ---- Dynamic Island -------------------------------------------------------------------

    /** The Island is one physical size across the range, so this matches the iPhone 17's to a third of a point. */
    override val IslandWidth = 125.33f
    override val IslandHeight = 36.67f

    /** Gap between the top of the display and the top of the Dynamic Island. */
    override val IslandTop = 14f

    // ---- Status bar -----------------------------------------------------------------------

    /** Top safe-area inset, as the running device reports it. */
    override val SafeAreaTop = 62f

    /** Centre of the clock's digits, measured from the left edge of the display. */
    override val ClockCenterX = 83f

    /** Vertical centre of the clock's digits, measured from the top of the display. */
    override val ClockCenterY = 32.67f

    /**
     * Cap height of the clock's digits. iOS sets the Pro Max status bar a little larger than the
     * iPhone 17's 12.67 pt rather than simply scaling it with the screen, which is why every figure
     * in this block was re-measured instead of derived.
     */
    override val ClockCapHeight = 13.33f

    /** Left edge of the shortest cellular bar. */
    override val CellularX = 312.33f

    /** Baseline all four cellular bars sit on. */
    override val CellularBottom = 39f

    override val CellularBarWidth = 3.67f

    /** Left-edge to left-edge distance between neighbouring bars (a 2.33 pt gap). */
    override val CellularBarPitch = 6f

    override val CellularBarCorner = 1f

    /** Heights of the four bars, shortest first. */
    override val CellularBarHeights = listOf(5.33f, 7.67f, 10.67f, 13.67f)

    override val WifiX = 341.66f
    override val WifiTop = 25.27f
    override val WifiWidth = 19f

    /**
     * Fitted against the committed capture in its own right rather than scaled from the iPhone 17's:
     * iOS draws this glyph a little squarer here — a shallower sweep over a longer radius — and the
     * scaled-up shape only reaches 0.85 IoU where this reaches 0.96.
     */
    override val Wifi = WifiGlyph(
        Width = 19f,
        Height = 14f,
        Stroke = 2.65f,
        OuterRadius = 11.75f,
        InnerRadius = 7.08f,
        HalfSweep = 40.88f,
        DotHalfWidth = 2.89f,
        DotCenterY = 10.68f,
    )

    override val BatteryX = 368.33f
    override val BatteryTop = 25f
    override val BatteryWidth = 28.33f
}
