package dev.lucianosantos.storescreenshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lays out [count] equal-width, full-height cells in a row — one per screenshot that
 * [ScreenshotRule.splitScreenshot] slices the canvas into. Put self-contained content in each
 * [panel] — a per-screenshot headline, caption, or badge — and it stays inside that one
 * screenshot, never cut by a seam.
 *
 * Use this for the text that belongs to a single screenshot. Elements that should flow *across*
 * the seams — a continuous background, or a row of devices that straddle two screenshots — go in
 * a separate layer behind or in front of this one:
 *
 * ```kotlin
 * @Test fun story() = splitScreenshot(panels = 3) {
 *     Box(Modifier.fillMaxSize().background(brand))      // continuous background (spans seams)
 *     DeviceShelf()                                      // devices straddling the seams
 *     SplitPanels(count = 3) { index ->                  // per-screenshot text — never cropped
 *         Text(
 *             headlines[index],
 *             Modifier.align(Alignment.TopCenter).padding(top = 80.dp),
 *         )
 *     }
 * }
 * ```
 *
 * Each cell takes an equal `weight`, so its edges line up exactly with the slice boundaries —
 * pass the same value for [count] here as the `panels` you give `splitScreenshot`, and the same
 * [gap] you give it so the cells line up with the gapped slices. Cells are clipped to their own
 * bounds, so per-panel content can never bleed into the next screenshot.
 */
@Composable
fun SplitPanels(
    count: Int,
    modifier: Modifier = Modifier,
    gap: Dp = 0.dp,
    panel: @Composable BoxScope.(index: Int) -> Unit,
) {
    Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
        for (index in 0 until count) {
            Box(Modifier.weight(1f).fillMaxHeight().clipToBounds()) { panel(index) }
        }
    }
}
