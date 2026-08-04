package dev.lucianosantos.storescreenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

/**
 * Every PNG this library writes must be opaque — no alpha channel.
 *
 * Not a cosmetic preference. Google Play's images endpoint returns a bare HTTP 500 when a
 * wear screenshot carries an alpha channel, while the same image flattened to RGB returns
 * 200 (verified against the Publisher API in August 2026). Because `supply` aborts the
 * whole listing upload on the first failure, one RGBA wear screenshot silently froze an
 * entire store listing — phone screenshots, texts and all — across four consecutive
 * releases, with nothing in the error to point at the cause.
 *
 * Roborazzi captures ARGB_8888, so without the flatten in [ScreenshotRule] every output
 * regresses to RGBA at once. This is the guard.
 */
class OpaqueOutputTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test
    fun opaqueContentIsWrittenWithoutAnAlphaChannel() {
        screenshot(fileName = "opaque") {
            Box(Modifier.fillMaxSize().background(Color(0xFF6A1B9A)))
        }
        assertNoAlpha(written("opaque"))
    }

    /**
     * A translucent composition is the case that would betray a flatten that only *marks*
     * the bitmap opaque without compositing it: the channel would be gone but the colours
     * would be wrong. Asserting the result reads as the blend over black covers both.
     */
    @Test
    fun translucentContentIsCompositedRatherThanJustMarkedOpaque() {
        screenshot(fileName = "translucent") {
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.5f)))
        }

        val png = written("translucent")
        assertNoAlpha(png)

        val image = ImageIO.read(png)
        // Sampled inside the device mockup, where the translucent content is drawn.
        val centre = image.getRGB(image.width / 2, image.height / 2)
        val red = (centre shr 16) and 0xFF
        assertTrue(
            "centre pixel red was $red — 50% white over black should land mid-grey; " +
                "full white would mean the alpha was discarded rather than composited",
            red in 90..170
        )
    }

    private fun written(name: String): File {
        val root = ScreenshotRule.defaultOutputRoot()
        return root.walkTopDown().firstOrNull { it.name == "$name.png" }
            ?: error("no $name.png written under $root")
    }

    private fun assertNoAlpha(file: File) {
        val image = ImageIO.read(file) ?: error("could not decode $file")
        assertFalse(
            "${file.name} still carries an alpha channel — Play rejects that on wearScreenshots",
            image.colorModel.hasAlpha()
        )
    }
}
