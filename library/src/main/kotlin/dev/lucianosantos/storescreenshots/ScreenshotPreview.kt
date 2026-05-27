package dev.lucianosantos.storescreenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.lucianosantos.storescreenshots.frames.AppleFrame
import dev.lucianosantos.storescreenshots.frames.PhoneFrame
import dev.lucianosantos.storescreenshots.frames.TabletFrame
import dev.lucianosantos.storescreenshots.frames.WearFrame

/**
 * Renders the exact same framed layout that [ScreenshotRule.capture] produces — title,
 * description, device mockup, background, and [ScreenshotStyle] overrides. Use this inside
 * a `@Preview` so the IDE preview matches the PNG output pixel-for-pixel.
 *
 * ```kotlin
 * // Preview — shows the framed screenshot in Android Studio
 * @Preview(widthDp = 411, heightDp = 914)
 * @Composable
 * fun HomePreview() = ScreenshotPreview(
 *     formFactor = FormFactor.Phone,
 *     title = "Welcome home",
 *     description = "Sign in to get started",
 * ) { HomeScreen() }
 *
 * // Test — generates the identical PNG
 * @Test @Screenshot(title = "Welcome home", description = "Sign in to get started")
 * fun home() = capture { HomeScreen() }
 * ```
 *
 * To avoid duplicating the title/description strings, extract them into `const val`s
 * that both `@Preview` and `@Screenshot` reference.
 */
@Composable
fun ScreenshotPreview(
    formFactor: FormFactor,
    title: String = "",
    description: String = "",
    backgroundColor: Color = Color(0xFF1F2937),
    contentColor: Color = Color.White,
    style: ScreenshotStyle = ScreenshotStyle(),
    content: @Composable () -> Unit,
) {
    when (formFactor) {
        FormFactor.Phone -> PhoneFrame(title, description, backgroundColor, contentColor, style, content)
        FormFactor.Wear -> WearFrame(backgroundColor, content)
        FormFactor.Tablet7,
        FormFactor.Tablet10 -> TabletFrame(title, description, backgroundColor, contentColor, style, content = content)
        FormFactor.AppleIPhone67 -> AppleFrame(title, description, backgroundColor, contentColor, style, content)
    }
}
