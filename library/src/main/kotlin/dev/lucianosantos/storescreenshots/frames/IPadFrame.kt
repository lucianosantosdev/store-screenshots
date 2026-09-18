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
import dev.lucianosantos.storescreenshots.iPadBodySize
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
 * The mockup is bounded by height and takes its width from the body's proportions, so on the iPad
 * slot's squarish 2048x2732 canvas it fits the space left after the title and description instead
 * of overflowing and covering them. [aspectRatio] reshapes that body — pass `null` (the default) to
 * draw the iPad at the proportions its own display and bezel give it.
 */
@Composable
fun AppleIPadFrame(
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    style: ScreenshotStyle = ScreenshotStyle(),
    aspectRatio: Float? = null,
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
        tiltHandledByMockup = true,
        mockup = { externalModifier -> IPadMockup(externalModifier, style, aspectRatio, content) }
    )
}

@Composable
private fun ColumnScope.IPadMockup(
    externalModifier: Modifier,
    style: ScreenshotStyle,
    aspectRatio: Float?,
    content: @Composable () -> Unit,
) {
    // The slot's logical screen (1024x1366, a real 13" iPad's display) and the body around it.
    // Content is measured there and the mockup scaled into the banner's room, the same contract
    // as [AppleFrame] and [PhoneFrame] — not at whatever dp the footprint spans on the canvas.
    val logical = FormFactor.AppleIPad13.logicalSize
    val (bodyWidth, nativeBodyHeight) = iPadBodySize(logical.width, logical.height)
    val bodyHeight = aspectRatio?.let { (bodyWidth.value / it).dp } ?: nativeBodyHeight
    // See [AppleFrame]: the bezel is uniform, so an overridden ratio stretches the screen by what
    // it stretches the body by, and that — not the logical height — is what content is measured in.
    val screenHeight = logical.height + (bodyHeight - nativeBodyHeight)
    MockupSurface(
        nativeWidth = bodyWidth,
        nativeHeight = bodyHeight,
        modifier = externalModifier,
        body = iPadBody(bodyWidth, bodyHeight),
        tilt = style.mockupTilt(),
        elevation = style.mockupElevation,
        material = style.mockupMaterial,
    ) { chrome ->
        IPadBezel(
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
