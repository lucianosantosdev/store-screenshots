package dev.lucianosantos.storescreenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.frames.AppleFrame
import dev.lucianosantos.storescreenshots.frames.FramedLayout
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
    check(formFactor != FormFactor.GooglePlayFeatureGraphic) {
        "FormFactor.GooglePlayFeatureGraphic has no built-in frame to preview. " +
            "Preview a feature graphic by annotating your own banner composable with " +
            "@GooglePlayFeatureGraphicScreenshotPreview."
    }
    val customFrame = style.mockupFrame
    if (customFrame != null) {
        FramedLayout(
            title = title,
            description = description,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            style = style,
            horizontalPadding = 28.dp,
            verticalPadding = 48.dp,
            mockup = { externalModifier ->
                Box(externalModifier) { customFrame(content) }
            }
        )
    } else {
        when (formFactor) {
            FormFactor.Phone -> PhoneFrame(title, description, backgroundColor, contentColor, style, content)
            FormFactor.Wear -> WearFrame(backgroundColor, content)
            FormFactor.Tablet7,
            FormFactor.Tablet10 -> TabletFrame(title, description, backgroundColor, contentColor, style, content = content)
            FormFactor.AppleIPhone67 -> AppleFrame(title, description, backgroundColor, contentColor, style, content)
            // Guarded against above; a feature graphic is previewed via its own banner composable.
            FormFactor.GooglePlayFeatureGraphic -> error("unreachable")
        }
    }
}
