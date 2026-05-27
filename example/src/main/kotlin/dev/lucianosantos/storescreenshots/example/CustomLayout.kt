package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomScreenshotLayout(
    mockup: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A1A),
                        Color(0xFF0F0A2A),
                        Color(0xFF150D35),
                        Color(0xFF0A0A1A),
                    )
                )
            )
    ) {
        // Ambient glow blobs
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7C3AED).copy(alpha = 0.3f),
                            Color.Transparent,
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 100.dp, y = 60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3B82F6).copy(alpha = 0.25f),
                            Color.Transparent,
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = 40.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFEC4899).copy(alpha = 0.2f),
                            Color.Transparent,
                        )
                    )
                )
        )

        // Diagonal purple accent stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .offset(x = (-40).dp, y = 520.dp)
                .rotate(-12f)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF6D28D9).copy(alpha = 0.8f),
                            Color(0xFF7C3AED).copy(alpha = 0.6f),
                            Color(0xFF8B5CF6).copy(alpha = 0.4f),
                        )
                    ),
                    RoundedCornerShape(24.dp),
                )
        )

        // Device mockup (drawn before text so text stays on top)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.85f)
                .offset(x = 60.dp, y = 80.dp)
                .rotate(12f)
        ) {
            mockup()
        }

        // Text layer (drawn last = on top)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 28.dp, end = 28.dp, top = 64.dp, bottom = 40.dp),
        ) {
            Text(
                text = "STORE\nSCREENSHOTS",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                lineHeight = 46.sp,
            )
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    text = "TEMPLATE",
                    color = Color(0xFF6D28D9),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 46.sp,
                )
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.15f)
                            .height(2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color(0xFF8B5CF6), Color.Transparent)
                                ),
                                RoundedCornerShape(1.dp),
                            )
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "BY STORE-SCREENSHOTS",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                    )
                }
            }
        }
    }
}
