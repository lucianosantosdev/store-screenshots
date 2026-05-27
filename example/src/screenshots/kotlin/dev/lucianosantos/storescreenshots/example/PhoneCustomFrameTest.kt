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
                .padding(14.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.Black)
                .padding(3.dp)
                .clip(RoundedCornerShape(28.dp))
        ) {
            content()
        }

        // ---- TOP EDGE ----
        Gem(6.dp, diamond, Modifier.align(Alignment.TopCenter).offset(x = (-100).dp, y = (-3).dp))
        Gem(5.dp, ruby, Modifier.align(Alignment.TopCenter).offset(x = (-78).dp, y = (-2).dp))
        Gem(7.dp, sapphire, Modifier.align(Alignment.TopCenter).offset(x = (-54).dp, y = (-3).dp))
        Gem(5.dp, emerald, Modifier.align(Alignment.TopCenter).offset(x = (-32).dp, y = (-2).dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.TopCenter).offset(x = (-10).dp, y = (-4).dp))
        Gem(6.dp, topaz, Modifier.align(Alignment.TopCenter).offset(x = 12.dp, y = (-2).dp))
        Gem(5.dp, pink, Modifier.align(Alignment.TopCenter).offset(x = 34.dp, y = (-3).dp))
        Gem(7.dp, aqua, Modifier.align(Alignment.TopCenter).offset(x = 56.dp, y = (-2).dp))
        Gem(5.dp, ruby, Modifier.align(Alignment.TopCenter).offset(x = 78.dp, y = (-3).dp))
        Gem(6.dp, diamond, Modifier.align(Alignment.TopCenter).offset(x = 100.dp, y = (-2).dp))

        // ---- BOTTOM EDGE ----
        Gem(6.dp, diamond, Modifier.align(Alignment.BottomCenter).offset(x = (-100).dp, y = 3.dp))
        Gem(5.dp, amethyst, Modifier.align(Alignment.BottomCenter).offset(x = (-78).dp, y = 2.dp))
        Gem(7.dp, ruby, Modifier.align(Alignment.BottomCenter).offset(x = (-54).dp, y = 3.dp))
        Gem(5.dp, topaz, Modifier.align(Alignment.BottomCenter).offset(x = (-32).dp, y = 2.dp))
        Gem(8.dp, diamond, Modifier.align(Alignment.BottomCenter).offset(x = (-10).dp, y = 4.dp))
        Gem(6.dp, sapphire, Modifier.align(Alignment.BottomCenter).offset(x = 12.dp, y = 2.dp))
        Gem(5.dp, emerald, Modifier.align(Alignment.BottomCenter).offset(x = 34.dp, y = 3.dp))
        Gem(7.dp, pink, Modifier.align(Alignment.BottomCenter).offset(x = 56.dp, y = 2.dp))
        Gem(5.dp, aqua, Modifier.align(Alignment.BottomCenter).offset(x = 78.dp, y = 3.dp))
        Gem(6.dp, diamond, Modifier.align(Alignment.BottomCenter).offset(x = 100.dp, y = 2.dp))

        // ---- LEFT EDGE ----
        Gem(5.dp, emerald, Modifier.align(Alignment.CenterStart).offset(x = (-3).dp, y = (-200).dp))
        Gem(7.dp, diamond, Modifier.align(Alignment.CenterStart).offset(x = (-4).dp, y = (-170).dp))
        Gem(5.dp, ruby, Modifier.align(Alignment.CenterStart).offset(x = (-3).dp, y = (-140).dp))
        Gem(6.dp, sapphire, Modifier.align(Alignment.CenterStart).offset(x = (-3).dp, y = (-110).dp))
        Gem(5.dp, diamond, Modifier.align(Alignment.CenterStart).offset(x = (-4).dp, y = (-80).dp))
        Gem(7.dp, topaz, Modifier.align(Alignment.CenterStart).offset(x = (-3).dp, y = (-50).dp))
        Gem(5.dp, pink, Modifier.align(Alignment.CenterStart).offset(x = (-3).dp, y = (-20).dp))
        Gem(6.dp, diamond, Modifier.align(Alignment.CenterStart).offset(x = (-4).dp, y = 10.dp))
        Gem(5.dp, amethyst, Modifier.align(Alignment.CenterStart).offset(x = (-3).dp, y = 40.dp))
        Gem(7.dp, aqua, Modifier.align(Alignment.CenterStart).offset(x = (-3).dp, y = 70.dp))
        Gem(5.dp, diamond, Modifier.align(Alignment.CenterStart).offset(x = (-4).dp, y = 100.dp))
        Gem(6.dp, ruby, Modifier.align(Alignment.CenterStart).offset(x = (-3).dp, y = 130.dp))
        Gem(5.dp, emerald, Modifier.align(Alignment.CenterStart).offset(x = (-3).dp, y = 160.dp))
        Gem(7.dp, diamond, Modifier.align(Alignment.CenterStart).offset(x = (-4).dp, y = 190.dp))

        // ---- RIGHT EDGE ----
        Gem(5.dp, ruby, Modifier.align(Alignment.CenterEnd).offset(x = 3.dp, y = (-200).dp))
        Gem(7.dp, diamond, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = (-170).dp))
        Gem(5.dp, emerald, Modifier.align(Alignment.CenterEnd).offset(x = 3.dp, y = (-140).dp))
        Gem(6.dp, amethyst, Modifier.align(Alignment.CenterEnd).offset(x = 3.dp, y = (-110).dp))
        Gem(5.dp, diamond, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = (-80).dp))
        Gem(7.dp, pink, Modifier.align(Alignment.CenterEnd).offset(x = 3.dp, y = (-50).dp))
        Gem(5.dp, sapphire, Modifier.align(Alignment.CenterEnd).offset(x = 3.dp, y = (-20).dp))
        Gem(6.dp, diamond, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 10.dp))
        Gem(5.dp, topaz, Modifier.align(Alignment.CenterEnd).offset(x = 3.dp, y = 40.dp))
        Gem(7.dp, aqua, Modifier.align(Alignment.CenterEnd).offset(x = 3.dp, y = 70.dp))
        Gem(5.dp, diamond, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 100.dp))
        Gem(6.dp, emerald, Modifier.align(Alignment.CenterEnd).offset(x = 3.dp, y = 130.dp))
        Gem(5.dp, ruby, Modifier.align(Alignment.CenterEnd).offset(x = 3.dp, y = 160.dp))
        Gem(7.dp, diamond, Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 190.dp))

        // ---- CORNERS — large statement gems ----
        Gem(14.dp, diamond, Modifier.align(Alignment.TopStart).offset(x = 4.dp, y = 4.dp))
        Gem(14.dp, diamond, Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp))
        Gem(14.dp, diamond, Modifier.align(Alignment.BottomStart).offset(x = 4.dp, y = (-4).dp))
        Gem(14.dp, diamond, Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp))

        // ---- SPARKLE highlights — tiny white dots scattered on the border ----
        Sparkle(Modifier.align(Alignment.TopCenter).offset(x = (-65).dp, y = 5.dp))
        Sparkle(Modifier.align(Alignment.TopCenter).offset(x = 45.dp, y = 7.dp))
        Sparkle(Modifier.align(Alignment.BottomCenter).offset(x = (-40).dp, y = (-6).dp))
        Sparkle(Modifier.align(Alignment.BottomCenter).offset(x = 70.dp, y = (-5).dp))
        Sparkle(Modifier.align(Alignment.CenterStart).offset(x = 6.dp, y = (-95).dp))
        Sparkle(Modifier.align(Alignment.CenterStart).offset(x = 5.dp, y = 55.dp))
        Sparkle(Modifier.align(Alignment.CenterEnd).offset(x = (-6).dp, y = (-60).dp))
        Sparkle(Modifier.align(Alignment.CenterEnd).offset(x = (-5).dp, y = 120.dp))
        Sparkle(Modifier.align(Alignment.TopStart).offset(x = 18.dp, y = 18.dp))
        Sparkle(Modifier.align(Alignment.TopEnd).offset(x = (-18).dp, y = 18.dp))
        Sparkle(Modifier.align(Alignment.BottomStart).offset(x = 18.dp, y = (-18).dp))
        Sparkle(Modifier.align(Alignment.BottomEnd).offset(x = (-18).dp, y = (-18).dp))
    }
}

@Composable
private fun Gem(size: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.9f),
                        color.copy(alpha = 0.8f),
                        color,
                        color.copy(alpha = 0.6f),
                    ),
                    radius = size.value * 2.5f,
                )
            )
            .border(0.5.dp, gold.copy(alpha = 0.8f), CircleShape)
    ) {
        // Inner sparkle highlight
        Box(
            Modifier
                .size(size * 0.35f)
                .offset(x = size * 0.15f, y = size * 0.1f)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
        )
    }
}

@Composable
private fun Sparkle(modifier: Modifier) {
    Box(
        modifier = modifier
            .size(3.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.85f))
    )
}
