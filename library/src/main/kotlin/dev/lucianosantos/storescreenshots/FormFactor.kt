package dev.lucianosantos.storescreenshots

/**
 * Each form factor encodes the output pixel size and the Fastlane subdirectory the store expects.
 *
 * Pixel sizes are the minimums required by the store, chosen to match what the Play Console and
 * App Store Connect actually accept on new submissions.
 */
enum class FormFactor(
    val widthPx: Int,
    val heightPx: Int,
    val qualifiers: String,
    val fastlaneSubdir: String,
    val rootRelativeDir: String,
) {
    /** Play Store phone screenshot. Portrait 1080x1920. */
    Phone(
        widthPx = 1080,
        heightPx = 1920,
        qualifiers = "w411dp-h914dp-xxhdpi",
        fastlaneSubdir = "phoneScreenshots",
        rootRelativeDir = "fastlane/metadata/android",
    ),

    /** Play Store Wear OS screenshot. Round 384x384. */
    Wear(
        widthPx = 384,
        heightPx = 384,
        qualifiers = "w227dp-h227dp-round-xxhdpi",
        fastlaneSubdir = "wearScreenshots",
        rootRelativeDir = "fastlane/metadata/android",
    ),

    /** Play Store 7-inch tablet screenshot. Portrait 1200x1920. */
    Tablet7(
        widthPx = 1200,
        heightPx = 1920,
        qualifiers = "w600dp-h960dp-xhdpi",
        fastlaneSubdir = "sevenInchScreenshots",
        rootRelativeDir = "fastlane/metadata/android",
    ),

    /** Play Store 10-inch tablet screenshot. Portrait 1600x2560. */
    Tablet10(
        widthPx = 1600,
        heightPx = 2560,
        qualifiers = "w800dp-h1280dp-xhdpi",
        fastlaneSubdir = "tenInchScreenshots",
        rootRelativeDir = "fastlane/metadata/android",
    ),

    /** Apple App Store iPhone 6.7" screenshot. Portrait 1290x2796 (iPhone 14/15 Pro Max etc.). */
    AppleIPhone67(
        widthPx = 1290,
        heightPx = 2796,
        qualifiers = "w430dp-h932dp-xxhdpi",
        fastlaneSubdir = "iphone67",
        rootRelativeDir = "fastlane/screenshots",
    ),
}
