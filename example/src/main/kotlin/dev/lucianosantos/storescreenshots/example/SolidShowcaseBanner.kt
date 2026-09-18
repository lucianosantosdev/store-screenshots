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
import dev.lucianosantos.storescreenshots.MockupMaterial

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

/**
 * The device at [row], [col] out from the centre — its tilt, its finish, and its screen.
 *
 * Hue follows the direction the device is turned, so the grid reads as a colour wheel. The screen
 * takes the *same* hue as the body, which is the part that only works because the screen is live
 * Compose rather than a picture: nine different compositions are being measured, laid out and
 * warped onto nine different quads, and every one of them can be told what colour to be.
 *
 * The centre is white and has no direction to take a hue from — which is the point, since it is the
 * one device that is not turned at all. Its screen is left at [CounterScreen]'s own background for
 * the same reason: it is the reference, so it shows the app in the colours the app actually has.
 */
private fun compassDevice(row: Int, col: Int, count: Int): ShowcaseDevice {
    if (row == 0 && col == 0) {
        return ShowcaseDevice(
            rotationY = 0f,
            rotationX = 0f,
            rail = Color(0xFFF2F3F5),
            edge = Color(0xFFFFFFFF),
            back = Color(0xFFBFC3C7),
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
        rail = Color.hsl(hue, 0.34f, 0.60f),
        edge = Color.hsl(hue, 0.44f, 0.86f),
        back = Color.hsl(hue, 0.42f, 0.22f),
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
            compassDevice(row, col, count = index + 1)
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
                                if (device.screen == null) {
                                    CounterScreen(count = device.count)
                                } else {
                                    CounterScreen(count = device.count, background = device.screen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
