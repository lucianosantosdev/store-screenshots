package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class PhoneCustomFrameTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test fun custom_frame() = screenshot(
        title = "Custom device frame",
        description = "Your own bezel composable, library handles the rest",
        style = ScreenshotStyle(
            mockupFrame = { content ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(32.dp))
                        .border(4.dp, Color(0xFF06B6D4), RoundedCornerShape(32.dp))
                        .background(Color.Black)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(28.dp))
                ) { content() }
            },
        ),
    ) { CounterScreen(count = 42) }
}
