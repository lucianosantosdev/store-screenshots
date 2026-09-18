package dev.lucianosantos.storescreenshots.frames

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The generic Android phone and tablet enclosures, gathered in one place.
 *
 * Unlike the Apple frames these are not scale models of a named device — they are a neutral
 * Android-shaped body, and the figures were chosen by eye when the frames were written rather than
 * measured off hardware. They are collected here because the solid renderer needs the same numbers
 * the bezels draw with: the outline it extrudes has to be the outline the bezel clips itself to, or
 * the side walls would not meet the front face. Everything here is exactly what `PhoneBezel` and
 * `TabletBezel` used inline before, so moving them changes no pixels — `PhoneFrameGoldenTest` is
 * what holds that claim.
 *
 * The one genuinely new figure is [AndroidPhoneMetrics.ThicknessRatio] / [AndroidTabletMetrics.ThicknessRatio],
 * which no flat frame ever needed. See their KDoc for where the numbers come from.
 */
internal object AndroidPhoneMetrics {

    /** Native body size. The phone frame's screen and its body are the same rectangle. */
    val BodyWidth: Dp = 411.dp
    val BodyHeight: Dp = 822.dp

    val BodyCorner: Dp = 42.dp

    /** The bright outer rim, and the radius left once it is inset. */
    val Rim: Dp = 1.5.dp
    val RimCorner: Dp = 40.dp

    /** The black bezel between the rim and the display, and the display's own radius. */
    val Bezel: Dp = 7.dp
    val ScreenCorner: Dp = 32.dp

    /** The enclosure's gradient runs between these two, so they are also the rail's lit and unlit faces. */
    val RailColor = Color(0xFF3A3A3A)
    val BackColor = Color(0xFF1A1A1A)

    /** The machined highlight along the outer edge of the rail. */
    val RimColor = Color(0xFF6E6E6E)

    /**
     * Body thickness as a fraction of its width. A current large Android phone is about 8.5 mm
     * thick and 77 mm wide (a Pixel 9 Pro XL is 8.5 x 76.6), so 0.110 — which puts a 411dp-wide
     * body at a little over 45dp deep.
     */
    const val ThicknessRatio = 0.110f

    /** Side buttons: how wide the boss is, and how much of that stands proud of the body. */
    val ButtonWidth: Dp = 5.dp
    val ButtonProtrusion: Dp = 3.dp
    val ButtonCorner: Dp = 2.dp

    /**
     * How tall a button is across the body's depth. A volume button is a slim strip milled into the
     * rail — about 2.7 mm on a 77 mm-wide phone — not a slab spanning it.
     */
    val ButtonDepth: Dp = BodyWidth * (2.7f / 77f)

    /** Volume up then volume down, on the left edge: (top edge from the body's top, height). */
    val LeftButtons: List<Pair<Dp, Dp>> = listOf(110.dp to 38.dp, 156.dp to 58.dp)

    /** The power button, on the right edge. */
    val RightButtons: List<Pair<Dp, Dp>> = listOf(92.dp to 70.dp)

    /** A button's face, and the shade it drops to where it tucks under the enclosure. */
    val ButtonFace = Color(0xFF2E2E2E)
    val ButtonShadow = Color(0xFF0F0F0F)

    /**
     * The port and speaker grille in the bottom rail, from the same hardware figures the Apple
     * frames use — a USB-C connector is a USB-C connector. A large Android phone is about 77 mm
     * wide, which is what [BodyWidth] stands in for here.
     */
    val BottomEdge = BottomEdgeMetrics(BodyWidth.value / 77f, BodyWidth.value * ThicknessRatio)
}

/** The neutral Android tablet enclosure. Sized by its body; the screen is inset on all four sides. */
internal object AndroidTabletMetrics {

    val Rim: Dp = 2.dp
    val Bezel: Dp = 8.dp

    /** How far the screen sits inside the body's edge, on every side. */
    val Inset: Dp = Rim + Bezel

    val BodyCorner: Dp = 28.dp
    val RimCorner: Dp = 26.dp
    val ScreenCorner: Dp = 20.dp

    val RailColor = Color(0xFF3A3A3A)
    val BackColor = Color(0xFF1A1A1A)
    val RimColor = Color(0xFF6E6E6E)

    /**
     * Body thickness as a fraction of its width. Tablets are proportionally far thinner than phones:
     * a 10-inch Android tablet is about 6.5 mm thick across 254 mm of width, so 0.024 — a 820dp body
     * comes out about 20dp deep.
     */
    const val ThicknessRatio = 0.024f
}
