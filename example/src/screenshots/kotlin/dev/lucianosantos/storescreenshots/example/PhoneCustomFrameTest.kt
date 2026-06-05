package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

private val gold = Color(0xFFD4AF37)
private val roseGold = Color(0xFFB76E79)
private val diamond = Color(0xFFE8F0FE)
private val ruby = Color(0xFFE0115F)
private val sapphire = Color(0xFF0F52BA)
private val emerald = Color(0xFF50C878)
private val amethyst = Color(0xFF9966CC)
private val topaz = Color(0xFFFFC87C)
private val pink = Color(0xFFFF69B4)
private val aqua = Color(0xFF7FFFD4)

class PhoneCustomFrameTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test fun custom_frame() = screenshot(
        title = "Swarovski Edition",
        description = "Because your app deserves a jeweled frame",
        style = ScreenshotStyle(
            mockupFrame = { content -> SwarovskiFrame { content() } },
        ),
    ) { CounterScreen(count = 42) }
}

@Composable
private fun SwarovskiFrame(content: @Composable () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        // Thick gold/rose-gold border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 18f)
                .clip(RoundedCornerShape(40.dp))
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            gold, Color(0xFFFFF8DC), roseGold, gold,
                            Color(0xFFFFE4B5), roseGold, Color(0xFFFFF8DC), gold,
                        )
                    )
                )
                .padding(16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.Black)
                .padding(3.dp)
                .clip(RoundedCornerShape(26.dp))
        ) {
            content()
        }

        // ---- TOP EDGE — gems inset into the gold border, centered on one line ----
        Gem(6.dp, diamond, Modifier.align(Alignment.TopCenter))
        Gem(6.dp, ruby, Modifier.align(Alignment.TopCenter).offset(x = (-72).dp))
        Gem(6.dp, sapphire, Modifier.align(Alignment.TopCenter).offset(x = (-48).dp))
        Gem(6.dp, emerald, Modifier.align(Alignment.TopCenter).offset(x = (-26).dp))
        Gem(6.dp, diamond, Modifier.align(Alignment.TopCenter).offset(x = 0.dp))
        Gem(6.dp, topaz, Modifier.align(Alignment.TopCenter).offset(x = 26.dp))
        Gem(6.dp, pink, Modifier.align(Alignment.TopCenter).offset(x = 48.dp))
        Gem(6.dp, aqua, Modifier.align(Alignment.TopCenter).offset(x = 72.dp))
        Gem(6.dp, ruby, Modifier.align(Alignment.TopCenter).offset(x = 95.dp))

        // ---- BOTTOM EDGE ----
        Gem(6.dp, amethyst, Modifier.align(Alignment.BottomCenter).offset(x = (-95).dp))
        Gem(6.dp, diamond, Modifier.align(Alignment.BottomCenter).offset(x = (-72).dp))
        Gem(6.dp, ruby, Modifier.align(Alignment.BottomCenter).offset(x = (-48).dp))
        Gem(6.dp, topaz, Modifier.align(Alignment.BottomCenter).offset(x = (-26).dp))
        Gem(6.dp, diamond, Modifier.align(Alignment.BottomCenter).offset(x = 0.dp))
        Gem(6.dp, sapphire, Modifier.align(Alignment.BottomCenter).offset(x = 26.dp))
        Gem(6.dp, emerald, Modifier.align(Alignment.BottomCenter).offset(x = 48.dp))
        Gem(6.dp, pink, Modifier.align(Alignment.BottomCenter).offset(x = 72.dp))
        Gem(6.dp, aqua, Modifier.align(Alignment.BottomCenter).offset(x = 95.dp))

        // ---- LEFT EDGE ----
        Gem(8.dp, emerald, Modifier.align(Alignment.CenterStart).offset(y = (-200).dp, x = (-4).dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.CenterStart).offset(y = (-170).dp, x = (-4).dp))
        Gem(8.dp, ruby, Modifier.align(Alignment.CenterStart).offset(y = (-140).dp, x = (-4).dp))
        Gem(8.dp, sapphire, Modifier.align(Alignment.CenterStart).offset(y = (-110).dp, x = (-4).dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.CenterStart).offset(y = (-80).dp, x = (-4).dp))
        Gem(8.dp, topaz, Modifier.align(Alignment.CenterStart).offset(y = (-50).dp, x = (-4).dp))
        Gem(8.dp, pink, Modifier.align(Alignment.CenterStart).offset(y = (-20).dp, x = (-4).dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.CenterStart).offset(y = 10.dp, x = (-4).dp))
        Gem(8.dp, amethyst, Modifier.align(Alignment.CenterStart).offset(y = 40.dp, x = (-4).dp))
        Gem(8.dp, aqua, Modifier.align(Alignment.CenterStart).offset(y = 70.dp, x = (-4).dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.CenterStart).offset(y = 100.dp, x = (-4).dp))
        Gem(8.dp, ruby, Modifier.align(Alignment.CenterStart).offset(y = 130.dp, x = (-4).dp))
        Gem(8.dp, emerald, Modifier.align(Alignment.CenterStart).offset(y = 160.dp, x = (-4).dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.CenterStart).offset(y = 190.dp, x = (-4).dp))

        // ---- RIGHT EDGE ----
        Gem(8.dp, ruby, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = (-200).dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = (-170).dp))
        Gem(8.dp, emerald, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = (-140).dp))
        Gem(8.dp, amethyst, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = (-110).dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = (-80).dp))
        Gem(8.dp, pink, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = (-50).dp))
        Gem(8.dp, sapphire, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = (-20).dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 10.dp))
        Gem(8.dp, topaz, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 40.dp))
        Gem(8.dp, aqua, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 70.dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 100.dp))
        Gem(8.dp, emerald, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 130.dp))
        Gem(8.dp, ruby, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 160.dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 190.dp))

        // ---- CORNERS — large statement gems ----
        Gem(10.dp, diamond, Modifier.align(Alignment.TopStart).offset(x = 4.dp, y = 4.dp))
        Gem(10.dp, diamond, Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp))
        Gem(10.dp, diamond, Modifier.align(Alignment.BottomStart).offset(x = 4.dp, y = (-4).dp))
        Gem(10.dp, diamond, Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp))
    }
}

@Composable
private fun Gem(size: Dp, color: Color, modifier: Modifier = Modifier) {
    val glowSize = size * 3f
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Radial glow around the gem
        Box(
            Modifier
                .size(glowSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.5f),
                            color.copy(alpha = 0.2f),
                            Color.Transparent,
                        )
                    )
                )
        )
        // Gem body
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color.White.copy(alpha = 0.85f),
                            color.copy(alpha = 0.7f),
                            color,
                            color.copy(alpha = 0.5f),
                        ),
                        radius = size.value * 3f,
                    )
                )
                .border(0.5.dp, gold.copy(alpha = 0.7f), CircleShape)
        ) {
            // Inner sparkle highlight
            Box(
                Modifier
                    .size(size * 0.3f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f))
            )
        }
    }
}
