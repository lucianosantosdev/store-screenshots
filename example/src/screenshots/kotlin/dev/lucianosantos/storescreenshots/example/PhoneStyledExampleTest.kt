package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/**
 * Demonstrates every customization point on [ScreenshotStyle]:
 *
 * - `MockupPosition.Middle` — title above, device centered, description below.
 * - `fontFamily = FontFamily.Serif` — affects the default Text composables when no override is set.
 * - `background` — replaces the flat `backgroundColor` with a gradient + decorative blobs.
 * - `title` / `description` — render with custom typography (shadows, larger size, monospace).
 */
class PhoneStyledExampleTest : StoreScreenshotsTest(
    formFactor = FormFactor.Phone,
    style = ScreenshotStyle(
        mockupPosition = MockupPosition.Middle,
        fontFamily = FontFamily.Serif,
        background = { MarketingBackground() },
        title = { text -> StyledTitle(text) },
        description = { text -> StyledDescription(text) },
    ),
) {

    @Test
    @Screenshot(
        title = "Designed your way",
        description = "Custom fonts · gradient backgrounds · centered devices · all from one ScreenshotStyle",
    )
    fun counter_styled() = capture { CounterScreen(count = 42) }
}

@Composable
private fun MarketingBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFBBF24), Color(0xFFEF4444), Color(0xFF7C3AED))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(280.dp)
                .offset(x = 80.dp, y = 100.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
    }
}

@Composable
private fun StyledTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            shadow = Shadow(color = Color.Black.copy(alpha = 0.4f), offset = androidx.compose.ui.geometry.Offset(0f, 4f), blurRadius = 8f),
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StyledDescription(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.95f),
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}
