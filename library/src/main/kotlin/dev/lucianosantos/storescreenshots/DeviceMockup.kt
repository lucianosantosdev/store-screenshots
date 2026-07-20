package dev.lucianosantos.storescreenshots

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import dev.lucianosantos.storescreenshots.frames.StatusBar
import dev.lucianosantos.storescreenshots.frames.StatusBarHeight

/**
 * Default perspective strength for 3D mockup rotation. It is the camera-to-plane distance in
 * density-independent units (multiplied by display density when applied): larger values flatten
 * the perspective toward orthographic, smaller values exaggerate the foreshortening. Tune it when
 * a steep [DeviceMockup] `rotationX`/`rotationY` looks too warped or too flat.
 */
const val DefaultMockupCameraDistance = 12f

/**
 * Applies a perspective 3D rotation (tilt) around the X, Y, and Z axes in a single
 * [graphicsLayer]. [rotationX] tips the device toward/away from the viewer, [rotationY] turns it
 * left/right, and [rotationZ] spins it in-plane (the same as `Modifier.rotate`). [cameraDistance]
 * controls how strong the perspective is — see [DefaultMockupCameraDistance].
 *
 * No-ops when all three angles are zero, so an untransformed mockup renders byte-for-byte the same
 * as before this modifier existed.
 */
internal fun Modifier.mockup3dRotation(
    rotationX: Float,
    rotationY: Float,
    rotationZ: Float,
    cameraDistance: Float = DefaultMockupCameraDistance,
): Modifier =
    if (rotationX == 0f && rotationY == 0f && rotationZ == 0f) {
        this
    } else {
        graphicsLayer {
            this.rotationX = rotationX
            this.rotationY = rotationY
            this.rotationZ = rotationZ
            this.cameraDistance = cameraDistance * density
        }
    }

/**
 * Orientation of a [DeviceMockup]. [Landscape] swaps the device's native width/height so its
 * [content] is laid out — and, crucially, *measured* — in landscape. Use it to capture a screen
 * whose UI only appears in landscape (e.g. a tablet two-/three-pane layout), which a portrait frame
 * can never trigger because the content would still measure taller-than-wide. The status bar/notch
 * stay along the top edge, so the frame reads as a device rotated a quarter turn.
 */
enum class MockupOrientation { Portrait, Landscape }

private fun orientSize(portraitWidth: Dp, portraitHeight: Dp, orientation: MockupOrientation): Pair<Dp, Dp> =
    if (orientation == MockupOrientation.Landscape) portraitHeight to portraitWidth else portraitWidth to portraitHeight

/**
 * Renders just the device bezel/frame for the given [formFactor] — no title, no description,
 * no background banner. Use inside a fully custom layout via `customScreenshot { … }`.
 *
 * The bezel keeps its device aspect ratio but takes its size from [modifier]: bound the width
 * (e.g. `Modifier.fillMaxWidth(0.6f)`) and the height follows, or bound the height
 * (e.g. `Modifier.fillMaxHeight(0.8f)`) and the width follows. The latter is what you want when
 * lining several devices up in a `Row` for a feature-graphic "device family" banner:
 *
 * ```kotlin
 * Row(verticalAlignment = Alignment.Bottom) {
 *     DeviceMockup(FormFactor.Tablet10, Modifier.fillMaxHeight(0.85f)) { HomeScreen() }
 *     DeviceMockup(FormFactor.Phone, Modifier.fillMaxHeight(0.7f)) { HomeScreen() }
 *     DeviceMockup(FormFactor.Wear, Modifier.fillMaxHeight(0.35f)) { WatchScreen() }
 * }
 * ```
 *
 * The whole mockup — bezel, status bar, notch, and [content] — is laid out at the device's
 * native size and then uniformly scaled to fit, so chrome and content keep real-device
 * proportions no matter how small you draw the device. [FormFactor.Wear] renders a round watch;
 * use [WatchMockup] directly to pick [WatchShape.Round] or [WatchShape.Square].
 *
 * [orientation] draws the device portrait (default) or landscape. [MockupOrientation.Landscape]
 * swaps the frame's native width/height, so [content] is laid out — and measured — in landscape;
 * use it to capture a screen whose UI only appears in landscape (e.g. a tablet two-/three-pane
 * layout), which a portrait frame can't trigger. Ignored by [FormFactor.Wear] (square).
 *
 * [rotationX], [rotationY], and [rotationZ] tilt the whole device in 3D for a perspective mockup
 * (degrees): X tips it toward/away from the viewer, Y turns it left/right, Z spins it in-plane.
 * [cameraDistance] controls the perspective strength — see [DefaultMockupCameraDistance].
 *
 * [FormFactor.GooglePlayFeatureGraphic] is a banner canvas rather than a device, so it has no
 * bezel — compose real devices onto it instead.
 */
@Composable
fun DeviceMockup(
    formFactor: FormFactor,
    modifier: Modifier = Modifier,
    orientation: MockupOrientation = MockupOrientation.Portrait,
    showStatusBar: Boolean = true,
    statusBarClock: String = "12:00",
    statusBarContentDark: Boolean = false,
    edgeToEdge: Boolean = true,
    rotationX: Float = 0f,
    rotationY: Float = 0f,
    rotationZ: Float = 0f,
    cameraDistance: Float = DefaultMockupCameraDistance,
    content: @Composable () -> Unit,
) {
    val rotated = modifier.mockup3dRotation(rotationX, rotationY, rotationZ, cameraDistance)
    when (formFactor) {
        FormFactor.Phone -> {
            val (w, h) = orientSize(411.dp, 822.dp, orientation)
            ScaledMockup(w, h, rotated) { PhoneBezel(Modifier.fillMaxSize(), showStatusBar, statusBarClock, statusBarContentDark, edgeToEdge) { ProvideDeviceConfiguration(w, h, content) } }
        }
        FormFactor.Wear ->
            WatchMockup(WatchShape.Round, rotated, content = content)
        FormFactor.Tablet7 -> {
            // Native size matches the form factor's own 16:10 qualifier (w600dp-h960dp) so the frame
            // and the content it measures reflect a real Android tablet, not a 4:3 iPad.
            val (w, h) = orientSize(600.dp, 960.dp, orientation)
            ScaledMockup(w, h, rotated) { TabletBezel(Modifier.fillMaxSize(), showStatusBar, statusBarClock, statusBarContentDark, edgeToEdge) { ProvideDeviceConfiguration(w, h, content) } }
        }
        FormFactor.Tablet10 -> {
            // 16:10 to match the w800dp-h1280dp qualifier (Pixel Tablet, Galaxy Tab, …).
            val (w, h) = orientSize(800.dp, 1280.dp, orientation)
            ScaledMockup(w, h, rotated) { TabletBezel(Modifier.fillMaxSize(), showStatusBar, statusBarClock, statusBarContentDark, edgeToEdge) { ProvideDeviceConfiguration(w, h, content) } }
        }
        FormFactor.AppleIPhone67 -> {
            val (w, h) = orientSize(430.dp, 932.dp, orientation)
            ScaledMockup(w, h, rotated) { AppleBezel(Modifier.fillMaxSize(), showStatusBar, statusBarClock, statusBarContentDark, edgeToEdge) { ProvideDeviceConfiguration(w, h, content) } }
        }
        FormFactor.AppleIPhone65 -> {
            val (w, h) = orientSize(428.dp, 926.dp, orientation)
            ScaledMockup(w, h, rotated) { AppleBezel(Modifier.fillMaxSize(), showStatusBar, statusBarClock, statusBarContentDark, edgeToEdge) { ProvideDeviceConfiguration(w, h, content) } }
        }
        FormFactor.GooglePlayFeatureGraphic -> error(
            "FormFactor.GooglePlayFeatureGraphic is a banner canvas, not a device. " +
                "Compose real devices with DeviceMockup(formFactor = FormFactor.Phone / Tablet10 / …) " +
                "inside customScreenshot { }."
        )
    }
}

/** Watch case shape for [WatchMockup]. */
enum class WatchShape { Round, Square }

/**
 * A standalone smartwatch mockup with a metallic case, side crown, and a strap above and below
 * the case. [Round] is a circular Wear OS-style case; [Square] uses Apple Watch proportions
 * (a 374x446 screen). Sized by [modifier] like [DeviceMockup] (bound width or height; the other
 * follows the watch's footprint) and scaled from a native size so [content] keeps its proportions.
 *
 * [rotationX], [rotationY], [rotationZ], and [cameraDistance] tilt the case in 3D exactly like
 * [DeviceMockup].
 */
@Composable
fun WatchMockup(
    shape: WatchShape = WatchShape.Round,
    modifier: Modifier = Modifier,
    rotationX: Float = 0f,
    rotationY: Float = 0f,
    rotationZ: Float = 0f,
    cameraDistance: Float = DefaultMockupCameraDistance,
    content: @Composable () -> Unit,
) {
    val spec = watchSpec(shape)
    val rotated = modifier.mockup3dRotation(rotationX, rotationY, rotationZ, cameraDistance)
    ScaledMockup(spec.nativeWidth, spec.nativeHeight, rotated) { WatchBezel(shape, content) }
}

/** Geometry for a [WatchMockup], derived per [WatchShape]. All values are native (pre-scale) dp. */
private class WatchSpec(
    val caseWidth: Dp,
    val caseHeight: Dp,
    val caseShape: Shape,
    val screenShape: Shape,
    /**
     * The real device's logical screen width in dp. The bezel's physical screen area (derived from
     * [caseWidth]) is larger than a real watch's dp resolution, so the screen content is rendered at
     * this reference width and scaled to fill — otherwise app content sized in `sp`/`dp` (built for a
     * real ~227dp watch) would appear far too small relative to the case. Keep it aligned with the
     * matching Play Store form factor (round Wear = 227dp) so mockups and standalone Wear screenshots
     * render content at the same proportions.
     */
    val screenReferenceWidth: Dp,
    /**
     * How far the strap tucks under the case. Round cases need a deeper tuck so the flared base
     * stays behind the part of the circle that is wide enough to hide it, rather than poking out
     * near the narrow top of the circle.
     */
    val bandOverlap: Dp,
) {
    val bandWidth: Dp get() = caseWidth * 0.6f
    val bandHeight: Dp get() = caseHeight * 0.4f
    val crownWidth: Dp get() = 16.dp
    val crownHeight: Dp get() = caseHeight * 0.16f

    /** Case is centered with a 22dp margin each side; the crown lives in the right margin. */
    val nativeWidth: Dp get() = caseWidth + 44.dp
    /** Bands extend above and below the case, overlapping it by [bandOverlap]. */
    val nativeHeight: Dp get() = caseHeight + (bandHeight - bandOverlap) * 2
}

private fun watchSpec(shape: WatchShape): WatchSpec = when (shape) {
    WatchShape.Round -> WatchSpec(
        caseWidth = 440.dp,
        caseHeight = 440.dp,
        caseShape = CircleShape,
        screenShape = CircleShape,
        // Matches FormFactor.Wear (w227dp), so a round mockup and a standalone Wear screenshot
        // render app content at identical proportions.
        screenReferenceWidth = 227.dp,
        bandOverlap = 72.dp,
    )
    // Apple Watch: 374x446 screen inside a squircle case (case = screen + 26dp rim each side).
    WatchShape.Square -> WatchSpec(
        caseWidth = 426.dp,
        caseHeight = 498.dp,
        caseShape = RoundedCornerShape(108.dp),
        screenShape = RoundedCornerShape(84.dp),
        // Real Apple Watch logical width (~198pt for the 45mm/Ultra screens).
        screenReferenceWidth = 198.dp,
        bandOverlap = 24.dp,
    )
}

/**
 * Makes [content] see a [Configuration] that describes the device this mockup renders — its screen
 * size in dp and its orientation — instead of inheriting the surrounding canvas's config. Without
 * it, content placed in a portrait phone mockup on a landscape banner (e.g. a feature graphic)
 * reads `orientation == LANDSCAPE` and picks a landscape layout. Orientation is derived from the
 * (already orientation-adjusted) [widthDp] / [heightDp] the mockup lays the device out at, so
 * `MockupOrientation.Landscape` content also reads landscape.
 */
@Composable
private fun ProvideDeviceConfiguration(
    widthDp: Dp,
    heightDp: Dp,
    content: @Composable () -> Unit,
) {
    val base = LocalConfiguration.current
    val deviceConfig = remember(base, widthDp, heightDp) {
        Configuration(base).apply {
            screenWidthDp = widthDp.value.roundToInt()
            screenHeightDp = heightDp.value.roundToInt()
            orientation = if (widthDp >= heightDp) {
                Configuration.ORIENTATION_LANDSCAPE
            } else {
                Configuration.ORIENTATION_PORTRAIT
            }
        }
    }
    CompositionLocalProvider(LocalConfiguration provides deviceConfig, content = content)
}

/**
 * Lays [bezel] out at its native ([nativeWidth] x [nativeHeight]) size, then uniformly scales it
 * to fill a footprint that keeps the same aspect ratio. The footprint is sized by [modifier], so
 * callers control how big the device appears while the bezel + content keep device proportions.
 *
 * The bezel is measured at its native size (so fixed `dp`/`sp` chrome and content stay in real
 * proportion) and drawn through a scaling placement layer — it is not laid out into the small
 * footprint directly, which would shrink the box but not the text inside it.
 */
@Composable
private fun ScaledMockup(
    nativeWidth: Dp,
    nativeHeight: Dp,
    modifier: Modifier,
    bezel: @Composable () -> Unit,
) {
    Layout(
        content = { bezel() },
        // matchHeightConstraintsFirst: size from the height when the caller bounds height
        // (Modifier.fillMaxHeight) — the common case for lining devices up in a Row — and fall
        // back to the width otherwise (e.g. Modifier.fillMaxWidth).
        modifier = modifier.aspectRatio(nativeWidth / nativeHeight, matchHeightConstraintsFirst = true),
    ) { measurables, constraints ->
        val nativeWpx = nativeWidth.roundToPx()
        val nativeHpx = nativeHeight.roundToPx()
        val placeable = measurables.first().measure(Constraints.fixed(nativeWpx, nativeHpx))
        // The aspectRatio modifier has already shaped the incoming constraints to the device
        // aspect ratio, so the footprint is whatever the caller's size resolves to.
        val targetW = if (constraints.hasBoundedWidth) constraints.maxWidth else nativeWpx
        val targetH = if (constraints.hasBoundedHeight) constraints.maxHeight else nativeHpx
        val scale = targetW.toFloat() / nativeWpx
        layout(targetW, targetH) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

@Composable
private fun PhoneBezel(
    modifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    statusBarContentDark: Boolean,
    edgeToEdge: Boolean,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        // Side buttons
        SideButton(Modifier.align(Alignment.TopStart).offset(x = (-3).dp, y = 110.dp), 38, true)
        SideButton(Modifier.align(Alignment.TopStart).offset(x = (-3).dp, y = 156.dp), 58, true)
        SideButton(Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = 92.dp), 70, false)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(42.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))))
                .padding(1.5.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color.Black)
                .padding(7.dp)
                .clip(RoundedCornerShape(32.dp))
        ) {
            Box(Modifier.fillMaxSize().padding(top = if (edgeToEdge) 0.dp else StatusBarHeight)) { content() }
            if (showStatusBar) StatusBar(
                clock,
                Modifier.align(Alignment.TopCenter),
                contentColor = if (statusBarContentDark) Color.Black else Color.White,
            )
            CameraNotch(Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun WatchBezel(shape: WatchShape, content: @Composable () -> Unit) {
    val spec = watchSpec(shape)
    val bandBrush = Brush.verticalGradient(listOf(Color(0xFF2C2C2C), Color(0xFF141414)))
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Straps above and below the case (drawn first so the case tucks over their ends).
        // Each strap has a rounded tip and flares with a curved lug shoulder into the case.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = spec.bandWidth, height = spec.bandHeight)
                .clip(bandShape(capAtTop = true))
                .background(bandBrush)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = spec.bandWidth, height = spec.bandHeight)
                .clip(bandShape(capAtTop = false))
                .background(bandBrush)
        )
        // Digital crown, tucked against the right edge of the case (case is drawn over its inner part).
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(width = spec.crownWidth, height = spec.crownHeight)
                .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp, topStart = 3.dp, bottomStart = 3.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF454545), Color(0xFF101010))))
        )
        // Watch case: metallic rim, black bezel, then the screen.
        Box(
            modifier = Modifier
                .size(width = spec.caseWidth, height = spec.caseHeight)
                .clip(spec.caseShape)
                .background(Brush.linearGradient(listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))))
                .padding(6.dp)
                .clip(spec.caseShape)
                .background(Color.Black)
                .padding(20.dp)
                .clip(spec.screenShape)
        ) {
            Box(Modifier.fillMaxSize().clip(spec.screenShape)) {
                WatchScreenContent(spec.screenReferenceWidth, content)
            }
        }
    }
}

/**
 * Renders watch [content] as if the screen were [referenceWidth] dp wide, scaling it to fill the
 * (larger) physical mockup screen. Real Wear/Apple Watch screens are ~200–227dp, but the mockup
 * case is drawn much bigger for visual weight; without this remap, app content sized in `sp`/`dp`
 * would render at a fraction of its real on-watch size. Overriding [LocalDensity] keeps every `dp`
 * and `sp` in proportion (unlike scaling only fonts) and matches the standalone Wear screenshot.
 */
@Composable
private fun WatchScreenContent(referenceWidth: Dp, content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val outer = LocalDensity.current
        val scaledDensity = constraints.maxWidth / referenceWidth.value
        CompositionLocalProvider(
            LocalDensity provides Density(scaledDensity, outer.fontScale),
            content = content,
        )
    }
}

/**
 * Watch strap silhouette: a squared-off outer tip (flat edge with small rounded corners) that runs
 * mostly straight, then flares through a curved lug shoulder to full width where it meets the case.
 * [capAtTop] puts the tip at the top for the upper strap, or at the bottom for the lower strap; the
 * flared case edge tucks behind the case (round cases tuck deeper so the wide base stays hidden).
 */
private fun bandShape(capAtTop: Boolean): Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val sideInset = w * 0.12f
    val cornerR = w * 0.16f
    if (capAtTop) {
        // Tip at top (y=0), case edge at bottom (y=h).
        moveTo(sideInset, cornerR)
        quadraticBezierTo(sideInset, 0f, sideInset + cornerR, 0f)
        lineTo(w - sideInset - cornerR, 0f)
        quadraticBezierTo(w - sideInset, 0f, w - sideInset, cornerR)
        quadraticBezierTo(w - sideInset, h * 0.72f, w, h)
        lineTo(0f, h)
        quadraticBezierTo(sideInset, h * 0.72f, sideInset, cornerR)
        close()
    } else {
        // Tip at bottom (y=h), case edge at top (y=0).
        moveTo(sideInset, h - cornerR)
        quadraticBezierTo(sideInset, h, sideInset + cornerR, h)
        lineTo(w - sideInset - cornerR, h)
        quadraticBezierTo(w - sideInset, h, w - sideInset, h - cornerR)
        quadraticBezierTo(w - sideInset, h * 0.28f, w, 0f)
        lineTo(0f, 0f)
        quadraticBezierTo(sideInset, h * 0.28f, sideInset, h - cornerR)
        close()
    }
}

@Composable
private fun TabletBezel(
    modifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    statusBarContentDark: Boolean,
    edgeToEdge: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))))
            .padding(2.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.Black)
            .padding(8.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(Modifier.fillMaxSize().padding(top = if (edgeToEdge) 0.dp else StatusBarHeight)) { content() }
        if (showStatusBar) StatusBar(
            clock,
            Modifier.align(Alignment.TopCenter),
            contentColor = if (statusBarContentDark) Color.Black else Color.White,
        )
    }
}

@Composable
private fun AppleBezel(
    modifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    statusBarContentDark: Boolean,
    edgeToEdge: Boolean,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(58.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color(0xFF0A0A0A))))
                .padding(2.dp)
                .clip(RoundedCornerShape(56.dp))
                .background(Color.Black)
                .padding(6.dp)
                .clip(RoundedCornerShape(50.dp))
        ) {
            Box(Modifier.fillMaxSize().padding(top = if (edgeToEdge) 0.dp else StatusBarHeight)) { content() }
            if (showStatusBar) StatusBar(
                clock,
                Modifier.align(Alignment.TopCenter),
                contentColor = if (statusBarContentDark) Color.Black else Color.White,
            )
            DynamicIsland(Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun SideButton(modifier: Modifier, heightDp: Int, isLeft: Boolean) {
    val shape = if (isLeft) RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)
    else RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
    Box(
        modifier = modifier
            .size(width = 5.dp, height = heightDp.dp)
            .background(
                Brush.horizontalGradient(
                    if (isLeft) listOf(Color(0xFF0F0F0F), Color(0xFF2E2E2E))
                    else listOf(Color(0xFF2E2E2E), Color(0xFF0F0F0F))
                ),
                shape
            )
    )
}

@Composable
private fun CameraNotch(modifier: Modifier) {
    Row(
        modifier = modifier
            .padding(top = 7.dp)
            .size(width = 90.dp, height = 24.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
            .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(50))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(50))
                .background(Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF2E2E2E))))
                .border(0.5.dp, Color(0xFF333333), RoundedCornerShape(50))
        )
        Box(
            Modifier.size(12.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF1F2A40), Color(0xFF0A0F1A), Color.Black)))
                .border(0.5.dp, Color(0xFF2A2A2A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFF0D1422), Color.Black))))
            Box(Modifier.align(Alignment.TopStart).padding(start = 2.dp, top = 2.dp).size(width = 3.dp, height = 2.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)))
        }
    }
}

@Composable
private fun DynamicIsland(modifier: Modifier) {
    Box(
        modifier = modifier
            .padding(top = 12.dp)
            .size(width = 124.dp, height = 36.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
            .border(0.5.dp, Color(0xFF222222), RoundedCornerShape(50))
    )
}
