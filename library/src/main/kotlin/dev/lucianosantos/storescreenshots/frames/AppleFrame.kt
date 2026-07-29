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
import dev.lucianosantos.storescreenshots.frames.IPhone17Metrics as M

/** Width-to-height ratio of an iPhone 17's enclosure, measured off the Simulator. */
val AppleIPhoneAspectRatio: Float = M.BodyWidth / M.BodyHeight

/**
 * iPhone frame for Apple App Store screenshots.
 *
 * The mockup is a scale model of an iPhone 17 as the Simulator draws it — machined rail, black
 * bezel, rounded display, side buttons, Dynamic Island, and an iOS status bar rather than a
 * Material one, which App Store Review guideline 2.3.10 rejects.
 *
 * [aspectRatio] is the aspect ratio of the *body*, and defaults to the real one. Pass [notch] to
 * draw an older iPhone: the 6.5" slot depicts devices that shipped with a notch, so drawing a
 * Dynamic Island there would show a phone that never existed at that size.
 */
@Composable
fun AppleFrame(
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    style: ScreenshotStyle = ScreenshotStyle(),
    aspectRatio: Float = AppleIPhoneAspectRatio,
    notch: AppleNotchStyle = AppleNotchStyle.DynamicIsland,
    content: @Composable () -> Unit,
) {
    FramedLayout(
        title = title,
        description = description,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        style = style,
        // The side buttons stand proud of the body, so the frame keeps a little more room than the
        // body itself needs.
        horizontalPadding = 28.dp,
        verticalPadding = 28.dp,
        titleFontSize = 26.sp,
        descriptionFontSize = 14.sp,
        mockup = { externalModifier ->
            IPhoneMockup(externalModifier, style, aspectRatio, notch, content)
        }
    )
}

@Composable
private fun ColumnScope.IPhoneMockup(
    externalModifier: Modifier,
    style: ScreenshotStyle,
    aspectRatio: Float,
    notch: AppleNotchStyle,
    content: @Composable () -> Unit,
) {
    IPhoneBezel(
        modifier = externalModifier
            .fillMaxHeight()
            .aspectRatio(aspectRatio),
        showStatusBar = style.showStatusBar,
        clock = style.statusBarClock,
        statusBarContentDark = style.statusBarContentDark,
        edgeToEdge = style.edgeToEdge,
        notch = notch,
        elevation = style.mockupElevation,
        content = content,
    )
}
