package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.GlassEffect
import dev.lucianosantos.storescreenshots.GlassReflexStyle
import dev.lucianosantos.storescreenshots.GlassShadow
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotStyle

val styledScreenshotStyle = ScreenshotStyle(
    mockupPosition = MockupPosition.Middle,
    mockupOffset = DpOffset(x = 100.dp, y = 32.dp),
    mockupRotation = -5f,
    titleFontFamily = FontFamily.Serif,
    descriptionFontFamily = FontFamily.Monospace,
    background = { MarketingBackground() },
    title = { text -> StyledTitle(text) },
    description = { text -> StyledDescription(text) },
)

/**
 * A perspective 3D tilt: the device turns on its Y axis, tips a little on X, and spins slightly on
 * Z — the look of a marketing hero shot, all from [ScreenshotStyle]'s `mockupRotationY` /
 * `mockupRotationX` / `mockupRotation`.
 *
 * Any X or Y tilt draws the device as a solid body rather than a flat card, so this also shows the
 * parts that only exist once it has real thickness: the side rail the turn exposes, the buttons
 * standing on it, and the shadow cast from the tilted silhouette. The glass is a plain
 * [GlassEffect] — it is drawn on the screen and warped along with it, and the renderer adds its own
 * sheen on top that follows where the light falls on a surface at this angle.
 */
val perspectiveScreenshotStyle = ScreenshotStyle(
    mockupPosition = MockupPosition.Middle,
    mockupOffset = DpOffset(x = 24.dp, y = 0.dp),
    mockupRotationY = -26f,
    mockupRotationX = 8f,
    mockupRotation = -6f,
    mockupCameraDistance = 12f,
    mockupElevation = 18.dp,
    screenGlass = GlassEffect(
        reflexStyle = GlassReflexStyle.Wedge,
        reflexAngle = -32f,
        reflexPosition = 0.55f,
        reflexWidth = 0.5f,
        reflexAlpha = 0.20f,
        shadow = GlassShadow.BottomLeft,
        shadowAlpha = 0.22f,
    ),
    background = { MarketingBackground() },
    title = { text -> StyledTitle(text) },
    description = { text -> StyledDescription(text) },
)

@Composable
fun MarketingBackground() {
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
fun StyledTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.4f),
                offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                blurRadius = 8f,
            ),
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun StyledDescription(text: String) {
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
