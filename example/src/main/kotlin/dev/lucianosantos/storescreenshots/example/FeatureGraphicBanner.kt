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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0)))
            )
    ) {
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
                    color = Color(0xFF0F172A),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.screenshot_feature_desc),
                    color = Color(0xFF475569),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                )
            }

            // Device family on the right: tablet behind, phone in front, and both a round and a
            // square watch peeking in. Each device is sized by height (the other dimension
            // follows its aspect ratio).
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(end = 20.dp),
            ) {
                DeviceMockup(
                    formFactor = FormFactor.Tablet10,
                    modifier = Modifier
                        .fillMaxHeight(0.82f)
                        .align(Alignment.CenterEnd),
                ) { CounterScreen(count = 42) }

                DeviceMockup(
                    formFactor = FormFactor.Phone,
                    modifier = Modifier
                        .fillMaxHeight(0.74f)
                        .align(Alignment.BottomCenter)
                        .offset(x = 34.dp),
                ) { CounterScreen(count = 42) }

                WatchMockup(
                    shape = WatchShape.Square,
                    modifier = Modifier
                        .fillMaxHeight(0.72f)
                        .align(Alignment.BottomStart),
                ) { WearCounterScreen(count = 42) }

                WatchMockup(
                    shape = WatchShape.Round,
                    modifier = Modifier
                        .fillMaxHeight(0.66f)
                        .align(Alignment.BottomStart)
                        .offset(x = 74.dp),
                ) { WearCounterScreen(count = 42) }
            }
        }
    }
}
