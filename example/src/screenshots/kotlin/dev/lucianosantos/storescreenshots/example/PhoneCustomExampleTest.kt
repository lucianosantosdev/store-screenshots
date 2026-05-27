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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class PhoneCustomExampleTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test fun custom_layout() = customScreenshot {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0F))
        ) {
            // Diagonal purple accent stripe behind the mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .offset(x = (-40).dp, y = 520.dp)
                    .rotate(-12f)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6D28D9),
                                Color(0xFF7C3AED),
                                Color(0xFF8B5CF6),
                            )
                        ),
                        RoundedCornerShape(24.dp),
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 28.dp, end = 28.dp, top = 64.dp, bottom = 40.dp),
            ) {
                // Bold headline with accent color on last word
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White)) { append("STORE\n") }
                        withStyle(SpanStyle(color = Color.White)) { append("SCREENSHOTS\n") }
                        withStyle(SpanStyle(color = Color(0xFF8B5CF6))) { append("TEMPLATE.") }
                    },
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 46.sp,
                )

                Spacer(Modifier.height(32.dp))

                // Device mockup — rotated and offset to crop into the right edge
                Mockup(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .offset(x = 60.dp, y = 20.dp)
                        .rotate(12f)
                ) { CounterScreen(count = 42) }

                Spacer(Modifier.height(24.dp))

                // Footer with thin line
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
}
