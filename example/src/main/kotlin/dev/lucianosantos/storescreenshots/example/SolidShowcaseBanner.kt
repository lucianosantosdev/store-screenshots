package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import dev.lucianosantos.storescreenshots.DeviceMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.GlassEffect
import dev.lucianosantos.storescreenshots.GlassReflexStyle
import dev.lucianosantos.storescreenshots.GlassShadow
import dev.lucianosantos.storescreenshots.screenGlass
import dev.lucianosantos.storescreenshots.MockupMaterial
import dev.lucianosantos.storescreenshots.ScreenshotStyle

/** One device in the grid: how far it is turned, what it is made of, and what is on it. */
@Immutable
private class ShowcaseDevice(
    val rotationY: Float,
    val rotationX: Float,
    val rail: Color,
    val edge: Color,
    val back: Color,
    /** `null` keeps [CounterScreen]'s own page background — the app as it really looks. */
    val screen: Brush?,
    val count: Int,
)

/**
 * The light every screen in the grid catches.
 *
 * One effect shared by all nine, at a fixed angle, because they are meant to read as nine devices
 * in the same room rather than nine devices each lit by their own sun. It is drawn *inside* the
 * content, so it is warped onto the front face along with the screen and leans with each device
 * instead of lying flat across the banner — which is the whole reason a reflection is worth having
 * here rather than being painted over the top afterwards.
 *
 * This is [ScreenshotStyle.screenGlass]'s reflex, not the renderer's own
 * [MockupMaterial.glassSheen]; the two are separate layers and both are in play.
 */
private val ShowcaseGlass = GlassEffect(
    reflexStyle = GlassReflexStyle.Wedge,
    reflexAngle = -32f,
    reflexPosition = 0.58f,
    reflexWidth = 0.46f,
    reflexAlpha = 0.16f,
    shadow = GlassShadow.BottomLeft,
    shadowAlpha = 0.20f,
)

/** How far the grid reaches either side of its centre: three rows and three columns. */
private const val CompassReach = 1

/**
 * How far a device is turned, in degrees, when a single axis is doing all the turning.
 *
 * A tilt has to clear the angle the camera already views that edge from, or the rail it is meant to
 * expose never appears. A phone is twice as tall as it is wide, so its top and bottom edges are
 * already seen from about 25 degrees above or below where its sides are seen from only 13 — which
 * is why this is well past both rather than split to suit the easier one. It stays inside the 75
 * degrees the renderer supports with room to spare.
 */
private const val CompassTilt = 48f

/**
 * What each axis takes when a corner device turns on both at once.
 *
 * Roughly 1/sqrt(2), so the total turn is the same as an edge device's: a corner that took the full
 * [CompassTilt] on each axis would read as far steeper than its neighbours rather than as the same
 * turn pointed diagonally. Thirty-four degrees still clears the 25 the top and bottom rails need.
 */
private const val DiagonalShare = 0.71f

/** An enclosure finish: the rail's face, its machined edge, and the shade it falls to at the back. */
@Immutable
private class Finish(val rail: Color, val edge: Color, val back: Color)

/**
 * What the eight turned devices are made of.
 *
 * Deliberately *unrelated* to the screens in front of them. These are enclosure colours — anodised
 * darks, metals, and a couple no phone has ever shipped in — while the screens run a bright spectrum
 * of their own. If both took the same hue the grid would suggest the body colour is somehow derived
 * from the app running on it, and it is not: `mockupMaterial` is told three colours and knows
 * nothing whatever about the content, so the two are set independently and look it.
 */
private val Finishes = listOf(
    Finish(Color(0xFF4A4E54), Color(0xFF9BA1A9), Color(0xFF1B1E22)), // graphite
    Finish(Color(0xFFD4AF6A), Color(0xFFF5E3B8), Color(0xFF6B5118)), // gold
    Finish(Color(0xFF2E4668), Color(0xFF8FA9C9), Color(0xFF121C2B)), // midnight blue
    Finish(Color(0xFFB87A63), Color(0xFFEFC6B4), Color(0xFF4A2A1E)), // copper
    Finish(Color(0xFF8A8073), Color(0xFFD8D0C4), Color(0xFF33302A)), // warm titanium
    Finish(Color(0xFFC0C4C9), Color(0xFFFFFFFF), Color(0xFF7C8084)), // silver
    Finish(Color(0xFF6E5A9B), Color(0xFFC3B4E4), Color(0xFF2A2140)), // violet, which no phone is
    Finish(Color(0xFF3E6B4F), Color(0xFF9ECBAE), Color(0xFF16281C)), // forest green
)

// Ordered so no device ends up wearing its own screen's colour. A green body behind a green screen
// is a coincidence the eye reads as a rule, and the whole point of this list is that there isn't
// one — so the two greens are kept apart.

/** The reference device in the middle: white, untilted, showing the app's own colours. */
private val CentreFinish = Finish(Color(0xFFF2F3F5), Color(0xFFFFFFFF), Color(0xFFBFC3C7))

/**
 * The device at [row], [col] out from the centre — its tilt, its finish, and its screen.
 *
 * The screen hue follows the direction the device is turned, so the grid reads as a colour wheel;
 * the body takes a [Finish] from a list that has nothing to do with it. Nine different compositions
 * are being measured, laid out and warped onto nine different quads, and every one of them can be
 * told what colour to be independently of the device holding it.
 *
 * The centre is the exception twice over: it is not turned, so it has no direction to take a screen
 * hue from, and its screen is left at [CounterScreen]'s own background — it is the reference, so it
 * shows the app in the colours the app actually has.
 */
private fun compassDevice(row: Int, col: Int, count: Int, finish: Finish): ShowcaseDevice {
    if (row == 0 && col == 0) {
        return ShowcaseDevice(
            rotationY = 0f,
            rotationX = 0f,
            rail = finish.rail,
            edge = finish.edge,
            back = finish.back,
            screen = null,
            count = count,
        )
    }
    val share = if (row != 0 && col != 0) DiagonalShare else 1f
    // A device in the left column shows its *left* rail, and a positive rotationY is what brings
    // that edge toward the viewer — so the sign is the opposite of the column's.
    val hue = ((Math.toDegrees(atan2(row.toDouble(), col.toDouble())).toFloat() + 360f) % 360f)
    return ShowcaseDevice(
        rotationY = -col * CompassTilt * share,
        rotationX = row * CompassTilt * share,
        rail = finish.rail,
        edge = finish.edge,
        back = finish.back,
        screen = Brush.verticalGradient(
            listOf(Color.hsl(hue, 0.58f, 0.56f), Color.hsl(hue, 0.62f, 0.32f))
        ),
        count = count,
    )
}

/**
 * Three rows of three, laid out as a compass: each device is turned toward the side of the grid it
 * sits on.
 *
 * So the top row shows its top rail, the left column its left, and the corners show the two that
 * meet there — which is where the band has to stay continuous around the radius, the thing that
 * comes free from extruding one outline and does not from drawing four separate faces. Between the
 * nine, every rail the body has is in view, along with the buttons, speaker grille and charge port
 * that live on them.
 *
 * The centre is the point of the whole grid: **no tilt at all**. It does not go through the solid
 * renderer — it takes the original flat path, and is the only one of the nine with no rail to show.
 * It is white rather than left at the default, because a finish applies to an untilted device too:
 * it has no sides in view, but it is still made of something. Every other device is the same frame
 * at the same measurements, differing only in how far it is turned, in three colours of
 * `mockupMaterial`, and in the screen it is showing.
 */
private val ShowcaseGrid: List<List<ShowcaseDevice>> =
    (-CompassReach..CompassReach).map { row ->
        (-CompassReach..CompassReach).map { col ->
            // Counted in reading order, so no two devices show the same screen and the numbers run
            // left to right and top to bottom the way the eye already reads the grid.
            val index = (row + CompassReach) * (CompassReach * 2 + 1) + (col + CompassReach)
            // Finishes are handed out in the same order, skipping the centre, which has its own.
            val centre = CompassReach * (CompassReach * 2 + 1) + CompassReach
            val finish = when {
                index == centre -> CentreFinish
                index < centre -> Finishes[index]
                else -> Finishes[index - 1]
            }
            compassDevice(row, col, count = index + 1, finish = finish)
        }
    }

@Composable
fun SolidShowcaseBanner(title: String, description: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF111827), Color(0xFF1F2937), Color(0xFF0B1120))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(420.dp)
                .offset(x = (-120).dp, y = (-140).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(480.dp)
                .offset(x = 170.dp, y = 200.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (description != null) {
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            ) {
                ShowcaseGrid.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        row.forEach { device ->
                            DeviceMockup(
                                formFactor = FormFactor.Phone,
                                modifier = Modifier.fillMaxHeight(0.96f),
                                elevation = 14.dp,
                                rotationX = device.rotationX,
                                rotationY = device.rotationY,
                                material = MockupMaterial(
                                    railColor = device.rail,
                                    edgeHighlightColor = device.edge,
                                    backEdgeColor = device.back,
                                ),
                            ) {
                                val glass = Modifier.screenGlass(ShowcaseGlass)
                                if (device.screen == null) {
                                    CounterScreen(count = device.count, modifier = glass)
                                } else {
                                    CounterScreen(
                                        count = device.count,
                                        modifier = glass,
                                        background = device.screen,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
