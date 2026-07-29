package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.frames.IPhone17Metrics as M

/**
 * The iOS status bar, drawn to the proportions of a real iPhone: the clock on the left of the
 * Dynamic Island, then four cellular bars, the Wi-Fi glyph, and the battery on the right.
 *
 * Every glyph is a vector path measured off the iPhone 17 Simulator (see [IPhone17Metrics]) rather
 * than a Material icon. That is not cosmetic — App Store Review guideline 2.3.10 rejects
 * screenshots whose status bar is not an iOS status bar, which is exactly what
 * `Icons.Filled.SignalCellular4Bar` and `Icons.Filled.Wifi` look like to a reviewer.
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
    // One "reference point" of the measured iPhone 17, expressed in this frame's dp.
    val unit = screenWidth.value / M.ScreenWidth
    Box(modifier = modifier.size(screenWidth, (M.SafeAreaTop * unit).dp)) {
        Canvas(Modifier.fillMaxSize()) { drawStatusBarIcons(contentColor) }
        // Anchoring a centred box at twice the clock's centre puts the digits' midpoint exactly on
        // the measured centre, whatever the clock string is.
        Box(
            modifier = Modifier.size(
                width = (M.ClockCenterX * 2 * unit).dp,
                height = (M.ClockCenterY * 2 * unit).dp,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = clock,
                color = contentColor,
                // Sized by cap height, not em size: the measured 12.67 pt is the height of the
                // digits themselves, and that is what has to match at a glance.
                fontSize = with(LocalDensity.current) {
                    (M.ClockCapHeight / DigitCapHeightRatio * unit).dp.toSp()
                },
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/**
 * Fraction of the font size taken up by a digit in the default sans-serif family. Used to turn the
 * measured cap height into a font size.
 */
private const val DigitCapHeightRatio = 0.711f

/**
 * Draws the trailing cluster — cellular bars, Wi-Fi, battery — into a canvas that spans the full
 * screen width, so [M]'s point coordinates map straight onto it.
 */
private fun DrawScope.drawStatusBarIcons(contentColor: Color) {
    val u = size.width / M.ScreenWidth

    // Four bars standing on a common baseline, shortest first.
    M.CellularBarHeights.forEachIndexed { index, heightPt ->
        val left = (M.CellularX + index * M.CellularBarPitch) * u
        drawRoundRect(
            color = contentColor,
            topLeft = Offset(left, (M.CellularBottom - heightPt) * u),
            size = Size(M.CellularBarWidth * u, heightPt * u),
            cornerRadius = CornerRadius(M.CellularBarCorner * u),
        )
    }

    drawWifi(contentColor, u)
    drawBattery(contentColor, u)
}

/**
 * Two concentric arc bands with round caps, plus a dot that tapers to a point — the shape iOS
 * draws, rather than Material's filled fan.
 */
private fun DrawScope.drawWifi(contentColor: Color, u: Float) {
    val centerX = (M.WifiX + M.WifiWidth / 2) * u
    val arcCenterY = (M.WifiTop + M.WifiArcCenterY) * u
    val stroke = Stroke(width = M.WifiStroke * u, cap = StrokeCap.Round)
    // 0° points right and angles run clockwise, so straight up is -90°.
    val startAngle = -90f - M.WifiHalfSweep
    val sweepAngle = M.WifiHalfSweep * 2

    listOf(M.WifiOuterRadius, M.WifiInnerRadius).forEach { radiusPt ->
        val radius = radiusPt * u
        drawArc(
            color = contentColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(centerX - radius, arcCenterY - radius),
            size = Size(radius * 2, radius * 2),
            style = stroke,
        )
    }

    // A dome as tall as half the band is thick, coming to a point on the baseline.
    val dotHalfWidth = M.WifiDotHalfWidth * u
    val dotCenterY = (M.WifiTop + M.WifiDotCenterY) * u
    val domeRise = M.WifiStroke / 2 * u
    val dot = Path().apply {
        moveTo(centerX - dotHalfWidth, dotCenterY)
        // A quadratic peaks halfway to its control point, so pull the control twice as high.
        quadraticTo(centerX, dotCenterY - 2 * domeRise, centerX + dotHalfWidth, dotCenterY)
        lineTo(centerX, (M.WifiTop + M.WifiHeight) * u)
        close()
    }
    drawPath(dot, contentColor)
}

/** Rounded outline at 40% opacity, a solid charge level inset inside it, and the terminal. */
private fun DrawScope.drawBattery(contentColor: Color, u: Float) {
    val stroke = M.BatteryStroke * u
    drawRoundRect(
        color = contentColor.copy(alpha = M.BatteryOutlineAlpha),
        topLeft = Offset(M.BatteryX * u + stroke / 2, M.BatteryTop * u + stroke / 2),
        size = Size(M.BatteryWidth * u - stroke, M.BatteryHeight * u - stroke),
        cornerRadius = CornerRadius(M.BatteryCorner * u - stroke / 2),
        style = Stroke(width = stroke),
    )
    drawRoundRect(
        color = contentColor,
        topLeft = Offset((M.BatteryX + M.BatteryFillInset) * u, (M.BatteryTop + M.BatteryFillInset) * u),
        size = Size(
            (M.BatteryWidth - 2 * M.BatteryFillInset) * u,
            (M.BatteryHeight - 2 * M.BatteryFillInset) * u,
        ),
        cornerRadius = CornerRadius(M.BatteryFillCorner * u),
    )
    drawRoundRect(
        color = contentColor.copy(alpha = M.BatteryNubAlpha),
        topLeft = Offset(
            (M.BatteryX + M.BatteryWidth + M.BatteryNubGap) * u,
            (M.IconCenterY - M.BatteryNubHeight / 2) * u,
        ),
        size = Size(M.BatteryNubWidth * u, M.BatteryNubHeight * u),
        cornerRadius = CornerRadius(M.BatteryNubWidth / 2 * u),
    )
}
