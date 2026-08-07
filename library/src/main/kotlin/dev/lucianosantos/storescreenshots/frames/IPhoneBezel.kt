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

/**
 * The iPhone an Apple frame is drawn as.
 *
 * Both App Store iPhone slots default to [IPhone17ProMax]. That is not a style choice: guideline
 * 2.3.10 has screenshots dismissed for status bar and hardware imagery that is not current iOS, and
 * both slots are Pro Max-class sizes (428x926 pt and 430x932 pt) that a reviewer expects to see a
 * current Pro Max in. [IPhone17] is here for composing a frame deliberately at the smaller 6.3"
 * proportions.
 */
enum class AppleIPhoneModel(internal val metrics: IPhoneMetrics) {
    /** iPhone 17 Pro Max — 440x956 pt, the device the App Store slots depict. */
    IPhone17ProMax(IPhone17ProMaxMetrics),

    /** iPhone 17 — 402x874 pt. */
    IPhone17(IPhone17Metrics);

    /** Width-to-height ratio of this device's enclosure. */
    val aspectRatio: Float get() = metrics.BodyWidth / metrics.BodyHeight
}

/**
 * An iPhone body: machined rail, black bezel, rounded display, side buttons, and the Dynamic Island.
 *
 * [modifier] sizes the *body*, and everything inside is a scale model of [metrics] — the bezel, both
 * corner radii, the Island, the side buttons and the status bar are all a fixed fraction of the
 * body's width. Draw the frame at any size and it stays a believable iPhone instead of a rounded
 * rectangle with fixed-dp chrome bolted on.
 *
 * The side buttons stand proud of [modifier]'s bounds by [IPhoneMetrics.LeftButtonProtrusion] /
 * [IPhoneMetrics.RightButtonProtrusion] scaled points, the way they do on the device; leave a
 * little horizontal room around the frame for them.
 */
@Composable
internal fun IPhoneBezel(
    modifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    statusBarContentDark: Boolean,
    edgeToEdge: Boolean,
    metrics: IPhoneMetrics,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier) {
        // One measured device point, in this frame's dp.
        val u = maxWidth.value / metrics.BodyWidth
        val screenWidth = (metrics.ScreenWidth * u).dp
        val statusBarColor = if (statusBarContentDark) Color.Black else Color.White
        val bodyShape = RoundedCornerShape((metrics.BodyCorner * u).dp)

        SideButtons(metrics, u, elevation)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .mockupShadow(elevation, bodyShape)
                .clip(bodyShape)
                .background(metrics.RimColor)
                .padding((metrics.Rim * u).dp)
                .clip(RoundedCornerShape(((metrics.BodyCorner - metrics.Rim) * u).dp))
                .background(metrics.RailColor)
                .padding(((metrics.Rail - metrics.Rim) * u).dp)
                .clip(RoundedCornerShape(((metrics.BodyCorner - metrics.Rail) * u).dp))
                .background(metrics.BezelColor)
                .padding(((metrics.Bezel - metrics.Rail) * u).dp)
                .clip(RoundedCornerShape((metrics.ScreenCorner * u).dp))
        ) {
            // Non-edge-to-edge: reserve the top safe area so a standalone screen's own top bar is
            // not drawn under the status bar and the Island.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (edgeToEdge) 0.dp else (metrics.SafeAreaTop * u).dp)
            ) { content() }

            if (showStatusBar) {
                IosStatusBar(
                    clock = clock,
                    screenWidth = screenWidth,
                    metrics = metrics,
                    modifier = Modifier.align(Alignment.TopStart),
                    contentColor = statusBarColor,
                )
            }
            DynamicIsland(metrics, u, Modifier.align(Alignment.TopCenter))
        }
    }
}

/**
 * Action button and the two volume buttons on the left, side button on the right. Drawn before the
 * body so the enclosure covers the half of each button that sits inside it, leaving only the
 * protrusion showing — the same sliver the Simulator leaves.
 */
@Composable
private fun BoxScope.SideButtons(m: IPhoneMetrics, u: Float, elevation: Dp) {
    m.LeftButtons.forEach { (top, height) ->
        SideButton(m, u, top, height, m.LeftButtonProtrusion, isLeft = true, elevation = elevation, scope = this)
    }
    m.RightButtons.forEach { (top, height) ->
        SideButton(m, u, top, height, m.RightButtonProtrusion, isLeft = false, elevation = elevation, scope = this)
    }
}

/**
 * One machined button. Across its width it goes from the rail's own grey to
 * [IPhoneMetrics.ButtonShadowColor] where it meets the enclosure, which is the contact shadow the
 * capture shows. A hairline runs around its outline on top of that, so the light catches the
 * button's milled edge the way it does on the device — the face itself stays flat.
 */
@Composable
private fun SideButton(
    m: IPhoneMetrics,
    u: Float,
    top: Float,
    height: Float,
    protrusion: Float,
    isLeft: Boolean,
    elevation: Dp,
    scope: BoxScope,
) = with(scope) {
    val corner = (m.ButtonCorner * u).dp
    // Extended past the enclosure edge by the corner radius so the rounding only ever shows on the
    // outer end; the inner end is hidden under the body.
    val width = ((protrusion + m.ButtonCorner * 2) * u).dp
    val shape = if (isLeft) {
        RoundedCornerShape(topStart = corner, bottomStart = corner)
    } else {
        RoundedCornerShape(topEnd = corner, bottomEnd = corner)
    }
    // The face runs flat until the last stretch, where it drops into shadow against the enclosure.
    val faceStops = arrayOf(
        0f to m.RailColor,
        (1f - m.ButtonShadowFraction) to m.RailColor,
        1f to m.ButtonShadowColor,
    )
    val face = Brush.horizontalGradient(
        colorStops = if (isLeft) faceStops else faceStops.reversedStops(),
    )
    // Brightest along the top cap, falling away down the button, as though lit from above.
    val rim = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = m.ButtonRimTopAlpha),
            Color.White.copy(alpha = m.ButtonRimBottomAlpha),
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
            .border(width = (m.ButtonRim * u).dp, brush = rim, shape = shape)
    )
}

/** Mirrors colour stops so one gradient definition serves both sides of the device. */
private fun Array<Pair<Float, Color>>.reversedStops(): Array<Pair<Float, Color>> =
    Array(size) { i -> (1f - this[size - 1 - i].first) to this[size - 1 - i].second }

/** The pill cut out of the top of the display. */
@Composable
private fun DynamicIsland(m: IPhoneMetrics, u: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = (m.IslandTop * u).dp)
            .size(width = (m.IslandWidth * u).dp, height = (m.IslandHeight * u).dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
    )
}
