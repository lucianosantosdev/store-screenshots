package dev.lucianosantos.storescreenshots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Which edge or corner the glass [screenGlass] shadow darkens toward. The gradient is fully
 * transparent across the rest of the screen and ramps to the shadow color at the named side, so
 * it reads as ambient light falling off into a corner rather than a flat tint. [None] disables the
 * shadow entirely.
 */
enum class GlassShadow {
    None,
    Top,
    Bottom,
    Left,
    Right,
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight,
}

/**
 * Shape of the [screenGlass] reflex.
 *
 * - [Band] — a symmetric streak of light: transparent, brightening to a peak at the reflex
 *   position, then fading back to transparent. The default "reflected window" look.
 * - [Wedge] — a half-screen sheen: transparent on the near side of the reflex line and brightening
 *   to a solid fill that runs out to the screen border on the far side. Use it for a glossy
 *   diagonal that starts near the center and sweeps off the edge, like cover glass catching a
 *   single broad light.
 */
enum class GlassReflexStyle { Band, Wedge }

/**
 * Configuration for the glass sheen drawn by [screenGlass]. Hold one of these to reuse a look
 * across screenshots, or to set it on [ScreenshotStyle.screenGlass] so every mockup gets the same
 * glass automatically. The two layers — the diagonal reflex line and the corner shadow gradient —
 * are described on [screenGlass]; the defaults here are the same "default glass" look.
 */
@Immutable
data class GlassEffect(
    val reflexAngle: Float = 25f,
    val reflexPosition: Float = 0.35f,
    val reflexWidth: Float = 0.16f,
    val reflexStyle: GlassReflexStyle = GlassReflexStyle.Band,
    val reflexColor: Color = Color.White,
    val reflexAlpha: Float = 0.18f,
    val shadow: GlassShadow = GlassShadow.BottomRight,
    val shadowColor: Color = Color.Black,
    val shadowAlpha: Float = 0.22f,
)

/**
 * Draws a default glass sheen in front of the content this modifier decorates — drop it on a
 * screen composable inside a [DeviceMockup] to make the screen look like it sits behind glass:
 *
 * ```kotlin
 * DeviceMockup(FormFactor.Phone) {
 *     HomeScreen(Modifier.screenGlass())
 * }
 * ```
 *
 * The effect has two independently controllable layers, both painted on top of the content:
 *
 * **Reflex line** — a soft diagonal band of light, like a window reflected on the glass.
 * [reflexAngle] sets its direction in degrees (0 = horizontal, positive tilts the line clockwise);
 * [reflexPosition] slides it across the screen perpendicular to that direction (0 = one corner,
 * 1 = the opposite); [reflexWidth] is the band thickness as a fraction of the screen; and
 * [reflexColor]/[reflexAlpha] set its tint and peak strength. Set [reflexAlpha] to 0 to drop the
 * reflex and keep only the shadow. [reflexStyle] switches between a symmetric [GlassReflexStyle.Band]
 * streak and a [GlassReflexStyle.Wedge] that fills from the reflex line out to the border — for a
 * wedge, [reflexWidth] is the softness of that diagonal edge.
 *
 * **Shadow gradient** — an ambient darkening that falls off toward one [shadow] edge or corner,
 * tinted [shadowColor] at [shadowAlpha] strength. Use [GlassShadow.None] to drop it and keep only
 * the reflex.
 *
 * The overlay fills the modified bounds and is clipped by whatever clips the content (the device
 * bezel already rounds the screen corners), so the glass follows the screen's rounded edges.
 * Because it is a [Modifier.drawWithContent] layer it adds no extra layout and composes with the
 * 3D tilt of a [DeviceMockup].
 *
 * To apply the same glass to every mockup in a screenshot run, set [ScreenshotStyle.screenGlass]
 * instead of placing this modifier by hand.
 */
fun Modifier.screenGlass(
    reflexAngle: Float = 25f,
    reflexPosition: Float = 0.35f,
    reflexWidth: Float = 0.16f,
    reflexStyle: GlassReflexStyle = GlassReflexStyle.Band,
    reflexColor: Color = Color.White,
    reflexAlpha: Float = 0.18f,
    shadow: GlassShadow = GlassShadow.BottomRight,
    shadowColor: Color = Color.Black,
    shadowAlpha: Float = 0.22f,
): Modifier = screenGlass(
    GlassEffect(
        reflexAngle = reflexAngle,
        reflexPosition = reflexPosition,
        reflexWidth = reflexWidth,
        reflexStyle = reflexStyle,
        reflexColor = reflexColor,
        reflexAlpha = reflexAlpha,
        shadow = shadow,
        shadowColor = shadowColor,
        shadowAlpha = shadowAlpha,
    )
)

/** Draws the glass sheen described by [effect] in front of the content. See [screenGlass]. */
fun Modifier.screenGlass(effect: GlassEffect): Modifier = drawWithContent {
    drawContent()
    if (effect.reflexAlpha > 0f) {
        drawReflex(
            effect.reflexAngle,
            effect.reflexPosition,
            effect.reflexWidth,
            effect.reflexStyle,
            effect.reflexColor,
            effect.reflexAlpha,
        )
    }
    if (effect.shadow != GlassShadow.None && effect.shadowAlpha > 0f) {
        drawShadow(effect.shadow, effect.shadowColor, effect.shadowAlpha)
    }
}

/**
 * Wraps [content] in a full-size [Box] carrying [effect]'s glass, or returns [content] unchanged
 * when [effect] is null. Used to apply [ScreenshotStyle.screenGlass] to the screen drawn inside a
 * mockup bezel, where the content slot is a bare `@Composable () -> Unit` with no modifier.
 */
internal fun glassWrap(
    effect: GlassEffect?,
    content: @Composable () -> Unit,
): @Composable () -> Unit =
    if (effect == null) content else {
        { Box(Modifier.fillMaxSize().screenGlass(effect)) { content() } }
    }

/** Direction the shadow gradient darkens toward, in screen degrees (0 = right, 90 = down). */
private val GlassShadow.angleDegrees: Float
    get() = when (this) {
        GlassShadow.None -> 0f
        GlassShadow.Right -> 0f
        GlassShadow.BottomRight -> 45f
        GlassShadow.Bottom -> 90f
        GlassShadow.BottomLeft -> 135f
        GlassShadow.Left -> 180f
        GlassShadow.TopLeft -> 225f
        GlassShadow.Top -> 270f
        GlassShadow.TopRight -> 315f
    }

/**
 * Start/end of a gradient axis that runs across the whole draw area at [angleDegrees], centered on
 * the box: a fraction of 0 maps to the leading edge in that direction and 1 to the trailing edge,
 * whatever the box aspect ratio.
 */
private fun DrawScope.gradientAxis(angleDegrees: Float): Pair<Offset, Offset> {
    val radians = angleDegrees.toDouble() * PI / 180.0
    val dx = cos(radians).toFloat()
    val dy = sin(radians).toFloat()
    val half = (abs(size.width * dx) + abs(size.height * dy)) / 2f
    val cx = size.width / 2f
    val cy = size.height / 2f
    return Offset(cx - dx * half, cy - dy * half) to Offset(cx + dx * half, cy + dy * half)
}

private fun DrawScope.drawReflex(
    angle: Float,
    position: Float,
    width: Float,
    style: GlassReflexStyle,
    color: Color,
    alpha: Float,
) {
    // The line runs along `angle`; brightness varies along the perpendicular axis.
    val (start, end) = gradientAxis(angle + 90f)
    val peak = color.copy(alpha = alpha.coerceIn(0f, 1f))
    val clear = color.copy(alpha = 0f)
    val center = position.coerceIn(0f, 1f)
    val half = (width / 2f).coerceIn(0f, 0.5f)
    val lo = (center - half).coerceIn(0f, 1f)
    val hi = (center + half).coerceIn(0f, 1f)
    val stops = when (style) {
        // Symmetric streak: clear → peak at the line → clear.
        GlassReflexStyle.Band ->
            listOf(0f to clear, lo to clear, center to peak, hi to clear, 1f to clear)
        // Half-screen wedge: clear on the near side, ramping across the edge to a solid fill
        // that runs out to the far border.
        GlassReflexStyle.Wedge ->
            listOf(0f to clear, lo to clear, hi to peak, 1f to peak)
    }
        .sortedBy { it.first }
        .distinctBy { it.first }
        .toTypedArray()
    drawRect(Brush.linearGradient(colorStops = stops, start = start, end = end))
}

private fun DrawScope.drawShadow(shadow: GlassShadow, color: Color, alpha: Float) {
    val (start, end) = gradientAxis(shadow.angleDegrees)
    val dark = color.copy(alpha = alpha.coerceIn(0f, 1f))
    val clear = color.copy(alpha = 0f)
    // Keep the screen center clean and concentrate the darkening near the edge/corner.
    drawRect(
        Brush.linearGradient(
            0f to clear,
            0.45f to clear,
            1f to dark,
            start = start,
            end = end,
        )
    )
}
