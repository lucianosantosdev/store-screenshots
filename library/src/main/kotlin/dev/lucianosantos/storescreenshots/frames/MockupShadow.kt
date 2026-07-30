package dev.lucianosantos.storescreenshots.frames

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import android.graphics.Paint as AndroidPaint

/**
 * Casts a soft drop shadow behind a device mockup, in the shape of its own enclosure, so the
 * device lifts off the banner instead of sitting flat on it.
 *
 * The shadow is painted directly rather than through `Modifier.shadow`: the platform's elevation
 * shadows are drawn by the render node, which needs a hardware-accelerated canvas, and screenshots
 * here are rendered on Robolectric's software canvas — where they come out missing or clipped.
 * Blurring a path costs nothing at screenshot time and gives a shadow that is the same on every
 * machine, which is what a committed PNG needs.
 *
 * [elevation] reads like Material elevation: it sets the blur radius, and the shadow is offset
 * downward by a fraction of it, as though the light were above the device. A value of `0.dp`
 * returns the modifier untouched, so an un-elevated frame renders exactly as it did before this
 * existed.
 */
internal fun Modifier.mockupShadow(
    elevation: Dp,
    shape: Shape,
    color: Color = Color.Black,
): Modifier = if (elevation <= 0.dp) this else drawBehind {
    val blur = elevation.toPx()
    if (blur <= 0f) return@drawBehind
    val paint = AndroidPaint().apply {
        isAntiAlias = true
        this.color = color.copy(alpha = color.alpha * ShadowAlpha).toArgb()
        maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
    }
    val path = shape.outlinePath(size, layoutDirection, this).asAndroidPath()
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val checkpoint = native.save()
        native.translate(0f, blur * ShadowDrop)
        native.drawPath(path, paint)
        native.restoreToCount(checkpoint)
    }
}

/** Opacity of the cast shadow at its darkest, before the blur spreads it out. */
private const val ShadowAlpha = 0.45f

/** How far the shadow sits below the device, as a fraction of the blur radius. */
private const val ShadowDrop = 0.45f

private fun Shape.outlinePath(size: Size, layoutDirection: LayoutDirection, density: Density): Path =
    when (val outline = createOutline(size, layoutDirection, density)) {
        is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
        is Outline.Generic -> outline.path
    }
