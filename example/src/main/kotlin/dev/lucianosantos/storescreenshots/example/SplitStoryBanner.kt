package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.DeviceImageMockup
import dev.lucianosantos.storescreenshots.DeviceMockup
import dev.lucianosantos.storescreenshots.FormFactor
import dev.lucianosantos.storescreenshots.GlassShadow
import dev.lucianosantos.storescreenshots.Screen
import dev.lucianosantos.storescreenshots.SplitPanels
import dev.lucianosantos.storescreenshots.screenGlass

/**
 * A single design drawn [panels] phone-screenshots wide, meant to be sliced by `splitScreenshot`.
 * It layers three things, each with a different relationship to the seams:
 *
 * - The gradient background and diagonal band **span** the whole canvas.
 * - [devices] is a free-form layer scoped to the whole canvas ([BoxScope]): the caller places each
 *   mockup itself — its position and rotation are visible right at the call site (see [SplitDevice])
 *   — so a single phone can straddle two screenshots and the device count has nothing to do with
 *   [panels]. It is *not* one mockup per panel.
 * - [caption] is the only per-screenshot layer, laid out with [SplitPanels] so each caption stays
 *   inside its own screenshot.
 *
 * [gap] is the dp gap between screenshots — it must match the `gap` passed to `splitScreenshot` so
 * the captions line up with the gapped slices.
 */
@Composable
fun SplitStoryBanner(
    panels: Int = 3,
    gap: Dp = SplitStoryGap,
    devices: @Composable BoxScope.() -> Unit,
    caption: @Composable BoxScope.(index: Int) -> Unit,
) {
    val background = Brush.verticalGradient(
        listOf(Color(0xFF7C3AED), Color(0xFF5B21B6), Color(0xFF4C1D95))
    )
    Box(Modifier.fillMaxSize().background(background)) {
        // Spanning — a translucent band raked across the whole width, crossing every seam.
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(540.dp)
                .offset(y = 60.dp)
                .rotate(-10f)
                .background(Color.White.copy(alpha = 0.06f))
        )

        // Free-form device layer — the caller decides how many mockups and where they sit.
        devices()

        // Per-screenshot layer — each panel's own caption, clipped to its panel and aligned to the
        // gapped slices.
        SplitPanels(count = panels, gap = gap) { index -> caption(index) }
    }
}

/** The gap (in dp) the split example renders between its screenshots. Non-zero, so the example
 *  shows a slice cut out between panels rather than a perfectly continuous design. */
val SplitStoryGap: Dp = 24.dp

/**
 * The split-screenshot example composition — the single source of truth shared by the screenshot
 * test and the `@Preview`, so they render identically. A phone and a watch ([DeviceMockup]) plus a
 * chroma-keyed phone photo ([DeviceImageMockup]) are placed freely across a three-screenshot-wide
 * canvas, each with the glass effect, under the per-panel [StoryCaption]s.
 *
 * [chromaFrame] is passed in because the test and the preview load images from different places
 * (the test from the classpath, the preview from the debug assets via the inspection Context).
 * [gap] is the dp gap cut between screenshots; pass the same value to `splitScreenshot`.
 */
@Composable
fun SplitStory(chromaFrame: ImageBitmap, gap: Dp = SplitStoryGap) {
    SplitStoryBanner(
        panels = 3,
        gap = gap,
        devices = {
            // A phone (DeviceMockup) scaled up as the hero, glass on its screen.
            SplitDevice(x = 200.dp, y = 220.dp, height = 720.dp, rotation = 60f) {
                DeviceMockup(FormFactor.Phone) {
                    CounterScreen(
                        count = 42,
                        modifier = Modifier.screenGlass(),
                        background = Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFF7C2D12))),
                    )
                }
            }
            // A chroma-keyed phone photo (DeviceImageMockup) straddling the second seam; the glass
            // is warped into the device plane along with the screen content.
            SplitDevice(x = 520.dp, y = 190.dp, height = 700.dp, rotation = -10f) {
                DeviceImageMockup(
                    frame = chromaFrame,
                    screens = listOf(
                        Screen {
                            CounterScreen(
                                count = 7,
                                modifier = Modifier.screenGlass(),
                                background = Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1E3A8A))),
                            )
                        },
                    ),
                    modifier = Modifier.fillMaxHeight(),
                    screenColor = Color.Green,
                    screenColorTolerance = 0.2f,
                )
            }
            // A watch — a different form factor via DeviceMockup — in the third screenshot. The
            // watch screen has no Modifier slot, so the glass goes on a Box wrapping it.
            SplitDevice(x = 940.dp, y = 320.dp, height = 300.dp, rotation = 20f) {
                DeviceMockup(FormFactor.Wear) {
                    Box(Modifier.fillMaxSize().screenGlass(reflexAlpha = 0.16f, shadow = GlassShadow.None)) {
                        WearCounterScreen(
                            count = 99,
                            background = Brush.radialGradient(listOf(Color(0xFF059669), Color(0xFF064E3B))),
                        )
                    }
                }
            }
        },
        caption = { index -> StoryCaption(index) },
    )
}

/**
 * Places a single mockup on the canvas at an explicit position and rotation. Use it in the
 * [SplitStoryBanner] `devices` layer so each device's placement is spelled out at the call site:
 *
 * ```kotlin
 * devices = {
 *     SplitDevice(x = 251.dp, y = 230.dp, height = 640.dp, rotation = -15f) { Mockup { … } }
 *     SplitDevice(x = 662.dp, y = 230.dp, height = 640.dp, rotation = -15f) { Mockup { … } }
 * }
 * ```
 *
 * [x]/[y] are measured from the top-left of the whole (multi-screenshot) canvas, [height] sizes the
 * device (its width follows the device aspect ratio), and [rotation] tilts it in degrees — so a
 * phone centred on a seam straddles two screenshots.
 */
@Composable
fun BoxScope.SplitDevice(
    x: Dp,
    y: Dp,
    height: Dp,
    rotation: Float,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .align(Alignment.TopStart)
            .offset(x = x, y = y)
            .height(height)
            .rotate(rotation)
    ) { content() }
}

/**
 * The per-panel captions used by the split example, shared between the screenshot test and the
 * `@Preview` so they stay identical. Each branch is an independent composable with its own colours,
 * font family, and background treatment — showing that a [SplitStoryBanner] caption can be styled
 * however you like and differ completely per screenshot.
 */
@Composable
fun BoxScope.StoryCaption(index: Int) {
    val ink = Color(0xFF0F0A1F)
    val amber = Color(0xFFFBBF24)
    when (index) {
        // Panel 1 — stacked highlight blocks (white knockout + an amber accent line), heavy sans.
        0 -> Column(
            Modifier.align(Alignment.TopStart).padding(start = 40.dp, top = 86.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            HighlightLine("COUNT", background = Color.White, color = ink, fontFamily = FontFamily.SansSerif, fontSize = 46.sp)
            HighlightLine("EVERY", background = Color.White, color = ink, fontFamily = FontFamily.SansSerif, fontSize = 46.sp)
            HighlightLine("TAP.", background = amber, color = ink, fontFamily = FontFamily.SansSerif, fontSize = 46.sp)
        }
        // Panel 2 — a serif headline on the gradient, with the last word in the amber accent.
        1 -> Column(Modifier.align(Alignment.TopStart).padding(start = 40.dp, top = 92.dp)) {
            Text("Share", color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black, fontSize = 50.sp)
            Text("it with", color = Color.White, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black, fontSize = 50.sp)
            Text("friends.", color = amber, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black, fontSize = 50.sp)
        }
        // Panel 3 — a monospace label on a solid purple card: a different font and background again.
        else -> Column(
            Modifier
                .align(Alignment.TopStart)
                .padding(start = 28.dp, top = 88.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF4C1D95))
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Text("KEEP YOUR", color = amber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 4.sp)
            Spacer(Modifier.height(4.dp))
            Text("STREAK", color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 40.sp)
        }
    }
}

/** A word set on a solid highlight block — the knockout-headline look (white or accent fill). */
@Composable
private fun HighlightLine(
    text: String,
    background: Color,
    color: Color,
    fontFamily: FontFamily,
    fontSize: TextUnit,
) {
    Text(
        text = text,
        color = color,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Black,
        fontSize = fontSize,
        modifier = Modifier.background(background).padding(horizontal = 10.dp, vertical = 1.dp),
    )
}
