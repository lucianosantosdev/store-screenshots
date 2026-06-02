package dev.lucianosantos.storescreenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.frames.StatusBar

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
 * [FormFactor.GooglePlayFeatureGraphic] is a banner canvas rather than a device, so it has no
 * bezel — compose real devices onto it instead.
 */
@Composable
fun DeviceMockup(
    formFactor: FormFactor,
    modifier: Modifier = Modifier,
    showStatusBar: Boolean = true,
    statusBarClock: String = "12:00",
    content: @Composable () -> Unit,
) {
    when (formFactor) {
        FormFactor.Phone ->
            ScaledMockup(411.dp, 822.dp, modifier) { PhoneBezel(Modifier.fillMaxSize(), showStatusBar, statusBarClock, content) }
        FormFactor.Wear ->
            WatchMockup(WatchShape.Round, modifier, content)
        FormFactor.Tablet7 ->
            ScaledMockup(600.dp, 800.dp, modifier) { TabletBezel(Modifier.fillMaxSize(), showStatusBar, statusBarClock, content) }
        FormFactor.Tablet10 ->
            ScaledMockup(800.dp, 1067.dp, modifier) { TabletBezel(Modifier.fillMaxSize(), showStatusBar, statusBarClock, content) }
        FormFactor.AppleIPhone67 ->
            ScaledMockup(430.dp, 932.dp, modifier) { AppleBezel(Modifier.fillMaxSize(), showStatusBar, statusBarClock, content) }
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
 */
@Composable
fun WatchMockup(
    shape: WatchShape = WatchShape.Round,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val spec = watchSpec(shape)
    ScaledMockup(spec.nativeWidth, spec.nativeHeight, modifier) { WatchBezel(shape, content) }
}

/** Geometry for a [WatchMockup], derived per [WatchShape]. All values are native (pre-scale) dp. */
private class WatchSpec(
    val caseWidth: Dp,
    val caseHeight: Dp,
    val caseShape: Shape,
    val screenShape: Shape,
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
        bandOverlap = 72.dp,
    )
    // Apple Watch: 374x446 screen inside a squircle case (case = screen + 26dp rim each side).
    WatchShape.Square -> WatchSpec(
        caseWidth = 426.dp,
        caseHeight = 498.dp,
        caseShape = RoundedCornerShape(108.dp),
        screenShape = RoundedCornerShape(84.dp),
        bandOverlap = 24.dp,
    )
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
            Box(Modifier.fillMaxSize()) { content() }
            if (showStatusBar) StatusBar(clock, Modifier.align(Alignment.TopCenter))
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
            Box(Modifier.fillMaxSize().clip(spec.screenShape)) { content() }
        }
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
        Box(Modifier.fillMaxSize()) { content() }
        if (showStatusBar) StatusBar(clock, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun AppleBezel(
    modifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
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
            Box(Modifier.fillMaxSize()) { content() }
            if (showStatusBar) StatusBar(clock, Modifier.align(Alignment.TopCenter))
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
