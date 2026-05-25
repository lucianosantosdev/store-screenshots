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

/**
 * iPhone frame with Dynamic Island for Apple App Store screenshots.
 * Targets 6.7" iPhone (1290x2796) — the size App Store Connect requires for new submissions.
 */
@Composable
fun AppleFrame(
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
        horizontalPadding = 24.dp,
        verticalPadding = 28.dp,
        titleFontSize = 26.sp,
        descriptionFontSize = 14.sp,
        mockup = { IPhoneMockup(content) }
    )
}

@Composable
private fun ColumnScope.IPhoneMockup(content: @Composable () -> Unit) {
    // weight(1f) + aspectRatio lets the device shrink to whatever vertical space is left
    // after the title/description, avoiding overflow on the tall 1290x2796 canvas.
    Box(
        modifier = Modifier
            .weight(1f, fill = false)
            .fillMaxHeight()
            .aspectRatio(1290f / 2796f)
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
            DynamicIsland(modifier = Modifier.align(Alignment.TopCenter))
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
