package dev.lucianosantos.storescreenshots

/**
 * Marks a JUnit test as a Play Store / App Store screenshot.
 *
 * The associated [ScreenshotRule] reads the annotation and renders the test once per locale,
 * writing each rendering into the Fastlane metadata layout under the consuming project.
 *
 * For visual styling (mockup position, fonts, composable overrides for background/title/description),
 * pass a [ScreenshotStyle] to [StoreScreenshotsTest] (class-level default) or to
 * `capture(style = …)` (per-method override).
 *
 * @param locales BCP-47 locale tags. Each entry produces one image. Default is en-US.
 * @param title Headline shown above the device frame (raw string, same for all locales unless
 *   overridden by [titleByLocale]). Ignored when [titleRes] is set.
 * @param description Sub-headline shown below the title. Ignored when [descriptionRes] is set.
 * @param titleRes Android string resource ID for the title (e.g. `R.string.screenshot_home_title`).
 *   Resolved per locale automatically — no need for [titleByLocale]. Takes precedence over [title].
 * @param descriptionRes Same as [titleRes] but for the description.
 * @param titleByLocale Per-locale overrides for [title]. Format: "locale=text". Only used when
 *   [titleRes] is 0.
 * @param descriptionByLocale Per-locale overrides for [description]. Same format.
 * @param backgroundColor Banner background as 0xAARRGGBB (e.g. 0xFF1F2937).
 * @param contentColor Foreground text color as 0xAARRGGBB. Default white.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Screenshot(
    val locales: Array<String> = ["en-US"],
    val title: String = "",
    val description: String = "",
    val titleRes: Int = 0,
    val descriptionRes: Int = 0,
    val titleByLocale: Array<String> = [],
    val descriptionByLocale: Array<String> = [],
    val backgroundColor: Long = DEFAULT_BACKGROUND,
    val contentColor: Long = DEFAULT_CONTENT,
) {
    companion object {
        const val DEFAULT_BACKGROUND: Long = 0xFF1F2937
        const val DEFAULT_CONTENT: Long = 0xFFFFFFFF
    }
}
