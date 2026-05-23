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
class Tablet7ExampleTest {

    @get:Rule
    val screenshot = ScreenshotRule(FormFactor.Tablet7)

    @Test
    @Screenshot(
        title = "Built for every screen",
        description = "The same Compose UI, framed for 7-inch tablets",
    )
    fun counter() = screenshot.capture {
        CounterScreen(count = 42)
    }
}
