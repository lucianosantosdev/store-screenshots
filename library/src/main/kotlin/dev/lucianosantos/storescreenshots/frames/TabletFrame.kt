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
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.TabletBezel
import dev.lucianosantos.storescreenshots.tabletBodySize

/**
 * Tablet frame with thinner bezels than the phone frame and no camera notch.
 * Used for both 7-inch and 10-inch Play Store screenshots.
 *
 * [content] is measured at [formFactor]'s logical size — the dp rectangle the slot's qualifiers
 * name, 600x960 for the 7-inch slot and 800x1280 for the 10-inch — and the whole mockup is then
 * scaled uniformly into the room the banner leaves, the same contract as [PhoneFrame] and
 * [AppleFrame]. The screen inside the frame therefore lays out at real-tablet proportions however
 * small the banner draws the device, instead of being measured at whatever dp the bezel's footprint
 * happens to span on the canvas.
 *
 * [aspectRatio] reshapes the *body* — pass `null` (the default) to draw the tablet at the
 * proportions its own display gives it.
 */
@Composable
fun TabletFrame(
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    style: ScreenshotStyle = ScreenshotStyle(),
    aspectRatio: Float? = null,
    formFactor: FormFactor = FormFactor.Tablet10,
    content: @Composable () -> Unit,
) {
    require(formFactor == FormFactor.Tablet7 || formFactor == FormFactor.Tablet10) {
        "TabletFrame draws the Play Store tablet slots; $formFactor is not one."
    }
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
        tiltHandledByMockup = true,
        mockup = { externalModifier ->
            TabletMockup(externalModifier, style, formFactor, aspectRatio, content)
        }
    )
}

@Composable
private fun ColumnScope.TabletMockup(
    externalModifier: Modifier,
    style: ScreenshotStyle,
    formFactor: FormFactor,
    aspectRatio: Float?,
    content: @Composable () -> Unit,
) {
    // The slot's logical screen, and the body that display sits in.
    val logical = formFactor.logicalSize
    val (bodyWidth, nativeBodyHeight) = tabletBodySize(logical.width, logical.height)
    val bodyHeight = aspectRatio?.let { (bodyWidth.value / it).dp } ?: nativeBodyHeight
    // The bezel insets the screen by the same amount whatever height the body is drawn at, so an
    // overridden ratio stretches the screen by exactly what it stretches the body by. Reporting
    // that rather than the logical height keeps what content is told it has and what it is
    // actually measured in the same number.
    val screenHeight = logical.height + (bodyHeight - nativeBodyHeight)
    MockupSurface(
        nativeWidth = bodyWidth,
        nativeHeight = bodyHeight,
        modifier = externalModifier,
        body = androidTabletBody(bodyWidth, bodyHeight),
        tilt = style.mockupTilt(),
        elevation = style.mockupElevation,
        material = style.mockupMaterial,
    ) { chrome ->
        TabletBezel(
            modifier = Modifier.fillMaxSize(),
            showStatusBar = style.showStatusBar,
            clock = style.statusBarClock,
            statusBarContentDark = style.statusBarContentDark,
            edgeToEdge = style.edgeToEdge,
            chrome = chrome,
        ) {
            ProvideDeviceEnvironment(logical.width, screenHeight, content)
        }
    }
}
