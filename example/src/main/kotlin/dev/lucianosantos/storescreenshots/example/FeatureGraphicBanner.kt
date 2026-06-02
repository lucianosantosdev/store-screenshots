package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.DeviceMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.WatchMockup
import dev.lucianosantos.storescreenshots.WatchShape

/**
 * Example Google Play feature graphic: a 1024x500 promotional banner that shows the app on a
 * family of devices. There is no built-in frame for [FormFactor.GooglePlayFeatureGraphic] — you
 * compose the banner yourself and drop a [DeviceMockup] for each form factor your app supports.
 */
@Composable
fun FeatureGraphicBanner() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF083344), Color(0xFF0E7490), Color(0xFF0EA5E9))
                )
            )
    ) {
        // Soft decorative circles for a marketing feel.
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-60).dp, y = (-80).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(320.dp)
                .offset(x = 120.dp, y = 130.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Marketing copy on the left.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 44.dp, end = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.screenshot_feature_title),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.screenshot_feature_desc),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                )
            }

            // Device family, arranged on a shared baseline and ascending in size left-to-right:
            // the two watches sit at the front-left, the phone in the middle, and the tablet
            // large and mostly unobscured on the right. Each device is sized by height (the other
            // dimension follows its aspect ratio); drawn back-to-front so the tablet stays behind.
            Box(
                modifier = Modifier
                    .weight(1.45f)
                    .fillMaxHeight()
                    .padding(end = 16.dp, bottom = 18.dp),
            ) {
                DeviceMockup(
                    formFactor = FormFactor.Tablet10,
                    modifier = Modifier
                        .fillMaxHeight(0.84f)
                        .align(Alignment.BottomEnd),
                ) { CounterScreen(count = 42) }

                DeviceMockup(
                    formFactor = FormFactor.Phone,
                    modifier = Modifier
                        .fillMaxHeight(0.66f)
                        .align(Alignment.BottomStart)
                        .offset(x = 104.dp),
                ) { CounterScreen(count = 42) }

                WatchMockup(
                    shape = WatchShape.Square,
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .align(Alignment.BottomStart),
                ) { WearCounterScreen(count = 42) }

                WatchMockup(
                    shape = WatchShape.Round,
                    modifier = Modifier
                        .fillMaxHeight(0.46f)
                        .align(Alignment.BottomStart)
                        .offset(x = 50.dp),
                ) { WearCounterScreen(count = 42) }
            }
        }
    }
}
