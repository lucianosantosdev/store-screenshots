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
class WearExampleTest {

    @get:Rule
    val screenshot = ScreenshotRule(FormFactor.Wear)

    @Test
    @Screenshot(backgroundColor = 0xFF000000)
    fun counter() = screenshot.capture {
        WearCounterScreen(count = 42)
    }
}
