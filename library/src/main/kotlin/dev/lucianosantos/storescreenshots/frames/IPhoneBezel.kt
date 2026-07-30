package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.frames.IPhone17Metrics as M

/**
 * The screen cutout an iPhone frame draws.
 */
enum class AppleNotchStyle {
    /** Pill-shaped Dynamic Island — iPhone 14 Pro and later. */
    DynamicIsland,

    /** Wide top notch — iPhone X through 14 Plus, which is what the 6.5" slot depicts. */
    Notch,
}

/**
 * An iPhone body: machined rail, black bezel, rounded display, side buttons, and the screen cutout.
 *
 * [modifier] sizes the *body*, and everything inside is a scale model of the measured iPhone 17
 * (see [IPhone17Metrics]) — the bezel, both corner radii, the Dynamic Island, the side buttons and
 * the status bar are all a fixed fraction of the body's width. Draw the frame at any size and it
 * stays a believable iPhone instead of a rounded rectangle with fixed-dp chrome bolted on.
 *
 * The side buttons stand proud of [modifier]'s bounds by [IPhone17Metrics.LeftButtonProtrusion] /
 * [IPhone17Metrics.RightButtonProtrusion] scaled points, the way they do on the device; leave a
 * little horizontal room around the frame for them.
 */
@Composable
internal fun IPhoneBezel(
    modifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    statusBarContentDark: Boolean,
    edgeToEdge: Boolean,
    notch: AppleNotchStyle,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier) {
        // One measured iPhone 17 point, in this frame's dp.
        val u = maxWidth.value / M.BodyWidth
        val screenWidth = (M.ScreenWidth * u).dp
        val statusBarColor = if (statusBarContentDark) Color.Black else Color.White
        val bodyShape = RoundedCornerShape((M.BodyCorner * u).dp)

        SideButtons(u, elevation)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .mockupShadow(elevation, bodyShape)
                .clip(bodyShape)
                .background(M.RimColor)
                .padding((M.Rim * u).dp)
                .clip(RoundedCornerShape(((M.BodyCorner - M.Rim) * u).dp))
                .background(M.RailColor)
                .padding(((M.Rail - M.Rim) * u).dp)
                .clip(RoundedCornerShape(((M.BodyCorner - M.Rail) * u).dp))
                .background(M.BezelColor)
                .padding(((M.Bezel - M.Rail) * u).dp)
                .clip(RoundedCornerShape((M.ScreenCorner * u).dp))
        ) {
            // Non-edge-to-edge: reserve the top safe area so a standalone screen's own top bar is
            // not drawn under the status bar and the cutout.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (edgeToEdge) 0.dp else (M.SafeAreaTop * u).dp)
            ) { content() }

            if (showStatusBar) {
                IosStatusBar(
                    clock = clock,
                    screenWidth = screenWidth,
                    modifier = Modifier.align(Alignment.TopStart),
                    contentColor = statusBarColor,
                )
            }
            when (notch) {
                AppleNotchStyle.DynamicIsland -> DynamicIsland(u, Modifier.align(Alignment.TopCenter))
                AppleNotchStyle.Notch -> Notch(u, Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

/**
 * Action button and the two volume buttons on the left, side button on the right. Drawn before the
 * body so the enclosure covers the half of each button that sits inside it, leaving only the
 * protrusion showing — the same sliver the Simulator leaves.
 */
@Composable
private fun BoxScope.SideButtons(u: Float, elevation: Dp) {
    M.LeftButtons.forEach { (top, height) ->
        SideButton(u, top, height, M.LeftButtonProtrusion, isLeft = true, elevation = elevation, scope = this)
    }
    M.RightButtons.forEach { (top, height) ->
        SideButton(u, top, height, M.RightButtonProtrusion, isLeft = false, elevation = elevation, scope = this)
    }
}

/**
 * One machined button. Across its width it goes from the rail's own grey to
 * [IPhone17Metrics.ButtonShadowColor] where it meets the enclosure, which is the contact shadow the
 * capture shows. A hairline runs around its outline on top of that, so the light catches the
 * button's milled edge the way it does on the device — the face itself stays flat.
 */
@Composable
private fun SideButton(
    u: Float,
    top: Float,
    height: Float,
    protrusion: Float,
    isLeft: Boolean,
    elevation: Dp,
    scope: BoxScope,
) = with(scope) {
    val corner = (M.ButtonCorner * u).dp
    // Extended past the enclosure edge by the corner radius so the rounding only ever shows on the
    // outer end; the inner end is hidden under the body.
    val width = ((protrusion + M.ButtonCorner * 2) * u).dp
    val shape = if (isLeft) {
        RoundedCornerShape(topStart = corner, bottomStart = corner)
    } else {
        RoundedCornerShape(topEnd = corner, bottomEnd = corner)
    }
    // The face runs flat until the last stretch, where it drops into shadow against the enclosure.
    val faceStops = arrayOf(
        0f to M.RailColor,
        (1f - M.ButtonShadowFraction) to M.RailColor,
        1f to M.ButtonShadowColor,
    )
    val face = Brush.horizontalGradient(
        colorStops = if (isLeft) faceStops else faceStops.reversedStops(),
    )
    // Brightest along the top cap, falling away down the button, as though lit from above.
    val rim = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = M.ButtonRimTopAlpha),
            Color.White.copy(alpha = M.ButtonRimBottomAlpha),
        ),
    )
    Box(
        modifier = Modifier
            .align(if (isLeft) Alignment.TopStart else Alignment.TopEnd)
            .offset(x = if (isLeft) -(protrusion * u).dp else (protrusion * u).dp, y = (top * u).dp)
            .size(width = width, height = (height * u).dp)
            .mockupShadow(elevation, shape)
            .clip(shape)
            .background(face)
            .border(width = (M.ButtonRim * u).dp, brush = rim, shape = shape)
    )
}

/** Mirrors colour stops so one gradient definition serves both sides of the device. */
private fun Array<Pair<Float, Color>>.reversedStops(): Array<Pair<Float, Color>> =
    Array(size) { i -> (1f - this[size - 1 - i].first) to this[size - 1 - i].second }

/** The pill cut out of the top of the display on iPhone 14 Pro and later. */
@Composable
private fun DynamicIsland(u: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = (M.IslandTop * u).dp)
            .size(width = (M.IslandWidth * u).dp, height = (M.IslandHeight * u).dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
    )
}

/**
 * The pre-14-Pro notch: flush with the top edge, so only its bottom corners are rounded. Wider and
 * shorter than the Dynamic Island, and not inset from the top. Sized from the same scale factor so
 * a 6.5" frame keeps the proportions of the device it depicts.
 */
@Composable
private fun Notch(u: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = (NotchWidth * u).dp, height = (NotchHeight * u).dp)
            .clip(RoundedCornerShape(bottomStart = (NotchCorner * u).dp, bottomEnd = (NotchCorner * u).dp))
            .background(Color.Black)
    )
}

/** Notch geometry in the same reference points as [IPhone17Metrics] (iPhone 14 Plus proportions). */
private const val NotchWidth = 160f
private const val NotchHeight = 33f
private const val NotchCorner = 20f
