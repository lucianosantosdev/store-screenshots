package dev.lucianosantos.storescreenshots

/**
 * Each form factor encodes the output pixel size and the subdirectory name for screenshots.
 *
 * The output path under `destDir` is:
 * - Android form factors: `{locale}/images/{subdir}/{name}.png`
 * - Apple form factors: `{locale}/{subdir}/{name}.png`
 *
 * To match Fastlane's layout, set `destDir` to `fastlane/metadata/android` (Android) or
 * `fastlane/screenshots` (Apple). By default screenshots land in `build/outputs/store-screenshots/`.
 */
enum class FormFactor(
    val widthPx: Int,
    val heightPx: Int,
    val qualifiers: String,
    val subdir: String,
    val useImagesSubdir: Boolean,
) {
    /** Play Store phone screenshot. Portrait 1080x1920. */
    Phone(
        widthPx = 1080,
        heightPx = 1920,
        qualifiers = "w411dp-h914dp-xxhdpi",
        subdir = "phoneScreenshots",
        useImagesSubdir = true,
    ),

    /** Play Store Wear OS screenshot. Round 384x384. */
    Wear(
        widthPx = 384,
        heightPx = 384,
        qualifiers = "w227dp-h227dp-round-xxhdpi",
        subdir = "wearScreenshots",
        useImagesSubdir = true,
    ),

    /** Play Store 7-inch tablet screenshot. Portrait 1200x1920. */
    Tablet7(
        widthPx = 1200,
        heightPx = 1920,
        qualifiers = "w600dp-h960dp-xhdpi",
        subdir = "sevenInchScreenshots",
        useImagesSubdir = true,
    ),

    /** Play Store 10-inch tablet screenshot. Portrait 1600x2560. */
    Tablet10(
        widthPx = 1600,
        heightPx = 2560,
        qualifiers = "w800dp-h1280dp-xhdpi",
        subdir = "tenInchScreenshots",
        useImagesSubdir = true,
    ),

    /** Apple App Store iPhone 6.7" screenshot. Portrait 1290x2796 (iPhone 14/15 Pro Max etc.). */
    AppleIPhone67(
        widthPx = 1290,
        heightPx = 2796,
        qualifiers = "w430dp-h932dp-xxhdpi",
        subdir = "iphone67",
        useImagesSubdir = false,
    ),
}
