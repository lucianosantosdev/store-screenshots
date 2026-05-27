package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.draw.rotate
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

class PhoneCustomFrameTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test fun custom_frame() = screenshot(
        title = "Swarovski Edition",
        description = "Because your app deserves a jeweled frame on a tripod",
        style = ScreenshotStyle(
            mockupFrame = { content -> SwarovskiFrame { content() } },
        ),
    ) { CounterScreen(count = 42) }
}

@Composable
private fun SwarovskiFrame(content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Phone with jeweled border
        Box(contentAlignment = Alignment.Center) {
            // Outer gold border with gems
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 17f)
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(gold, roseGold, gold, Color(0xFFFFF8DC), gold)
                        )
                    )
                    .padding(12.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                content()
            }

            // Gems on top edge
            Row(Modifier.align(Alignment.TopCenter).offset(y = (-4).dp)) {
                Gem(8.dp, diamond); Spacer(Modifier.width(14.dp))
                Gem(6.dp, ruby); Spacer(Modifier.width(18.dp))
                Gem(10.dp, sapphire); Spacer(Modifier.width(18.dp))
                Gem(6.dp, emerald); Spacer(Modifier.width(14.dp))
                Gem(8.dp, diamond)
            }
            // Gems on bottom edge
            Row(Modifier.align(Alignment.BottomCenter).offset(y = 4.dp)) {
                Gem(6.dp, amethyst); Spacer(Modifier.width(16.dp))
                Gem(10.dp, diamond); Spacer(Modifier.width(20.dp))
                Gem(8.dp, ruby); Spacer(Modifier.width(20.dp))
                Gem(10.dp, diamond); Spacer(Modifier.width(16.dp))
                Gem(6.dp, amethyst)
            }
            // Gems on left edge
            Column(Modifier.align(Alignment.CenterStart).offset(x = (-4).dp)) {
                Gem(7.dp, emerald); Spacer(Modifier.height(20.dp))
                Gem(9.dp, diamond); Spacer(Modifier.height(24.dp))
                Gem(6.dp, sapphire); Spacer(Modifier.height(24.dp))
                Gem(9.dp, diamond); Spacer(Modifier.height(20.dp))
                Gem(7.dp, ruby)
            }
            // Gems on right edge
            Column(Modifier.align(Alignment.CenterEnd).offset(x = 4.dp)) {
                Gem(7.dp, ruby); Spacer(Modifier.height(20.dp))
                Gem(9.dp, diamond); Spacer(Modifier.height(24.dp))
                Gem(6.dp, amethyst); Spacer(Modifier.height(24.dp))
                Gem(9.dp, diamond); Spacer(Modifier.height(20.dp))
                Gem(7.dp, emerald)
            }
            // Corner accent gems
            Gem(12.dp, diamond, Modifier.align(Alignment.TopStart).offset(x = 6.dp, y = 6.dp))
            Gem(12.dp, diamond, Modifier.align(Alignment.TopEnd).offset(x = (-6).dp, y = 6.dp))
            Gem(12.dp, diamond, Modifier.align(Alignment.BottomStart).offset(x = 6.dp, y = (-6).dp))
            Gem(12.dp, diamond, Modifier.align(Alignment.BottomEnd).offset(x = (-6).dp, y = (-6).dp))
        }

        // Tripod
        Spacer(Modifier.height(4.dp))
        // Mount plate
        Box(
            Modifier
                .width(40.dp)
                .height(8.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF444444), Color(0xFF222222))),
                    RoundedCornerShape(2.dp),
                )
        )
        // Tripod legs
        Box(contentAlignment = Alignment.TopCenter) {
            // Center leg
            Box(
                Modifier
                    .width(6.dp)
                    .height(60.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF333333), Color(0xFF1A1A1A))),
                        RoundedCornerShape(2.dp),
                    )
            )
            // Left leg
            Box(
                Modifier
                    .width(5.dp)
                    .height(65.dp)
                    .rotate(-20f)
                    .offset(x = (-20).dp, y = 2.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF333333), Color(0xFF1A1A1A))),
                        RoundedCornerShape(2.dp),
                    )
            )
            // Right leg
            Box(
                Modifier
                    .width(5.dp)
                    .height(65.dp)
                    .rotate(20f)
                    .offset(x = 20.dp, y = 2.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF333333), Color(0xFF1A1A1A))),
                        RoundedCornerShape(2.dp),
                    )
            )
        }
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
                        color.copy(alpha = 0.9f),
                        color,
                        color.copy(alpha = 0.7f),
                    )
                )
            )
            .border(0.5.dp, gold.copy(alpha = 0.6f), CircleShape)
    )
}
