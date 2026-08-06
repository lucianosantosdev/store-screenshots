package dev.lucianosantos.storescreenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints

/**
 * Measures [content] at the size it wants — the width on offer, height unbounded — and, if that is
 * taller than the space available, scales the whole thing down uniformly until it fits.
 *
 * This is what keeps a custom [ScreenshotStyle.mockupFrame] on the canvas. The built-in bezels are
 * laid out at a native size and scaled to their footprint, so they shrink to fit whatever canvas
 * they are given. A hand-written frame has no such contract, and the natural way to write one
 * derives its height from its width:
 *
 * ```kotlin
 * Modifier.fillMaxWidth().aspectRatio(9f / 18f)
 * ```
 *
 * On a canvas that is tall enough, that is fine. On one that is not, `fillMaxWidth` has already
 * fixed the width, so no height satisfies the aspect ratio inside the constraints, `aspectRatio`
 * gives up and passes the constraints through, and the frame lays out past the bottom of the image
 * and is **clipped** — quietly, since a clipped frame still renders. Scaling instead means a custom
 * frame behaves like a built-in one: it gets smaller on a shorter canvas rather than losing its
 * bottom edge and covering the title.
 *
 * Only ever shrinks. Content that already fits is placed untouched, so this is a no-op on every
 * layout that was not overflowing — including a custom layout that hands down unbounded height,
 * where there is no "available" height to fit into.
 */
@Composable
internal fun ScaleToFit(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        // Natural size: as wide as offered, as tall as it likes. A frame whose height comes from
        // its width can only report that height when the height is not already bounded.
        val natural = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
        var placeables = measurables.map { it.measure(natural) }
        var width = placeables.maxOfOrNull { it.width } ?: 0
        var height = placeables.maxOfOrNull { it.height } ?: 0

        // A frame sized by fillMaxSize() reports nothing against an unbounded height. It has no
        // natural size to preserve, so measure it the ordinary way and leave it alone.
        if (height == 0 && constraints.hasBoundedHeight) {
            placeables = measurables.map { it.measure(constraints) }
            width = placeables.maxOfOrNull { it.width } ?: 0
            height = placeables.maxOfOrNull { it.height } ?: 0
        }

        val scale = when {
            height <= 0 || !constraints.hasBoundedHeight -> 1f
            height <= constraints.maxHeight -> 1f
            else -> constraints.maxHeight.toFloat() / height
        }
        val outWidth = (width * scale).toInt().coerceIn(constraints.minWidth, constraints.maxWidth)
        val outHeight = (height * scale).toInt()
            .coerceIn(constraints.minHeight, if (constraints.hasBoundedHeight) constraints.maxHeight else height)

        layout(outWidth, outHeight) {
            placeables.forEach { placeable ->
                if (scale == 1f) {
                    placeable.place(0, 0)
                } else {
                    placeable.placeWithLayer(0, 0) {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                }
            }
        }
    }
}
