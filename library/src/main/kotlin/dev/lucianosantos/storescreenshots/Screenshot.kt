package dev.lucianosantos.storescreenshots

/**
 * Marks a JUnit test as a Play Store / App Store screenshot.
 *
 * The associated [ScreenshotRule] reads the annotation and renders the test once per locale,
 * writing each rendering into the Fastlane metadata layout under the consuming project.
 *
 * @param locales BCP-47 locale tags. Each entry produces one image. Default is en-US.
 * @param title Headline shown above the device frame. Same value for all locales unless [titleByLocale] is set.
 * @param description Sub-headline shown below the title.
 * @param titleByLocale Per-locale overrides for [title]. Keys must match entries in [locales]. Format: "locale=text".
 * @param descriptionByLocale Per-locale overrides for [description]. Same format as [titleByLocale].
 * @param backgroundColor Banner background as a 0xAARRGGBB long (e.g. 0xFF1F2937).
 * @param contentColor Foreground text color as 0xAARRGGBB. Default white.
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
) {
    companion object {
        const val DEFAULT_BACKGROUND: Long = 0xFF1F2937
        const val DEFAULT_CONTENT: Long = 0xFFFFFFFF
    }
}
