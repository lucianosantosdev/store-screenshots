package dev.lucianosantos.storescreenshots

import kotlin.math.roundToInt

/**
 * An override for the canvas a form factor renders on, so a project is not stuck with the size
 * [FormFactor] ships as its default.
 *
 * Store requirements are a range, not a number — Play documents portrait phone screenshots from
 * 320px to 3840px with the long side no more than twice the short side — and which point in that
 * range is right is a design decision, not a fact about the store. The default phone canvas is 1:2
 * because it is the tallest shape inside that range and so the one that leaves the mockup largest.
 * (The 2:1 maximum is documented but not enforced at upload; taller images do go through today.)
 *
 * The requirement worth opting into deliberately is promotional eligibility, which is separate and
 * stricter — at least four screenshots, 1080px or wider, at 9:16. The 1:2 default does not meet it,
 * so a project that wants Play's promotional slots takes 9:16 and accepts a smaller device:
 *
 * ```kotlin
 * class HomeShots : StoreScreenshotsTest(
 *     FormFactor.Phone,
 *     canvas = ScreenshotCanvas.px(1242, 2208), // 9:16
 * )
 * ```
 *
 * The canvas is a *logical* size plus a density, because that is what decides how the shot is
 * composed: content, title, and description are laid out in dp, so 1242x2484 at density 3 gives
 * 414x828dp of room, while the same pixels at density 2 give 621x1242dp and everything sized in
 * `dp`/`sp` renders half as large against the image. [px] therefore resolves against a density —
 * the form factor's own unless you pass another — and [dp] states the logical size outright.
 *
 * Only the size is replaced. Every other qualifier the form factor names is kept, so a resized
 * [FormFactor.Wear] is still `-round`.
 */
class ScreenshotCanvas private constructor(
    private val width: Int,
    private val height: Int,
    private val inPixels: Boolean,
    private val density: Float?,
) {

    init {
        require(width > 0 && height > 0) {
            "ScreenshotCanvas must be positive, was ${width}x$height."
        }
        require(density == null || density > 0f) {
            "ScreenshotCanvas density must be positive, was $density."
        }
    }

    /**
     * The Robolectric qualifiers this canvas renders as, built from [formFactor]'s own so
     * everything except the size and (when given) the density is carried over untouched.
     */
    internal fun qualifiersFor(formFactor: FormFactor): String {
        val density = this.density ?: formFactor.qualifiers.density()
        val (widthDp, heightDp) = if (inPixels) {
            toDp(width, density, 'w') to toDp(height, density, 'h')
        } else {
            width to height
        }
        var qualifiers = formFactor.qualifiers
            .replace(Regex("""w\d+dp"""), "w${widthDp}dp")
            .replace(Regex("""h\d+dp"""), "h${heightDp}dp")
        if (this.density != null) {
            qualifiers = qualifiers.replace(Regex("""(^|-)\w*dpi(-|$)""")) { match ->
                "${match.groupValues[1]}${densityQualifier(density)}${match.groupValues[2]}"
            }
        }
        return qualifiers
    }

    /**
     * The dp size [px] corresponds to at [density], rejecting a pair that does not land on a whole
     * dp — Robolectric's canvas is specified in dp, so `1000px` at density 3 could only ever be
     * rendered as 333dp = 999px, and silently emitting an image one pixel off the size the caller
     * asked for is the failure mode this whole class exists to avoid.
     */
    private fun toDp(px: Int, density: Float, axis: Char): Int {
        val dp = (px / density).roundToInt()
        val rendered = (dp * density).roundToInt()
        require(rendered == px) {
            "ScreenshotCanvas.px($width, $height) cannot be rendered at density $density: " +
                "${px}px on the ${if (axis == 'w') "width" else "height"} axis is ${px / density}dp, " +
                "which rounds to ${dp}dp and renders back as ${rendered}px. Pick a pixel size " +
                "divisible by the density, or state the logical size with ScreenshotCanvas.dp()."
        }
        return dp
    }

    companion object {
        /**
         * A canvas [width] x [height] **pixels** — the units both stores state their requirements
         * in. Resolved against [density] to the logical size the screenshot is composed at;
         * `null` (the default) keeps the form factor's own density, so
         * `ScreenshotCanvas.px(1242, 2208)` on [FormFactor.Phone] is 414x736dp at xxhdpi.
         *
         * Fails if the pixel size does not land on a whole dp at that density.
         */
        fun px(width: Int, height: Int, density: Float? = null): ScreenshotCanvas =
            ScreenshotCanvas(width, height, inPixels = true, density = density)

        /**
         * A canvas [width] x [height] **dp**, rendered at [density] — `null` (the default) keeps
         * the form factor's own. The output PNG is the dp size times the density, so
         * `ScreenshotCanvas.dp(414, 828)` on [FormFactor.Phone] writes 1242x2484.
         */
        fun dp(width: Int, height: Int, density: Float? = null): ScreenshotCanvas =
            ScreenshotCanvas(width, height, inPixels = false, density = density)
    }
}

/** The qualifiers a form factor renders with, once an optional [canvas] override is applied. */
internal fun FormFactor.qualifiersWith(canvas: ScreenshotCanvas?): String =
    canvas?.qualifiersFor(this) ?: qualifiers
