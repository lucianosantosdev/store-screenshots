package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

/**
 * Tablet frame with thinner bezels than the phone frame and no camera notch.
 * Used for both 7-inch and 10-inch Play Store screenshots.
 */
@Composable
fun TabletFrame(
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    style: ScreenshotStyle = ScreenshotStyle(),
    aspectRatio: Float = 3f / 4f,
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
        mockup = { externalModifier -> TabletMockup(externalModifier, style.showStatusBar, style.statusBarClock, aspectRatio, content) }
    )
}

@Composable
private fun ColumnScope.TabletMockup(
    externalModifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    aspectRatio: Float,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = externalModifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))))
            .padding(2.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.Black)
            .padding(8.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(Modifier.fillMaxSize()) { content() }
        if (showStatusBar) StatusBar(clock = clock, modifier = Modifier.align(Alignment.TopCenter))
    }
}
