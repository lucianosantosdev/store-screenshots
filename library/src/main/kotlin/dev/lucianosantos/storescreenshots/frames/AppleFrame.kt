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

/** Width-to-height ratio of the default [AppleFrame] device's enclosure. */
val AppleIPhoneAspectRatio: Float = AppleIPhoneModel.IPhone17ProMax.aspectRatio

/**
 * iPhone frame for Apple App Store screenshots.
 *
 * The mockup is a scale model of [device] as the Simulator draws it — machined rail, black bezel,
 * rounded display, side buttons, Dynamic Island, and an iOS status bar rather than a Material one,
 * which App Store Review guideline 2.3.10 rejects.
 *
 * [device] defaults to the iPhone 17 Pro Max, which is what both App Store iPhone slots are sized
 * for and what a reviewer expects a current submission to depict. [aspectRatio] is the aspect ratio
 * of the *body*, and follows [device] unless you override it.
 */
@Composable
fun AppleFrame(
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    style: ScreenshotStyle = ScreenshotStyle(),
    device: AppleIPhoneModel = AppleIPhoneModel.IPhone17ProMax,
    aspectRatio: Float = device.aspectRatio,
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
            IPhoneMockup(externalModifier, style, aspectRatio, device, content)
        }
    )
}

@Composable
private fun ColumnScope.IPhoneMockup(
    externalModifier: Modifier,
    style: ScreenshotStyle,
    aspectRatio: Float,
    device: AppleIPhoneModel,
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
        metrics = device.metrics,
        elevation = style.mockupElevation,
        content = content,
    )
}
