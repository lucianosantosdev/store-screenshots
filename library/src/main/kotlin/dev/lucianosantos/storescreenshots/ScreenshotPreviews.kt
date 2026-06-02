package dev.lucianosantos.storescreenshots

import androidx.compose.ui.tooling.preview.Preview

/**
 * Pre-configured `@Preview` annotations matching each [FormFactor]'s dimensions.
 * Use instead of `@Preview(widthDp = …, heightDp = …)` so the preview canvas
 * matches the generated screenshot pixel-for-pixel.
 *
 * ```kotlin
 * @PhoneScreenshotPreview
 * @Composable
 * fun HomePreview() = ScreenshotPreview(FormFactor.Phone, title = "…") { HomeScreen() }
 * ```
 */

@Preview(name = "Phone (1080×1920)", widthDp = 411, heightDp = 914)
annotation class PhoneScreenshotPreview

@Preview(name = "Wear (384×384)", widthDp = 227, heightDp = 227)
annotation class WearScreenshotPreview

@Preview(name = "Tablet 7\" (1200×1920)", widthDp = 600, heightDp = 960)
annotation class Tablet7ScreenshotPreview

@Preview(name = "Tablet 10\" (1600×2560)", widthDp = 800, heightDp = 1280)
annotation class Tablet10ScreenshotPreview

@Preview(name = "iPhone 6.7\" (1290×2796)", widthDp = 430, heightDp = 932)
annotation class AppleIPhone67ScreenshotPreview

@Preview(name = "Feature Graphic (1024×500)", widthDp = 512, heightDp = 250)
annotation class GooglePlayFeatureGraphicScreenshotPreview

/**
 * Multi-preview: renders all form factors at once. Useful for quick visual checks
 * but produces a large preview panel.
 */
@PhoneScreenshotPreview
@WearScreenshotPreview
@Tablet7ScreenshotPreview
@Tablet10ScreenshotPreview
@AppleIPhone67ScreenshotPreview
@GooglePlayFeatureGraphicScreenshotPreview
annotation class AllScreenshotPreviews
