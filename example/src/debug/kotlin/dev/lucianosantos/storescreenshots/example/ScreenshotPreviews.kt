package dev.lucianosantos.storescreenshots.example

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.AppleIPhone67ScreenshotPreview
import dev.lucianosantos.storescreenshots.DeviceImageMockup
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

@PhoneScreenshotPreview
@Composable
fun PerspectivePreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Tilt it in 3D",
    description = "Perspective XYZ rotation — just mockupRotationX / Y / Z on ScreenshotStyle",
    style = perspectiveScreenshotStyle,
) { CounterScreen(count = 42) }

// --- DeviceImageMockup previews (mirror DeviceImageMockupTest). Debug-only: the device-frame images
// live in the screenshots source set and are exposed to debug via a resources srcDir in build.gradle. ---

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun DeviceImageTrioPreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFFEFEFEF)), contentAlignment = Alignment.Center) {
        DeviceImageMockup(
            frame = mockupFrame("phone_mockup_trio.jpg"),
            screens = listOf({ CounterScreen(count = 7) }, { CounterScreen(count = 42) }, { CounterScreen(count = 99) }),
            modifier = Modifier.fillMaxHeight(),
        )
    }
}

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun DeviceImagePodiumPreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFFF3DAD2)), contentAlignment = Alignment.Center) {
        DeviceImageMockup(mockupFrame("phone_mockup_podium.jpg"), screens = listOf { CounterScreen(count = 42) }, modifier = Modifier.fillMaxHeight())
    }
}

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun DeviceImageTabletPreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFF2B2B30)), contentAlignment = Alignment.Center) {
        DeviceImageMockup(
            mockupFrame("tablet_mockup_keyboard.jpg"),
            screens = listOf { CounterScreen(count = 42) },
            modifier = Modifier.fillMaxHeight(),
            screenNativeWidth = 760.dp,
        )
    }
}

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun DeviceImageChromaPreview() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF7C3AED), Color(0xFFDB2777)))
        )
    ) {
        Box(Modifier.size(260.dp).offset(x = (-70).dp, y = (-90).dp).clip(CircleShape).background(Color.White.copy(alpha = 0.10f)))
        Box(Modifier.align(Alignment.BottomEnd).size(340.dp).offset(x = 130.dp, y = 150.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)))
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(start = 48.dp, end = 16.dp)) {
                Text(stringResource(R.string.screenshot_chroma_title), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.screenshot_chroma_desc), color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp, lineHeight = 21.sp)
            }
            Box(Modifier.weight(0.95f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                DeviceImageMockup(
                    mockupFrame("phone_chroma.png"),
                    screens = listOf { CounterScreen(count = 42) },
                    modifier = Modifier.fillMaxHeight(0.96f),
                    screenColor = Color.Green,
                    screenColorTolerance = 0.2f,
                )
            }
        }
    }
}

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun DeviceImageWatchChromaPreview() {
    Box(Modifier.fillMaxSize().clipToBounds(), contentAlignment = Alignment.Center) {
        DeviceImageMockup(
            mockupFrame("watch_chroma.png"),
            screens = listOf { WearCounterScreen(count = 42) },
            modifier = Modifier.fillMaxWidth(),
            screenNativeWidth = 200.dp,
            screenColor = Color.Green,
            screenColorTolerance = 0.2f,
        )
    }
}

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun DeviceImageWatchPhonePreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFF6B533A)), contentAlignment = Alignment.Center) {
        DeviceImageMockup(
            frame = mockupFrame("watch_phone_mockup.jpg"),
            screens = listOf({ WearCounterScreen(count = 42) }, { CounterScreen(count = 42) }),
            modifier = Modifier.fillMaxHeight().scale(1.5f).offset(x = 24.dp, y = 36.dp),
        )
    }
}

/** Loads a device-frame image from the debug classpath (src/screenshots/resources/mockups). */
private fun mockupFrame(name: String): ImageBitmap =
    object {}.javaClass.classLoader!!.getResourceAsStream("mockups/$name").use {
        BitmapFactory.decodeStream(it).asImageBitmap()
    }
