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
 *
 * Exception: [GooglePlayFeatureGraphicScreenshotPreview] renders at 1.5× its 1024×500 output rather
 * than pixel-for-pixel. At native size the short banner looks tiny next to the phone/tablet previews
 * in a shared preview panel; the feature graphic is resolution-independent (proportional layout), so
 * scaling it up keeps it legible and comparable without changing the exported PNG.
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

@Preview(name = "iPhone 6.5\" (1284×2778)", widthDp = 428, heightDp = 926)
annotation class AppleIPhone65ScreenshotPreview

@Preview(name = "iPad 13\" (2048×2732)", widthDp = 1024, heightDp = 1366)
annotation class AppleIPad13ScreenshotPreview

// Rendered at 1.5× the 1024×500 output so the short banner isn't dwarfed by the phone/tablet
// previews beside it — see the note above. The exported PNG is unaffected (that comes from the test).
@Preview(name = "Feature Graphic (1024×500)", widthDp = 1536, heightDp = 750)
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
@AppleIPhone65ScreenshotPreview
@AppleIPad13ScreenshotPreview
@GooglePlayFeatureGraphicScreenshotPreview
annotation class AllScreenshotPreviews
