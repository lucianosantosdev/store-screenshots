package dev.lucianosantos.storescreenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
 * Pass a [ScreenshotStyle] to customize mockup position, font family, or to swap in your own
 * composables for the background, title, or description.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = StoreScreenshotsStubApplication::class)
abstract class StoreScreenshotsTest(
    formFactor: FormFactor,
    style: ScreenshotStyle = ScreenshotStyle(),
) {

    @get:Rule
    val screenshot: ScreenshotRule = ScreenshotRule(formFactor, style)

    /**
     * Render [content] inside the form-factor frame and capture per-locale PNGs.
     *
     * @param title Raw string headline. Ignored when [titleRes] is set.
     * @param description Raw string sub-headline. Ignored when [descriptionRes] is set.
     * @param titleRes String resource ID (`R.string.xxx`). Resolved per locale automatically.
     * @param descriptionRes Same as [titleRes] for the description.
     * @param fileName Output PNG name (without locale path). Defaults to the test method name.
     * @param style Override the class-level style for just this screenshot.
     */
    fun screenshot(
        locales: List<String> = listOf("en-US"),
        title: String = "",
        description: String = "",
        titleRes: Int = 0,
        descriptionRes: Int = 0,
        backgroundColor: Color = Color(0xFF1F2937),
        contentColor: Color = Color.White,
        fileName: String? = null,
        style: ScreenshotStyle = screenshot.style,
        content: @Composable () -> Unit,
    ) = screenshot.screenshot(locales, title, description, titleRes, descriptionRes, backgroundColor, contentColor, fileName, style, content)

    /**
     * Fully custom layout — you control everything. [ScreenshotScope] provides a `Mockup`
     * composable for the device bezel; place it wherever you want.
     *
     * ```kotlin
     * @Test fun custom() = screenshot {
     *     Box(Modifier.fillMaxSize().background(Color.Red)) {
     *         Text("Custom title")
     *         Mockup { HomeScreen() }
     *     }
     * }
     * ```
     */
    fun customScreenshot(
        locales: List<String> = listOf("en-US"),
        fileName: String? = null,
        content: @Composable ScreenshotScope.() -> Unit,
    ) = screenshot.customScreenshot(locales, fileName, content)

    /**
     * Render one continuous layout [panels] screenshots wide and slice it into that many PNGs that
     * complement each other in the store (a background, headline, or device row can flow across the
     * seams). Slices are written `{name}_01.png`, `{name}_02.png`, … left to right. See
     * [ScreenshotRule.splitScreenshot].
     *
     * ```kotlin
     * @Test fun story() = splitScreenshot(panels = 3) {
     *     Row(Modifier.fillMaxSize()) { repeat(3) { Box(Modifier.weight(1f)) { /* … */ } } }
     * }
     * ```
     */
    fun splitScreenshot(
        panels: Int,
        locales: List<String> = listOf("en-US"),
        fileName: String? = null,
        content: @Composable ScreenshotScope.() -> Unit,
    ) = screenshot.splitScreenshot(panels, locales, fileName, content)
}
