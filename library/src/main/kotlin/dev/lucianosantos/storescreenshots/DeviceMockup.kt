package dev.lucianosantos.storescreenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.frames.StatusBar
import dev.lucianosantos.storescreenshots.frames.WearFrame

/**
 * Renders just the device bezel/frame for the given [formFactor] — no title, no description,
 * no background banner. Use inside a fully custom layout via `screenshot { Mockup { … } }`.
 */
@Composable
fun DeviceMockup(
    formFactor: FormFactor,
    modifier: Modifier = Modifier,
    showStatusBar: Boolean = true,
    statusBarClock: String = "12:00",
    content: @Composable () -> Unit,
) {
    when (formFactor) {
        FormFactor.Phone -> PhoneBezel(modifier.fillMaxWidth().aspectRatio(9f / 18f), showStatusBar, statusBarClock, content)
        FormFactor.Wear -> WearBezel(modifier.size(227.dp), content)
        FormFactor.Tablet7 -> TabletBezel(modifier.fillMaxWidth().aspectRatio(3f / 4f), showStatusBar, statusBarClock, content)
        FormFactor.Tablet10 -> TabletBezel(modifier.fillMaxWidth().aspectRatio(3f / 4f), showStatusBar, statusBarClock, content)
        FormFactor.AppleIPhone67 -> AppleBezel(modifier.fillMaxHeight().aspectRatio(1290f / 2796f), showStatusBar, statusBarClock, content)
    }
}

@Composable
private fun PhoneBezel(
    modifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        // Side buttons
        SideButton(Modifier.align(Alignment.TopStart).offset(x = (-3).dp, y = 110.dp), 38, true)
        SideButton(Modifier.align(Alignment.TopStart).offset(x = (-3).dp, y = 156.dp), 58, true)
        SideButton(Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = 92.dp), 70, false)

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
            Box(Modifier.fillMaxSize()) { content() }
            if (showStatusBar) StatusBar(clock, Modifier.align(Alignment.TopCenter))
            CameraNotch(Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun WearBezel(modifier: Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black)
    ) {
        content()
    }
}

@Composable
private fun TabletBezel(
    modifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))))
            .padding(2.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.Black)
            .padding(8.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(Modifier.fillMaxSize()) { content() }
        if (showStatusBar) StatusBar(clock, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun AppleBezel(
    modifier: Modifier,
    showStatusBar: Boolean,
    clock: String,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
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
            Box(Modifier.fillMaxSize()) { content() }
            if (showStatusBar) StatusBar(clock, Modifier.align(Alignment.TopCenter))
            DynamicIsland(Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun SideButton(modifier: Modifier, heightDp: Int, isLeft: Boolean) {
    val shape = if (isLeft) RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)
    else RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
    Box(
        modifier = modifier
            .size(width = 5.dp, height = heightDp.dp)
            .background(
                Brush.horizontalGradient(
                    if (isLeft) listOf(Color(0xFF0F0F0F), Color(0xFF2E2E2E))
                    else listOf(Color(0xFF2E2E2E), Color(0xFF0F0F0F))
                ),
                shape
            )
    )
}

@Composable
private fun CameraNotch(modifier: Modifier) {
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
            Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(50))
                .background(Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF2E2E2E))))
                .border(0.5.dp, Color(0xFF333333), RoundedCornerShape(50))
        )
        Box(
            Modifier.size(12.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF1F2A40), Color(0xFF0A0F1A), Color.Black)))
                .border(0.5.dp, Color(0xFF2A2A2A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFF0D1422), Color.Black))))
            Box(Modifier.align(Alignment.TopStart).padding(start = 2.dp, top = 2.dp).size(width = 3.dp, height = 2.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)))
        }
    }
}

@Composable
private fun DynamicIsland(modifier: Modifier) {
    Box(
        modifier = modifier
            .padding(top = 12.dp)
            .size(width = 124.dp, height = 36.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
            .border(0.5.dp, Color(0xFF222222), RoundedCornerShape(50))
    )
}
