package dev.lucianosantos.storescreenshots.example

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.DeviceImageMockup
import dev.lucianosantos.storescreenshots.DeviceKind
import dev.lucianosantos.storescreenshots.GlassReflexStyle
import dev.lucianosantos.storescreenshots.GlassShadow
import dev.lucianosantos.storescreenshots.Screen
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import dev.lucianosantos.storescreenshots.screenGlass
import org.junit.Test

class DeviceImageMockupTest : StoreScreenshotsTest(FormFactor.GooglePlayFeatureGraphic) {

    // The mockup fits inside the banner by height; scale/translate it with normal Modifiers
    // (`Modifier.scale(…)` to zoom, `Modifier.offset(…)` to move) if you want to fill more or reframe.

    // Three different screens placed onto the three detected mockups (left → right).
    @Test
    fun device_image_trio() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFFEFEFEF)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(
                frame = frame("phone_mockup_trio.jpg"),
                screens = listOf(
                    // Distinct counts and background colours — three independent screens, not one repeated.
                    Screen { CounterScreen(count = 7, background = Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1E3A8A)))) },
                    Screen { CounterScreen(count = 42) },
                    Screen { CounterScreen(count = 99, background = Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFF881337)))) },
                ),
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }

    // A render with a GRAY (not white) empty screen — detection keys off the dark bezel, not colour.
    @Test
    fun device_image_podium() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFFF3DAD2)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(frame("phone_mockup_podium.jpg"), screens = listOf(Screen { CounterScreen(count = 42) }), modifier = Modifier.fillMaxHeight())
        }
    }

    // A strongly-tilted tablet on a dark background — detection still finds the single screen.
    @Test
    fun device_image_tablet() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFF2B2B30)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(
                frame("tablet_mockup_keyboard.jpg"),
                // kind = Tablet lays the UI out at tablet size (not phone size) and orients to the image.
                screens = listOf(Screen(DeviceKind.Tablet) { CounterScreen(count = 42) }),
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }

    // CHROMA KEY: a 3D render whose empty screen is painted flat green, on a transparent background so
    // it drops straight onto a colourful marketing banner. Instead of bezel detection we pass
    // `screenColor` — detection locks onto the green blob and ignores body, bezel and background.
    @Test
    fun device_image_chroma() = customScreenshot {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF4338CA), Color(0xFF7C3AED), Color(0xFFDB2777))
                    )
                )
        ) {
            // Soft decorative circles for a marketing feel.
            Box(
                Modifier
                    .size(260.dp)
                    .offset(x = (-70).dp, y = (-90).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
            )
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(340.dp)
                    .offset(x = 130.dp, y = 150.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                // Marketing copy on the left.
                Column(Modifier.weight(1f).padding(start = 48.dp, end = 16.dp)) {
                    Text(
                        text = stringResource(R.string.screenshot_chroma_title),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 38.sp,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.screenshot_chroma_desc),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                    )
                }
                // The chroma-keyed phone, composited onto the gradient via its transparent render.
                Box(Modifier.weight(0.95f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    DeviceImageMockup(
                        frame("phone_chroma.png"),
                        screens = listOf(Screen { CounterScreen(count = 42) }),
                        modifier = Modifier.fillMaxHeight(0.96f),
                        screenColor = Color.Green,
                        screenColorTolerance = 0.2f,
                    )
                }
            }
        }
    }

    // CHROMA KEY on a PHOTO: a real photograph of a smartwatch whose screen was painted flat green.
    // The opaque photo is the banner itself; chroma keying drops the live watch UI onto the screen.
    @Test
    fun device_image_watch_chroma() = customScreenshot {
        Box(Modifier.fillMaxSize().clipToBounds(), contentAlignment = Alignment.Center) {
            DeviceImageMockup(
                frame("watch_chroma.png"),
                // kind = Watch orients by the longer edge (taller than wide) even worn at an angle.
                screens = listOf(Screen(DeviceKind.Watch) { WearCounterScreen(count = 42) }),
                modifier = Modifier.fillMaxWidth(),
                screenColor = Color.Red,
                screenColorTolerance = 0.2f,
            )
        }
    }

    // CHROMA KEY across a DEVICE FAMILY: one transparent render of a desktop, laptop, phone and tablet
    // whose screens are all painted flat red. A single `screenColor` pass finds all four (ordered
    // left-to-right) and drops an independent live UI onto each — distinct counts AND background colours.
    @Test
    fun device_image_four_devices_chroma() = customScreenshot {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF0EA5E9))
                    )
                )
        ) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(300.dp)
                    .offset(x = 90.dp, y = (-100).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Column(Modifier.align(Alignment.TopStart).padding(start = 44.dp, top = 24.dp, end = 44.dp)) {
                Text(
                    text = stringResource(R.string.screenshot_four_devices_title),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.screenshot_four_devices_desc),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.fillMaxWidth(0.5f),
                )
            }
            DeviceImageMockup(
                frame("four_devices_chroma.png"),
                // Four independent screens (left-to-right) — each tagged with its device kind, which sets
                // the orientation (landscape laptop/desktop, portrait phone/tablet) and native width —
                // and given a different count AND background colour.
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

    // Two devices (watch + phone). Scaled up and nudged down so both fill the banner with the watch in frame.
    @Test
    fun device_image_watch_phone() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFF6B533A)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(
                frame = frame("watch_phone_mockup.jpg"),
                // Different count and background per device — the watch and phone run independent UIs.
                // A glass sheen sits in front of each screen; it is warped into the device plane with
                // the rest of the content, so the reflex rakes across the glass at the device's angle.
                screens = listOf(
                    Screen(DeviceKind.Watch) {
                        Box(
                            Modifier.fillMaxSize().screenGlass(
                                reflexAngle = 30f,
                                reflexPosition = 0.34f,
                                reflexWidth = 0.32f,
                                reflexAlpha = 0.22f,
                                shadow = GlassShadow.None,
                            )
                        ) { WearCounterScreen(count = 7, background = Brush.verticalGradient(listOf(Color(0xFF059669), Color(0xFF064E3B)))) }
                    },
                    Screen(DeviceKind.Phone) {
                        Box(
                            Modifier.fillMaxSize().screenGlass(
                                reflexAngle = 40f,
                                reflexPosition = 0.6f,
                                reflexWidth = 0.5f,
                                reflexStyle = GlassReflexStyle.Wedge,
                                reflexAlpha = 0.50f,
                                shadow = GlassShadow.BottomLeft,
                                shadowAlpha = 0.16f,
                            )
                        ) { CounterScreen(count = 42) }
                    },
                ),
                modifier = Modifier.fillMaxHeight().scale(1.5f).offset(x = 24.dp, y = 36.dp),
            )
        }
    }
}

/** Loads a device-frame image from `src/screenshots/resources/mockups/` (test classpath, not shipped). */
private fun frame(name: String): ImageBitmap =
    DeviceImageMockupTest::class.java.classLoader!!.getResourceAsStream("mockups/$name").use {
        BitmapFactory.decodeStream(it).asImageBitmap()
    }
