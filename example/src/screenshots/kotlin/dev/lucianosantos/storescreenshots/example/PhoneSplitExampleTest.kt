package dev.lucianosantos.storescreenshots.example

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

class PhoneSplitExampleTest : StoreScreenshotsTest(FormFactor.Phone) {

    /**
     * One banner drawn three phone-screenshots wide, then sliced into `story_01/02/03.png` for each
     * locale. The composition is [SplitStory] — the same source of truth the `SplitStoryPreview`
     * renders — so the IDE preview and the generated screenshots stay identical. The devices (a
     * phone, a chroma-keyed `DeviceImageMockup`, and a watch) straddle the seams, while each panel's
     * caption stays inside its own screenshot.
     */
    @Test
    fun story() = splitScreenshot(
        panels = 3,
        locales = listOf("en-US", "pt-BR"),
        fileName = "story",
        gap = SplitStoryGap,
    ) {
        SplitStory(chromaFrame = frame("phone_chroma.png"))
    }

    /** Loads a device-frame image from `src/screenshots/resources/mockups/` (test classpath). */
    private fun frame(name: String): ImageBitmap =
        javaClass.classLoader!!.getResourceAsStream("mockups/$name").use {
            BitmapFactory.decodeStream(it).asImageBitmap()
        }
}
