package dev.lucianosantos.storescreenshots

import kotlin.math.roundToInt

/**
 * Each form factor encodes the output pixel size and the subdirectory name for screenshots.
 *
 * The output path under `destDir` is:
 * - Android form factors: `{locale}/images/{subdir}/{name}.png`
 * - Apple form factors: `{locale}/{subdir}/{name}.png`
 *
 * A form factor whose output is a single image directly under `images/` (e.g. the feature
 * graphic) uses `subdir = "."`, so the path collapses to `{locale}/images/{name}.png`.
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
    /**
     * Play Store phone screenshot. Portrait 1233x2430 — `411dp x 810dp` at xxhdpi (density 3.0).
     *
     * Play rejects a screenshot whose long side is more than twice its short side. The canvas used
     * to be `411dp x 914dp`, which renders 1233x2742 — a 1:2.22 image the Play Console turns away.
     * The width is unchanged, so title and description keep wrapping exactly as before; only the
     * canvas height comes down, which leaves the phone mockup slightly less room and scales it down
     * a few percent.
     */
    Phone(
        widthPx = 1233,
        heightPx = 2430,
        qualifiers = "w411dp-h810dp-xxhdpi",
        subdir = "phoneScreenshots",
        useImagesSubdir = true,
    ),

    /**
     * Play Store Wear OS screenshot. Round 681x681 — `227dp x 227dp` at xxhdpi (density 3.0), the
     * logical size of a real round Wear display. Play takes a square watch screenshot anywhere from
     * 384x384 up to 3840x3840, so this renders above the floor rather than at it.
     */
    Wear(
        widthPx = 681,
        heightPx = 681,
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

    /**
     * Apple App Store iPhone 6.5" screenshot. Portrait 1284x2778 (iPhone 12/13/14 Plus and Pro Max).
     *
     * The size App Store Connect selects by default, so it is usually the slot to fill first —
     * Apple scales it into the others. The 6.5" slot also accepts 1242x2688 (414dp x 896dp);
     * 428dp x 926dp is used here because it is the taller, current-generation ratio and Apple
     * takes either.
     *
     * These devices have a notch rather than a Dynamic Island, which is why [AppleFrame] takes
     * the cutout as a parameter.
     */
    AppleIPhone65(
        widthPx = 1284,
        heightPx = 2778,
        qualifiers = "w428dp-h926dp-xxhdpi",
        subdir = "iphone65",
        useImagesSubdir = false,
    ),

    /**
     * Apple App Store 13" iPad screenshot. Portrait 2048x2732 (12.9"/13" iPad Pro & iPad Air).
     *
     * App Store Connect requires a 13" iPad screenshot to submit any app that runs on iPad — even an
     * iPhone-first design still lists on iPad, so the slot is mandatory. 2048x2732 is the long-standing
     * size the slot accepts (1024dp x 1366dp at @2x / xhdpi); it also takes 2064x2752 and either
     * landscape orientation. The 4:3 portrait 2048x2732 used here is what every iPad build has shipped.
     *
     * Drawn with the same neutral tablet bezel as the Android tablets in [DeviceMockup] — a uniform
     * rounded frame with no notch, which reads correctly as an iPad.
     */
    AppleIPad13(
        widthPx = 2048,
        heightPx = 2732,
        qualifiers = "w1024dp-h1366dp-xhdpi",
        subdir = "ipad13",
        useImagesSubdir = false,
    ),

    /**
     * Google Play feature graphic. Landscape 1024x500 promotional banner shown at the top of
     * the store listing. `512dp x 250dp` at xhdpi (density 2.0) renders exactly 1024x500 px.
     * It is a single image, not a folder of screenshots, so `subdir = "."` places it directly at
     * `{locale}/images/featureGraphic.png` (the exact path Fastlane's supply expects) when the shot
     * is named `featureGraphic`.
     */
    GooglePlayFeatureGraphic(
        widthPx = 1024,
        heightPx = 500,
        qualifiers = "w512dp-h250dp-xhdpi",
        subdir = ".",
        useImagesSubdir = true,
    ),

    ;

    init {
        val (qualifierWidthPx, qualifierHeightPx) = qualifiers.toPixelSize()
        require(widthPx == qualifierWidthPx && heightPx == qualifierHeightPx) {
            "FormFactor.$name declares ${widthPx}x$heightPx but its qualifiers '$qualifiers' " +
                "render ${qualifierWidthPx}x$qualifierHeightPx. Keep the two in step: the " +
                "qualifiers are what actually decide the PNG's size."
        }
    }
}

/** Density multiplier for each Android density bucket that can appear in a qualifier string. */
private val densityBuckets = mapOf(
    "ldpi" to 0.75f,
    "mdpi" to 1f,
    "hdpi" to 1.5f,
    "tvdpi" to 1.33125f,
    "xhdpi" to 2f,
    "xxhdpi" to 3f,
    "xxxhdpi" to 4f,
)

/**
 * The pixel size a Robolectric qualifier string renders at: its `wNNNdp` x `hNNNdp` logical size
 * multiplied by the density bucket it names (defaulting to mdpi when it names none).
 */
private fun String.toPixelSize(): Pair<Int, Int> {
    val widthDp = Regex("""w(\d+)dp""").find(this)?.groupValues?.get(1)?.toInt()
        ?: error("Qualifiers '$this' have no wNNNdp width.")
    val heightDp = Regex("""h(\d+)dp""").find(this)?.groupValues?.get(1)?.toInt()
        ?: error("Qualifiers '$this' have no hNNNdp height.")
    val density = split("-").firstNotNullOfOrNull(densityBuckets::get) ?: 1f
    return (widthDp * density).roundToInt() to (heightDp * density).roundToInt()
}
