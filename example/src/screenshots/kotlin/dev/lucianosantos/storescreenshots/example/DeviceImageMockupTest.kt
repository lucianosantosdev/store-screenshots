package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.DeviceImageMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class DeviceImageMockupTest : StoreScreenshotsTest(FormFactor.Phone) {

    // Three different screens placed onto the three detected mockups (left → right).
    @Test
    fun device_image_trio() = customScreenshot {
        val frame = ImageBitmap.imageResource(R.drawable.phone_mockup_trio)
        Box(Modifier.fillMaxSize().background(Color(0xFFEFEFEF)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(
                frame = frame,
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
        val frame = ImageBitmap.imageResource(R.drawable.phone_mockup_podium)
        Box(Modifier.fillMaxSize().background(Color(0xFFF3DAD2)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(frame, screens = listOf { CounterScreen(count = 42) }, modifier = Modifier.fillMaxSize())
        }
    }

    // A strongly-tilted tablet on a dark background — detection still finds the single screen.
    @Test
    fun device_image_tablet() = customScreenshot {
        val frame = ImageBitmap.imageResource(R.drawable.tablet_mockup_keyboard)
        Box(Modifier.fillMaxSize().background(Color(0xFF2B2B30)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(frame, screens = listOf { CounterScreen(count = 42) }, modifier = Modifier.fillMaxSize())
        }
    }

    // Two devices (watch + phone), each its own screen — watch on the left, phone on the right.
    @Test
    fun device_image_watch_phone() = customScreenshot {
        val frame = ImageBitmap.imageResource(R.drawable.watch_phone_mockup)
        Box(Modifier.fillMaxSize().background(Color(0xFF6B533A)), contentAlignment = Alignment.Center) {
            DeviceImageMockup(
                frame = frame,
                screens = listOf({ WearCounterScreen(count = 42) }, { CounterScreen(count = 42) }),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    // All references in one image: every device render filled with live content via auto-detection.
    @Test
    fun device_image_showcase() = customScreenshot {
        Column(
            Modifier.fillMaxSize().background(Color(0xFF14161C)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Cell(Modifier.fillMaxWidth().weight(1.1f)) {
                DeviceImageMockup(
                    frame = ImageBitmap.imageResource(R.drawable.phone_mockup_trio),
                    screens = listOf({ CounterScreen(7) }, { CounterScreen(42) }, { CounterScreen(99) }),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Row(Modifier.fillMaxWidth().weight(1.6f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Cell(Modifier.weight(1f)) {
                    DeviceImageMockup(
                        frame = ImageBitmap.imageResource(R.drawable.phone_mockup_podium),
                        screens = listOf { CounterScreen(42) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Cell(Modifier.weight(1f)) {
                    DeviceImageMockup(
                        frame = ImageBitmap.imageResource(R.drawable.watch_phone_mockup),
                        screens = listOf({ WearCounterScreen(42) }, { CounterScreen(42) }),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Cell(Modifier.fillMaxWidth().weight(1f)) {
                DeviceImageMockup(
                    frame = ImageBitmap.imageResource(R.drawable.tablet_mockup_keyboard),
                    screens = listOf { CounterScreen(42) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun Cell(modifier: Modifier, content: @Composable () -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) { content() }
}
