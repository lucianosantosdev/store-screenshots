package dev.lucianosantos.storescreenshots

import kotlin.math.abs
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
     * Play Store phone screenshot. Portrait 1242x2484 — `414dp x 828dp` at xxhdpi (density 3.0),
     * exactly 1:2.
     *
     * The canvas used to be `411dp x 914dp`, which renders 1233x2742 — a 1:2.22 image, past the
     * maximum Play documents (long side no more than twice the short side). That limit is not
     * enforced at upload, and listings do ship 1:2.22 phone screenshots today, so this is about
     * staying inside the range Play asks for rather than clearing a gate that would block a
     * release. 1:2 is the tallest canvas inside it, and it is deliberately the default because it
     * is the one that changes the least: the phone mockup is limited by the canvas *width* here,
     * exactly as it was at 1:2.22, so a shot keeps roughly the proportions it had before (device
     * 321dp wide against 355dp, rather than the 275dp a 9:16 canvas forces).
     *
     * Play separately asks for 9:16 portrait at 1080px or more to be eligible for promotion, and
     * 1:2 does not satisfy that — 9:16 is 1:1.78. It is also a real trade rather than a strict
     * improvement: at 9:16 there is not enough height left beside a title and description for the
     * mockup, so the device shrinks by about a quarter and the shot reads emptier. It is one line
     * away when a project wants it:
     *
     * ```kotlin
     * class HomeShots : StoreScreenshotsTest(
     *     FormFactor.Phone,
     *     canvas = ScreenshotCanvas.px(1080, 1920), // or px(1242, 2208)
     * )
     * ```
     *
     * See [ScreenshotCanvas] for overriding any form factor's size.
     */
    Phone(
        qualifiers = "w414dp-h828dp-xxhdpi",
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

/**
 * Density multiplier for each named Android density bucket that can appear in a qualifier. Read in
 * both directions — [density] resolves a qualifier's bucket to its scale, and [densityQualifier]
 * goes back the other way for [ScreenshotCanvas] — so it is one table rather than two that would
 * have to be kept in step.
 */
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
 * The qualifier segment naming [density] — the bucket's name when it is one Android names, and the
 * `NNNdpi` form otherwise (Robolectric accepts either).
 *
 * Matched with a tolerance because the buckets are not all exact: `tvdpi` is 1.33125, which no
 * caller is going to type back exactly.
 */
internal fun densityQualifier(density: Float): String =
    densityBuckets.entries.firstOrNull { abs(it.value - density) < 0.001f }?.key
        ?: "${(density * 160).roundToInt()}dpi"

/**
 * The pixel size a Robolectric qualifier string renders at: its `wNNNdp` x `hNNNdp` logical size
 * multiplied by the density it names. A qualifier with no density segment is mdpi, per Android's
 * own default. A numeric bucket such as `560dpi` scales by `560/160`.
 *
 * A density segment that names no scale — `nodpi`, `anydpi` — is an error rather than a silent
 * fallback to mdpi, which would otherwise report a plausible-looking but wrong pixel size.
 */
internal fun String.toPixelSize(): Pair<Int, Int> {
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
internal fun String.density(): Float {
    val segment = split("-").lastOrNull { it.endsWith("dpi") } ?: return 1f
    densityBuckets[segment]?.let { return it }
    Regex("""^(\d+)dpi$""").find(segment)?.let { return it.groupValues[1].toInt() / 160f }
    error(
        "Qualifiers '$this' name density '$segment', which has no pixel scale. Use one of " +
            "${densityBuckets.keys.joinToString()} or a numeric bucket such as '560dpi'."
    )
}
