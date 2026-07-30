package dev.lucianosantos.storescreenshots.frames

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.DeviceMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotStyle

/**
 * Phone-shaped frame for Play Store screenshots. Title + description + a stylized phone
 * mockup with status bar, notch, and side buttons. Layout/positioning/overrides come from
 * [style]; [content] is rendered inside the phone bezel.
 */
@Composable
fun PhoneFrame(
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    style: ScreenshotStyle = ScreenshotStyle(),
    content: @Composable () -> Unit,
) {
    FramedLayout(
        title = title,
        description = description,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        style = style,
        horizontalPadding = 28.dp,
        verticalPadding = 48.dp,
        mockup = { externalModifier ->
            DeviceMockup(
                formFactor = FormFactor.Phone,
                modifier = externalModifier,
                showStatusBar = style.showStatusBar,
                statusBarClock = style.statusBarClock,
                statusBarContentDark = style.statusBarContentDark,
                edgeToEdge = style.edgeToEdge,
                elevation = style.mockupElevation,
                content = content,
            )
        },
    )
}
