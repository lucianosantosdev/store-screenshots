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
import dev.lucianosantos.storescreenshots.frames.IPadAir13Metrics as M

/**
 * An iPad body: machined rail, black bezel, squared-off display, volume buttons on the right edge
 * and the power button along the top.
 *
 * [modifier] sizes the *body*, and everything inside is a scale model of the measured 13-inch iPad
 * Air (see [IPadAir13Metrics]) — bezel, both corner radii, the buttons, and the status bar are all a
 * fixed fraction of the body's width, so the tablet stays in proportion at any size.
 *
 * This replaces the generic Android tablet bezel for the App Store's iPad slot. The difference that
 * matters is the status bar: [IPadOsStatusBar] draws iPadOS's, not Material's, which App Store
 * Review guideline 2.3.10 rejects.
 */
@Composable
internal fun IPadBezel(
    modifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    statusBarContentDark: Boolean,
    edgeToEdge: Boolean,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier) {
        // One measured iPad point, in this frame's dp.
        val u = maxWidth.value / M.BodyWidth
        val screenWidth = (M.ScreenWidth * u).dp
        val bodyShape = RoundedCornerShape((M.BodyCorner * u).dp)

        Buttons(u, elevation)

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
            // not drawn under the status bar.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (edgeToEdge) 0.dp else (M.SafeAreaTop * u).dp)
            ) { content() }

            if (showStatusBar) {
                IPadOsStatusBar(
                    clock = clock,
                    screenWidth = screenWidth,
                    modifier = Modifier.align(Alignment.TopStart),
                    contentColor = if (statusBarContentDark) Color.Black else Color.White,
                )
            }
        }
    }
}

/**
 * Volume up and down on the right edge, power along the top. Drawn before the body so the enclosure
 * covers the part of each that sits inside it, leaving only the protrusion showing.
 */
@Composable
private fun BoxScope.Buttons(u: Float, elevation: Dp) {
    M.RightButtons.forEach { (top, height) ->
        EdgeButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (M.RightButtonProtrusion * u).dp, y = (top * u).dp)
                .size(
                    width = ((M.RightButtonProtrusion + M.ButtonCorner * 2) * u).dp,
                    height = (height * u).dp,
                ),
            shape = RoundedCornerShape(
                topEnd = (M.ButtonCorner * u).dp,
                bottomEnd = (M.ButtonCorner * u).dp,
            ),
            // On the right edge the enclosure is to the left, so the contact shadow leads.
            face = Brush.horizontalGradient(
                0f to M.ButtonShadowColor,
                M.ButtonShadowFraction to M.RailColor,
                1f to M.RailColor,
            ),
            rim = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = M.ButtonRimTopAlpha),
                    Color.White.copy(alpha = M.ButtonRimBottomAlpha),
                ),
            ),
            u = u,
            elevation = elevation,
        )
    }
    val (topLeft, topWidth) = M.TopButton
    EdgeButton(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = (topLeft * u).dp, y = -(M.TopButtonProtrusion * u).dp)
            .size(
                width = (topWidth * u).dp,
                height = ((M.TopButtonProtrusion + M.ButtonCorner * 2) * u).dp,
            ),
        shape = RoundedCornerShape(
            topStart = (M.ButtonCorner * u).dp,
            topEnd = (M.ButtonCorner * u).dp,
        ),
        // Runs along the top edge, so its face shades vertically rather than across.
        face = Brush.verticalGradient(
            0f to M.RailColor,
            (1f - M.ButtonShadowFraction) to M.RailColor,
            1f to M.ButtonShadowColor,
        ),
        rim = Brush.horizontalGradient(
            listOf(
                Color.White.copy(alpha = M.ButtonRimTopAlpha),
                Color.White.copy(alpha = M.ButtonRimBottomAlpha),
            ),
        ),
        u = u,
        elevation = elevation,
    )
}

/**
 * One machined button: the rail's own grey, dropping into shadow where it meets the enclosure, with
 * a hairline around the outline so the light catches its milled edge.
 */
@Composable
private fun EdgeButton(
    modifier: Modifier,
    shape: RoundedCornerShape,
    face: Brush,
    rim: Brush,
    u: Float,
    elevation: Dp,
) {
    Box(
        modifier = modifier
            .mockupShadow(elevation, shape)
            .clip(shape)
            .background(face)
            .border(width = (M.ButtonRim * u).dp, brush = rim, shape = shape)
    )
}
