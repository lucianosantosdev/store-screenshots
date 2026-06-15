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
 * Overlays the [count] − 1 vertical guide lines that mark where [ScreenshotRule.splitScreenshot]
 * cuts a [count]-wide canvas into separate screenshots. Drop it on top of a split layout **inside
 * a `@Preview`** to see the seams while you design — so you can tell which content stays inside a
 * screenshot and which spans the boundaries.
 *
 * It's a preview aid: leave it out of the `splitScreenshot { … }` body itself, or the lines would
 * be baked into the exported PNGs.
 *
 * ```kotlin
 * @Preview(widthDp = 1233, heightDp = 914)
 * @Composable fun StoryPreview() = Box(Modifier.fillMaxSize()) {
 *     StoryBanner()
 *     SplitSeams(count = 3)   // two guide lines at the seams
 * }
 * ```
 */
@Composable
fun SplitSeams(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.45f),
    thickness: Dp = 2.dp,
) {
    Row(modifier = modifier.fillMaxSize()) {
        for (index in 0 until count) {
            Box(Modifier.weight(1f).fillMaxHeight()) {
                // A line on the trailing edge of every cell except the last marks each seam.
                if (index < count - 1) {
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(thickness)
                            .background(color)
                    )
                }
            }
        }
    }
}
