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
import dev.lucianosantos.storescreenshots.DeviceKind
import dev.lucianosantos.storescreenshots.Screen
import dev.lucianosantos.storescreenshots.DeviceMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.GlassReflexStyle
import dev.lucianosantos.storescreenshots.GlassShadow
import dev.lucianosantos.storescreenshots.GooglePlayFeatureGraphicScreenshotPreview
import dev.lucianosantos.storescreenshots.PhoneScreenshotPreview
import dev.lucianosantos.storescreenshots.ScreenshotPreview
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.GlassEffect
import dev.lucianosantos.storescreenshots.screenGlass
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

// --- Glass effect previews (mirror PhoneGlassExampleTest). These render the same framed output as
// the generated PNGs, so the reflex + shadow read identically in the IDE preview pane. ---

@PhoneScreenshotPreview
@Composable
fun GlassDefaultPreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Behind the glass",
    description = "A default reflex + bottom-right shadow — just Modifier.screenGlass()",
) { CounterScreen(count = 42, modifier = Modifier.screenGlass()) }

@PhoneScreenshotPreview
@Composable
fun GlassCustomPreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Tune the reflex",
    description = "Steeper reflex near the top, shadow anchored to the left edge",
) {
    CounterScreen(
        count = 42,
        modifier = Modifier.screenGlass(
            reflexAngle = 40f,
            reflexPosition = 0.2f,
            reflexWidth = 0.22f,
            reflexAlpha = 0.24f,
            shadow = GlassShadow.Left,
            shadowAlpha = 0.18f,
        ),
    )
}

@PhoneScreenshotPreview
@Composable
fun GlassViaStylePreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Glass for the whole run",
    description = "Set ScreenshotStyle.screenGlass once — every mockup gets it",
    style = ScreenshotStyle(screenGlass = GlassEffect()),
) { CounterScreen(count = 42) }

@PhoneScreenshotPreview
@Composable
fun GlassWedgeAPreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Wedge reflex",
    description = "A glossy diagonal filling from the center out to the border",
) {
    CounterScreen(
        count = 42,
        modifier = Modifier.screenGlass(
            reflexAngle = -32f,
            reflexPosition = 0.55f,
            reflexWidth = 0.5f,
            reflexStyle = GlassReflexStyle.Wedge,
            reflexAlpha = 0.20f,
            shadow = GlassShadow.BottomLeft,
            shadowAlpha = 0.20f,
        ),
    )
}

@PhoneScreenshotPreview
@Composable
fun GlassWedgeBPreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Wedge — sharper",
    description = "A crisp diagonal split through the center, no shadow",
) {
    CounterScreen(
        count = 42,
        modifier = Modifier.screenGlass(
            reflexAngle = -60f,
            reflexPosition = 0.5f,
            reflexWidth = 0.01f,
            reflexStyle = GlassReflexStyle.Wedge,
            reflexAlpha = 0.30f,
            shadow = GlassShadow.None,
        ),
    )
}

@PhoneScreenshotPreview
@Composable
fun GlassWedgeCPreview() = ScreenshotPreview(
    formFactor = FormFactor.Phone,
    title = "Wedge — softer",
    description = "A broader, gentler sheen from a steeper diagonal",
) {
    CounterScreen(
        count = 42,
        modifier = Modifier.screenGlass(
            reflexAngle = 40f,
            reflexPosition = 0.6f,
            reflexWidth = 0.5f,
            reflexStyle = GlassReflexStyle.Wedge,
            reflexAlpha = 0.50f,
            shadow = GlassShadow.BottomLeft,
            shadowAlpha = 0.16f,
        ),
    )
}

// --- DeviceImageMockup previews (mirror DeviceImageMockupTest). Debug-only: the device-frame images
// live in the screenshots source set and are exposed to debug via a resources srcDir in build.gradle. ---

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun DeviceImageTrioPreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFFEFEFEF)), contentAlignment = Alignment.Center) {
        DeviceImageMockup(
            frame = mockupFrame("phone_mockup_trio.jpg"),
            screens = listOf(
                Screen { CounterScreen(count = 7, background = Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1E3A8A)))) },
                Screen { CounterScreen(count = 42) },
                Screen { CounterScreen(count = 99, background = Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFF881337)))) },
            ),
            modifier = Modifier.fillMaxHeight(),
        )
    }
}

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun DeviceImagePodiumPreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFFF3DAD2)), contentAlignment = Alignment.Center) {
        DeviceImageMockup(mockupFrame("phone_mockup_podium.jpg"), screens = listOf(Screen { CounterScreen(count = 42) }), modifier = Modifier.fillMaxHeight())
    }
}

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun DeviceImageTabletPreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFF2B2B30)), contentAlignment = Alignment.Center) {
        DeviceImageMockup(
            mockupFrame("tablet_mockup_keyboard.jpg"),
            screens = listOf(Screen(DeviceKind.Tablet) { CounterScreen(count = 42) }),
            modifier = Modifier.fillMaxHeight(),
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
                    screens = listOf(Screen { CounterScreen(count = 42) }),
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
            screens = listOf(Screen(DeviceKind.Watch) { WearCounterScreen(count = 42) }),
            modifier = Modifier.fillMaxWidth(),
            screenColor = Color.Red,
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
            screens = listOf(
                Screen(DeviceKind.Watch) { WearCounterScreen(count = 7, background = Brush.verticalGradient(listOf(Color(0xFF059669), Color(0xFF064E3B)))) },
                Screen(DeviceKind.Phone) { CounterScreen(count = 42) },
            ),
            modifier = Modifier.fillMaxHeight().scale(1.5f).offset(x = 24.dp, y = 36.dp),
        )
    }
}

@GooglePlayFeatureGraphicScreenshotPreview
@Composable
fun DeviceImageFourDevicesChromaPreview() {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF0EA5E9)))
        )
    ) {
        Box(Modifier.align(Alignment.TopEnd).size(300.dp).offset(x = 90.dp, y = (-100).dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)))
        Column(Modifier.fillMaxWidth().padding(start = 48.dp, top = 30.dp, end = 40.dp)) {
            Text(stringResource(R.string.screenshot_four_devices_title), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.screenshot_four_devices_desc), color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 19.sp, modifier = Modifier.fillMaxWidth(0.62f))
        }
        DeviceImageMockup(
            mockupFrame("four_devices_chroma.png"),
            screens = listOf(
                Screen(DeviceKind.Laptop) { CounterScreen(count = 512, background = Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1E3A8A)))) },
                Screen(DeviceKind.Desktop) { CounterScreen(count = 1024, background = Brush.verticalGradient(listOf(Color(0xFF059669), Color(0xFF064E3B)))) },
                Screen(DeviceKind.Phone) { CounterScreen(count = 7) },
                Screen(DeviceKind.Tablet) { CounterScreen(count = 42, background = Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFF7C2D12)))) },
            ),
            modifier = Modifier.align(Alignment.BottomEnd).fillMaxHeight(0.74f),
            screenColor = Color.Red,
            screenColorTolerance = 0.25f,
        )
    }
}

/** Loads a device-frame image from the debug classpath (src/screenshots/resources/mockups). */
private fun mockupFrame(name: String): ImageBitmap =
    object {}.javaClass.classLoader!!.getResourceAsStream("mockups/$name").use {
        BitmapFactory.decodeStream(it).asImageBitmap()
    }
