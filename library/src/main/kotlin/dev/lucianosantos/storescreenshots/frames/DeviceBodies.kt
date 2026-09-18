package dev.lucianosantos.storescreenshots.frames

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.frames.IPadAir13Metrics as IPad

/**
 * The solid each built-in frame is the front face of, built from the measurements that frame
 * already draws with.
 *
 * Nothing here invents a dimension. A body's outline, its corner radius and its buttons come
 * straight out of the device's own metrics, so the extruded shape is the shape the bezel clips
 * itself to and the side walls meet the front face exactly. The only figure that is new is the
 * thickness, which no flat frame ever had a use for — see each metrics object for where it is from.
 */

/**
 * The generic Android phone, at the size the mockup lays it out — which is the device's own
 * 411x822 turned on its side for a landscape mockup. Thickness comes off the *narrow* side either
 * way, because a phone laid on its side does not get deeper.
 */
internal fun androidPhoneBody(bodyWidth: Dp, bodyHeight: Dp): DeviceBody = with(AndroidPhoneMetrics) {
    val thickness = minOf(bodyWidth, bodyHeight) * ThicknessRatio
    DeviceBody(
        width = bodyWidth,
        height = bodyHeight,
        cornerRadius = BodyCorner,
        thickness = thickness,
        screen = ScreenSpec(inset = Rim + Bezel, corner = ScreenCorner),
        railColor = RailColor,
        rimColor = RimColor,
        backColor = BackColor,
        buttons = LeftButtons.map { (top, height) -> button(RailEdge.Left, top, height, thickness) } +
            RightButtons.map { (top, height) -> button(RailEdge.Right, top, height, thickness) },
        features = bottomEdgeFeatures(BottomEdge, bodyWidth, unit = 1f),
    )
}

/**
 * The connector and the speaker grille either side of it, laid out along the bottom rail from
 * [metrics], with the body's own width deciding where the centre line falls.
 */
private fun bottomEdgeFeatures(
    metrics: BottomEdgeMetrics,
    bodyWidth: Dp,
    unit: Float,
): List<RailFeature> {
    val centre = bodyWidth / 2f
    fun pt(value: Float): Dp = (value * unit).dp
    val connector = RailFeature(
        edge = RailEdge.Bottom,
        centre = centre,
        width = pt(metrics.ConnectorWidth),
        depth = depthAround(metrics.ConnectorDepth),
        corner = pt(metrics.ConnectorWidth) / 2f,
        color = metrics.CutColor,
    )
    val holes = (0 until metrics.SpeakerHolesPerSide).flatMap { index ->
        val offset = pt(metrics.SpeakerFirstOffset + metrics.SpeakerHolePitch * index)
        listOf(-1, 1).map { side ->
            RailFeature(
                edge = RailEdge.Bottom,
                centre = centre + offset * side.toFloat(),
                width = pt(metrics.SpeakerHoleWidth),
                depth = depthAround(metrics.SpeakerHoleDepth),
                corner = pt(metrics.SpeakerHoleWidth) / 2f,
                color = metrics.CutColor,
            )
        }
    }
    return listOf(connector) + holes
}

/** A span of [size] (as a fraction of the body's depth) centred in the rail. */
private fun depthAround(size: Float): ClosedFloatingPointRange<Float> {
    val half = (size / 2f).coerceIn(0.02f, 0.49f)
    return (0.5f - half)..(0.5f + half)
}

/**
 * How much a button's boss is rounded, as a fraction of its depth.
 *
 * Deliberately not the `ButtonCorner` the metrics carry. That figure is the rounding on the outer
 * *end* of the thin 2D sliver a flat frame draws — a point or two, because face-on that sliver is
 * only a few points wide. The boss a tilted device shows is a different shape entirely: a strip as
 * long as the button and as deep as the rail, whose ends are visibly radiused on real hardware.
 * Reusing the flat figure here leaves them looking machined with a square file.
 */
private const val ButtonRounding = 0.38f

/**
 * Where a button of [depth] sits through a body of [thickness], as a fraction of it.
 *
 * Centred, and never allowed to fill the rail: a button is a strip milled into the metal, and one
 * spanning the whole depth reads as a slab stuck on the side rather than part of the device.
 */
private fun buttonDepth(depth: Dp, thickness: Dp): ClosedFloatingPointRange<Float> =
    depthAround((depth.value / thickness.value).coerceIn(0.1f, 0.6f))

private fun button(edge: RailEdge, start: Dp, length: Dp, thickness: Dp): RailButton = with(AndroidPhoneMetrics) {
    RailButton(
        edge = edge,
        start = start,
        length = length,
        protrusion = ButtonProtrusion,
        corner = ButtonDepth * ButtonRounding,
        across = buttonDepth(ButtonDepth, thickness),
        // A button is milled out of the same metal as the rail it sits in, so in 3D it takes the
        // rail's own colour. The flat bezel paints a darker face instead, because with no geometry
        // to show the boss standing proud, shading it darker is the only way to read it as separate.
        face = RailColor,
        shadow = ButtonShadow,
    )
}

/**
 * The generic Android tablet, at whatever body the slot sizes it to. It carries no buttons, because
 * the flat frame draws none and this is not the place to invent hardware.
 */
internal fun androidTabletBody(bodyWidth: Dp, bodyHeight: Dp): DeviceBody = with(AndroidTabletMetrics) {
    DeviceBody(
        width = bodyWidth,
        height = bodyHeight,
        cornerRadius = BodyCorner,
        thickness = minOf(bodyWidth, bodyHeight) * ThicknessRatio,
        screen = ScreenSpec(inset = Inset, corner = ScreenCorner),
        railColor = RailColor,
        rimColor = RimColor,
        backColor = BackColor,
    )
}

/**
 * An iPhone, as a scale model of [metrics]. Every figure is scaled by the same "one device point in
 * this frame's dp" unit the bezel derives from the body's width, so the solid and the face it wraps
 * stay in step however large the mockup is drawn.
 */
internal fun iPhoneBody(metrics: IPhoneMetrics, bodyWidth: Dp, bodyHeight: Dp): DeviceBody {
    val u = bodyWidth.value / metrics.BodyWidth
    fun pt(value: Float): Dp = (value * u).dp
    val thickness = pt(metrics.BodyThickness)
    val across = buttonDepth(pt(metrics.ButtonDepth), thickness)
    val bossCorner = pt(metrics.ButtonDepth) * ButtonRounding
    return DeviceBody(
        width = bodyWidth,
        height = bodyHeight,
        cornerRadius = pt(metrics.BodyCorner),
        thickness = thickness,
        screen = ScreenSpec(inset = pt(metrics.Bezel), corner = pt(metrics.ScreenCorner)),
        // The rail's own face, the bright machined edge along it, and the black the body falls away
        // to at the back — the three shades the bezel already uses for the same aluminium.
        railColor = metrics.RailColor,
        rimColor = metrics.RimColor,
        backColor = metrics.BezelColor,
        buttons = metrics.LeftButtons.map { (top, height) ->
            appleButton(metrics, RailEdge.Left, pt(top), pt(height), pt(metrics.LeftButtonProtrusion), bossCorner, across)
        } + metrics.RightButtons.map { (top, height) ->
            appleButton(metrics, RailEdge.Right, pt(top), pt(height), pt(metrics.RightButtonProtrusion), bossCorner, across)
        },
        features = bottomEdgeFeatures(metrics.BottomEdge, bodyWidth, u),
    )
}

/** An iPad. Its power button lies along the *top* edge rather than a side, so it exercises that rail. */
internal fun iPadBody(bodyWidth: Dp, bodyHeight: Dp): DeviceBody {
    val u = bodyWidth.value / IPad.BodyWidth
    fun pt(value: Float): Dp = (value * u).dp
    val (topLeft, topWidth) = IPad.TopButton
    val thickness = pt(IPad.BodyThickness)
    val across = buttonDepth(pt(IPad.ButtonDepth), thickness)
    val bossCorner = pt(IPad.ButtonDepth) * ButtonRounding
    return DeviceBody(
        width = bodyWidth,
        height = bodyHeight,
        cornerRadius = pt(IPad.BodyCorner),
        thickness = thickness,
        screen = ScreenSpec(inset = pt(IPad.Bezel), corner = pt(IPad.ScreenCorner)),
        railColor = IPad.RailColor,
        rimColor = IPad.RimColor,
        backColor = IPad.BezelColor,
        buttons = IPad.RightButtons.map { (top, height) ->
            RailButton(
                edge = RailEdge.Right,
                start = pt(top),
                length = pt(height),
                protrusion = pt(IPad.RightButtonProtrusion),
                corner = bossCorner,
                across = across,
                face = IPad.RailColor,
                shadow = IPad.ButtonShadowColor,
            )
        } + RailButton(
            edge = RailEdge.Top,
            start = pt(topLeft),
            length = pt(topWidth),
            protrusion = pt(IPad.TopButtonProtrusion),
            corner = bossCorner,
            across = across,
            face = IPad.RailColor,
            shadow = IPad.ButtonShadowColor,
        ),
    )
}

private fun appleButton(
    metrics: IPhoneMetrics,
    edge: RailEdge,
    start: Dp,
    length: Dp,
    protrusion: Dp,
    corner: Dp,
    across: ClosedFloatingPointRange<Float>,
): RailButton = RailButton(
    edge = edge,
    start = start,
    length = length,
    protrusion = protrusion,
    corner = corner,
    across = across,
    // A button is milled from the same aluminium as the rail, so it shares its face and the shade
    // it drops to where it tucks under the enclosure.
    face = metrics.RailColor,
    shadow = metrics.ButtonShadowColor,
)
