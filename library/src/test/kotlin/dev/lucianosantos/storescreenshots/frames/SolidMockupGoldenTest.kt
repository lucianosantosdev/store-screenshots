package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.GlassEffect
import dev.lucianosantos.storescreenshots.GlassReflexStyle
import dev.lucianosantos.storescreenshots.GlassShadow
import dev.lucianosantos.storescreenshots.MockupMaterial
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.StoreScreenshotsTest
import org.junit.Test

/**
 * Goldens for the solid device renderer.
 *
 * [SolidFrontFaceMatchesFlatTiltTest] proves the projection is right and `DeviceProjectionTest`
 * proves the model is self-consistent; neither can say whether the result *looks* like a device.
 * These are what hold the finished pixels still — the rails, their shading, the buttons standing on
 * them, the cast shadow and the sheen — so a change to any of it is a golden someone had to look at
 * and re-record on purpose.
 *
 * See [GoldenImage] for how the comparison works and how to re-record one.
 */
class SolidPhoneGoldenTest : StoreScreenshotsTest(FormFactor.Phone) {

    /** The marketing angle the example ships: a firm Y turn showing the right rail and its buttons. */
    @Test
    fun hero() {
        screenshot(
            fileName = "golden_solid_phone_hero",
            title = "Solid, not flat",
            description = "The rail, its buttons and the light follow the angle",
            style = tilted(rotationY = -26f, rotationX = 8f, rotationZ = -6f),
        ) { GoldenContent() }
        assertTilted("golden_solid_phone_hero", "solid_phone_hero.png")
    }

    /**
     * X only, and steep enough to matter: a phone is twice as tall as it is wide, so the camera
     * already looks at its bottom edge from about 25 degrees above and the tilt has to pass that
     * before a horizontal rail appears at all. This is the case that would silently render flat if
     * that threshold were ever got wrong.
     */
    @Test
    fun tiltX() {
        screenshot(
            fileName = "golden_solid_phone_tilt_x",
            title = "Tipped back",
            description = "An X tilt steep enough to show the bottom rail",
            style = tilted(rotationX = 50f),
        ) { GoldenContent() }
        assertTilted("golden_solid_phone_tilt_x", "solid_phone_tilt_x.png")
    }

    /** Near the supported limit, where the rail is widest and the perspective strongest. */
    @Test
    fun steep() {
        screenshot(
            fileName = "golden_solid_phone_steep",
            title = "Turned hard",
            description = "A 45 degree turn, near the range the look is tuned for",
            style = tilted(rotationY = 45f),
        ) { GoldenContent() }
        assertTilted("golden_solid_phone_steep", "solid_phone_steep.png")
    }

    /** Elevation and a wedge glass on top of the tilt — the combination the README demonstrates. */
    @Test
    fun elevatedWithGlass() {
        screenshot(
            fileName = "golden_solid_phone_elevated_glass",
            title = "Lifted off the banner",
            description = "A cast shadow from the tilted silhouette, and glass over the screen",
            style = tilted(rotationY = -26f, rotationX = 8f, rotationZ = -6f).copy(
                mockupElevation = 18.dp,
                screenGlass = GlassEffect(
                    reflexStyle = GlassReflexStyle.Wedge,
                    reflexAngle = -32f,
                    reflexPosition = 0.55f,
                    reflexWidth = 0.5f,
                    reflexAlpha = 0.20f,
                    shadow = GlassShadow.BottomLeft,
                    shadowAlpha = 0.22f,
                ),
            ),
        ) { GoldenContent() }
        assertTilted("golden_solid_phone_elevated_glass", "solid_phone_elevated_glass.png")
    }

    /**
     * A button deliberately not made of the rail's metal: a white enclosure with black buttons.
     *
     * This is the one finish [dev.lucianosantos.storescreenshots.MockupMaterial.railColor] cannot
     * express on its own, because a button follows the rail by default and saying "black" there
     * would take the rail with it.
     *
     * Turned to bring the *left* rail forward, which is the one carrying two buttons rather than
     * one, so the golden holds both the boss and the seat it stands in.
     */
    @Test
    fun contrastingButtons() {
        screenshot(
            fileName = "golden_solid_phone_contrast_buttons",
            title = "Parted from the rail",
            description = "A white enclosure with black buttons standing on it",
            style = tilted(rotationY = 34f, rotationX = 8f).copy(mockupMaterial = Contrasting),
        ) { GoldenContent() }
        assertTilted("golden_solid_phone_contrast_buttons", "solid_phone_contrast_buttons.png")
    }

    /**
     * The same finish with no tilt at all, so it goes down the flat path instead.
     *
     * Worth its own golden because the two paths draw the buttons in completely different ways —
     * the solid renderer extrudes them onto a projected rail, the bezel lays them out as offset
     * boxes — and an override wired into one and not the other would look perfectly correct in
     * whichever of them anyone happened to look at.
     */
    @Test
    fun contrastingButtonsUntilted() {
        screenshot(
            fileName = "golden_flat_contrast_buttons",
            title = "Parted, face on",
            description = "The same button override on a device that is not turned",
            style = ScreenshotStyle(
                mockupPosition = MockupPosition.Middle,
                mockupMaterial = Contrasting,
            ),
        ) { GoldenContent() }
        GoldenImage.assertMatches("golden_flat_contrast_buttons", "flat_contrast_buttons.png")
    }

    /**
     * An in-plane spin on its own leaves the device face-on, so it must stay on the original flat
     * path and render exactly as it always did. This is the byte-identity claim the whole routing
     * change rests on; if the solid renderer ever started catching this case, the device would grow
     * a rail here and the golden would say so.
     */
    @Test
    fun inPlaneSpinStaysFlat() {
        screenshot(
            fileName = "golden_flat_zonly",
            title = "Just a spin",
            description = "mockupRotation alone keeps the device flat",
            style = ScreenshotStyle(mockupPosition = MockupPosition.Middle, mockupRotation = -5f),
        ) { GoldenContent() }
        GoldenImage.assertMatches("golden_flat_zonly", "flat_zonly.png")
    }
}

/** The iPhone: rails driven by real device metrics, with three buttons on one and one on the other. */
class SolidIPhoneGoldenTest : StoreScreenshotsTest(FormFactor.AppleIPhone67) {

    @Test
    fun hero() {
        screenshot(
            fileName = "golden_solid_iphone_hero",
            title = "Buttons on the rail",
            description = "Action, volume and side buttons standing on the projected edge",
            style = tilted(rotationY = -30f, rotationX = 6f, rotationZ = -4f),
        ) { GoldenContent() }
        assertTilted("golden_solid_iphone_hero", "solid_iphone_hero.png")
    }
}

/**
 * The iPad: a much thinner body than a phone's, at the proportions a real one has.
 *
 * Turned as far as the Android tablet is, and for the same reason — at a gentle angle a tablet's
 * rail is a few pixels on a canvas it already fills, which is not enough for a golden to be holding
 * anything. A Y turn *narrows* a device, so unlike a steep X tip it costs nothing in headroom.
 *
 * Its rail is visibly shallower than the Android tablet's at the same angle, and that is the point
 * rather than a shortfall: an iPad Air 13 is 6.1 mm deep across 214.9 mm, where a Pixel Tablet is
 * 8.1 mm across 169.5 mm — barely half the proportional depth. The two frames disagree here because
 * the hardware does.
 */
class SolidIPadGoldenTest : StoreScreenshotsTest(FormFactor.AppleIPad13) {

    @Test
    fun hero() {
        screenshot(
            fileName = "golden_solid_ipad_hero",
            title = "A thinner slab",
            description = "An iPad is barely half a Pixel Tablet's depth, relative to its own width",
            style = tilted(rotationY = -45f, rotationX = 6f),
        ) { GoldenContent() }
        assertTilted("golden_solid_ipad_hero", "solid_ipad_hero.png")
    }
}

/**
 * The Android tablet: no buttons at all, which is its own case worth pinning.
 *
 * Turned further than the phone is. A tablet is proportionally about half as deep relative to its
 * width, and it fills far more of its canvas, so the modest angle that gives a phone a broad rail
 * leaves a tablet with one a handful of pixels wide — too little for a golden to be holding
 * anything. This is steep enough that the rail is actually the subject.
 */
class SolidTabletGoldenTest : StoreScreenshotsTest(FormFactor.Tablet10) {

    @Test
    fun hero() {
        screenshot(
            fileName = "golden_solid_tablet_hero",
            title = "No hardware to invent",
            description = "The tablet frame draws no buttons, so neither does its rail",
            style = tilted(rotationY = -45f, rotationX = 8f),
        ) { GoldenContent() }
        assertTilted("golden_solid_tablet_hero", "solid_tablet_hero.png")
    }
}

/** A white enclosure with black buttons: the finish the rail colour cannot reach on its own. */
private val Contrasting = MockupMaterial(
    railColor = Color(0xFFE7E9EC),
    edgeHighlightColor = Color(0xFFFFFFFF),
    backEdgeColor = Color(0xFFA9AEB4),
    buttonColor = Color(0xFF14161A),
)

private fun tilted(
    rotationY: Float = 0f,
    rotationX: Float = 0f,
    rotationZ: Float = 0f,
) = ScreenshotStyle(
    mockupPosition = MockupPosition.Middle,
    mockupRotationY = rotationY,
    mockupRotationX = rotationX,
    mockupRotation = rotationZ,
)

private fun assertTilted(writtenName: String, referenceName: String) = GoldenImage.assertMatches(
    writtenName = writtenName,
    referenceName = referenceName,
    differingPixelTolerance = GoldenImage.TiltedDifferingPixelTolerance,
)

/**
 * Deliberately plain: flat colours and one line of text, so a diff points at the device rather than
 * at whatever the sample app happened to draw.
 */
@Composable
private fun GoldenContent() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF6A1B9A)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "42", color = Color.White, fontSize = 96.sp, fontWeight = FontWeight.Bold)
    }
}
