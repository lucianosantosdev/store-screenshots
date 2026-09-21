package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.lucianosantos.storescreenshots.ScaledMockup
import dev.lucianosantos.storescreenshots.MockupMaterial
import dev.lucianosantos.storescreenshots.ScreenshotStyle
import dev.lucianosantos.storescreenshots.mockup3dRotation
import android.graphics.Matrix as AndroidMatrix

/**
 * Draws a device as a solid object rather than a flat card: the live [bezel] is warped onto the
 * projected front face, and the side walls the tilt turns toward the viewer are drawn as projected
 * polygons behind it, so the device reads as something with real thickness.
 *
 * ## How the live screen survives being tilted
 *
 * [bezel] is composed at its native size into a [rememberGraphicsLayer], then replayed through the
 * homography that carries the native rectangle onto the projected front face. A plane's perspective
 * image *is* a homography, so four corner correspondences reproduce every interior point exactly —
 * the screen is not approximated, and it stays a live composable rather than becoming a bitmap. On
 * the software canvas that Roborazzi and the IDE preview both render through, replaying the layer
 * re-executes its recorded drawing under the canvas transform, so text and rounded clips are drawn
 * *through* the projection as vectors instead of being rasterised and resampled.
 *
 * The same [DeviceProjection] model produces both the front quad and the side walls, so the two
 * agree by construction — nothing here has to reverse-engineer what `Modifier.graphicsLayer` does
 * internally, and nothing drifts when they disagree, because they cannot.
 *
 * ## What [bezel] must not draw
 *
 * Anything a bezel would normally put *outside* its own bounds has to be suppressed here and drawn
 * in 3D instead — a [rememberGraphicsLayer] does not clip, so it would otherwise be warped along
 * with the front face and land in the wrong place:
 *
 * - **Side buttons** belong on the rail they are milled into, not on the front plane.
 * - **The cast shadow** is a `BlurMaskFilter`, which works in device space and does not compose
 *   with a projective transform at all; the router passes `elevation = 0.dp` down and this draws
 *   the shadow itself from the projected silhouette.
 *
 * See [BezelChrome], which is how the router tells a bezel what to leave out.
 */
@Composable
internal fun SolidDeviceMockup(
    nativeWidth: Dp,
    nativeHeight: Dp,
    modifier: Modifier,
    body: DeviceBody,
    tilt: MockupTilt,
    lighting: MockupLighting,
    elevation: Dp,
    bezel: @Composable () -> Unit,
) {
    val layer = rememberGraphicsLayer()
    val clamped = tilt.clamped()

    Layout(
        content = {
            // The bezel, composed at its native size, recorded, and drawn nowhere: this
            // drawWithContent deliberately never calls drawContent() on its own canvas, so the only
            // place the content lands is the layer.
            Box(
                Modifier.drawWithContent {
                    layer.record(IntSize(size.width.toInt(), size.height.toInt())) {
                        this@drawWithContent.drawContent()
                    }
                }
            ) { bezel() }

            Canvas(Modifier) { drawSolidDevice(layer, body, clamped, lighting, elevation, nativeWidth, nativeHeight) }
        },
        // Same footprint contract as ScaledMockup: the caller bounds one dimension and the device's
        // own aspect ratio supplies the other.
        modifier = modifier.aspectRatio(nativeWidth / nativeHeight, matchHeightConstraintsFirst = true),
    ) { measurables, constraints ->
        val nativeW = nativeWidth.roundToPx()
        val nativeH = nativeHeight.roundToPx()
        val recorder = measurables[0].measure(Constraints.fixed(nativeW, nativeH))
        val targetW = if (constraints.hasBoundedWidth) constraints.maxWidth else nativeW
        val targetH = if (constraints.hasBoundedHeight) constraints.maxHeight else nativeH
        val canvas = measurables[1].measure(Constraints.fixed(targetW, targetH))
        layout(targetW, targetH) {
            // The recorder is placed first so its layer is filled before the canvas replays it.
            recorder.place(0, 0)
            canvas.place(0, 0)
        }
    }
}

/**
 * Painter order for one solid device. Everything physical is drawn before the front face, which is
 * safe without depth-sorting the two against each other: a visible side wall of a convex body whose
 * front face still points at the camera can never project inside that face. `DeviceProjectionTest`
 * checks that numerically across the supported range.
 */
private fun DrawScope.drawSolidDevice(
    layer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    body: DeviceBody,
    tilt: MockupTilt,
    lighting: MockupLighting,
    elevation: Dp,
    nativeWidth: Dp,
    nativeHeight: Dp,
) {
    val nativeW = nativeWidth.toPx()
    val nativeH = nativeHeight.toPx()
    if (nativeW <= 0f || nativeH <= 0f) return

    val pivot = Offset(size.width / 2f, size.height / 2f)
    // Never let the camera sit closer than the mockup's longest side: hwui's own advice, and what
    // keeps a small mockupCameraDistance from folding the geometry through the near plane.
    val cameraPx = maxOf(tilt.cameraPx(density), size.width, size.height)

    val quad = projectFrontQuad(size.width, size.height, tilt, cameraPx, pivot)
    val homography = quad?.let {
        homography(floatArrayOf(0f, 0f, nativeW, 0f, nativeW, nativeH, 0f, nativeH), it)
    }
    if (homography == null) {
        // Degenerate geometry — an angle at the edge of the supported range, or a footprint of
        // nothing. Fall back to the flat, unrotated mockup rather than drawing nonsense.
        drawRecordedFace(layer, scaleOnly(nativeW, nativeH))
        return
    }

    // The body's outline, in the footprint's own pixels so it shares the front face's projection.
    val scale = size.width / nativeW
    val cornerPx = body.cornerRadius.toPx() * scale
    val ring = projectRing(
        samples = sampleRoundRectRing(size.width, size.height, cornerPx, cornerSamplesFor(cornerPx)),
        thickness = body.thickness.toPx() * scale,
        tilt = tilt,
        cameraPx = cameraPx,
        pivot = pivot,
    )

    val matrix = AndroidMatrix().apply { setValues(homography) }
    if (ring != null && elevation > 0.dp) drawProjectedShadow(ring, elevation, lighting)
    if (ring != null) drawRails(ring, body, tilt, lighting)
    drawRailFeatures(body, tilt, lighting, cameraPx, pivot, scale)
    drawRailButtons(body, tilt, lighting, cameraPx, pivot, scale)
    drawRecordedFace(layer, matrix)
    if (ring != null) drawFrontBevel(ring, body, lighting)
    drawScreenOcclusion(body, matrix, nativeW, nativeH)
    drawGlassSheen(body, tilt, lighting, matrix, nativeW, nativeH)
}

/**
 * A hairline along the front face's outline, brightest where the light grazes it.
 *
 * The rails have their own highlight along the *outer* edge of the body; this is the near edge,
 * where the bezel's front surface turns over into the rail. Without it a tilted device reads as a
 * picture of a phone pasted onto a block — this is the line that says the two are the same object.
 * It follows the body's own outline, so it fades around the silhouette as the light does instead of
 * being a uniform stroke.
 */
private fun DrawScope.drawFrontBevel(ring: ProjectedRing, body: DeviceBody, lighting: MockupLighting) {
    val n = ring.front.size
    (0 until n).forEach { i ->
        val lit = (ring.normals[i] dot lighting.unit).coerceAtLeast(0f)
        if (lit <= 0.05f) return@forEach
        val j = (i + 1) % n
        drawLine(
            color = body.rimColor.copy(alpha = (lit * BevelStrength).coerceIn(0f, 1f)),
            start = ring.front[i],
            end = ring.front[j],
            strokeWidth = BevelWidth,
        )
    }
}

/**
 * The thin shadow the bezel casts onto the display it surrounds. A real screen sits a fraction below
 * the glass, so its edge is never quite as bright as its middle; drawn through the same homography
 * as the face, so it follows the screen's rounded corners at any angle.
 */
private fun DrawScope.drawScreenOcclusion(
    body: DeviceBody,
    matrix: AndroidMatrix,
    nativeW: Float,
    nativeH: Float,
) {
    val inset = body.screen.inset.toPx()
    if (inset <= 0f) return
    val left = inset
    val top = inset
    val right = nativeW - inset
    val bottom = nativeH - inset
    if (right - left <= 1f || bottom - top <= 1f) return
    val corner = body.screen.corner.toPx()
    val screen = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = left, top = top, right = right, bottom = bottom,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            )
        )
    }
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val checkpoint = native.save()
        native.concat(matrix)
        native.clipPath(screen.asAndroidPath())
        drawPath(
            screen,
            Color.Black.copy(alpha = ScreenOcclusionAlpha),
            style = Stroke(ScreenOcclusionWidth),
        )
        native.restoreToCount(checkpoint)
    }
}

/**
 * The shadow a solid device casts, in the shape of the silhouette it actually presents once tilted
 * rather than of the rectangle it started as.
 *
 * Painted by hand for the same reason [mockupShadow] is — the platform's elevation shadows need a
 * hardware canvas and come out missing on the software one a screenshot is rendered through — and
 * it has to be drawn out here rather than inside the bezel, because a `BlurMaskFilter` works in
 * device space and does not compose with a projective transform at all. Offset away from the light
 * instead of straight down, so the shadow and the shading on the rails agree about where the light
 * is.
 */
private fun DrawScope.drawProjectedShadow(ring: ProjectedRing, elevation: Dp, lighting: MockupLighting) {
    val blur = elevation.toPx()
    if (blur <= 0f) return
    val hull = convexHull(ring.front.toList() + ring.back.toList())
    if (hull.size < 3) return

    val path = android.graphics.Path().apply {
        moveTo(hull[0].x, hull[0].y)
        hull.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = Color.Black.copy(alpha = ShadowAlpha).toArgb()
        maskFilter = android.graphics.BlurMaskFilter(blur, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    val light = lighting.unit
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val checkpoint = native.save()
        native.translate(-light.x * blur * ShadowDrop, -light.y * blur * ShadowDrop)
        native.drawPath(path, paint)
        native.restoreToCount(checkpoint)
    }
}

/**
 * A soft sheen on the cover glass that moves as the device turns.
 *
 * This is the renderer's own layer, drawn on top of the warped face and clipped to the display. It
 * does not touch [dev.lucianosantos.storescreenshots.GlassEffect]: a caller's reflex angle,
 * position, width and alpha are drawn inside the screen content and warped along with it, so they
 * already follow the tilt and are never overridden. What this adds is the part a flat effect cannot
 * know about — where the light is relative to a surface that has turned.
 *
 * The screen's shape comes for free: it is built as a rounded rectangle in the bezel's own native
 * coordinates and then run through the very same homography the face was, corner curvature and all.
 */
private fun DrawScope.drawGlassSheen(
    body: DeviceBody,
    tilt: MockupTilt,
    lighting: MockupLighting,
    matrix: AndroidMatrix,
    nativeW: Float,
    nativeH: Float,
) {
    if (lighting.specular <= 0f) return
    val inset = body.screen.inset.toPx()
    val left = inset
    val top = inset
    val right = nativeW - inset
    val bottom = nativeH - inset
    if (right - left <= 1f || bottom - top <= 1f) return

    // How strongly this surface, at this angle, throws the light back at the viewer.
    val normal = tilt.rotate(Vec3(0f, 0f, 1f)).normalized()
    val half = Vec3(lighting.unit.x, lighting.unit.y, lighting.unit.z + 1f).normalized()
    val strength = lighting.specular *
        Math.pow((normal dot half).coerceAtLeast(0f).toDouble(), lighting.shininess.toDouble()).toFloat()
    if (strength < 0.004f) return

    // The light's direction within the face's own plane, which is what decides where on the glass
    // the sheen sits and which way it runs.
    val alongX = lighting.unit dot tilt.rotate(Vec3(1f, 0f, 0f)).normalized()
    val alongY = lighting.unit dot tilt.rotate(Vec3(0f, 1f, 0f)).normalized()
    val length = kotlin.math.hypot(alongX, alongY)
    if (length < 1e-3f) return
    val dx = alongX / length
    val dy = alongY / length

    val corner = body.screen.corner.toPx()
    val screen = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = left, top = top, right = right, bottom = bottom,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            )
        )
    }
    val centreX = (left + right) / 2f
    val centreY = (top + bottom) / 2f
    val reach = kotlin.math.hypot(right - left, bottom - top) / 2f
    val start = Offset(centreX + dx * reach, centreY + dy * reach)
    val end = Offset(centreX - dx * reach, centreY - dy * reach)

    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val checkpoint = native.save()
        native.concat(matrix)
        native.clipPath(screen.asAndroidPath())
        drawRect(
            brush = Brush.linearGradient(
                0f to Color.White.copy(alpha = strength),
                0.55f to Color.White.copy(alpha = strength * 0.25f),
                1f to Color.Transparent,
                start = start,
                end = end,
            ),
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
        )
        native.restoreToCount(checkpoint)
    }
}

/** Opacity of the cast shadow at its darkest, and how far it slides away from the light. */
private const val ShadowAlpha = 0.45f
private const val ShadowDrop = 0.45f

/**
 * The side walls the tilt has turned toward the viewer.
 *
 * Each is the quad between one segment of the body's outline at the front face and the same segment
 * at the back of the body, so left, right, top and bottom rails — and the rounded corners that join
 * them — all fall out of one loop over the outline rather than out of a chain of `if (rotationY <
 * 0)`. Which ones are drawn comes from their projected winding, so it is the geometry that decides,
 * and a combined tilt needs no special case.
 *
 * Drawn in two passes per run of adjacent visible segments. The first fills the whole run as a
 * single antialiased path, which is what keeps the outer silhouette smooth and leaves no hairline
 * gaps between neighbours. The second lays the individual segments over it *without* antialiasing,
 * carrying the shading that varies along the run; because the base is already solid underneath,
 * their untouched edges cannot show through.
 */
private fun DrawScope.drawRails(
    ring: ProjectedRing,
    body: DeviceBody,
    tilt: MockupTilt,
    lighting: MockupLighting,
) {
    // The chamfer is a bevel rolling from the front face over onto the rail, so the direction it
    // faces is halfway between the two. That is why it stays bright on a rail that is otherwise
    // turned away from the light: a 45-degree bevel finds the light long after the flat beside it
    // has lost it — which is exactly what makes a machined edge legible on a dark device.
    val faceNormal = tilt.rotate(Vec3(0f, 0f, 1f)).normalized()
    val backNormal = Vec3(-faceNormal.x, -faceNormal.y, -faceNormal.z)
    val visible = ring.visibleSegments()
    if (visible.isEmpty()) return
    val n = ring.front.size

    // The band's inner edge is our sampled polygon while the front face's edge is the bezel's own
    // antialiased rounded rect, and the two disagree by a fraction of a pixel. Pushing the inner
    // edge a pixel further under the face closes that seam; the face is drawn last and covers it.
    val inner = Array(n) { i ->
        val f = ring.front[i]
        val b = ring.back[i]
        val dx = f.x - b.x
        val dy = f.y - b.y
        val length = kotlin.math.hypot(dx, dy)
        if (length < 1e-3f) f else Offset(f.x + dx / length * SeamOverlap, f.y + dy / length * SeamOverlap)
    }

    contiguousRuns(visible, n).forEach { run ->
        val path = Path()
        run.forEachIndexed { index, segment ->
            val p = inner[segment]
            if (index == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }
        val last = (run.last() + 1) % n
        path.lineTo(inner[last].x, inner[last].y)
        path.lineTo(ring.back[last].x, ring.back[last].y)
        for (index in run.indices.reversed()) {
            val p = ring.back[run[index]]
            path.lineTo(p.x, p.y)
        }
        path.close()

        // Base pass: one antialiased fill for the whole run, at its mean shade.
        val mean = run.map { lighting.shadeFactor(ring.normals[it]) }.average().toFloat()
        drawPath(path, tint(body.railColor, mean))

        // Detail pass: each wall as a single fill whose colour runs the depth of the rail.
        //
        // The stops are not a colour ramp picked by eye — each one is the rail *shaded at the
        // direction that surface actually faces at that depth*, and the direction rolls smoothly
        // from the front face, round through the rail's own, to the back (see [filletNormal]). That
        // is what makes the join to the glass read as a rounded edge rather than a mitre: a real
        // machined edge has no boundary between the chamfer and the flat beside it, because it is
        // one continuously curving surface, and a flat band of highlight with a hard line at each
        // side is exactly what it does not look like.
        run.forEach { segment ->
            val next = (segment + 1) % n
            val quad = Path().apply {
                moveTo(inner[segment].x, inner[segment].y)
                lineTo(inner[next].x, inner[next].y)
                lineTo(ring.back[next].x, ring.back[next].y)
                lineTo(ring.back[segment].x, ring.back[segment].y)
                close()
            }
            val front = Offset(
                (inner[segment].x + inner[next].x) / 2f,
                (inner[segment].y + inner[next].y) / 2f,
            )
            val back = Offset(
                (ring.back[segment].x + ring.back[next].x) / 2f,
                (ring.back[segment].y + ring.back[next].y) / 2f,
            )
            val rail = ring.normals[segment]
            // The gradient has to run *across* the rail and stay constant along it. Its axis is
            // therefore the perpendicular to the rail's own direction, not the line between the two
            // midpoints: those two are not the same once the projection shears a long edge, and
            // using the midpoints slides the shading along the rail's length — the same rail then
            // reads bright at one end and dark at the other.
            val brush = depthAxis(inner[segment], inner[next], front, back)?.let { (start, end) ->
                Brush.linearGradient(
                    colorStops = FilletStops
                        .map { it to filletColor(it, body, lighting, faceNormal, backNormal, rail) }
                        .toTypedArray(),
                    start = start,
                    end = end,
                )
            } ?: SolidColor(tint(body.railColor, lighting.shadeFactor(rail)))
            drawPath(quad, brush, style = Fill)
        }
    }

    // A dark contact line where the rail meets the bezel, and a bright one along the machined outer
    // edge — the two cues that read as a milled edge rather than a painted band.
    contiguousRuns(visible, n).forEach { run ->
        val contact = Path()
        val rim = Path()
        (run + ((run.last() + 1) % n)).forEachIndexed { index, segment ->
            val f = ring.front[segment]
            val b = ring.back[segment]
            if (index == 0) { contact.moveTo(f.x, f.y); rim.moveTo(b.x, b.y) }
            else { contact.lineTo(f.x, f.y); rim.lineTo(b.x, b.y) }
        }
        drawPath(contact, Color.Black.copy(alpha = lighting.contactShadowAlpha), style = Stroke(ContactLineWidth))
        val lit = run.map { lighting.shadeFactor(ring.normals[it]) }.max()
        drawPath(
            rim,
            body.rimColor.copy(alpha = (lit - lighting.ambient).coerceIn(0f, 1f)),
            style = Stroke(RimLineWidth),
        )
    }
}

/**
 * The local frame of one rail: where it begins, and its three axes in the body's own space.
 *
 * A rail is a flat band, so anything living on one — a button standing proud of it, a port milled
 * into it — is much easier to describe in the rail's own two dimensions (along the edge, and across
 * the body's depth) than in the device's. This maps that back out.
 */
private class RailBasis(val origin: Vec3, val along: Vec3, val across: Vec3, val outward: Vec3) {

    /** [along] the rail, [across] its depth, and [out] of it. */
    fun point(along: Float, across: Float, out: Float) = Vec3(
        origin.x + this.along.x * along + this.across.x * across + outward.x * out,
        origin.y + this.along.y * along + this.across.y * across + outward.y * out,
        origin.z + this.along.z * along + this.across.z * across + outward.z * out,
    )

    /** A direction given in the rail's plane, as a direction in the body's space. */
    fun direction(along: Float, across: Float) = Vec3(
        this.along.x * along + this.across.x * across,
        this.along.y * along + this.across.y * across,
        this.along.z * along + this.across.z * across,
    )
}

private fun railBasis(edge: RailEdge, halfWidth: Float, halfHeight: Float): RailBasis = when (edge) {
    RailEdge.Left -> RailBasis(
        Vec3(-halfWidth, -halfHeight, 0f), Vec3(0f, 1f, 0f), Vec3(0f, 0f, -1f), Vec3(-1f, 0f, 0f),
    )
    RailEdge.Right -> RailBasis(
        Vec3(halfWidth, -halfHeight, 0f), Vec3(0f, 1f, 0f), Vec3(0f, 0f, -1f), Vec3(1f, 0f, 0f),
    )
    RailEdge.Top -> RailBasis(
        Vec3(-halfWidth, -halfHeight, 0f), Vec3(1f, 0f, 0f), Vec3(0f, 0f, -1f), Vec3(0f, -1f, 0f),
    )
    RailEdge.Bottom -> RailBasis(
        Vec3(-halfWidth, halfHeight, 0f), Vec3(1f, 0f, 0f), Vec3(0f, 0f, -1f), Vec3(0f, 1f, 0f),
    )
}

/** True when a face at [centre] pointing along [normal] is turned toward the camera. */
private fun facesCamera(normal: Vec3, centre: Vec3, cameraPx: Float): Boolean =
    (normal dot Vec3(-centre.x, -centre.y, cameraPx - centre.z)) > 0f

/**
 * True when a rail on [edge]'s own axis is not merely visible but *wide enough to read*, taking
 * [minWidthPx] as the width that counts — the protrusion of the button asking the question.
 *
 * Both rails of the axis are asked, not just [edge]'s, because the question is whether the device
 * reads as turned about that axis at all. And width, not visibility, is the test: a body centred on
 * the optical axis shows neither side rail until the tilt passes the angle the camera already views
 * that edge from, and for the first few degrees past it the band is a fraction of a pixel. Treating
 * that hairline as a depth cue is what put a real screenshot in the worst place on the curve — a
 * rail nobody could see, and far-side buttons culled as though the turn were obvious. A rail has to
 * be at least as wide as the bump it is explaining away before it can justify removing it.
 */
private fun axisRailReads(
    edge: RailEdge,
    halfWidth: Float,
    halfHeight: Float,
    thickness: Float,
    tilt: MockupTilt,
    cameraPx: Float,
    pivot: Offset,
    minWidthPx: Float,
): Boolean {
    val axis = when (edge) {
        RailEdge.Left, RailEdge.Right -> listOf(RailEdge.Left, RailEdge.Right)
        RailEdge.Top, RailEdge.Bottom -> listOf(RailEdge.Top, RailEdge.Bottom)
    }
    return axis.any { rail ->
        val basis = railBasis(rail, halfWidth, halfHeight)
        val midway = when (rail) {
            RailEdge.Left, RailEdge.Right -> halfHeight
            RailEdge.Top, RailEdge.Bottom -> halfWidth
        }
        val facing = facesCamera(
            normal = tilt.rotate(basis.outward).normalized(),
            centre = tilt.rotate(basis.point(midway, thickness / 2f, 0f)),
            cameraPx = cameraPx,
        )
        if (!facing) return@any false
        // The band is what lies between the front face's edge and the back's, at the rail's middle.
        val front = project(tilt.rotate(basis.point(midway, 0f, 0f)), cameraPx, pivot)
        val back = project(tilt.rotate(basis.point(midway, thickness, 0f)), cameraPx, pivot)
        front != null && back != null && (front - back).getDistance() >= minWidthPx
    }
}

/**
 * Where a button's boss sits through the body's depth.
 *
 * Normally the middle of the rail, which is where a button is milled. But that puts the boss half a
 * body back from the front face, and perspective shrinks anything that far back toward the axis: on
 * a phone the shrink at the body's edge is about 5dp against a 3dp protrusion, so head-on the whole
 * boss falls *inside* the silhouette and there is nothing left to draw. The flat bezel has no depth
 * to shrink and draws the button as a bump on the outline regardless, so the two paths disagreed —
 * a mockup at exactly zero tilt showed its buttons, and the same mockup one degree over lost them
 * until about five, when the turn finally carried the boss back out past the edge.
 *
 * So while [railReads] is false the boss is seated flush against the front face, which is where
 * the flat bezel effectively draws it: the button reads as the same bump either side of zero. Once a rail is
 * wide enough to read, the real seating takes over — by then the turn has moved the boss further
 * out than the seating ever did, so the handover is not something the eye can find.
 *
 * It also settles what a button on the *far* rail does. While no rail reads, both buttons show,
 * because nothing on screen yet says the device is turned. Once one does, the far boss is occluded
 * by the body, which is correct and now legible: the rail that hid it is right there, and wide
 * enough to see.
 */
private fun buttonAcrossCentre(
    button: RailButton,
    thickness: Float,
    acrossSpan: Float,
    railReads: Boolean,
): Float = if (railReads) {
    thickness * (button.across.start + button.across.endInclusive) / 2f
} else {
    // Half a span, not zero: the boss has to sit *behind* the front face and flush with it, so its
    // base lands exactly on the body's own edge. Centring it on the front plane instead leaves half
    // the boss in front of the body, where perspective pushes its base outboard of that edge and
    // opens a gap of background between button and phone.
    acrossSpan / 2f
}

/**
 * The buttons milled into the rails: rounded-ended bosses standing proud of the body.
 *
 * Extruded exactly the way the body itself is — a rounded-rectangle outline sampled in the rail's
 * own plane, pushed outward by the button's protrusion, with the walls between the two rings drawn
 * where they face the viewer and the cap drawn over them. That is what gives a button the rounded
 * ends and the chamfered edge a milled one has, instead of the flat slab a six-sided box produces.
 *
 * Continuity where the two rendering paths meet is [buttonAcrossCentre]'s job, not the culling's:
 * seating the boss at the front plane while no rail shows is what makes a hair of tilt look like no
 * tilt at all. Once a rail does show, a button whose own rail has turned away loses every face and
 * disappears through the ordinary culling below, with no special case for it.
 */
private fun DrawScope.drawRailButtons(
    body: DeviceBody,
    tilt: MockupTilt,
    lighting: MockupLighting,
    cameraPx: Float,
    pivot: Offset,
    scale: Float,
) {
    if (body.buttons.isEmpty()) return
    val halfWidth = size.width / 2f
    val halfHeight = size.height / 2f
    val thickness = body.thickness.toPx() * scale

    body.buttons.forEach { button ->
        val length = button.length.toPx() * scale
        val protrusion = button.protrusion.toPx() * scale
        if (length <= 0f || protrusion <= 0f) return@forEach
        val basis = railBasis(button.edge, halfWidth, halfHeight)

        // The boss's footprint on the rail: as long as the button, as deep as the span it occupies
        // through the body, and rounded at the ends.
        val acrossSpan = thickness * (button.across.endInclusive - button.across.start)
        val acrossCentre = buttonAcrossCentre(
            button = button,
            thickness = thickness,
            acrossSpan = acrossSpan,
            // The button's own protrusion is the yardstick: a rail narrower than the bump it hides
            // is not yet a depth cue. See [axisRailReads].
            railReads = axisRailReads(
                edge = button.edge,
                halfWidth = halfWidth,
                halfHeight = halfHeight,
                thickness = thickness,
                tilt = tilt,
                cameraPx = cameraPx,
                pivot = pivot,
                minWidthPx = protrusion,
            ),
        )
        val alongCentre = button.start.toPx() * scale + length / 2f
        val corner = (button.corner.toPx() * scale).coerceAtMost(minOf(length, acrossSpan) / 2f)
        val outline = sampleRoundRectRing(length, acrossSpan, corner, cornerSamplesFor(corner))

        // The edge between a button's wall and its cap is a radius, not a mitre. Rather than fake
        // that in shading — which blends the two faces together and loses the edge entirely — it is
        // built: the wall stops a fillet short of full height, a chamfer band carries the surface
        // over, and the flat of the cap is inset by the same amount. The cap stays crisp because it
        // is still a flat face; it is simply a slightly smaller one with a rounded lip around it.
        val fillet = minOf(protrusion * FilletOfProtrusion, minOf(length, acrossSpan) * FilletOfSpan)
        val shoulder = (protrusion - fillet).coerceAtLeast(0f)
        val innerOutline = sampleRoundRectRing(
            w = (length - fillet * 2f).coerceAtLeast(1f),
            h = (acrossSpan - fillet * 2f).coerceAtLeast(1f),
            r = (corner - fillet).coerceAtLeast(0f),
            cornerSamples = cornerSamplesFor(corner),
        )

        val base = outline.map {
            tilt.rotate(basis.point(alongCentre + it.x, acrossCentre + it.y, 0f))
        }
        // Where the wall ends and the radius begins.
        val cap = outline.map {
            tilt.rotate(basis.point(alongCentre + it.x, acrossCentre + it.y, shoulder))
        }
        // Where the radius ends and the flat of the cap begins.
        val crest = innerOutline.map {
            tilt.rotate(basis.point(alongCentre + it.x, acrossCentre + it.y, protrusion))
        }
        val wallNormals = outline.map { tilt.rotate(basis.direction(it.nx, it.ny)).normalized() }
        val capNormal = tilt.rotate(basis.outward).normalized()
        // A quarter-round rolled into a single band faces halfway between the two it joins.
        val filletNormals = wallNormals.map {
            Vec3(it.x + capNormal.x, it.y + capNormal.y, it.z + capNormal.z).normalized()
        }

        val baseAt = base.map { project(it, cameraPx, pivot) ?: return@forEach }
        val capAt = cap.map { project(it, cameraPx, pivot) ?: return@forEach }
        val crestAt = crest.map { project(it, cameraPx, pivot) ?: return@forEach }
        val capCentre = crest.fold(Vec3(0f, 0f, 0f)) { a, c -> Vec3(a.x + c.x, a.y + c.y, a.z + c.z) }
            .let { Vec3(it.x / crest.size, it.y / crest.size, it.z / crest.size) }

        // Walls first, cap over them: the cap is the nearest surface of a convex boss.
        //
        // Drawn as one path per run of neighbouring walls rather than one per wall. A boss is small
        // and its rounded ends are sampled finely, so quad-per-wall puts dozens of antialiased
        // edges against each other across a few dozen pixels, and every one of them leaves a
        // fringe: the wall comes out visibly hatched rather than smooth. Merging the run removes
        // the internal edges altogether — there is nothing left to seam.
        val n = outline.size
        val visibleWalls = (0 until n).filter { i ->
            val j = (i + 1) % n
            val centre = Vec3(
                (base[i].x + base[j].x + cap[i].x + cap[j].x) / 4f,
                (base[i].y + base[j].y + cap[i].y + cap[j].y) / 4f,
                (base[i].z + base[j].z + cap[i].z + cap[j].z) / 4f,
            )
            facesCamera(wallNormals[i], centre, cameraPx)
        }
        // The seat first, and only then the boss standing in it. This line lies on the rail's own
        // surface, so the part of it behind the button has to be covered by the button — stroking
        // the whole ring after the cap draws the far half straight over the near face, and the
        // button comes out looking like a wireframe of itself.
        if (visibleWalls.isNotEmpty() || facesCamera(capNormal, capCentre, cameraPx)) {
            val seat = Path().apply {
                moveTo(baseAt[0].x, baseAt[0].y)
                baseAt.drop(1).forEach { lineTo(it.x, it.y) }
                close()
            }
            drawPath(seat, button.shadow.copy(alpha = lighting.contactShadowAlpha), style = Stroke(ContactLineWidth))
        }

        contiguousRuns(visibleWalls, n).forEach { run ->
            val last = (run.last() + 1) % n
            val wall = Path().apply {
                moveTo(baseAt[run.first()].x, baseAt[run.first()].y)
                run.drop(1).forEach { lineTo(baseAt[it].x, baseAt[it].y) }
                lineTo(baseAt[last].x, baseAt[last].y)
                lineTo(capAt[last].x, capAt[last].y)
                for (index in run.indices.reversed()) {
                    lineTo(capAt[run[index]].x, capAt[run[index]].y)
                }
                close()
            }
            // Shaded by the run as a whole, and darkest where it tucks under the enclosure — the
            // shade the flat bezel paints across its buttons, and the one the capture shows.
            val shade = run.map { lighting.shadeFactor(wallNormals[it]) }.average().toFloat()
            val middle = run[run.size / 2]
            val brush = if ((baseAt[middle] - capAt[middle]).getDistance() < 0.5f) {
                SolidColor(tint(button.face, shade))
            } else {
                Brush.linearGradient(
                    0f to tint(button.shadow, shade),
                    1f to tint(button.face, shade),
                    start = baseAt[middle],
                    end = capAt[middle],
                )
            }
            drawPath(wall, brush)
        }

        // The radius itself, run by run, merged the same way the wall is so its own segments cannot
        // seam. Lit by the direction the roll faces, which is halfway between the wall's and the
        // cap's — so it catches light neither of them does and reads as a lip rather than a line.
        contiguousRuns(
            (0 until n).filter { i ->
                val j = (i + 1) % n
                val centre = Vec3(
                    (cap[i].x + cap[j].x + crest[i].x + crest[j].x) / 4f,
                    (cap[i].y + cap[j].y + crest[i].y + crest[j].y) / 4f,
                    (cap[i].z + cap[j].z + crest[i].z + crest[j].z) / 4f,
                )
                facesCamera(filletNormals[i], centre, cameraPx)
            },
            n,
        ).forEach { run ->
            val last = (run.last() + 1) % n
            val band = Path().apply {
                moveTo(capAt[run.first()].x, capAt[run.first()].y)
                run.drop(1).forEach { lineTo(capAt[it].x, capAt[it].y) }
                lineTo(capAt[last].x, capAt[last].y)
                lineTo(crestAt[last].x, crestAt[last].y)
                for (index in run.indices.reversed()) {
                    lineTo(crestAt[run[index]].x, crestAt[run[index]].y)
                }
                close()
            }
            val shade = run.map { lighting.shadeFactor(filletNormals[it]) }.average().toFloat()
            drawPath(band, tint(button.face, shade))
        }
        if (facesCamera(capNormal, capCentre, cameraPx)) {
            val face = Path().apply {
                moveTo(crestAt[0].x, crestAt[0].y)
                crestAt.drop(1).forEach { lineTo(it.x, it.y) }
                close()
            }
            drawPath(face, tint(button.face, lighting.shadeFactor(capNormal)))
            // The hairline around a button's outline is its milled edge catching light, and like the
            // body's own chamfer it keeps a floor of brightness whichever way the button is turned.
            // It is that outline, not the face, that reads as a machined button — the face itself
            // stays as flat as the Simulator capture shows it.
            val polish = ButtonRimFloor + (1f - ButtonRimFloor) *
                ((capNormal dot lighting.unit) * 0.5f + 0.5f).coerceIn(0f, 1f)
            drawPath(
                face,
                body.rimColor.copy(alpha = (polish * ButtonRimStrength).coerceIn(0f, 1f)),
                style = Stroke(RimLineWidth),
            )
        }
    }
}

/**
 * The charge connector and the speaker grille, milled into the rail they sit on.
 *
 * Flat on the rail's surface rather than standing proud of it, so each is simply its outline
 * projected and filled — and each appears only while its rail is turned toward the viewer. A port on
 * the bottom edge therefore shows up exactly when a real one would: on a device tipped far enough
 * forward that you are looking under it.
 */
private fun DrawScope.drawRailFeatures(
    body: DeviceBody,
    tilt: MockupTilt,
    lighting: MockupLighting,
    cameraPx: Float,
    pivot: Offset,
    scale: Float,
) {
    if (body.features.isEmpty()) return
    val halfWidth = size.width / 2f
    val halfHeight = size.height / 2f
    val thickness = body.thickness.toPx() * scale

    body.features.forEach { feature ->
        val width = feature.width.toPx() * scale
        val depth = thickness * (feature.depth.endInclusive - feature.depth.start)
        if (width <= 0.5f || depth <= 0.5f) return@forEach
        val basis = railBasis(feature.edge, halfWidth, halfHeight)
        val outward = tilt.rotate(basis.outward).normalized()

        val alongCentre = feature.centre.toPx() * scale
        val acrossCentre = thickness * (feature.depth.start + feature.depth.endInclusive) / 2f
        val centre = tilt.rotate(basis.point(alongCentre, acrossCentre, 0f))
        if (!facesCamera(outward, centre, cameraPx)) return@forEach

        val corner = (feature.corner.toPx() * scale).coerceAtMost(minOf(width, depth) / 2f)
        val outline = sampleRoundRectRing(width, depth, corner, cornerSamplesFor(corner))
        val points = outline.map {
            project(tilt.rotate(basis.point(alongCentre + it.x, acrossCentre + it.y, 0f)), cameraPx, pivot)
                ?: return@forEach
        }
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }
        drawPath(path, feature.color)
        // A cut in metal catches a little light on its far lip, which is what stops a port reading
        // as a sticker on the rail.
        drawPath(
            path,
            body.rimColor.copy(alpha = ((lighting.shadeFactor(outward) - lighting.ambient) * FeatureRimStrength).coerceIn(0f, 1f)),
            style = Stroke(FeatureRimWidth),
        )
    }
}

/** Monotone-chain convex hull, so a button's silhouette can be stroked as one outline. */
private fun convexHull(points: List<Offset>): List<Offset> {
    if (points.size < 3) return points
    val sorted = points.sortedWith(compareBy({ it.x }, { it.y }))
    fun half(source: List<Offset>): MutableList<Offset> {
        val out = mutableListOf<Offset>()
        source.forEach { p ->
            while (out.size >= 2) {
                val a = out[out.size - 2]
                val b = out[out.size - 1]
                if ((b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x) > 0f) break
                out.removeAt(out.size - 1)
            }
            out += p
        }
        return out
    }
    val lower = half(sorted)
    val upper = half(sorted.reversed())
    lower.removeAt(lower.size - 1)
    upper.removeAt(upper.size - 1)
    return lower + upper
}

/**
 * Groups [visible] segment indices into runs of neighbours around a ring of [n], joining a run that
 * wraps past the end back onto the one that starts at zero.
 */
private fun contiguousRuns(visible: List<Int>, n: Int): List<List<Int>> {
    if (visible.isEmpty()) return emptyList()
    val sorted = visible.sorted()
    val runs = mutableListOf<MutableList<Int>>()
    sorted.forEach { index ->
        val last = runs.lastOrNull()
        if (last != null && index == last.last() + 1) last += index else runs += mutableListOf(index)
    }
    // A run ending on the last segment continues into one starting at the first.
    if (runs.size > 1 && runs.first().first() == 0 && runs.last().last() == n - 1) {
        val head = runs.removeAt(0)
        runs.last().addAll(head)
    }
    return runs
}

/**
 * An axis running square across a rail, from its front edge to its back one.
 *
 * [edgeStart] and [edgeEnd] are the ends of the front edge; [front] and [back] its midpoints. The
 * result is perpendicular to the edge and exactly as long as the rail is deep, so a gradient drawn
 * along it is constant everywhere on a line parallel to the edge — which is what a shaded band
 * across a rail has to be. Null when the rail is edge-on and has no depth to shade.
 */
private fun depthAxis(
    edgeStart: Offset,
    edgeEnd: Offset,
    front: Offset,
    back: Offset,
): Pair<Offset, Offset>? {
    val ex = edgeEnd.x - edgeStart.x
    val ey = edgeEnd.y - edgeStart.y
    val edge = kotlin.math.hypot(ex, ey)
    if (edge < 1e-3f) return null
    // Perpendicular to the edge, turned to point across the rail rather than back over the face.
    var nx = -ey / edge
    var ny = ex / edge
    val depth = (back.x - front.x) * nx + (back.y - front.y) * ny
    if (kotlin.math.abs(depth) < 0.5f) return null
    if (depth < 0f) { nx = -nx; ny = -ny }
    val span = kotlin.math.abs(depth)
    return front to Offset(front.x + nx * span, front.y + ny * span)
}

/**
 * The direction the body's edge faces at [t] of the way through its depth.
 *
 * A device's edge is not three flat faces meeting at two mitres — it is the front, a rolled edge,
 * the flat of the rail, another rolled edge, and the back. Turning the normal smoothly through
 * those rolls, and shading each part of the band by the normal it actually has, is what draws a
 * rounded edge without needing any more geometry than the two rings already give us.
 */
private fun filletNormal(t: Float, face: Vec3, back: Vec3, rail: Vec3): Vec3 = when {
    t <= FrontFillet -> mix(face, rail, smoothStep(t / FrontFillet))
    t >= 1f - BackFillet -> mix(rail, back, smoothStep((t - (1f - BackFillet)) / BackFillet))
    else -> rail
}.normalized()

/** The rail's colour at [t] of the way through its depth, lit by the direction it faces there. */
private fun filletColor(
    t: Float,
    body: DeviceBody,
    lighting: MockupLighting,
    face: Vec3,
    back: Vec3,
    rail: Vec3,
): Color {
    val normal = filletNormal(t, face, back, rail)
    // Polished where it is rolling over, flat metal across the middle, falling away at the back.
    val base = when {
        t <= FrontFillet -> mixColor(body.rimColor, body.railColor, smoothStep(t / FrontFillet))
        else -> mixColor(body.railColor, body.backColor, smoothStep((t - FrontFillet) / (1f - FrontFillet)))
    }
    // A rolled edge behaves more like a mirror strip than a matte surface, so it keeps a floor of
    // brightness whichever way it is turned; the flat of the rail does not.
    val roll = (1f - smoothStep(t / FrontFillet)).coerceIn(0f, 1f)
    val lit = lighting.shadeFactor(normal)
    val polished = lit + (1f - lit) * roll * PolishFloor
    return tint(base, polished)
}

/** Hermite ease, so a roll has no corner where it begins or ends. */
private fun smoothStep(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

private fun mix(from: Vec3, to: Vec3, t: Float) = Vec3(
    from.x + (to.x - from.x) * t,
    from.y + (to.y - from.y) * t,
    from.z + (to.z - from.z) * t,
)

private fun mixColor(from: Color, to: Color, t: Float) = Color(
    red = from.red + (to.red - from.red) * t,
    green = from.green + (to.green - from.green) * t,
    blue = from.blue + (to.blue - from.blue) * t,
    alpha = from.alpha + (to.alpha - from.alpha) * t,
)

/**
 * Where the rail's gradient is sampled through its depth. Clustered at the front, because that is
 * where the edge is rolling over and the shading changes quickest; the flat behind it needs almost
 * none.
 */
private val FilletStops = listOf(0f, 0.03f, 0.07f, 0.12f, 0.18f, 0.26f, 0.45f, 0.7f, 0.88f, 1f)

/** A point [t] of the way from [from] to [to]. */
private fun lerp(from: Offset, to: Offset, t: Float) =
    Offset(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t)

/** [base] at [factor] of its brightness, keeping its alpha. */
private fun tint(base: Color, factor: Float): Color = Color(
    red = (base.red * factor).coerceIn(0f, 1f),
    green = (base.green * factor).coerceIn(0f, 1f),
    blue = (base.blue * factor).coerceIn(0f, 1f),
    alpha = base.alpha,
)

/** How far the rail's inner edge tucks under the front face, in pixels. */
private const val SeamOverlap = 1f

/** The dark line where a rail meets the bezel, and the bright one along its outer edge. */
private const val ContactLineWidth = 1.5f
private const val RimLineWidth = 1f

/** A button's milled edge: how strongly it shows, and how much of that survives with the light behind. */
private const val ButtonRimStrength = 0.8f
private const val ButtonRimFloor = 0.45f

/**
 * The radius rolling a button's wall over onto its cap, as a fraction of the button's protrusion
 * and, so a long shallow button cannot swallow its own face, of its smaller span.
 */
private const val FilletOfProtrusion = 0.42f
private const val FilletOfSpan = 0.18f


/** The far lip of a port or a speaker hole, where the cut through the metal catches the light. */
private const val FeatureRimStrength = 0.45f
private const val FeatureRimWidth = 1f

/**
 * How much of a rail's depth is taken by the edge rolling over from the front face, and by the one
 * rolling onto the back. It is these rolls, not the shade of the flat between them, that read as
 * machined metal — and giving them a width rather than a hard line is what stops the body looking
 * like three flat faces glued together.
 */
private const val FrontFillet = 0.22f
private const val BackFillet = 0.16f

/** How much brightness a rolled edge keeps even with the light behind it, being polished. */
private const val PolishFloor = 0.5f

/** The bevel where the front face turns over into the rail: a hairline, and how brightly it lights. */
private const val BevelWidth = 1.5f
private const val BevelStrength = 0.55f

/** The bezel's own shadow on the display it surrounds — drawn in the bezel's native pixels. */
private const val ScreenOcclusionAlpha = 0.16f
private const val ScreenOcclusionWidth = 3f

/** The native-size layer scaled into the footprint, with no tilt: the fall back. */
private fun DrawScope.scaleOnly(nativeW: Float, nativeH: Float): AndroidMatrix =
    AndroidMatrix().apply { setScale(size.width / nativeW, size.height / nativeH) }

private fun DrawScope.drawRecordedFace(
    layer: androidx.compose.ui.graphics.layer.GraphicsLayer,
    matrix: AndroidMatrix,
) {
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val checkpoint = native.save()
        native.concat(matrix)
        drawLayer(layer)
        native.restoreToCount(checkpoint)
    }
}

/**
 * What chrome a bezel should draw for itself. The solid renderer draws some of a device's features
 * in 3D, and a bezel handed one of these must leave those to it — see [SolidDeviceMockup].
 */
internal data class BezelChrome(
    /** False when the renderer is drawing the side buttons on their rails instead. */
    val sideButtons: Boolean = true,
    /** Zero when the renderer is casting a projected shadow instead. */
    val elevation: Dp = 0.dp,
    /**
     * The enclosure's finish, when a [MockupMaterial] overrides it. Null keeps the device's own, so
     * a mockup with no material set draws exactly the frame it always did.
     *
     * A bezel needs these even though the rails are the renderer's business: an untilted mockup has
     * no rails at all, and asking for a silver device and getting the default black one back
     * because it happened not to be turned would be a strange thing to have to work around. The
     * black surround inside the enclosure is not included — that is the screen's border rather than
     * the body's metal, and it is black on a device of any finish.
     */
    val railColor: Color? = null,
    val edgeColor: Color? = null,
    val backColor: Color? = null,
    /**
     * The button face, when it is deliberately not the rail's. Null falls back to [railColor], and
     * then to the device's own — a button is the rail's metal unless told otherwise.
     */
    val buttonColor: Color? = null,
)

/**
 * Lays a device mockup out at its native size, scales it into the footprint [modifier] gives it,
 * and tilts it — flat or solid, whichever the angles call for.
 *
 * Every built-in frame goes through here rather than calling `ScaledMockup` directly, so the choice
 * between the two paths is made once and every form factor gets the same one.
 *
 * With no X or Y tilt there is no thickness to see, so this is the original path, unchanged: the
 * bezel draws its own buttons and its own shadow, and an in-plane [MockupTilt.rotationZ] spin still
 * goes through the same single `graphicsLayer` it always did. That is what keeps an untilted — or
 * merely spun — mockup rendering exactly as it did before the solid renderer existed.
 */
@Composable
internal fun MockupSurface(
    nativeWidth: Dp,
    nativeHeight: Dp,
    modifier: Modifier,
    body: DeviceBody,
    tilt: MockupTilt,
    elevation: Dp,
    material: MockupMaterial = MockupMaterial(),
    bezel: @Composable (chrome: BezelChrome) -> Unit,
) {
    val clamped = tilt.clamped()
    if (clamped.isFlat || !clamped.frontFaceVisible()) {
        ScaledMockup(
            nativeWidth = nativeWidth,
            nativeHeight = nativeHeight,
            modifier = modifier.mockup3dRotation(
                rotationX = clamped.rotationX,
                rotationY = clamped.rotationY,
                rotationZ = clamped.rotationZ,
                cameraDistance = clamped.cameraDistance,
            ),
        ) {
        bezel(
            BezelChrome(
                sideButtons = true,
                elevation = elevation,
                railColor = material.railColor,
                edgeColor = material.edgeHighlightColor,
                backColor = material.backEdgeColor,
                buttonColor = material.buttonColor,
            )
        )
    }
    } else {
        SolidDeviceMockup(
            nativeWidth = nativeWidth,
            nativeHeight = nativeHeight,
            modifier = modifier,
            body = body.withMaterial(material),
            tilt = clamped,
            lighting = material.lighting(),
            elevation = elevation,
        ) {
            bezel(
                BezelChrome(
                    sideButtons = false,
                    elevation = 0.dp,
                    railColor = material.railColor,
                    edgeColor = material.edgeHighlightColor,
                    backColor = material.backEdgeColor,
                    buttonColor = material.buttonColor,
                )
            )
        }
    }
}

/** The tilt a [ScreenshotStyle] asks for, as the renderer's own type. */
internal fun ScreenshotStyle.mockupTilt(): MockupTilt = MockupTilt(
    rotationX = mockupRotationX,
    rotationY = mockupRotationY,
    rotationZ = mockupRotation,
    cameraDistance = mockupCameraDistance,
)
