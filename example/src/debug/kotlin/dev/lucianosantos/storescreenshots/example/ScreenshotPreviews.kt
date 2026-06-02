package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.AppleIPhone67ScreenshotPreview
import dev.lucianosantos.storescreenshots.DeviceMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.GooglePlayFeatureGraphicScreenshotPreview
import dev.lucianosantos.storescreenshots.PhoneScreenshotPreview
import dev.lucianosantos.storescreenshots.ScreenshotPreview
import dev.lucianosantos.storescreenshots.Tablet10ScreenshotPreview
import dev.lucianosantos.storescreenshots.Tablet7ScreenshotPreview
import dev.lucianosantos.storescreenshots.WatchMockup
import dev.lucianosantos.storescreenshots.WatchShape
import dev.lucianosantos.storescreenshots.WearScreenshotPreview

@PhoneScreenshotPreview
@Composable
fun PhonePreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Count anything, anywhere",
    description = "A focused tap counter that gets out of your way",
) { CounterScreen(count = 42) }

@WearScreenshotPreview
@Composable
fun WearPreview() = ScreenshotPreview(
    formFactor = FormFactor.Wear,
    backgroundColor = Color.Black,
) { WearCounterScreen(count = 42) }

@Tablet7ScreenshotPreview
@Composable
fun Tablet7Preview() = ScreenshotPreview(
    formFactor = FormFactor.Tablet7,
    title = "Built for every screen",
    description = "The same Compose UI, framed for 7-inch tablets",
) { CounterScreen(count = 42) }

@Tablet10ScreenshotPreview
@Composable
fun Tablet10Preview() = ScreenshotPreview(
    formFactor = FormFactor.Tablet10,
    title = "Big screen, same code",
    description = "10-inch layout uses identical Compose UI",
) { CounterScreen(count = 42) }

@AppleIPhone67ScreenshotPreview
@Composable
fun ApplePreview() = ScreenshotPreview(
    formFactor = FormFactor.AppleIPhone67,
    title = "Ship cross-store",
    description = "App Store Connect 6.7\" size, ready to upload",
) { CounterScreen(count = 42) }

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun FeatureGraphicPreview() = FeatureGraphicBanner()

@Preview(name = "Watch — Round & Square", widthDp = 280, heightDp = 150)
@Composable
fun WatchMockupPreview() {
    Row(
        modifier = Modifier.fillMaxSize().background(Color(0xFFE2E8F0)),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WatchMockup(WatchShape.Round, Modifier.fillMaxHeight(0.8f)) { WearCounterScreen(count = 42) }
        WatchMockup(WatchShape.Square, Modifier.fillMaxHeight(0.8f)) { WearCounterScreen(count = 42) }
    }
}

@PhoneScreenshotPreview
@Composable
fun StyledPreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Designed your way",
    description = "Custom fonts · gradient backgrounds · centered devices · all from one ScreenshotStyle",
    style = styledScreenshotStyle,
) { CounterScreen(count = 42) }

@PhoneScreenshotPreview
@Composable
fun CustomLayoutPreview() = CustomScreenshotLayout {
    DeviceMockup(formFactor = FormFactor.Phone) { CounterScreen(count = 42) }
}
