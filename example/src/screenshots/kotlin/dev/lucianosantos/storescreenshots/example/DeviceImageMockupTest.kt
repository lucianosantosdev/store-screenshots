package dev.lucianosantos.storescreenshots.example

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.DeviceImageMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
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
                    { CounterScreen(count = 7) },
                    { CounterScreen(count = 42) },
                    { CounterScreen(count = 99) },
                ),
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }

    // A render with a GRAY (not white) empty screen — detection keys off the dark bezel, not colour.
    @Test
    fun device_image_podium() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFFF3DAD2)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(frame("phone_mockup_podium.jpg"), screens = listOf { CounterScreen(count = 42) }, modifier = Modifier.fillMaxHeight())
        }
    }

    // A strongly-tilted tablet on a dark background — detection still finds the single screen.
    @Test
    fun device_image_tablet() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFF2B2B30)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(
                frame("tablet_mockup_keyboard.jpg"),
                screens = listOf { CounterScreen(count = 42) },
                modifier = Modifier.fillMaxHeight(),
                screenNativeWidth = 760.dp, // lay the UI out at tablet size, not phone size
            )
        }
    }

    // CHROMA KEY: a 3D render whose empty screen is painted flat green. Instead of bezel detection we
    // pass `screenColor` — detection locks onto the green blob and ignores body, bezel and background.
    @Test
    fun device_image_chroma() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFFE8E8E8)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(
                frame("phone_chroma.jpg"),
                screens = listOf { CounterScreen(count = 42) },
                modifier = Modifier.fillMaxHeight(),
                screenColor = Color.Green,
                screenColorTolerance = 0.2f,
            )
        }
    }

    // Two devices (watch + phone). Scaled up and nudged down so both fill the banner with the watch in frame.
    @Test
    fun device_image_watch_phone() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFF6B533A)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(
                frame = frame("watch_phone_mockup.jpg"),
                screens = listOf({ WearCounterScreen(count = 42) }, { CounterScreen(count = 42) }),
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
