package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ProvideDeviceEnvironment
import dev.lucianosantos.storescreenshots.ScaledMockup
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.iPhoneBodySize

/** Width-to-height ratio of the default [AppleFrame] device's enclosure. */
val AppleIPhoneAspectRatio: Float = AppleIPhoneModel.IPhone17ProMax.aspectRatio

/**
 * iPhone frame for Apple App Store screenshots.
 *
 * The mockup is a scale model of [device] as the Simulator draws it — machined rail, black bezel,
 * rounded display, side buttons, Dynamic Island, and an iOS status bar rather than a Material one,
 * which App Store Review guideline 2.3.10 rejects.
 *
 * [content] is measured at [formFactor]'s logical size — the dp rectangle the slot's qualifiers
 * name, 428x926 for the 6.5" slot and 430x932 for the 6.7" — and the whole mockup is then scaled
 * uniformly into the room the banner leaves, exactly the way [PhoneFrame]'s content is measured at
 * a real phone's 411x822dp. The screen inside the frame therefore lays out at real-device
 * proportions however small the banner draws the device, instead of being measured at whatever dp
 * the bezel's footprint happens to span on the canvas.
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
    formFactor: FormFactor = FormFactor.AppleIPhone67,
    device: AppleIPhoneModel = AppleIPhoneModel.IPhone17ProMax,
    aspectRatio: Float = device.aspectRatio,
    content: @Composable () -> Unit,
) {
    require(formFactor == FormFactor.AppleIPhone65 || formFactor == FormFactor.AppleIPhone67) {
        "AppleFrame draws the App Store iPhone slots; $formFactor is not one."
    }
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
            IPhoneMockup(externalModifier, style, formFactor, device, aspectRatio, content)
        }
    )
}

@Composable
private fun ColumnScope.IPhoneMockup(
    externalModifier: Modifier,
    style: ScreenshotStyle,
    formFactor: FormFactor,
    device: AppleIPhoneModel,
    aspectRatio: Float,
    content: @Composable () -> Unit,
) {
    // The slot's logical screen, and the body that display sits in at the device's proportions.
    val logical = formFactor.logicalSize
    val (bodyWidth, nativeBodyHeight) = iPhoneBodySize(device, logical.width, logical.height)
    // An overridden ratio reshapes the body it is documented to describe; the default lands on the
    // derived height exactly.
    val bodyHeight = if (aspectRatio == device.aspectRatio) {
        nativeBodyHeight
    } else {
        (bodyWidth.value / aspectRatio).dp
    }
    ScaledMockup(bodyWidth, bodyHeight, externalModifier) {
        IPhoneBezel(
            modifier = Modifier.fillMaxSize(),
            showStatusBar = style.showStatusBar,
            clock = style.statusBarClock,
            statusBarContentDark = style.statusBarContentDark,
            edgeToEdge = style.edgeToEdge,
            metrics = device.metrics,
            elevation = style.mockupElevation,
        ) {
            ProvideDeviceEnvironment(logical.width, logical.height, content)
        }
    }
}
