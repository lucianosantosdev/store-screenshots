package dev.lucianosantos.storescreenshots

/**
 * Marks a JUnit test as a Play Store / App Store screenshot.
 *
 * The associated [ScreenshotRule] reads the annotation and renders the test once per locale.
 * Title, description, and styling are passed to `capture()` — not here — so you can use
 * `R.string.*` resource IDs for automatic localization.
 *
 * @param locales BCP-47 locale tags. Each entry produces one image. Default is en-US.
 * @param backgroundColor Banner background as 0xAARRGGBB (e.g. 0xFF1F2937).
 * @param contentColor Foreground text color as 0xAARRGGBB. Default white.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Screenshot(
    val locales: Array<String> = ["en-US"],
    val backgroundColor: Long = DEFAULT_BACKGROUND,
    val contentColor: Long = DEFAULT_CONTENT,
) {
    companion object {
        const val DEFAULT_BACKGROUND: Long = 0xFF1F2937
        const val DEFAULT_CONTENT: Long = 0xFFFFFFFF
    }
}
