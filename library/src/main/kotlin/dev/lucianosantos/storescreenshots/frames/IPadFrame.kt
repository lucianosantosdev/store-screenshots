package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.frames.IPadAir13Metrics as M

/** Width-to-height ratio of a 13-inch iPad's enclosure, measured off the Simulator. */
val AppleIPadAspectRatio: Float = M.BodyWidth / M.BodyHeight

/**
 * iPad frame for the App Store's 13-inch iPad slot.
 *
 * A scale model of a 13-inch iPad Air as the Simulator draws it — machined rail, black bezel,
 * squared-off display, volume buttons, top power button, and an iPadOS status bar rather than a
 * Material one, which App Store Review guideline 2.3.10 rejects.
 *
 * The mockup is bounded by height and takes its width from [aspectRatio], so on the iPad slot's
 * squarish 2048x2732 canvas it fits the space left after the title and description instead of
 * overflowing and covering them.
 */
@Composable
fun AppleIPadFrame(
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    style: ScreenshotStyle = ScreenshotStyle(),
    aspectRatio: Float = AppleIPadAspectRatio,
    content: @Composable () -> Unit,
) {
    FramedLayout(
        title = title,
        description = description,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        style = style,
        horizontalPadding = 48.dp,
        verticalPadding = 56.dp,
        titleFontSize = 36.sp,
        descriptionFontSize = 18.sp,
        mockup = { externalModifier -> IPadMockup(externalModifier, style, aspectRatio, content) }
    )
}

@Composable
private fun ColumnScope.IPadMockup(
    externalModifier: Modifier,
    style: ScreenshotStyle,
    aspectRatio: Float,
    content: @Composable () -> Unit,
) {
    IPadBezel(
        modifier = externalModifier
            .fillMaxHeight()
            .aspectRatio(aspectRatio),
        showStatusBar = style.showStatusBar,
        clock = style.statusBarClock,
        statusBarContentDark = style.statusBarContentDark,
        edgeToEdge = style.edgeToEdge,
        elevation = style.mockupElevation,
        content = content,
    )
}
