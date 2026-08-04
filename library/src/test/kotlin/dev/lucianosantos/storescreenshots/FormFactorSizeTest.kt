package dev.lucianosantos.storescreenshots

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The pixel size each form factor emits, pinned.
 *
 * [FormFactor.widthPx]/[FormFactor.heightPx] are derived from `qualifiers`, so the two can no
 * longer disagree the way they did when the size was declared by hand — but the qualifiers are
 * still editable, and they are what the store sees. This is the gate that makes an edit to them
 * a deliberate act: changing a size here means changing the image every consumer uploads.
 *
 * Kept as a test rather than an `init` check inside the enum, where a failure would surface as
 * `ExceptionInInitializerError` in a consumer's own test run instead of in this build.
 */
class FormFactorSizeTest {

    private val expectedSizes = mapOf(
        FormFactor.Phone to (1242 to 2208),
        FormFactor.Wear to (681 to 681),
        FormFactor.Tablet7 to (1200 to 1920),
        FormFactor.Tablet10 to (1600 to 2560),
        FormFactor.AppleIPhone67 to (1290 to 2796),
        FormFactor.AppleIPhone65 to (1284 to 2778),
        FormFactor.AppleIPad13 to (2048 to 2732),
        FormFactor.GooglePlayFeatureGraphic to (1024 to 500),
    )

    @Test
    fun everyFormFactorRendersTheSizeItIsExpectedTo() {
        assertEquals(
            "a form factor was added or removed without pinning its size here",
            FormFactor.entries.toSet(),
            expectedSizes.keys,
        )
        for ((formFactor, expected) in expectedSizes) {
            assertEquals(
                "${formFactor.name} qualifiers '${formFactor.qualifiers}' render the wrong size",
                expected,
                formFactor.widthPx to formFactor.heightPx,
            )
        }
    }

    /**
     * Play turns away any screenshot whose long side is more than twice its short side — the bug
     * that sent the phone canvas back at 1233x2742 (1:2.22).
     *
     * Only the Play *screenshot* slots are held to it. The App Store sets its own dimensions and
     * several of them are past 1:2 by Apple's own mandate (iPhone 6.7" is 1290x2796, 1:2.17), and
     * the feature graphic is a fixed 1024x500 promotional banner, not a screenshot.
     */
    @Test
    fun noPlayScreenshotIsTooElongatedForPlayToAccept() {
        val playScreenshots = listOf(
            FormFactor.Phone,
            FormFactor.Wear,
            FormFactor.Tablet7,
            FormFactor.Tablet10,
        )
        for (formFactor in playScreenshots) {
            val longest = maxOf(formFactor.widthPx, formFactor.heightPx)
            val shortest = minOf(formFactor.widthPx, formFactor.heightPx)
            assertEquals(
                "${formFactor.name} is ${formFactor.widthPx}x${formFactor.heightPx}, a " +
                    "1:${"%.2f".format(longest.toFloat() / shortest)} image — Play rejects " +
                    "anything past 1:2",
                true,
                longest <= shortest * 2,
            )
        }
    }

    /**
     * The `@Preview` canvases must be the same logical size as the form factor they stand for —
     * the library's whole claim about them is that the preview matches the PNG pixel-for-pixel.
     *
     * They cannot derive it: `@Preview` arguments are compile-time constants, and the annotation
     * is `AnnotationRetention.BINARY`, so nothing can read it back at runtime either. The dp
     * constants in `ScreenshotPreviews.kt` are the one place that second copy lives, and this is
     * what holds them against the qualifiers.
     */
    @Test
    fun everyPreviewAnnotationUsesItsFormFactorsCanvas() {
        val previewCanvases = mapOf(
            FormFactor.Phone to (PhonePreviewWidthDp to PhonePreviewHeightDp),
            FormFactor.Wear to (WearPreviewWidthDp to WearPreviewHeightDp),
            FormFactor.Tablet7 to (Tablet7PreviewWidthDp to Tablet7PreviewHeightDp),
            FormFactor.Tablet10 to (Tablet10PreviewWidthDp to Tablet10PreviewHeightDp),
            FormFactor.AppleIPhone67 to (AppleIPhone67PreviewWidthDp to AppleIPhone67PreviewHeightDp),
            FormFactor.AppleIPhone65 to (AppleIPhone65PreviewWidthDp to AppleIPhone65PreviewHeightDp),
            FormFactor.AppleIPad13 to (AppleIPad13PreviewWidthDp to AppleIPad13PreviewHeightDp),
        )
        for ((formFactor, preview) in previewCanvases) {
            assertEquals(
                "${formFactor.name}'s @Preview canvas no longer matches its qualifiers " +
                    "'${formFactor.qualifiers}' — the IDE preview would stop matching the PNG",
                formFactor.logicalSize(),
                preview,
            )
        }
    }

    /**
     * The feature graphic is the documented exception: it previews at 1.5× its 1024x500 output so
     * the short banner isn't dwarfed beside the phone and tablet previews. Pinned as a ratio so
     * the exception stays deliberate rather than becoming somewhere else drift can hide.
     */
    @Test
    fun theFeatureGraphicPreviewIsScaledUpByTheDocumentedFactor() {
        val (widthDp, heightDp) = FormFactor.GooglePlayFeatureGraphic.logicalSize()
        assertEquals(
            "feature graphic preview width should be 1.5× its ${FormFactor.GooglePlayFeatureGraphic.widthPx}px output",
            (FormFactor.GooglePlayFeatureGraphic.widthPx * 1.5f).toInt() to
                (FormFactor.GooglePlayFeatureGraphic.heightPx * 1.5f).toInt(),
            FeatureGraphicPreviewWidthDp to FeatureGraphicPreviewHeightDp,
        )
        assertEquals("feature graphic canvas is 512x250dp at xhdpi", 512 to 250, widthDp to heightDp)
    }

    /** The `wNNNdp` x `hNNNdp` canvas a form factor's qualifiers name, before density. */
    private fun FormFactor.logicalSize(): Pair<Int, Int> {
        fun dp(axis: Char) = Regex("""$axis(\d+)dp""").find(qualifiers)!!.groupValues[1].toInt()
        return dp('w') to dp('h')
    }

    /**
     * Clearing the 1:2 rejection rule is the floor, not the target. Play's promotional-eligibility
     * guidance asks for 9:16 portrait screenshots at 1080px or wider, so the phone canvas is sized
     * to that exactly: a canvas that is merely legal passes the gate that rejects and misses the
     * gate that promotes.
     */
    @Test
    fun thePhoneCanvasIsExactlyNineBySixteenAndWideEnoughToBePromoted() {
        val (width, height) = FormFactor.Phone.widthPx to FormFactor.Phone.heightPx
        assertEquals("phone screenshots must be 9:16", 9 * height, 16 * width)
        assertEquals(
            "phone screenshots must be at least 1080px wide to be promotion-eligible, was $width",
            true,
            width >= 1080,
        )
    }
}
