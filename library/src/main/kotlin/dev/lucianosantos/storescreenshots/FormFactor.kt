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
    val qualifiers: String,
    val subdir: String,
    val useImagesSubdir: Boolean,
) {
    /**
     * Play Store phone screenshot. Portrait 1242x2208 — `414dp x 736dp` at xxhdpi (density 3.0),
     * exactly 9:16.
     *
     * The canvas used to be `411dp x 914dp`, which renders 1233x2742 — a 1:2.22 image the Play
     * Console turns away, since it rejects a screenshot whose long side is more than twice its
     * short side. Clearing that rule alone would allow anything up to 1:2, but Play's separate
     * promotional-eligibility guidance asks for 9:16 portrait at 1080px or more, so this targets
     * 9:16 exactly rather than merely legal. The width moves by 3dp, so title and description wrap
     * essentially as before; the height comes down enough that the phone mockup scales to fit.
     */
    Phone(
        qualifiers = "w414dp-h736dp-xxhdpi",
        subdir = "phoneScreenshots",
        useImagesSubdir = true,
    ),

    /**
     * Play Store Wear OS screenshot. Round 681x681 — `227dp x 227dp` at xxhdpi (density 3.0), the
     * logical size of a real round Wear display. Play takes a square watch screenshot anywhere from
     * 384x384 up to 3840x3840, so this renders above the floor rather than at it.
     */
    Wear(
        qualifiers = "w227dp-h227dp-round-xxhdpi",
        subdir = "wearScreenshots",
        useImagesSubdir = true,
    ),

    /** Play Store 7-inch tablet screenshot. Portrait 1200x1920. */
    Tablet7(
        qualifiers = "w600dp-h960dp-xhdpi",
        subdir = "sevenInchScreenshots",
        useImagesSubdir = true,
    ),

    /** Play Store 10-inch tablet screenshot. Portrait 1600x2560. */
    Tablet10(
        qualifiers = "w800dp-h1280dp-xhdpi",
        subdir = "tenInchScreenshots",
        useImagesSubdir = true,
    ),

    /** Apple App Store iPhone 6.7" screenshot. Portrait 1290x2796 (iPhone 14/15 Pro Max etc.). */
    AppleIPhone67(
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
        qualifiers = "w512dp-h250dp-xhdpi",
        subdir = ".",
        useImagesSubdir = true,
    ),

    ;

    /**
     * Width in pixels of the PNG this form factor produces, derived from [qualifiers] rather than
     * declared alongside them — the qualifiers are what actually decide the size, so a hand-written
     * copy could only ever drift out of step with them.
     */
    val widthPx: Int get() = qualifiers.toPixelSize().first

    /** Height in pixels of the PNG this form factor produces. See [widthPx]. */
    val heightPx: Int get() = qualifiers.toPixelSize().second
}

/** Density multiplier for each named Android density bucket that can appear in a qualifier. */
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
 * multiplied by the density it names. A qualifier with no density segment is mdpi, per Android's
 * own default. A numeric bucket such as `560dpi` scales by `560/160`.
 *
 * A density segment that names no scale — `nodpi`, `anydpi` — is an error rather than a silent
 * fallback to mdpi, which would otherwise report a plausible-looking but wrong pixel size.
 */
private fun String.toPixelSize(): Pair<Int, Int> {
    val widthDp = Regex("""w(\d+)dp""").find(this)?.groupValues?.get(1)?.toInt()
        ?: error("Qualifiers '$this' have no wNNNdp width.")
    val heightDp = Regex("""h(\d+)dp""").find(this)?.groupValues?.get(1)?.toInt()
        ?: error("Qualifiers '$this' have no hNNNdp height.")
    val density = density()
    return (widthDp * density).roundToInt() to (heightDp * density).roundToInt()
}

/**
 * The density a qualifier string names, as a multiplier over mdpi. The `wNNNdp`/`hNNNdp` segments
 * end in `dp`, not `dpi`, so they never match here.
 */
private fun String.density(): Float {
    val segment = split("-").lastOrNull { it.endsWith("dpi") } ?: return 1f
    densityBuckets[segment]?.let { return it }
    Regex("""^(\d+)dpi$""").find(segment)?.let { return it.groupValues[1].toInt() / 160f }
    error(
        "Qualifiers '$this' name density '$segment', which has no pixel scale. Use one of " +
            "${densityBuckets.keys.joinToString()} or a numeric bucket such as '560dpi'."
    )
}
