package dev.lucianosantos.storescreenshots

import androidx.compose.runtime.Composable
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Base class that bundles the boilerplate every screenshot test needs:
 *
 * - `@RunWith(RobolectricTestRunner::class)` for Robolectric.
 * - `@GraphicsMode(NATIVE)` so Compose renders to a real bitmap.
 * - `@Config(sdk = [35])` for a recent Android API.
 * - A `@get:Rule` [ScreenshotRule] wired to the form factor passed to the constructor.
 * - A `capture { … }` shortcut so test bodies stay one line.
 *
 * Usage:
 *
 * ```kotlin
 * @Config(application = MyStubApplication::class)
 * class HomeScreenshots : StoreScreenshotsTest(FormFactor.Phone) {
 *
 *     @Test
 *     @Screenshot(title = "Welcome", description = "Sign in or create an account")
 *     fun home() = capture { HomeScreen() }
 * }
 * ```
 *
 * Subclasses still declare `@Config(application = …)` themselves because the stub Application
 * class is project-specific.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = StoreScreenshotsStubApplication::class)
abstract class StoreScreenshotsTest(formFactor: FormFactor) {

    @get:Rule
    val screenshot: ScreenshotRule = ScreenshotRule(formFactor)

    /** Render [content] inside the form-factor frame and capture per-locale PNGs. */
    fun capture(content: @Composable () -> Unit) = screenshot.capture(content)
}
