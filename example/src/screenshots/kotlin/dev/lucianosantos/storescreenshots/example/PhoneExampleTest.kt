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
class PhoneExampleTest {

    @get:Rule
    val screenshot = ScreenshotRule(FormFactor.Phone)

    @Test
    @Screenshot(
        title = "Count anything, anywhere",
        description = "A focused tap counter that gets out of your way",
    )
    fun counter() = screenshot.capture {
        CounterScreen(count = 42)
    }
}
