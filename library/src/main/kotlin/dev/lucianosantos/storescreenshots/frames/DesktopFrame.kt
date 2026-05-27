package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
 * Laptop / monitor frame for desktop screenshots. A thin-bezel display on a stand/hinge,
 * landscape 1440x900.
 */
@Composable
fun DesktopFrame(
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
        horizontalPadding = 48.dp,
        verticalPadding = 36.dp,
        titleFontSize = 28.sp,
        descriptionFontSize = 14.sp,
        mockup = { externalModifier -> LaptopMockup(externalModifier, style.showStatusBar, style.statusBarClock, content) }
    )
}

@Composable
private fun ColumnScope.LaptopMockup(
    externalModifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = externalModifier.weight(1f, fill = false).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))))
                .padding(2.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black)
                .padding(4.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            Box(Modifier.fillMaxSize()) { content() }
            if (showStatusBar) StatusBar(clock = clock, modifier = Modifier.align(Alignment.TopCenter))
        }

        // Hinge
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(6.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF2E2E2E), Color(0xFF1A1A1A))),
                    RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp),
                )
        )

        // Stand base
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(4.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF3A3A3A), Color(0xFF222222))),
                    RoundedCornerShape(2.dp),
                )
        )
    }
}
