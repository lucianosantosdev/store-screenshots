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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .background(Color(0xFF0A0A0F))
    ) {
        // Layer 1: diagonal purple accent stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .offset(x = (-40).dp, y = 520.dp)
                .rotate(-12f)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF6D28D9), Color(0xFF7C3AED), Color(0xFF8B5CF6))
                    ),
                    RoundedCornerShape(24.dp),
                )
        )

        // Layer 2: device mockup (drawn before text so text stays on top)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.85f)
                .offset(x = 60.dp, y = 80.dp)
                .rotate(12f)
        ) {
            mockup()
        }

        // Layer 3: text (drawn last = on top of everything)
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
                    modifier = Modifier.background(Color.White).padding(horizontal = 12.dp),
                    text = "TEMPLATE",
                    color = Color(0xFF8B5CF6),
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
                            .background(Color(0xFF8B5CF6), RoundedCornerShape(1.dp))
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "BY STORE-SCREENSHOTS",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                    )
                }
            }
        }
    }
}
