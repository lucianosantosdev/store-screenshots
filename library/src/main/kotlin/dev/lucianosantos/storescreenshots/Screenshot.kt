package dev.lucianosantos.storescreenshots

/**
 * Marks a JUnit test as a Play Store / App Store screenshot.
 *
 * The associated [ScreenshotRule] reads the annotation and renders the test once per locale,
 * writing each rendering into the Fastlane metadata layout under the consuming project.
 *
 * Simple styling properties ([mockupPosition], [mockupOffsetXDp], [mockupOffsetYDp],
 * [titleFontFamily], [descriptionFontFamily]) can be set here per-screenshot. Values left at
 * their `Inherit` / `NaN` defaults fall through to the class-level [ScreenshotStyle].
 * Composable overrides (custom background, title, description composables) are only available
 * via [ScreenshotStyle] passed to [ScreenshotRule] or [StoreScreenshotsTest].
 *
 * @param locales BCP-47 locale tags. Each entry produces one image. Default is en-US.
 * @param title Headline shown above the device frame.
 * @param description Sub-headline shown below the title.
 * @param titleByLocale Per-locale overrides for [title]. Format: "locale=text".
 * @param descriptionByLocale Per-locale overrides for [description]. Same format.
 * @param backgroundColor Banner background as 0xAARRGGBB (e.g. 0xFF1F2937).
 * @param contentColor Foreground text color as 0xAARRGGBB. Default white.
 * @param mockupPosition Where the device sits vertically. [MockupPosition.Inherit] defers to class-level.
 * @param mockupOffsetXDp Horizontal visual nudge in dp. [Float.NaN] defers to class-level.
 * @param mockupOffsetYDp Vertical visual nudge in dp. [Float.NaN] defers to class-level.
 * @param titleFontFamily Font for the title. [FontFamilyName.Inherit] defers to class-level.
 * @param descriptionFontFamily Font for the description. [FontFamilyName.Inherit] defers to class-level.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Screenshot(
    val locales: Array<String> = ["en-US"],
    val title: String = "",
    val description: String = "",
    val titleByLocale: Array<String> = [],
    val descriptionByLocale: Array<String> = [],
    val backgroundColor: Long = DEFAULT_BACKGROUND,
    val contentColor: Long = DEFAULT_CONTENT,
    val mockupPosition: MockupPosition = MockupPosition.Inherit,
    val mockupOffsetXDp: Float = Float.NaN,
    val mockupOffsetYDp: Float = Float.NaN,
    val titleFontFamily: FontFamilyName = FontFamilyName.Inherit,
    val descriptionFontFamily: FontFamilyName = FontFamilyName.Inherit,
) {
    companion object {
        const val DEFAULT_BACKGROUND: Long = 0xFF1F2937
        const val DEFAULT_CONTENT: Long = 0xFFFFFFFF
    }
}
