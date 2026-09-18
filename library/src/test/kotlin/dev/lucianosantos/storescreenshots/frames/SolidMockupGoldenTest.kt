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
 * Gently turned, because an iPad already fills most of its App Store canvas and a steep tilt pushes
 * the near edge off it — a property of the perspective, not of the solid renderer, and one the flat
 * path shared. The rail it does show is the point: a quarter the depth of the phone's, from the
 * same renderer and the same measurements.
 */
class SolidIPadGoldenTest : StoreScreenshotsTest(FormFactor.AppleIPad13) {

    @Test
    fun hero() {
        screenshot(
            fileName = "golden_solid_ipad_hero",
            title = "A thinner slab",
            description = "A tablet's rail is a quarter of a phone's, from the same measurements",
            style = tilted(rotationY = -18f, rotationX = 6f),
        ) { GoldenContent() }
        assertTilted("golden_solid_ipad_hero", "solid_ipad_hero.png")
    }
}

/** The Android tablet: no buttons at all, which is its own case worth pinning. */
class SolidTabletGoldenTest : StoreScreenshotsTest(FormFactor.Tablet10) {

    @Test
    fun hero() {
        screenshot(
            fileName = "golden_solid_tablet_hero",
            title = "No hardware to invent",
            description = "The tablet frame draws no buttons, so neither does its rail",
            style = tilted(rotationY = -26f, rotationX = 8f),
        ) { GoldenContent() }
        assertTilted("golden_solid_tablet_hero", "solid_tablet_hero.png")
    }
}

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
