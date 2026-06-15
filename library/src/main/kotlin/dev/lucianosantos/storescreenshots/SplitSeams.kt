package dev.lucianosantos.storescreenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Overlays where [ScreenshotRule.splitScreenshot] cuts a [count]-wide canvas into separate
 * screenshots. With no [gap] it draws the [count] − 1 seam lines; with a [gap] it shades the
 * gap strips that get discarded — the bands of the design that won't appear in any screenshot.
 * Drop it on top of a split layout **inside a `@Preview`** to see where the cuts land.
 *
 * It's a preview aid: leave it out of the `splitScreenshot { … }` body itself, or the marks would
 * be baked into the exported PNGs. Pass the same [count] and [gap] you give `splitScreenshot`.
 *
 * ```kotlin
 * @Preview(widthDp = 1281, heightDp = 914)
 * @Composable fun StoryPreview() = Box(Modifier.fillMaxSize()) {
 *     StoryBanner()
 *     SplitSeams(count = 3, gap = 24.dp)   // shades the two discarded gap strips
 * }
 * ```
 */
@Composable
fun SplitSeams(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.45f),
    thickness: Dp = 2.dp,
    gap: Dp = 0.dp,
) {
    Row(modifier = modifier.fillMaxSize()) {
        for (index in 0 until count) {
            // Equal-weight cells are the panel widths; the explicit gap bars sit between them, so
            // this lines up with the slices exactly.
            Box(Modifier.weight(1f).fillMaxHeight()) {
                if (gap <= 0.dp && index < count - 1) {
                    // No gap — a thin line on the trailing edge marks the seam.
                    Box(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(thickness).background(color))
                }
            }
            if (gap > 0.dp && index < count - 1) {
                // The discarded gap strip, shaded so you can see what gets cut out.
                Box(Modifier.width(gap).fillMaxHeight().background(color))
            }
        }
    }
}
