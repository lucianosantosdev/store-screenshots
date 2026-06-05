package dev.lucianosantos.storescreenshots.example

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.DeviceImageMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class DeviceImageMockupTest : StoreScreenshotsTest(FormFactor.Phone) {

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
                modifier = Modifier.fillMaxSize().padding(16.dp),
            )
        }
    }

    // A render with a GRAY (not white) empty screen — detection keys off the dark bezel, not colour.
    @Test
    fun device_image_podium() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFFF3DAD2)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(frame("phone_mockup_podium.jpg"), screens = listOf { CounterScreen(count = 42) }, modifier = Modifier.fillMaxSize())
        }
    }

    // A strongly-tilted tablet on a dark background — detection still finds the single screen.
    @Test
    fun device_image_tablet() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFF2B2B30)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(frame("tablet_mockup_keyboard.jpg"), screens = listOf { CounterScreen(count = 42) }, modifier = Modifier.fillMaxSize())
        }
    }

    // Two devices (watch + phone), each its own screen — watch on the left, phone on the right.
    @Test
    fun device_image_watch_phone() = customScreenshot {
        Box(Modifier.fillMaxSize().background(Color(0xFF6B533A)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(
                frame = frame("watch_phone_mockup.jpg"),
                screens = listOf({ WearCounterScreen(count = 42) }, { CounterScreen(count = 42) }),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Loads a device-frame image from `src/screenshots/resources/mockups/` (test classpath, not shipped). */
private fun frame(name: String): ImageBitmap =
    DeviceImageMockupTest::class.java.classLoader!!.getResourceAsStream("mockups/$name").use {
        BitmapFactory.decodeStream(it).asImageBitmap()
    }
