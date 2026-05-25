package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        mockup = { externalModifier -> PhoneMockup(externalModifier, content) }
    )
}

@Composable
private fun ColumnScope.PhoneMockup(
    externalModifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = externalModifier
            .weight(1f, fill = false)
            .fillMaxWidth()
            .aspectRatio(9f / 18f)
    ) {
        SideButton(
            modifier = Modifier.align(Alignment.TopStart).offset(x = (-3).dp, y = 110.dp),
            heightDp = 38,
            isLeft = true
        )
        SideButton(
            modifier = Modifier.align(Alignment.TopStart).offset(x = (-3).dp, y = 156.dp),
            heightDp = 58,
            isLeft = true
        )
        SideButton(
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = 92.dp),
            heightDp = 70,
            isLeft = false
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(42.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))))
                .padding(1.5.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color.Black)
                .padding(7.dp)
                .clip(RoundedCornerShape(32.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize()) { content() }
            StatusBar(modifier = Modifier.align(Alignment.TopCenter))
            CameraNotch(modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun SideButton(modifier: Modifier, heightDp: Int, isLeft: Boolean) {
    val shape = if (isLeft) {
        RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)
    } else {
        RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
    }
    Box(
        modifier = modifier
            .size(width = 5.dp, height = heightDp.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (isLeft) listOf(Color(0xFF0F0F0F), Color(0xFF2E2E2E))
                    else listOf(Color(0xFF2E2E2E), Color(0xFF0F0F0F))
                ),
                shape = shape
            )
    )
}

@Composable
private fun StatusBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "10:28", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.SignalCellular4Bar, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Icon(Icons.Filled.Wifi, null, tint = Color.White, modifier = Modifier.size(14.dp))
            BatteryIcon()
        }
    }
}

@Composable
private fun BatteryIcon() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 22.dp, height = 11.dp)
                .border(1.dp, Color.White, RoundedCornerShape(3.dp))
                .padding(1.5.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .fillMaxHeight()
                    .background(Color.White, RoundedCornerShape(1.dp))
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 1.dp)
                .width(1.5.dp)
                .height(5.dp)
                .background(Color.White, RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
        )
    }
}

@Composable
private fun CameraNotch(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(top = 7.dp)
            .size(width = 90.dp, height = 24.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
            .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(50))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF2E2E2E))))
                .border(0.5.dp, Color(0xFF333333), RoundedCornerShape(50))
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF1F2A40), Color(0xFF0A0F1A), Color.Black))
                )
                .border(0.5.dp, Color(0xFF2A2A2A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF0D1422), Color.Black)))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp, top = 2.dp)
                    .size(width = 3.dp, height = 2.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
            )
        }
    }
}
