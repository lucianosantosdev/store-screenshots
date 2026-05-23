package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.Screenshot
import dev.lucianosantos.storescreenshots.ScreenshotRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = StubApplication::class)
class AppleExampleTest {

    @get:Rule
    val screenshot = ScreenshotRule(FormFactor.AppleIPhone67)

    @Test
    @Screenshot(
        title = "Ship cross-store",
        description = "App Store Connect 6.7\" size, ready to upload",
    )
    fun counter() = screenshot.capture {
        CounterScreen(count = 42)
    }
}
