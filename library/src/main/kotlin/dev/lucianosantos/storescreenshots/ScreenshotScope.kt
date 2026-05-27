package dev.lucianosantos.storescreenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Scope for fully custom screenshot layouts. Provides a [Mockup] composable that renders
 * the device bezel for the current form factor — you control everything else (background,
 * title, description, positioning).
 *
 * ```kotlin
 * @Test fun custom() = screenshot {
 *     Box(Modifier.fillMaxSize().background(Color.Red)) {
 *         Text("My custom title", Modifier.align(Alignment.TopCenter))
 *         Mockup(Modifier.align(Alignment.Center)) { HomeScreen() }
 *     }
 * }
 * ```
 */
class ScreenshotScope internal constructor(
    private val formFactor: FormFactor,
    private val style: ScreenshotStyle,
) {
    /**
     * Renders the device bezel (phone/tablet/watch/iPhone frame) with [content] inside.
     * Place it wherever you want in your custom layout.
     */
    @Composable
    fun Mockup(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) = DeviceMockup(
        formFactor = formFactor,
        modifier = modifier,
        showStatusBar = style.showStatusBar,
        statusBarClock = style.statusBarClock,
        content = content,
    )
}
