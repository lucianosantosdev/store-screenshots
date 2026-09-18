package dev.lucianosantos.storescreenshots.example

import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/**
 * The Android phone frame in a green no phone is actually sold in, to make the point the silver
 * iPhone cannot: `mockupMaterial` is not a list of finishes the library knows about, it is whatever
 * the metal should be. Same frame, same measurements, same status bar and camera cutout.
 *
 * Turned on both axes, so it also shows the two rails at once — volume buttons down the left, and
 * the speaker grille and charge port cut into the bottom, which only come into view once the tip
 * passes the angle the camera already looks at that edge from.
 */
class PhoneGreenExampleTest : StoreScreenshotsTest(FormFactor.Phone) {

    @Test
    fun counter_green() = screenshot(
        locales = listOf("en-US", "pt-BR"),
        titleRes = R.string.screenshot_green_title,
        descriptionRes = R.string.screenshot_green_desc,
        style = greenScreenshotStyle,
    ) { CounterScreen(count = 42) }
}
