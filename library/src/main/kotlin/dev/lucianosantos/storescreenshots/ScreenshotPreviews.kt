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

// `@Preview` arguments must be compile-time constants, so these annotations cannot derive their
// canvas from FormFactor.qualifiers the way FormFactor.widthPx does — the sizes have to be written
// out a second time. These constants are that second copy, in one place and held against the
// form factors by FormFactorSizeTest, so a preview cannot quietly stop matching the PNG it stands
// in for. Every label is computed from them for the same reason: both the phone and wear labels
// had already drifted to sizes the library has never emitted.

internal const val PhonePreviewWidthDp = 414
internal const val PhonePreviewHeightDp = 736

internal const val WearPreviewWidthDp = 227
internal const val WearPreviewHeightDp = 227

internal const val Tablet7PreviewWidthDp = 600
internal const val Tablet7PreviewHeightDp = 960

internal const val Tablet10PreviewWidthDp = 800
internal const val Tablet10PreviewHeightDp = 1280

internal const val AppleIPhone67PreviewWidthDp = 430
internal const val AppleIPhone67PreviewHeightDp = 932

internal const val AppleIPhone65PreviewWidthDp = 428
internal const val AppleIPhone65PreviewHeightDp = 926

internal const val AppleIPad13PreviewWidthDp = 1024
internal const val AppleIPad13PreviewHeightDp = 1366

internal const val FeatureGraphicPreviewWidthDp = 1536
internal const val FeatureGraphicPreviewHeightDp = 750

@Preview(
    name = "Phone (${PhonePreviewWidthDp * 3}×${PhonePreviewHeightDp * 3})",
    widthDp = PhonePreviewWidthDp,
    heightDp = PhonePreviewHeightDp,
)
annotation class PhoneScreenshotPreview

@Preview(
    name = "Wear (${WearPreviewWidthDp * 3}×${WearPreviewHeightDp * 3})",
    widthDp = WearPreviewWidthDp,
    heightDp = WearPreviewHeightDp,
)
annotation class WearScreenshotPreview

@Preview(
    name = "Tablet 7\" (${Tablet7PreviewWidthDp * 2}×${Tablet7PreviewHeightDp * 2})",
    widthDp = Tablet7PreviewWidthDp,
    heightDp = Tablet7PreviewHeightDp,
)
annotation class Tablet7ScreenshotPreview

@Preview(
    name = "Tablet 10\" (${Tablet10PreviewWidthDp * 2}×${Tablet10PreviewHeightDp * 2})",
    widthDp = Tablet10PreviewWidthDp,
    heightDp = Tablet10PreviewHeightDp,
)
annotation class Tablet10ScreenshotPreview

@Preview(
    name = "iPhone 6.7\" (${AppleIPhone67PreviewWidthDp * 3}×${AppleIPhone67PreviewHeightDp * 3})",
    widthDp = AppleIPhone67PreviewWidthDp,
    heightDp = AppleIPhone67PreviewHeightDp,
)
annotation class AppleIPhone67ScreenshotPreview

@Preview(
    name = "iPhone 6.5\" (${AppleIPhone65PreviewWidthDp * 3}×${AppleIPhone65PreviewHeightDp * 3})",
    widthDp = AppleIPhone65PreviewWidthDp,
    heightDp = AppleIPhone65PreviewHeightDp,
)
annotation class AppleIPhone65ScreenshotPreview

@Preview(
    name = "iPad 13\" (${AppleIPad13PreviewWidthDp * 2}×${AppleIPad13PreviewHeightDp * 2})",
    widthDp = AppleIPad13PreviewWidthDp,
    heightDp = AppleIPad13PreviewHeightDp,
)
annotation class AppleIPad13ScreenshotPreview

// Rendered at 1.5× the 1024×500 output so the short banner isn't dwarfed by the phone/tablet
// previews beside it — see the note above. The exported PNG is unaffected (that comes from the test).
@Preview(
    name = "Feature Graphic (1024×500)",
    widthDp = FeatureGraphicPreviewWidthDp,
    heightDp = FeatureGraphicPreviewHeightDp,
)
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
