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
import dev.lucianosantos.storescreenshots.DeviceMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.MockupMaterial

/** One device in the grid: how far it is turned, and what it is made of. */
@Immutable
private class ShowcaseDevice(
    val rotationY: Float,
    val rotationX: Float,
    val rail: Color,
    val edge: Color,
    val back: Color,
)

/**
 * Three rows of three, laid out as a compass: each device is turned toward the side of the grid it
 * sits on, and the one in the middle is not turned at all.
 *
 * So the top row shows its top rail, the left column its left, and the corners show the two that
 * meet there — which is where the band has to stay continuous around the radius, the thing that
 * comes free from extruding one outline and does not from drawing four separate faces. Between the
 * nine, every rail the body has is in view somewhere, along with the buttons, speaker grille and
 * charge port that live on them.
 *
 * The centre is the point of the whole grid: **no tilt at all**. It does not go through the solid
 * renderer — it takes the original flat path, and is the only one of the nine with no rail to show.
 * It is white rather than left at the default, because a finish applies to an untilted device too:
 * it has no sides in view, but it is still made of something. Everything around it is the same
 * frame at the same measurements, differing only in how far it is turned and in three colours of
 * `mockupMaterial`.
 *
 * The X tips are the steeper numbers on purpose. A phone is twice as tall as it is wide, so the
 * camera already looks at its bottom edge from about 25 degrees above, and the body has to pass
 * that before a horizontal rail comes into view — where a side rail shows from about 13. A negative
 * X brings the top toward the viewer and a positive one the bottom.
 */
private val ShowcaseGrid: List<List<ShowcaseDevice>> = listOf(
    listOf(
        ShowcaseDevice(34f, -34f, Color(0xFF4F9A6A), Color(0xFFB8E6C8), Color(0xFF1F4430)),
        ShowcaseDevice(0f, -46f, Color(0xFFD6D8DB), Color(0xFFFFFFFF), Color(0xFF8E9195)),
        ShowcaseDevice(-34f, -34f, Color(0xFF3F9AA0), Color(0xFFB2E4E8), Color(0xFF14464A)),
    ),
    listOf(
        ShowcaseDevice(50f, 0f, Color(0xFF4F7BC4), Color(0xFFBBD2F5), Color(0xFF1B3157)),
        // The middle of the grid: face on, and so still the original flat path. A finish reaches it
        // all the same — an untilted device has no rails to show, but it is still made of something.
        ShowcaseDevice(0f, 0f, Color(0xFFF2F3F5), Color(0xFFFFFFFF), Color(0xFFBFC3C7)),
        ShowcaseDevice(-50f, 0f, Color(0xFFC05C6E), Color(0xFFF0C3CC), Color(0xFF55202B)),
    ),
    listOf(
        ShowcaseDevice(34f, 34f, Color(0xFFC9913F), Color(0xFFF2DCA8), Color(0xFF5B3F14)),
        ShowcaseDevice(0f, 46f, Color(0xFF8B5FBF), Color(0xFFD9C2F0), Color(0xFF3A2354)),
        ShowcaseDevice(-34f, 34f, Color(0xFFB5645A), Color(0xFFEFC7C1), Color(0xFF4C231E)),
    ),
)

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
                            ) { CounterScreen(count = 42) }
                        }
                    }
                }
            }
        }
    }
}
