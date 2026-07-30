package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.frames.IPadAir13Metrics as Pad
import dev.lucianosantos.storescreenshots.frames.IPhone17Metrics as Phone

/**
 * The iPhone status bar: the clock to the left of the Dynamic Island, then four cellular bars, the
 * Wi-Fi glyph, and the battery on the right.
 *
 * Every glyph is a vector path measured off the Simulator (see [IPhone17Metrics]) rather than a
 * Material icon. That is not cosmetic — App Store Review guideline 2.3.10 rejects screenshots whose
 * status bar is not an iOS status bar, which is exactly what `Icons.Filled.SignalCellular4Bar` and
 * `Icons.Filled.Wifi` look like to a reviewer.
 *
 * The bar lays itself out across the full [screenWidth] and positions everything from that, so it
 * stays correct whether the frame is drawn at App Store resolution or thumbnail size. Place it at
 * the top-start corner of the screen area, not inside any padding.
 */
@Composable
internal fun IosStatusBar(
    clock: String,
    screenWidth: Dp,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
) {
    // One measured iPhone 17 point, in this frame's dp.
    val unit = screenWidth.value / Phone.ScreenWidth
    val measurer = rememberTextMeasurer()
    Box(modifier = modifier.size(screenWidth, (Phone.SafeAreaTop * unit).dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val u = size.width / Phone.ScreenWidth
            Phone.CellularBarHeights.forEachIndexed { index, heightPt ->
                drawRoundRect(
                    color = contentColor,
                    topLeft = Offset(
                        (Phone.CellularX + index * Phone.CellularBarPitch) * u,
                        (Phone.CellularBottom - heightPt) * u,
                    ),
                    size = Size(Phone.CellularBarWidth * u, heightPt * u),
                    cornerRadius = CornerRadius(Phone.CellularBarCorner * u),
                )
            }
            drawWifiGlyph(contentColor, Phone.WifiX * u, Phone.WifiTop * u, Phone.WifiWidth * u)
            drawBatteryGlyph(contentColor, Phone.BatteryX * u, Phone.BatteryTop * u, Phone.BatteryWidth * u)
            drawStatusBarClock(
                measurer = measurer,
                text = clock,
                color = contentColor,
                capHeight = Phone.ClockCapHeight * u,
                centerY = Phone.ClockCenterY * u,
                centerX = Phone.ClockCenterX * u,
            )
        }
    }
}

/**
 * The iPadOS status bar. Shallower than a phone's, with no cutout to work around: the clock sits at
 * the leading edge rather than centred, and a Wi-Fi iPad shows no cellular bars at all. The Wi-Fi
 * glyph and battery are the same shapes iOS draws on a phone, scaled down.
 *
 * iPadOS follows the time with the date. [clock] is drawn as one run of text at the leading edge,
 * so pass `"9:41  Wed 29 Jul"` for the full thing or just the time for a plainer bar.
 */
@Composable
internal fun IPadOsStatusBar(
    clock: String,
    screenWidth: Dp,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
) {
    val unit = screenWidth.value / Pad.ScreenWidth
    val measurer = rememberTextMeasurer()
    Box(modifier = modifier.size(screenWidth, (Pad.SafeAreaTop * unit).dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val u = size.width / Pad.ScreenWidth
            drawWifiGlyph(contentColor, Pad.WifiX * u, Pad.WifiTop * u, Pad.WifiWidth * u)
            drawBatteryGlyph(contentColor, Pad.BatteryX * u, Pad.BatteryTop * u, Pad.BatteryWidth * u)
            // Leading-aligned: iPadOS starts the clock at the edge rather than centring it.
            drawStatusBarClock(
                measurer = measurer,
                text = clock,
                color = contentColor,
                capHeight = Pad.ClockCapHeight * u,
                centerY = Pad.ClockCenterY * u,
                left = Pad.ClockLeft * u,
            )
        }
    }
}

/**
 * Draws the clock so its *digits* land where they were measured on the device.
 *
 * Laying a `Text` out and centring it in a box does not do that: the line box is taller than the
 * digits and sits differently in every font, so the glyphs end up several points off. Measuring the
 * text and placing it from its own baseline is exact — the baseline is the bottom of the digits, so
 * the ink's middle is half a cap height above it, wherever the font puts its ascent and descent.
 *
 * Pass [centerX] to centre the digits horizontally, the way iOS does beside the Dynamic Island, or
 * [left] to start them at an inset, the way iPadOS does.
 */
private fun DrawScope.drawStatusBarClock(
    measurer: TextMeasurer,
    text: String,
    color: Color,
    capHeight: Float,
    centerY: Float,
    centerX: Float? = null,
    left: Float? = null,
) {
    val layout = measurer.measure(
        text = AnnotatedString(text),
        style = TextStyle(
            color = color,
            fontSize = (capHeight / DigitCapHeightRatio).toSp(),
            fontWeight = FontWeight.SemiBold,
        ),
        maxLines = 1,
        softWrap = false,
    )
    val x = left ?: ((centerX ?: 0f) - layout.size.width / 2f)
    drawText(layout, topLeft = Offset(x, centerY + capHeight / 2f - layout.firstBaseline))
}

/**
 * Height of a rendered digit as a fraction of the font size, for the default sans-serif family.
 * Used only to turn a measured cap height into a font size — placement comes from the baseline, not
 * from this.
 *
 * Calibrated against both captures rather than taken from the font's cap-height metric (0.711 em):
 * what has to match is the *ink* the two rasterisers put down, and anti-aliasing makes that a little
 * taller than the nominal cap height at every scale.
 */
private const val DigitCapHeightRatio = 0.75f

/**
 * Two concentric arc bands with round caps, plus a dot that tapers to a point — the shape iOS
 * draws, rather than Material's filled fan. Sized from [width]; the proportions were fitted against
 * the iPhone capture and hold at any scale, so the iPad's smaller glyph reuses them.
 */
internal fun DrawScope.drawWifiGlyph(color: Color, left: Float, top: Float, width: Float) {
    val s = width / Phone.WifiWidth
    val centerX = left + width / 2
    val arcCenterY = top + Phone.WifiArcCenterY * s
    val stroke = Stroke(width = Phone.WifiStroke * s, cap = StrokeCap.Round)
    // 0° points right and angles run clockwise, so straight up is -90°.
    val startAngle = -90f - Phone.WifiHalfSweep
    val sweepAngle = Phone.WifiHalfSweep * 2

    listOf(Phone.WifiOuterRadius, Phone.WifiInnerRadius).forEach { radiusPt ->
        val radius = radiusPt * s
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(centerX - radius, arcCenterY - radius),
            size = Size(radius * 2, radius * 2),
            style = stroke,
        )
    }

    // A dome as tall as half the band is thick, coming to a point on the baseline.
    val dotHalfWidth = Phone.WifiDotHalfWidth * s
    val dotCenterY = top + Phone.WifiDotCenterY * s
    val domeRise = Phone.WifiStroke / 2 * s
    val dot = Path().apply {
        moveTo(centerX - dotHalfWidth, dotCenterY)
        // A quadratic peaks halfway to its control point, so pull the control twice as high.
        quadraticTo(centerX, dotCenterY - 2 * domeRise, centerX + dotHalfWidth, dotCenterY)
        lineTo(centerX, top + Phone.WifiHeight * s)
        close()
    }
    drawPath(dot, color)
}

/** Rounded outline at 40% opacity, a solid charge level inset inside it, and the terminal. */
internal fun DrawScope.drawBatteryGlyph(color: Color, left: Float, top: Float, width: Float) {
    val s = width / Phone.BatteryWidth
    val height = Phone.BatteryHeight * s
    val stroke = Phone.BatteryStroke * s
    drawRoundRect(
        color = color.copy(alpha = Phone.BatteryOutlineAlpha),
        topLeft = Offset(left + stroke / 2, top + stroke / 2),
        size = Size(width - stroke, height - stroke),
        cornerRadius = CornerRadius(Phone.BatteryCorner * s - stroke / 2),
        style = Stroke(width = stroke),
    )
    val inset = Phone.BatteryFillInset * s
    drawRoundRect(
        color = color,
        topLeft = Offset(left + inset, top + inset),
        size = Size(width - 2 * inset, height - 2 * inset),
        cornerRadius = CornerRadius(Phone.BatteryFillCorner * s),
    )
    drawRoundRect(
        color = color.copy(alpha = Phone.BatteryNubAlpha),
        topLeft = Offset(
            left + width + Phone.BatteryNubGap * s,
            top + height / 2 - Phone.BatteryNubHeight / 2 * s,
        ),
        size = Size(Phone.BatteryNubWidth * s, Phone.BatteryNubHeight * s),
        cornerRadius = CornerRadius(Phone.BatteryNubWidth / 2 * s),
    )
}
