package dev.lucianosantos.storescreenshots.frames

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import dev.lucianosantos.storescreenshots.DefaultAmbient
import dev.lucianosantos.storescreenshots.DefaultGlassSheen
import dev.lucianosantos.storescreenshots.DefaultLightAngle
import dev.lucianosantos.storescreenshots.DefaultLightElevation
import dev.lucianosantos.storescreenshots.DefaultMockupCameraDistance
import dev.lucianosantos.storescreenshots.MockupMaterial
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * The 3D model behind a solid device mockup: where a device's corners land on the canvas once it is
 * tilted, which of its side faces that leaves visible, and how the light falls on them.
 *
 * Kept free of Compose UI and of Android so it can be exercised by a plain JVM test — see
 * `DeviceProjectionTest`. The renderer that consumes it is [SolidDeviceMockup].
 *
 * ## The camera model, and why these exact numbers
 *
 * A tilted mockup used to be a `Modifier.graphicsLayer` on a flat layer. The solid renderer has to
 * reproduce *that same projection* so a device's front face lands where it always did, and so the
 * side faces it now draws line up with it. The numbers below are not inferred from Compose's
 * source — they were measured, by rendering four markers at known positions through a
 * `graphicsLayer` and solving for the model that reproduces their centroids (`CameraModelProbe`):
 *
 * - **Camera distance is `cameraDistance * density * 72` pixels.** Compose hands `cameraDistance`
 *   straight to `RenderNode.setCameraDistance`, which feeds Skia's `Sk3DView`; that takes a camera
 *   location in inches and stores it in points, hence the 72. The probe's best fit came out at
 *   71.967 and 72.111 against the two single-axis rotations.
 * - **Rotations compose as Z, then Y, then X, then project.** The probe fit the marketing angle to
 *   an RMS of 0.037 px in that order, against 9.5 px for a post-projection Z and 37-40 px for the
 *   other axis order. Z is a genuine 3D rotation before the perspective divide, not a 2D spin of
 *   the projected result.
 * - **Sign conventions** follow from the same fit: a positive [MockupTilt.rotationX] tips the top
 *   of the device *away* from the viewer, and a positive [MockupTilt.rotationY] brings its *left*
 *   edge *toward* the viewer.
 *
 * ## Supported angles
 *
 * The front face goes edge-on at 90 degrees, where the projection is singular; it is
 * ill-conditioned well before that. [MockupTilt.clamped] holds X and Y to [MaxTiltDegrees], and the
 * look is tuned for about +-45. Every entry point returns null rather than throwing on geometry it
 * cannot project, so the renderer can fall back to the flat path instead of drawing nonsense.
 */

/** Skia's `Sk3DView` takes a camera location in inches and stores it in points. */
internal const val CameraPointsPerUnit = 72f

/** Past this the front face is nearly edge-on and the projection stops being well conditioned. */
internal const val MaxTiltDegrees = 75f

/**
 * No point may come closer to the camera than this fraction of its distance.
 *
 * Generous on purpose: it is a blow-up guard, not an artistic limit. With the camera held at least
 * as far away as the mockup's longest side (see `minimumCameraPx`) and the tilt held to
 * [MaxTiltDegrees], the nearest corner of a body can reach about 0.68 of the camera distance — a
 * tall phone at a steep X tip gets most of the way there — so anything below this would reject
 * geometry that projects perfectly well.
 */
internal const val NearPlaneFraction = 0.75f

/** Projected side faces smaller than this are slivers, and drawing them only adds noise. */
internal const val MinQuadArea = 0.25f

/** A point or a direction in the device's own space: x right, y down, z toward the viewer. */
internal data class Vec3(val x: Float, val y: Float, val z: Float) {

    infix fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

    fun normalized(): Vec3 {
        val length = kotlin.math.sqrt(x * x + y * y + z * z)
        return if (length < 1e-6f) Vec3(0f, 0f, 1f) else Vec3(x / length, y / length, z / length)
    }
}

/**
 * How far a mockup is turned, and how strong the perspective is. Degrees, matching
 * `ScreenshotStyle.mockupRotationX` / `mockupRotationY` / `mockupRotation` exactly.
 */
internal data class MockupTilt(
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f,
    val cameraDistance: Float = DefaultMockupCameraDistance,
) {

    /**
     * True when no tilt reveals any thickness. An in-plane [rotationZ] spin on its own still shows
     * the device face-on, so it stays on the original flat path and renders exactly as it always
     * has — only an X or Y tilt turns a side face toward the viewer.
     */
    val isFlat: Boolean get() = rotationX == 0f && rotationY == 0f

    /** Angles held inside the supported range, and a camera that cannot sit on the drawing plane. */
    fun clamped(): MockupTilt = MockupTilt(
        rotationX = rotationX.coerceIn(-MaxTiltDegrees, MaxTiltDegrees),
        rotationY = rotationY.coerceIn(-MaxTiltDegrees, MaxTiltDegrees),
        rotationZ = rotationZ,
        cameraDistance = cameraDistance.coerceAtLeast(0.1f),
    )

    /** Camera-to-drawing-plane distance in canvas pixels. See this file's KDoc for the 72. */
    fun cameraPx(density: Float): Float = cameraDistance * density * CameraPointsPerUnit

    /**
     * Turns [p] about the device's centre. Z first, then Y, then X — the order a `graphicsLayer`
     * resolves to, measured rather than assumed.
     */
    fun rotate(p: Vec3): Vec3 {
        val rx = Math.toRadians(rotationX.toDouble())
        val ry = Math.toRadians(rotationY.toDouble())
        val rz = Math.toRadians(rotationZ.toDouble())

        var x = p.x.toDouble()
        var y = p.y.toDouble()
        var z = p.z.toDouble()

        if (rotationZ != 0f) {
            val nx = x * cos(rz) - y * sin(rz)
            val ny = x * sin(rz) + y * cos(rz)
            x = nx; y = ny
        }
        if (rotationY != 0f) {
            val nx = x * cos(ry) + z * sin(ry)
            val nz = -x * sin(ry) + z * cos(ry)
            x = nx; z = nz
        }
        if (rotationX != 0f) {
            val ny = y * cos(rx) - z * sin(rx)
            val nz = y * sin(rx) + z * cos(rx)
            y = ny; z = nz
        }
        return Vec3(x.toFloat(), y.toFloat(), z.toFloat())
    }

    /**
     * True when the front face still points at the camera. Visibility of a *plane* is a single sign
     * test — it is never partly turned away — so one check covers the whole face.
     */
    fun frontFaceVisible(): Boolean {
        val rx = Math.toRadians(rotationX.toDouble())
        val ry = Math.toRadians(rotationY.toDouble())
        val limit = cos(Math.toRadians(MaxTiltDegrees.toDouble()))
        return cos(rx) * cos(ry) > limit
    }
}

/**
 * Projects [p] onto the canvas, with [pivot] the point the camera looks through — the centre of the
 * mockup's footprint. Null when the point is at or through the near plane, which the caller turns
 * into a fall back to the flat path.
 */
internal fun project(p: Vec3, cameraPx: Float, pivot: Offset): Offset? {
    if (p.z >= cameraPx * NearPlaneFraction) return null
    val w = cameraPx - p.z
    if (w <= 1e-3f) return null
    val scale = cameraPx / w
    val x = pivot.x + p.x * scale
    val y = pivot.y + p.y * scale
    return if (x.isFinite() && y.isFinite()) Offset(x, y) else null
}

/**
 * One sample of a body's outline: where it sits in the device's own space, and which way the side
 * wall faces there. The wall is perpendicular to the front face, so its normal lies in the front
 * plane and its z is zero before the body is turned.
 */
internal class RingSample(val x: Float, val y: Float, val nx: Float, val ny: Float)

/**
 * How many samples each rounded corner is broken into, given its radius in pixels. Derived from an
 * integer so it cannot drift with the tilt — a golden should not move because an angle changed.
 */
internal fun cornerSamplesFor(radiusPx: Float): Int =
    ceil(radiusPx * (Math.PI.toFloat() / 2f) / 2f).toInt().coerceIn(8, 64)

/**
 * The outline of a rounded rectangle [w] x [h] with corner radius [r], centred on the origin and
 * walked clockwise from the top-left corner's end.
 *
 * The four straight edges contribute a single segment each: a straight line in 3D stays straight
 * under a perspective projection, and the wall's normal does not vary along them, so subdividing
 * would buy nothing. Only the corners are sampled.
 */
internal fun sampleRoundRectRing(w: Float, h: Float, r: Float, cornerSamples: Int): List<RingSample> {
    val hw = w / 2f
    val hh = h / 2f
    val radius = r.coerceIn(0f, minOf(hw, hh))
    val samples = ArrayList<RingSample>(4 + 4 * cornerSamples)

    // Corner centres, clockwise from top-left, paired with the angle their arc starts at. Angles run
    // clockwise in screen space (y down): 180 is straight left, 270 is straight up.
    val corners = listOf(
        Triple(-hw + radius, -hh + radius, 180f), // top-left, sweeping up to the top edge
        Triple(hw - radius, -hh + radius, 270f),  // top-right
        Triple(hw - radius, hh - radius, 0f),     // bottom-right
        Triple(-hw + radius, hh - radius, 90f),   // bottom-left
    )
    // The straight edge that follows each corner, as its constant outward normal.
    val edgeNormals = listOf(
        0f to -1f, // top edge
        1f to 0f,  // right edge
        0f to 1f,  // bottom edge
        -1f to 0f, // left edge
    )

    corners.forEachIndexed { index, (cx, cy, startDegrees) ->
        if (radius > 0f) {
            for (step in 0..cornerSamples) {
                val angle = Math.toRadians((startDegrees + 90f * step / cornerSamples).toDouble())
                val nx = cos(angle).toFloat()
                val ny = sin(angle).toFloat()
                samples += RingSample(cx + nx * radius, cy + ny * radius, nx, ny)
            }
        }
        // End of the straight edge that leaves this corner. Its start is the corner's last sample,
        // so one point closes it; the normal is the edge's, not the arc's.
        val (nx, ny) = edgeNormals[index]
        val (ex, ey) = when (index) {
            0 -> (hw - radius) to -hh
            1 -> hw to (hh - radius)
            2 -> (-hw + radius) to hh
            else -> -hw to (-hh + radius)
        }
        samples += RingSample(ex, ey, nx, ny)
    }

    // Each straight edge ends exactly where the next corner's arc begins, so walking the outline
    // this way lands on every join twice. A duplicate is a segment of no length, which is culled
    // for having no area — and a culled segment in the middle of a visible stretch splits it in
    // two. The band is then drawn as several runs that each have to be closed off, and every one of
    // those closures is a straight cut across the rail where the body is in fact continuous. The
    // duplicates carry the same outward normal as the point they repeat, so dropping them changes
    // the outline not at all and leaves the visible stretch in one piece.
    val deduped = ArrayList<RingSample>(samples.size)
    samples.forEach { sample ->
        val previous = deduped.lastOrNull()
        if (previous == null || !sample.coincidesWith(previous)) deduped += sample
    }
    while (deduped.size > 1 && deduped.last().coincidesWith(deduped.first())) {
        deduped.removeAt(deduped.size - 1)
    }
    return deduped
}

/** Two outline samples are the same point when they are within a fraction of a pixel of each other. */
private fun RingSample.coincidesWith(other: RingSample): Boolean =
    abs(x - other.x) < 1e-3f && abs(y - other.y) < 1e-3f

/**
 * Whether the side face spanning these four projected points turns toward the viewer. The ring is
 * walked clockwise, so a face pointing at the camera comes out wound one way and a face pointing
 * away the other; the sign of the enclosed area is the whole test. Slivers below [MinQuadArea] are
 * rejected as noise.
 */
internal fun isOutwardFacing(a: Offset, b: Offset, c: Offset, d: Offset): Boolean =
    signedArea(a, b, c, d) < -MinQuadArea

/** Twice the signed area of a quad, by the shoelace formula. Negative is clockwise in screen space. */
internal fun signedArea(a: Offset, b: Offset, c: Offset, d: Offset): Float =
    ((a.x * b.y - b.x * a.y) + (b.x * c.y - c.x * b.y) +
        (c.x * d.y - d.x * c.y) + (d.x * a.y - a.x * d.y)) / 2f

/**
 * The 3x3 homography carrying the quad [src] onto the quad [dst], both given as `x0,y0,…,x3,y3`.
 *
 * The perspective image of a plane *is* a homography, so four corner correspondences reproduce
 * every interior point exactly — this is not a fit. Returned in `android.graphics.Matrix.setValues`
 * order, so it can be handed straight to a canvas. Null when the system is singular, which happens
 * only for a degenerate quad.
 */
internal fun homography(src: FloatArray, dst: FloatArray): FloatArray? {
    require(src.size == 8 && dst.size == 8) { "a quad is four x,y pairs" }
    // Rows of  [x y 1 0 0 0 -xu -yu][h] = u  and  [0 0 0 x y 1 -xv -yv][h] = v.
    val a = Array(8) { DoubleArray(9) }
    for (i in 0 until 4) {
        val x = src[i * 2].toDouble()
        val y = src[i * 2 + 1].toDouble()
        val u = dst[i * 2].toDouble()
        val v = dst[i * 2 + 1].toDouble()
        a[i * 2] = doubleArrayOf(x, y, 1.0, 0.0, 0.0, 0.0, -x * u, -y * u, u)
        a[i * 2 + 1] = doubleArrayOf(0.0, 0.0, 0.0, x, y, 1.0, -x * v, -y * v, v)
    }
    val h = solve(a) ?: return null
    val out = FloatArray(9)
    for (i in 0 until 8) {
        if (!h[i].isFinite()) return null
        out[i] = h[i].toFloat()
    }
    out[8] = 1f
    return out
}

/** Gaussian elimination with partial pivoting on an 8x9 augmented matrix. Null when singular. */
private fun solve(a: Array<DoubleArray>): DoubleArray? {
    val n = a.size
    for (col in 0 until n) {
        var pivot = col
        for (row in col + 1 until n) if (abs(a[row][col]) > abs(a[pivot][col])) pivot = row
        if (abs(a[pivot][col]) < 1e-9) return null
        val swap = a[col]; a[col] = a[pivot]; a[pivot] = swap
        val head = a[col]
        for (row in 0 until n) {
            if (row == col) continue
            val factor = a[row][col] / head[col]
            if (factor == 0.0) continue
            for (k in col..n) a[row][k] -= factor * head[k]
        }
    }
    return DoubleArray(n) { a[it][n] / a[it][it] }
}

/** Applies a [homography] to a point, in the same `setValues` order. Null when it sends it to infinity. */
internal fun applyHomography(h: FloatArray, x: Float, y: Float): Offset? {
    val w = h[6] * x + h[7] * y + h[8]
    if (abs(w) < 1e-6f) return null
    val ox = (h[0] * x + h[1] * y + h[2]) / w
    val oy = (h[3] * x + h[4] * y + h[5]) / w
    return if (ox.isFinite() && oy.isFinite()) Offset(ox, oy) else null
}

/**
 * A device's outline projected onto the canvas twice — once at the front face and once at the back
 * of the body — together with the direction each side wall faces once the body has been turned.
 *
 * The wall between sample `i` and `i + 1` is the quad `front[i], front[i+1], back[i+1], back[i]`.
 */
internal class ProjectedRing(
    val front: Array<Offset>,
    val back: Array<Offset>,
    val normals: Array<Vec3>,
    val frontDepth: FloatArray,
    val backDepth: FloatArray,
) {

    /** Indices `i` whose wall segment `i -> i + 1` turns toward the viewer. */
    fun visibleSegments(): List<Int> = (front.indices).filter { i ->
        val j = (i + 1) % front.size
        isOutwardFacing(front[i], front[j], back[j], back[i])
    }
}

/**
 * Projects a body's outline at both depths. [thickness] is how far the body extends *behind* the
 * front face, in the same pixels as the samples. Null if any point cannot be projected, which the
 * renderer turns into a fall back to the flat path.
 */
internal fun projectRing(
    samples: List<RingSample>,
    thickness: Float,
    tilt: MockupTilt,
    cameraPx: Float,
    pivot: Offset,
): ProjectedRing? {
    val n = samples.size
    val front = arrayOfNulls<Offset>(n)
    val back = arrayOfNulls<Offset>(n)
    val normals = arrayOfNulls<Vec3>(n)
    val frontDepth = FloatArray(n)
    val backDepth = FloatArray(n)

    samples.forEachIndexed { i, s ->
        val f = tilt.rotate(Vec3(s.x, s.y, 0f))
        val b = tilt.rotate(Vec3(s.x, s.y, -thickness))
        front[i] = project(f, cameraPx, pivot) ?: return null
        back[i] = project(b, cameraPx, pivot) ?: return null
        // A direction turns with the body but is not displaced by it, so it goes through the same
        // rotation and no projection.
        normals[i] = tilt.rotate(Vec3(s.nx, s.ny, 0f)).normalized()
        frontDepth[i] = f.z
        backDepth[i] = b.z
    }
    @Suppress("UNCHECKED_CAST")
    return ProjectedRing(
        front = front as Array<Offset>,
        back = back as Array<Offset>,
        normals = normals as Array<Vec3>,
        frontDepth = frontDepth,
        backDepth = backDepth,
    )
}

/**
 * The four corners of a [w] x [h] front face, projected under [tilt], as `x0,y0,…,x3,y3` clockwise
 * from the top-left. This is the quad the live screen is warped onto.
 */
internal fun projectFrontQuad(
    w: Float,
    h: Float,
    tilt: MockupTilt,
    cameraPx: Float,
    pivot: Offset,
): FloatArray? {
    if (w <= 0f || h <= 0f) return null
    if (!tilt.frontFaceVisible()) return null
    val hw = w / 2f
    val hh = h / 2f
    val corners = listOf(-hw to -hh, hw to -hh, hw to hh, -hw to hh)
    val out = FloatArray(8)
    corners.forEachIndexed { i, (x, y) ->
        val p = project(tilt.rotate(Vec3(x, y, 0f)), cameraPx, pivot) ?: return null
        out[i * 2] = p.x
        out[i * 2 + 1] = p.y
    }
    return out
}

/** Which of a body's four rails a button is milled into. An iPad's power button lies along the top. */
internal enum class RailEdge { Left, Right, Top, Bottom }

/**
 * A machined button standing proud of one rail. [start] and [length] run along that rail from the
 * body's top or left edge, in the same units as the body, matching the `(top, height)` pairs the
 * device metrics already carry. [across] says where the boss sits through the body's thickness.
 */
internal data class RailButton(
    val edge: RailEdge,
    val start: Dp,
    val length: Dp,
    val protrusion: Dp,
    val corner: Dp,
    val face: Color,
    val shadow: Color,
    val across: ClosedFloatingPointRange<Float> = 0.18f..0.82f,
)

/** The display inside a body, so an effect drawn on the cover glass can be clipped to it. */
internal data class ScreenSpec(val inset: Dp, val corner: Dp)

/**
 * Something milled *into* a rail rather than standing proud of it — the charge connector, or one
 * hole of a speaker grille. Flat on the rail's surface, so it shows only while that rail is turned
 * toward the viewer, which for a bottom-edge port means only on a mockup tipped far enough forward
 * to look under the device.
 *
 * [centre] runs along the rail from the body's top or left edge; [depth] is where it sits through
 * the thickness, as a fraction, so a port stays centred in the rail however deep the device is.
 */
internal data class RailFeature(
    val edge: RailEdge,
    val centre: Dp,
    val width: Dp,
    val depth: ClosedFloatingPointRange<Float>,
    val corner: Dp,
    val color: Color,
)

/**
 * The solid a device bezel is the front face of: its outline, how deep it goes, and what its
 * exposed sides are made of. Dimensions are the bezel's own native dp, the size the bezel is
 * measured at before the mockup is scaled into its footprint.
 */
internal data class DeviceBody(
    val width: Dp,
    val height: Dp,
    val cornerRadius: Dp,
    val thickness: Dp,
    val screen: ScreenSpec,
    val railColor: Color,
    val rimColor: Color,
    val backColor: Color,
    val buttons: List<RailButton> = emptyList(),
    val features: List<RailFeature> = emptyList(),
)

/**
 * Direction-aware lighting for a solid body. Deterministic: the shade of every face is a pure
 * function of its normal and these numbers, with no randomness or noise anywhere, so a screenshot
 * renders the same on every machine.
 */
internal data class MockupLighting(
    val direction: Vec3 = lightDirection(DefaultLightAngle, DefaultLightElevation),
    val ambient: Float = DefaultAmbient,
    val diffuse: Float = 1f - DefaultAmbient,
    val specular: Float = DefaultGlassSheen,
    val shininess: Float = 28f,
    val contactShadowAlpha: Float = 0.35f,
) {
    val unit: Vec3 get() = direction.normalized()
}

/**
 * A light direction from an angle around the canvas and a height above it, both in degrees.
 * [angleDegrees] is measured clockwise from straight up, so a negative angle throws the light from
 * the upper left; [elevationDegrees] runs from grazing the screen's plane to straight in front of
 * it.
 */
internal fun lightDirection(angleDegrees: Float, elevationDegrees: Float): Vec3 {
    val angle = Math.toRadians(angleDegrees.toDouble())
    val elevation = Math.toRadians(elevationDegrees.toDouble())
    val inPlane = cos(elevation)
    // Straight up the canvas is -y, and a positive angle turns that clockwise toward +x.
    return Vec3(
        x = (sin(angle) * inPlane).toFloat(),
        y = (-cos(angle) * inPlane).toFloat(),
        z = sin(elevation).toFloat(),
    ).normalized()
}

/** The lighting this material describes. */
internal fun MockupMaterial.lighting(): MockupLighting = MockupLighting(
    direction = lightDirection(lightAngle, lightElevation),
    ambient = ambient.coerceIn(0f, 1f),
    diffuse = (1f - ambient).coerceIn(0f, 1f),
    specular = glassSheen.coerceIn(0f, 1f),
)

/** [this] with whatever the material overrides, leaving the device's own figures everywhere else. */
internal fun DeviceBody.withMaterial(material: MockupMaterial): DeviceBody = copy(
    thickness = material.thicknessRatio?.let { minOf(width, height) * it } ?: thickness,
    railColor = material.railColor ?: railColor,
    rimColor = material.edgeHighlightColor ?: rimColor,
    backColor = material.backEdgeColor ?: backColor,
    // A button is milled out of the same metal as the rail it sits in, so recolouring the rail
    // recolours it too — asking for a silver device and getting silver rails with the black buttons
    // of the default one would be a strange thing to have to work around. Only buttons that share
    // the rail's colour follow it, so a device that deliberately contrasts them keeps its contrast.
    buttons = material.railColor?.let { recoloured ->
        buttons.map { if (it.face == railColor) it.copy(face = recoloured) else it }
    } ?: buttons,
)

/**
 * How brightly a surface facing [normal] catches the light. Always in `[ambient, ambient + diffuse]`,
 * and monotonic in how squarely the surface faces the light.
 *
 * The diffuse term is *wrapped* — it runs from half strength at a grazing angle down to nothing only
 * when the surface faces directly away, rather than cutting off the moment it turns past 90 degrees.
 * A hard cutoff is right for a matte surface lit by one lamp in a void, and wrong for what is being
 * drawn here: a machined metal rail on a device sitting in a room, which picks up light from
 * everything around it. Without the wrap a rail that happens to face away from the light goes to
 * flat ambient and the device reads as a black card with a black edge — the shape is still correct,
 * but nothing about it looks like metal.
 */
internal fun MockupLighting.shadeFactor(normal: Vec3): Float {
    val wrapped = ((normal dot unit) * 0.5f + 0.5f).coerceIn(0f, 1f)
    return ambient + diffuse * wrapped
}

/** [base] lit by [shadeFactor], keeping its alpha. */
internal fun MockupLighting.shade(base: Color, normal: Vec3): Color {
    val f = shadeFactor(normal)
    return Color(
        red = (base.red * f).coerceIn(0f, 1f),
        green = (base.green * f).coerceIn(0f, 1f),
        blue = (base.blue * f).coerceIn(0f, 1f),
        alpha = base.alpha,
    )
}
