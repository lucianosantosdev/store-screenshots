package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.ScreenshotStyle

/** The screen cutout an iPhone frame draws. */
enum class AppleNotchStyle {
    /** Pill-shaped Dynamic Island — iPhone 14 Pro and later. */
    DynamicIsland,

    /** Wide top notch — iPhone X through 14 Plus, which is what the 6.5" slot depicts. */
    Notch,
}

/**
 * iPhone frame for Apple App Store screenshots.
 *
 * Defaults to the 6.7" body (1290x2796, Dynamic Island). Pass [aspectRatio] and [notch] to draw a
 * different iPhone — the 6.5" slot (1284x2778) shows devices that shipped with a notch, so drawing
 * a Dynamic Island there would depict a phone that never existed at that size.
 */
@Composable
fun AppleFrame(
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    style: ScreenshotStyle = ScreenshotStyle(),
    aspectRatio: Float = 1290f / 2796f,
    notch: AppleNotchStyle = AppleNotchStyle.DynamicIsland,
    content: @Composable () -> Unit,
) {
    FramedLayout(
        title = title,
        description = description,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        style = style,
        horizontalPadding = 24.dp,
        verticalPadding = 28.dp,
        titleFontSize = 26.sp,
        descriptionFontSize = 14.sp,
        mockup = { externalModifier ->
            IPhoneMockup(
                externalModifier,
                style.showStatusBar,
                style.statusBarClock,
                style.statusBarContentDark,
                aspectRatio,
                notch,
                content
            )
        }
    )
}

@Composable
private fun ColumnScope.IPhoneMockup(
    externalModifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    statusBarContentDark: Boolean,
    aspectRatio: Float,
    notch: AppleNotchStyle,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = externalModifier
            .fillMaxHeight()
            .aspectRatio(aspectRatio)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(58.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color(0xFF0A0A0A))))
                .padding(2.dp)
                .clip(RoundedCornerShape(56.dp))
                .background(Color.Black)
                .padding(6.dp)
                .clip(RoundedCornerShape(50.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize()) { content() }
            if (showStatusBar) StatusBar(
                clock = clock,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = if (statusBarContentDark) Color.Black else Color.White,
            )
            when (notch) {
                AppleNotchStyle.DynamicIsland -> DynamicIsland(Modifier.align(Alignment.TopCenter))
                AppleNotchStyle.Notch -> Notch(Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
private fun DynamicIsland(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 12.dp)
            .size(width = 124.dp, height = 36.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
            .border(0.5.dp, Color(0xFF222222), RoundedCornerShape(50))
    )
}

/**
 * The pre-14-Pro notch: flush with the top edge, so only its bottom corners are rounded.
 * Wider and shorter than the Dynamic Island, and not inset from the top.
 */
@Composable
private fun Notch(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 156.dp, height = 30.dp)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(Color.Black)
    )
}
