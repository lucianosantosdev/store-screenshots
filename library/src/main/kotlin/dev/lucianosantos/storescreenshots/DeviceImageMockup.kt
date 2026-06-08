package dev.lucianosantos.storescreenshots

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * A manual quarter-turn applied to a screen's content (clockwise), on top of the auto-detected
 * orientation. Pass via `DeviceImageMockup(..., screenRotations = …)` to override how the UI sits on
 * a screen — e.g. spin a square watch face or flip a device the detector guessed upside down.
 */
enum class ScreenRotation(internal val quarterTurns: Int) {
    None(0),
    Clockwise90(1),
    Clockwise180(2),
    Clockwise270(3),
}

/** How a [DeviceKind] decides which screen edge becomes the content's height. */
internal enum class KindOrientation { Portrait, Landscape, FromImage }

/**
 * The kind of device a screen belongs to in a [DeviceImageMockup] frame. Pass one per screen
 * (left-to-right, matching the detected order) via `deviceKinds` to set each screen's orientation and
 * the width its UI is composed at — explicitly, instead of leaving it to geometry. That removes the
 * ambiguity a near-square screen creates: a watch worn at an angle and a small landscape screen look
 * identical, so only you know which is which. Each kind also picks a sensible native layout width so a
 * desktop's UI isn't laid out at phone size.
 */
enum class DeviceKind(internal val nativeWidth: Dp, internal val orientation: KindOrientation) {
    /** A smartwatch — portrait (its longer edge is the height, even worn turned), composed small. */
    Watch(220.dp, KindOrientation.Portrait),
    /** A phone — portrait. */
    Phone(411.dp, KindOrientation.Portrait),
    /** A tablet — orientation follows however it sits in the image (portrait or landscape). */
    Tablet(640.dp, KindOrientation.FromImage),
    /** A laptop — landscape, composed at a large width. */
    Laptop(840.dp, KindOrientation.Landscape),
    /** A desktop monitor — landscape, composed at the largest width. */
    Desktop(1000.dp, KindOrientation.Landscape),
}

/**
 * The four corners of a device screen inside a frame image, as fractions (0f..1f) of the image:
 * `(0,0)` is the image's top-left, `(1,1)` its bottom-right. Corners are ordered [topLeft],
 * [topRight], [bottomRight], [bottomLeft] as seen on the (possibly perspective-tilted) screen.
 */
@Immutable
data class ScreenRegion(
    val topLeft: Offset,
    val topRight: Offset,
    val bottomRight: Offset,
    val bottomLeft: Offset,
)

/**
 * Finds the screen regions in a device [frame] image automatically. A screen is detected as a
 * region the device's **dark bezel walls off from the background** — independent of the screen's
 * own colour, so it works for white, gray or reflective empty screens alike. The detector floods
 * inward from the image border across everything brighter than [bezelDarkness] (the background);
 * whatever the dark bezel encloses is a screen. Regions smaller than [minAreaFraction] of the image
 * are discarded as noise. Returns one [ScreenRegion] per screen, ordered left-to-right by center.
 *
 * Assumptions: the device has a dark (near-black) bezel that fully surrounds the screen, and the
 * device isn't cropped by the image edge. When that doesn't hold (e.g. a light-bodied phone with no
 * dark rim, or a vector source that must be rasterized first), pass explicit [ScreenRegion]s to
 * [DeviceImageMockup] instead. Use this directly if you want to inspect or tweak the detection.
 */
fun detectScreenRegions(
    frame: ImageBitmap,
    bezelDarkness: Float = 0.35f,
    minAreaFraction: Float = 0.01f,
    minScreenFill: Float = 0.7f,
): List<ScreenRegion> {
    val w = frame.width
    val h = frame.height
    val pixels = frame.toPixelMap()
    val stride = max(1, min(w, h) / 360)
    val gw = w / stride
    val gh = h / stride
    val dark = BooleanArray(gw * gh)
    for (gy in 0 until gh) {
        for (gx in 0 until gw) {
            val c = pixels[gx * stride, gy * stride]
            val lum = 0.299f * c.red + 0.587f * c.green + 0.114f * c.blue
            dark[gy * gw + gx] = lum < bezelDarkness
        }
    }

    // Flood inward from the image border across non-dark pixels: that's the background ("outside").
    val outside = BooleanArray(gw * gh)
    val queue = IntArray(gw * gh)
    var head = 0; var tail = 0
    for (gx in 0 until gw) {
        val top = gx; val bot = (gh - 1) * gw + gx
        if (!dark[top] && !outside[top]) { outside[top] = true; queue[tail++] = top }
        if (!dark[bot] && !outside[bot]) { outside[bot] = true; queue[tail++] = bot }
    }
    for (gy in 0 until gh) {
        val left = gy * gw; val right = gy * gw + gw - 1
        if (!dark[left] && !outside[left]) { outside[left] = true; queue[tail++] = left }
        if (!dark[right] && !outside[right]) { outside[right] = true; queue[tail++] = right }
    }
    while (head < tail) {
        val idx = queue[head++]
        val x = idx % gw; val y = idx / gw
        if (x > 0 && !dark[idx - 1] && !outside[idx - 1]) { outside[idx - 1] = true; queue[tail++] = idx - 1 }
        if (x < gw - 1 && !dark[idx + 1] && !outside[idx + 1]) { outside[idx + 1] = true; queue[tail++] = idx + 1 }
        if (y > 0 && !dark[idx - gw] && !outside[idx - gw]) { outside[idx - gw] = true; queue[tail++] = idx - gw }
        if (y < gh - 1 && !dark[idx + gw] && !outside[idx + gw]) { outside[idx + gw] = true; queue[tail++] = idx + gw }
    }

    // A screen is a non-dark region the bezel walls off from the background. Connected-component it,
    // then fit each component's minimum-area rotated rectangle — that gives the four screen corners
    // at any in-plane rotation, and how fully the component fills that rect ("screen fill") cleanly
    // separates a solid screen from an irregular bright blob (a metallic body ring, merged shapes).
    val seen = BooleanArray(gw * gh)
    val minArea = (gw * gh * minAreaFraction).toInt().coerceAtLeast(1)
    val regions = mutableListOf<ScreenRegion>()
    fun free(i: Int) = !dark[i] && !outside[i] && !seen[i]
    for (start in 0 until gw * gh) {
        if (dark[start] || outside[start] || seen[start]) continue
        head = 0; tail = 0
        queue[tail++] = start; seen[start] = true
        while (head < tail) {
            val idx = queue[head++]
            val x = idx % gw; val y = idx / gw
            if (x > 0 && free(idx - 1)) { seen[idx - 1] = true; queue[tail++] = idx - 1 }
            if (x < gw - 1 && free(idx + 1)) { seen[idx + 1] = true; queue[tail++] = idx + 1 }
            if (y > 0 && free(idx - gw)) { seen[idx - gw] = true; queue[tail++] = idx - gw }
            if (y < gh - 1 && free(idx + gw)) { seen[idx + gw] = true; queue[tail++] = idx + gw }
        }
        val count = tail
        if (count < minArea) continue
        val pts = ArrayList<Offset>(count)
        for (k in 0 until count) pts += Offset((queue[k] % gw).toFloat(), (queue[k] / gw).toFloat())
        val (rectArea, rect) = minAreaRect(convexHull(pts))
        if (rectArea <= 0f || count / rectArea < minScreenFill) continue
        // The min-area rect bounds the region (and overshoots into rounded corners); pull each
        // corner back to the nearest actual screen pixel so the four corners are the real
        // perspective quad — that's what makes the warp perspective-correct, not just affine.
        val corners = rect.map { rc -> pts.minByOrNull { sqDist(it, rc) }!! }
        val oriented = orientCorners(corners)
        fun frac(o: Offset) = Offset(o.x / gw, o.y / gh)
        regions += ScreenRegion(frac(oriented[0]), frac(oriented[1]), frac(oriented[2]), frac(oriented[3]))
    }
    // Order left-to-right by the region center x.
    return regions.sortedBy { (it.topLeft.x + it.topRight.x + it.bottomRight.x + it.bottomLeft.x) / 4f }
}

/**
 * Finds screen regions by **chroma key**. Render your device with each empty screen painted a flat,
 * distinctive [screenColor] (a green or magenta that appears nowhere else in the image); every
 * connected blob of pixels within [tolerance] (0f..1f, per RGB channel) of that colour is taken as a
 * screen. This needs no dark bezel and ignores the device body and background entirely, so it's the
 * robust choice when [detectScreenRegions] can't separate the screen from its surroundings (a
 * white screen on a white background, a bezel-less render, a busy backdrop). Blobs smaller than
 * [minAreaFraction] of the image are discarded as noise; [minScreenFill] rejects blobs that don't
 * fill their bounding rectangle (stray colour splatters). Returns one [ScreenRegion] per screen,
 * ordered left-to-right.
 */
fun detectScreenRegionsByColor(
    frame: ImageBitmap,
    screenColor: Color,
    tolerance: Float = 0.18f,
    minAreaFraction: Float = 0.01f,
    minScreenFill: Float = 0.7f,
): List<ScreenRegion> {
    val w = frame.width
    val h = frame.height
    val pixels = frame.toPixelMap()
    val stride = max(1, min(w, h) / 360)
    val gw = w / stride
    val gh = h / stride
    val tr = screenColor.red; val tg = screenColor.green; val tb = screenColor.blue
    val match = BooleanArray(gw * gh)
    for (gy in 0 until gh) {
        for (gx in 0 until gw) {
            val c = pixels[gx * stride, gy * stride]
            match[gy * gw + gx] =
                abs(c.red - tr) <= tolerance && abs(c.green - tg) <= tolerance && abs(c.blue - tb) <= tolerance
        }
    }
    // Each connected blob of the keyed colour is a screen — same corner extraction as the bezel detector.
    val seen = BooleanArray(gw * gh)
    val queue = IntArray(gw * gh)
    val minArea = (gw * gh * minAreaFraction).toInt().coerceAtLeast(1)
    val regions = mutableListOf<ScreenRegion>()
    for (start in 0 until gw * gh) {
        if (!match[start] || seen[start]) continue
        var head = 0; var tail = 0
        queue[tail++] = start; seen[start] = true
        while (head < tail) {
            val idx = queue[head++]
            val x = idx % gw; val y = idx / gw
            if (x > 0 && match[idx - 1] && !seen[idx - 1]) { seen[idx - 1] = true; queue[tail++] = idx - 1 }
            if (x < gw - 1 && match[idx + 1] && !seen[idx + 1]) { seen[idx + 1] = true; queue[tail++] = idx + 1 }
            if (y > 0 && match[idx - gw] && !seen[idx - gw]) { seen[idx - gw] = true; queue[tail++] = idx - gw }
            if (y < gh - 1 && match[idx + gw] && !seen[idx + gw]) { seen[idx + gw] = true; queue[tail++] = idx + gw }
        }
        val count = tail
        if (count < minArea) continue
        val pts = ArrayList<Offset>(count)
        for (k in 0 until count) pts += Offset((queue[k] % gw).toFloat(), (queue[k] / gw).toFloat())
        val (rectArea, rect) = minAreaRect(convexHull(pts))
        if (rectArea <= 0f || count / rectArea < minScreenFill) continue
        val corners = rect.map { rc -> pts.minByOrNull { sqDist(it, rc) }!! }
        val oriented = orientCorners(corners)
        fun frac(o: Offset) = Offset(o.x / gw, o.y / gh)
        regions += ScreenRegion(frac(oriented[0]), frac(oriented[1]), frac(oriented[2]), frac(oriented[3]))
    }
    return regions.sortedBy { (it.topLeft.x + it.topRight.x + it.bottomRight.x + it.bottomLeft.x) / 4f }
}

/**
 * Labels four quad corners as top-left, top-right, bottom-right, bottom-left in the device's own
 * upright frame. The device is rotated into a canonical orientation (long edge vertical for a
 * portrait screen, horizontal for a wide one — split at aspect 0.6 so phones come out portrait even
 * when the render lies them down at an angle), the corners are labeled there, then mapped back.
 */
private fun orientCorners(c: List<Offset>, orientation: KindOrientation? = null): List<Offset> {
    val cx = (c[0].x + c[1].x + c[2].x + c[3].x) / 4f
    val cy = (c[0].y + c[1].y + c[2].y + c[3].y) / 4f
    val eA = (dist(c[0], c[1]) + dist(c[2], c[3])) / 2f // length of the A edges (dirA)
    val eB = (dist(c[1], c[2]) + dist(c[3], c[0])) / 2f // length of the B edges (dirB)
    val dirA = Offset(c[1].x - c[0].x, c[1].y - c[0].y)
    val dirB = Offset(c[2].x - c[1].x, c[2].y - c[1].y)
    val longEdge = if (eA >= eB) dirA else dirB
    val shortEdge = if (eA >= eB) dirB else dirA
    val nearestVertical = if (abs(dirA.y) / hypot(dirA.x, dirA.y) >= abs(dirB.y) / hypot(dirB.x, dirB.y)) dirA else dirB
    // Which screen axis becomes the content's vertical ("up"). When the screen's device kind is known,
    // it decides: a portrait device (phone/watch) stands its LONG edge up — a watch is taller than wide
    // even worn at an angle — a landscape device (laptop/desktop) stands its SHORT edge up, and a tablet
    // follows the image. With no kind, fall back to geometry: a near-square screen uses its long edge as
    // height; a clearly elongated one orients to the image so portrait stays portrait and wide stays wide.
    val heightDir = when (orientation) {
        KindOrientation.Portrait -> longEdge
        KindOrientation.Landscape -> shortEdge
        KindOrientation.FromImage -> nearestVertical
        null -> if (min(eA, eB) / max(eA, eB) > 0.78f) longEdge else nearestVertical
    }
    val rot = PI.toFloat() / 2f - atan2(heightDir.y, heightDir.x)
    val ca = cos(rot); val sa = sin(rot)
    val rotated = c.map { p -> val dx = p.x - cx; val dy = p.y - cy; Offset(dx * ca - dy * sa, dx * sa + dy * ca) }
    val idx = listOf(0, 1, 2, 3)
    val tl = idx.minByOrNull { rotated[it].x + rotated[it].y }!!
    val tr = idx.maxByOrNull { rotated[it].x - rotated[it].y }!!
    val br = idx.maxByOrNull { rotated[it].x + rotated[it].y }!!
    val bl = idx.minByOrNull { rotated[it].x - rotated[it].y }!!
    var TL = c[tl]; var TR = c[tr]; var BR = c[br]; var BL = c[bl]
    // If the canonical "up" ended up at the bottom of the image, flip so content isn't upside down.
    if (TL.y + TR.y > BL.y + BR.y) { val a = TL; val b = TR; TL = BL; TR = BR; BR = b; BL = a }
    // Enforce a front-facing winding (TR to the right of TL, BL below TL); otherwise the warp would
    // mirror the content. A left-handed quad (cross < 0) means left/right were swapped — flip them.
    val cross = (TR.x - TL.x) * (BL.y - TL.y) - (TR.y - TL.y) * (BL.x - TL.x)
    return if (cross < 0f) listOf(TR, TL, BL, BR) else listOf(TL, TR, BR, BL)
}

private fun sqDist(a: Offset, b: Offset): Float { val dx = a.x - b.x; val dy = a.y - b.y; return dx * dx + dy * dy }

/** Andrew's monotone-chain convex hull (counter-clockwise, no collinear points). */
private fun convexHull(points: List<Offset>): List<Offset> {
    if (points.size < 3) return points
    val pts = points.sortedWith(compareBy({ it.x }, { it.y }))
    fun cross(o: Offset, a: Offset, b: Offset) = (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)
    val lower = ArrayList<Offset>()
    for (p in pts) {
        while (lower.size >= 2 && cross(lower[lower.size - 2], lower[lower.size - 1], p) <= 0f) lower.removeAt(lower.size - 1)
        lower += p
    }
    val upper = ArrayList<Offset>()
    for (p in pts.asReversed()) {
        while (upper.size >= 2 && cross(upper[upper.size - 2], upper[upper.size - 1], p) <= 0f) upper.removeAt(upper.size - 1)
        upper += p
    }
    return lower.dropLast(1) + upper.dropLast(1)
}

/** Minimum-area enclosing rectangle of a convex [hull] (rotating calipers). Returns area + 4 corners. */
private fun minAreaRect(hull: List<Offset>): Pair<Float, List<Offset>> {
    if (hull.size < 3) return 0f to hull
    var bestArea = Float.MAX_VALUE
    var bestRect = hull
    for (i in hull.indices) {
        val a = hull[i]; val b = hull[(i + 1) % hull.size]
        val len = hypot(b.x - a.x, b.y - a.y)
        if (len == 0f) continue
        val ux = (b.x - a.x) / len; val uy = (b.y - a.y) / len  // edge direction
        val vx = -uy; val vy = ux                                // perpendicular
        var minU = Float.MAX_VALUE; var maxU = -Float.MAX_VALUE
        var minV = Float.MAX_VALUE; var maxV = -Float.MAX_VALUE
        for (p in hull) {
            val du = (p.x - a.x) * ux + (p.y - a.y) * uy
            val dv = (p.x - a.x) * vx + (p.y - a.y) * vy
            if (du < minU) minU = du; if (du > maxU) maxU = du
            if (dv < minV) minV = dv; if (dv > maxV) maxV = dv
        }
        val area = (maxU - minU) * (maxV - minV)
        if (area < bestArea) {
            bestArea = area
            fun corner(u: Float, v: Float) = Offset(a.x + ux * u + vx * v, a.y + uy * u + vy * v)
            bestRect = listOf(corner(minU, minV), corner(maxU, minV), corner(maxU, maxV), corner(minU, maxV))
        }
    }
    return bestArea to bestRect
}

/**
 * Composites live Compose [screens] into the screen areas of a pre-rendered device [frame] image,
 * with correct perspective — one composable per screen. The screen regions are detected
 * automatically (see [detectScreenRegions]), ordered left-to-right, so `screens[0]` lands on the
 * leftmost device, `screens[1]` on the next, and so on. Use this for a multi-device render (e.g.
 * three phones at different angles) or a single device — just give one content lambda.
 *
 * ```kotlin
 * DeviceImageMockup(
 *     frame = ImageBitmap.imageResource(R.drawable.three_phones),
 *     screens = listOf({ HomeScreen() }, { SettingsScreen() }, { ProfileScreen() }),
 * )
 * ```
 *
 * Each screen's content is laid out at a real-device width ([screenNativeWidth], height derived
 * from that screen's detected quad so it isn't distorted), recorded, then perspective-warped onto
 * the quad. The whole mockup keeps the image's aspect ratio and is sized by [modifier].
 *
 * Screens are located one of two ways. By default detection keys off the dark bezel (see
 * [detectScreenRegions]); tune [bezelDarkness] if needed. Alternatively, pass a [screenColor] to use
 * **chroma keying** (see [detectScreenRegionsByColor]): paint each empty screen a flat distinctive
 * colour in your render and detection locks onto that colour, ignoring bezel, body and background —
 * the robust choice for white-on-white or bezel-less renders. Either way, pass explicit
 * [ScreenRegion]s to the other overload to bypass detection entirely.
 */
@Composable
fun DeviceImageMockup(
    frame: ImageBitmap,
    screens: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    screenNativeWidth: Dp = 411.dp,
    screenColor: Color? = null,
    bezelDarkness: Float = 0.35f,
    screenColorTolerance: Float = 0.5f,
    screenRotations: List<ScreenRotation> = emptyList(),
    deviceKinds: List<DeviceKind> = emptyList(),
) {
    val regions = remember(frame, screenColor, bezelDarkness, screenColorTolerance) {
        if (screenColor != null) detectScreenRegionsByColor(frame, screenColor, screenColorTolerance)
        else detectScreenRegions(frame, bezelDarkness)
    }
    DeviceImageMockup(frame, regions, screens, modifier, screenNativeWidth, screenColorTolerance, screenRotations, deviceKinds)
}

/**
 * Like the auto-detecting [DeviceImageMockup], but you supply the screen [regions] yourself (e.g.
 * from [detectScreenRegions], or measured by hand for renders that don't have near-white screens).
 * `screens[i]` is warped onto `regions[i]`.
 */
@Composable
fun DeviceImageMockup(
    frame: ImageBitmap,
    regions: List<ScreenRegion>,
    screens: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    screenNativeWidth: Dp = 411.dp,
    screenColorTolerance: Float = 0.5f,
    screenRotations: List<ScreenRotation> = emptyList(),
    deviceKinds: List<DeviceKind> = emptyList(),
) {
    val density = LocalDensity.current
    val fw = frame.width.toFloat()
    val fh = frame.height.toFloat()
    val n = min(regions.size, screens.size)
    val layers = (0 until n).map { rememberGraphicsLayer() }

    // Width each screen's content is laid out at (height follows the screen's aspect). A device family
    // mixes very different sizes, so each screen takes its [DeviceKind]'s native width — a desktop laid
    // out at phone width gets an oversized UI. Falls back to the single [screenNativeWidth].
    fun nativeWidth(i: Int): Dp = deviceKinds.getOrNull(i)?.nativeWidth ?: screenNativeWidth

    // Magic-wand each screen from its center at full resolution: flood the contiguous pixels close
    // in colour to the seed (the bezel's colour change stops the flood), which selects the EXACT
    // screen — out to the true edge, rounded corners and all. That mask is knocked out of the frame
    // (content shows through, bezel crops), and the content corners are recomputed from it so the UI
    // fills the screen with no inset rim.
    val (knockedFrame, refined, visibleCounts) = remember(frame, regions, screenColorTolerance, deviceKinds) {
        magicWandScreens(frame, regions.take(n), screenColorTolerance, deviceKinds.map { it.orientation })
    }

    // The device silhouette (the original frame's own alpha): content is masked to it so overshoot
    // past a thin or zero-width bezel can't spill outside the device onto a transparent background.
    val deviceSilhouette = remember(frame) { frame.asAndroidBitmap() }
    val silhouettePaint = remember {
        android.graphics.Paint().apply {
            isFilterBitmap = true
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN)
        }
    }

    // Optional manual quarter-turn per screen, on top of the auto-detected orientation.
    fun steps(i: Int) = screenRotations.getOrElse(i) { ScreenRotation.None }.quarterTurns

    // Content size: the screen's aspect, swapped for a quarter-turn so the rotated UI still fills it.
    fun nativeHeight(r: ScreenRegion, rotSteps: Int, nw: Dp): Dp {
        fun px(o: Offset) = Offset(o.x * fw, o.y * fh)
        val tl = px(r.topLeft); val tr = px(r.topRight); val br = px(r.bottomRight); val bl = px(r.bottomLeft)
        val quadW = (dist(tl, tr) + dist(bl, br)) / 2f
        val quadH = (dist(tl, bl) + dist(tr, br)) / 2f
        val aspect = (quadH / quadW).coerceIn(0.05f, 20f)
        return nw * (if (rotSteps % 2 == 1) 1f / aspect else aspect)
    }

    Box(modifier.aspectRatio(fw / fh)) {
        // Recorders: compose each screen at native size, record into its layer, draw nothing.
        for (i in 0 until n) {
            val nh = nativeHeight(refined[i], steps(i), nativeWidth(i))
            Box(
                Modifier
                    .layout { measurable, _ ->
                        val placeable = measurable.measure(
                            Constraints.fixed(nativeWidth(i).roundToPx(), nh.roundToPx())
                        )
                        layout(0, 0) { placeable.place(0, 0) }
                    }
                    .drawWithContent { layers[i].record { this@drawWithContent.drawContent() } }
            ) { Box(Modifier.fillMaxSize()) { screens[i]() } }
        }

        // Depth order: a screen partly hidden by another device's frame floods fewer pixels than its
        // full quad, so its occluded fraction (1 − visible/quad) tells us how far back it sits. Draw the
        // most-occluded (furthest back) first and the least-occluded (front-most) last, so a front
        // device's content wins inside its own screen hole instead of a device behind it bleeding through.
        val drawOrder = remember(refined, visibleCounts, fw, fh, n) {
            (0 until n).sortedByDescending { i ->
                val quadPx = quadArea(refined[i]) * fw * fh
                if (quadPx < 1f) 0f else 1f - (visibleCounts[i] / quadPx).coerceIn(0f, 1f)
            }
        }

        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val dstRect = android.graphics.Rect(0, 0, w.toInt(), h.toInt())
            drawIntoCanvas { canvas ->
                val nc = canvas.nativeCanvas
                // Group the warped content into a layer so it can be masked as a whole.
                val layer = nc.saveLayer(null, null)
                // 1) Live content, perspective-warped onto each screen quad — drawn BEHIND the frame.
                for (i in drawOrder) {
                    val r = refined[i]
                    val s = steps(i)
                    val nw = with(density) { nativeWidth(i).toPx() }
                    val nhp = with(density) { nativeHeight(r, s, nativeWidth(i)).toPx() }
                    val src = floatArrayOf(0f, 0f, nw, 0f, nw, nhp, 0f, nhp)
                    // Manual rotation = cyclically shift which screen corner each content corner maps to.
                    val q = listOf(r.topLeft, r.topRight, r.bottomRight, r.bottomLeft)
                    val d = (0 until 4).map { q[(it + s) % 4] }
                    val dst = floatArrayOf(
                        d[0].x * w, d[0].y * h, d[1].x * w, d[1].y * h,
                        d[2].x * w, d[2].y * h, d[3].x * w, d[3].y * h,
                    )
                    val m = Matrix().apply { setPolyToPoly(src, 0, dst, 0, 4) }
                    nc.save()
                    nc.concat(m)
                    drawLayer(layers[i])
                    nc.restore()
                }
                // 2) Mask content to the device silhouette (DST_IN keeps it only where the original
                //    frame is opaque), so a grown quad that overshoots past a thin or zero-width bezel
                //    can't spill outside the device onto a transparent-background banner. Combined with
                //    the knocked-out frame on top, content ends up cropped to the screen shape itself.
                nc.drawBitmap(deviceSilhouette, null, dstRect, silhouettePaint)
                nc.restoreToCount(layer)
            }
            // 3) The frame with its screens knocked out, ON TOP — its bezel crops the content.
            drawImage(image = knockedFrame, dstSize = IntSize(w.toInt(), h.toInt()))
        }
    }
}

/**
 * Magic-wand selection of each screen. For every rough [regions] entry, flood-fills from its center
 * at full resolution across contiguous pixels whose colour is within [tolerance] (0..1) of the seed
 * — the bezel's colour change halts the flood, so the selection is the exact screen out to its true
 * edge (rounded corners included). Those pixels are made transparent in a copy of [frame] (the
 * knockout), and the four screen corners are recomputed from the selection so content fills it with
 * no inset. Returns the knocked-out frame and the refined regions, paired by index.
 */
private fun magicWandScreens(
    frame: ImageBitmap,
    regions: List<ScreenRegion>,
    tolerance: Float,
    orientations: List<KindOrientation?>,
): Triple<ImageBitmap, List<ScreenRegion>, List<Int>> {
    val bmp = frame.asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)
    bmp.setHasAlpha(true) // a JPEG-backed bitmap is flagged opaque; without this the alpha is ignored
    val w = bmp.width; val h = bmp.height
    val pixels = IntArray(w * h)
    bmp.getPixels(pixels, 0, w, 0, 0, w, h)
    val tol = (tolerance * 255f).toInt()
    val visited = BooleanArray(w * h)
    val queue = IntArray(w * h)
    val refined = ArrayList<ScreenRegion>(regions.size)
    // Visible (un-occluded) pixel count per screen: the flood stops at any device drawn in front of
    // this one, so a screen that's partly hidden floods fewer pixels than its full quad — the caller
    // uses this to order draws by depth (a more-occluded screen is further back).
    val visibleCounts = ArrayList<Int>(regions.size)

    for ((index, region) in regions.withIndex()) {
        val cx = ((region.topLeft.x + region.topRight.x + region.bottomRight.x + region.bottomLeft.x) / 4f * w).toInt().coerceIn(0, w - 1)
        val cy = ((region.topLeft.y + region.topRight.y + region.bottomRight.y + region.bottomLeft.y) / 4f * h).toInt().coerceIn(0, h - 1)
        val seed = cy * w + cx
        if (visited[seed]) { refined += region; visibleCounts += 0; continue }
        val sp = pixels[seed]
        val sr = (sp shr 16) and 0xFF; val sg = (sp shr 8) and 0xFF; val sb = sp and 0xFF
        // Per-row left/right and per-column top/bottom extents → a full outline for the corner fit.
        val rowMinX = IntArray(h) { Int.MAX_VALUE }
        val rowMaxX = IntArray(h) { -1 }
        val colMinY = IntArray(w) { Int.MAX_VALUE }
        val colMaxY = IntArray(w) { -1 }
        var head = 0; var tail = 0
        queue[tail++] = seed; visited[seed] = true
        while (head < tail) {
            val idx = queue[head++]
            val x = idx % w; val y = idx / w
            pixels[idx] = pixels[idx] and 0x00FFFFFF // alpha → 0 (knock out)
            if (x < rowMinX[y]) rowMinX[y] = x
            if (x > rowMaxX[y]) rowMaxX[y] = x
            if (y < colMinY[x]) colMinY[x] = y
            if (y > colMaxY[x]) colMaxY[x] = y
            if (x > 0 && !visited[idx - 1] && matchesSeed(pixels[idx - 1], sr, sg, sb, tol)) { visited[idx - 1] = true; queue[tail++] = idx - 1 }
            if (x < w - 1 && !visited[idx + 1] && matchesSeed(pixels[idx + 1], sr, sg, sb, tol)) { visited[idx + 1] = true; queue[tail++] = idx + 1 }
            if (y > 0 && !visited[idx - w] && matchesSeed(pixels[idx - w], sr, sg, sb, tol)) { visited[idx - w] = true; queue[tail++] = idx - w }
            if (y < h - 1 && !visited[idx + w] && matchesSeed(pixels[idx + w], sr, sg, sb, tol)) { visited[idx + w] = true; queue[tail++] = idx + w }
        }
        val floodCount = tail // visible (un-occluded) screen area, before the rim dilation grows it
        // Expand a few pixels into the keyed colour's anti-aliased RIM — the pixels where the screen
        // colour blends toward the bezel. They don't match the tight flood, but a LOOSER colour match
        // catches them; because it still matches the KEYED colour it can never eat a bezel of another
        // colour (a white, grey or coloured bezel is far from the key, so it's left alone). Bounded, and
        // it updates the extents so the corner fit lands on the softened edge instead of inside the rim.
        val rimTol = min(255, (tolerance * 255f * 2.5f).toInt())
        val maxRim = max(2, min(w, h) / 300)
        var rimStart = 0; var rimEnd = tail; var rim = 0
        while (rim < maxRim && rimStart < rimEnd) {
            for (qi in rimStart until rimEnd) {
                val idx = queue[qi]
                val x = idx % w; val y = idx / w
                fun grab(n: Int) {
                    if (!visited[n] && matchesSeed(pixels[n], sr, sg, sb, rimTol)) {
                        visited[n] = true
                        pixels[n] = pixels[n] and 0x00FFFFFF
                        val nx = n % w; val ny = n / w
                        if (nx < rowMinX[ny]) rowMinX[ny] = nx
                        if (nx > rowMaxX[ny]) rowMaxX[ny] = nx
                        if (ny < colMinY[nx]) colMinY[nx] = ny
                        if (ny > colMaxY[nx]) colMaxY[nx] = ny
                        queue[tail++] = n
                    }
                }
                if (x > 0) grab(idx - 1)
                if (x < w - 1) grab(idx + 1)
                if (y > 0) grab(idx - w)
                if (y < h - 1) grab(idx + w)
            }
            rimStart = rimEnd
            if (tail > rimEnd) { rimEnd = tail; rim++ } else break
        }
        // The corner fit uses the keyed-colour outline (the flood plus its rim) built here.
        val boundary = ArrayList<Offset>()
        for (y in 0 until h) if (rowMaxX[y] >= 0) {
            boundary += Offset(rowMinX[y].toFloat(), y.toFloat())
            boundary += Offset(rowMaxX[y].toFloat(), y.toFloat())
        }
        for (x in 0 until w) if (colMaxY[x] >= 0) {
            boundary += Offset(x.toFloat(), colMinY[x].toFloat())
            boundary += Offset(x.toFloat(), colMaxY[x].toFloat())
        }
        if (boundary.size < 8) { refined += region; visibleCounts += floodCount; continue }
        val rect = minAreaRect(convexHull(boundary)).second
        // Circumscribed corners: fit a line to each of the four straight sides and intersect adjacent
        // lines, so the corners land where the edges meet (past the rounded arcs) and content fills the
        // whole screen. When that's unreliable — e.g. another device overlaps this one and occludes a
        // side — fall back to the bounding [rect] corners directly (NOT the nearest visible boundary
        // point, which would pull an occluded corner inward and shrink the screen). The rect still spans
        // the full screen as long as the opposite corners are visible, and any overshoot behind the
        // occluding device is simply painted over by it.
        val corners = circumscribedCorners(boundary, rect) ?: rect
        // Grow the quad outward a hair — uniformly, in pixels — to cover the keyed outline's
        // anti-aliased rim (where the warped content edge and the knockout edge don't quite meet).
        // Overshoot past the screen lands on the bezel, which crops it; the bezel is never knocked out.
        val o = growPixels(snapAxisAligned(orientCorners(corners, orientations.getOrNull(index))), SCREEN_MARGIN_PX)
        refined += ScreenRegion(
            Offset(o[0].x / w, o[0].y / h), Offset(o[1].x / w, o[1].y / h),
            Offset(o[2].x / w, o[2].y / h), Offset(o[3].x / w, o[3].y / h),
        )
        visibleCounts += floodCount
    }
    featherBezelIntoScreen(pixels, w, h, queue)
    bmp.setPixels(pixels, 0, w, 0, 0, w, h)
    return Triple(bmp.asImageBitmap(), refined, visibleCounts)
}

/** Extra pixels to grow the quad past the dilation reach, covering the keyed rim and the feather ring. */
private const val SCREEN_MARGIN_PX = 1f

/**
 * Softens the knockout's edge by merging the BEZEL into the screen (not the other way round). The
 * [radius]-pixel ring just *inside* the screen is overlaid with the adjacent bezel's own colour at a
 * ramped alpha — nearly opaque where it touches the bezel, fading to transparent inward — so the dark
 * bezel dissolves over the screen's edge instead of the screen bleeding out into the bezel. That hides
 * any bright glass-edge rim under a soft inset rather than smearing it across the seam. A breadth-first
 * distance transform inward from the bezel boundary gives each ring pixel its index and the bezel
 * colour to apply; [scratch] is reused as the BFS queue.
 */
private fun featherBezelIntoScreen(pixels: IntArray, w: Int, h: Int, scratch: IntArray) {
    val radius = max(2, min(w, h) / 960)
    val dist = IntArray(w * h) { -1 }
    val color = IntArray(w * h)
    val sampleOut = max(2, min(w, h) / 300) // sample the bezel colour this far out, past any thin glass rim
    var head = 0; var tail = 0
    // Seed with the bezel (opaque) pixels that touch a knocked-out (transparent) screen pixel — but take
    // the colour from `sampleOut` pixels further OUT along the outward direction, past any thin glass-edge
    // highlight, so we feather with the real bezel colour whatever it is (dark, light or coloured) and a
    // bright rim doesn't get smeared back over the screen.
    for (i in pixels.indices) {
        if ((pixels[i] ushr 24) == 0) continue
        val x = i % w; val y = i / w
        var sdx = 0; var sdy = 0; var touchesScreen = false
        if (x > 0 && (pixels[i - 1] ushr 24) == 0) { sdx -= 1; touchesScreen = true }
        if (x < w - 1 && (pixels[i + 1] ushr 24) == 0) { sdx += 1; touchesScreen = true }
        if (y > 0 && (pixels[i - w] ushr 24) == 0) { sdy -= 1; touchesScreen = true }
        if (y < h - 1 && (pixels[i + w] ushr 24) == 0) { sdy += 1; touchesScreen = true }
        if (!touchesScreen) continue
        // outward = away from the screen (opposite the transparent neighbours)
        val ox = if (sdx > 0) -1 else if (sdx < 0) 1 else 0
        val oy = if (sdy > 0) -1 else if (sdy < 0) 1 else 0
        val sx = x + ox * sampleOut; val sy = y + oy * sampleOut
        val si = if (sx in 0 until w && sy in 0 until h && (pixels[sy * w + sx] ushr 24) != 0) sy * w + sx else i
        dist[i] = 0; color[i] = pixels[si]; scratch[tail++] = i
    }
    // Flood inward across screen (transparent) pixels only, carrying the bezel colour with it.
    while (head < tail) {
        val idx = scratch[head++]
        val d = dist[idx]
        if (d >= radius) continue
        val x = idx % w; val y = idx / w
        fun step(n: Int) {
            if (dist[n] < 0 && (pixels[n] ushr 24) == 0) { dist[n] = d + 1; color[n] = color[idx]; scratch[tail++] = n }
        }
        if (x > 0) step(idx - 1)
        if (x < w - 1) step(idx + 1)
        if (y > 0) step(idx - w)
        if (y < h - 1) step(idx + w)
    }
    // Overlay the bezel colour on the inner ring: strongest at the edge (d=1), fading to clear inward.
    for (i in pixels.indices) {
        val d = dist[i]
        if (d in 1..radius) pixels[i] = (color[i] and 0x00FFFFFF) or ((255 * (radius + 1 - d) / (radius + 1)) shl 24)
    }
}

/**
 * Grows a quad outward by [d] pixels uniformly, keeping its shape: each edge is offset [d] pixels
 * along its outward normal and adjacent offset edges are re-intersected for the new corners. Unlike a
 * centroid-relative scale, the outset is the same width on every side regardless of the quad's size or
 * aspect, so a small screen and a large one both gain exactly [d] pixels of margin. If two offset edges
 * are near-parallel their intersection shoots off to infinity; in that case the corner falls back to a
 * straight centroid-direction push so a slightly skewed fit can't explode the quad.
 */
private fun growPixels(c: List<Offset>, d: Float): List<Offset> {
    val cx = (c[0].x + c[1].x + c[2].x + c[3].x) / 4f
    val cy = (c[0].y + c[1].y + c[2].y + c[3].y) / 4f
    val lines = ArrayList<Pair<Offset, Offset>>(4)
    for (i in 0 until 4) {
        val a = c[i]; val b = c[(i + 1) % 4]
        val ex = b.x - a.x; val ey = b.y - a.y
        val len = hypot(ex, ey)
        if (len < 1e-3f) return c
        var nx = ey / len; var ny = -ex / len // edge normal
        val mx = (a.x + b.x) / 2f; val my = (a.y + b.y) / 2f
        if ((mx - cx) * nx + (my - cy) * ny < 0f) { nx = -nx; ny = -ny } // point outward, away from centroid
        lines += Offset(a.x + nx * d, a.y + ny * d) to Offset(ex / len, ey / len)
    }
    val maxReach = 4f * d + 4f // a clean corner lands ~d·√2 out; anything far past that is a degenerate fit
    return (0 until 4).map { i ->
        val p = lineIntersect(lines[(i + 3) % 4], lines[i])
        if (p == null || dist(p, c[i]) > maxReach) {
            val vx = c[i].x - cx; val vy = c[i].y - cy; val vl = hypot(vx, vy)
            if (vl < 1e-3f) c[i] else Offset(c[i].x + vx / vl * d, c[i].y + vy / vl * d)
        } else p
    }
}

private fun matchesSeed(p: Int, sr: Int, sg: Int, sb: Int, tol: Int): Boolean =
    abs(((p shr 16) and 0xFF) - sr) <= tol && abs(((p shr 8) and 0xFF) - sg) <= tol && abs((p and 0xFF) - sb) <= tol

/**
 * The four circumscribed corners of a rounded screen. Using the [rect]'s axes as a frame, [boundary]
 * points are split into the four straight sides (corner/arc points excluded); a line is fit to each
 * side (which averages out pixel staircasing on tilted edges and extrapolates past the rounding),
 * and adjacent side-lines are intersected. Corners land where the straight edges meet — outside the
 * arcs — so content fills the whole screen. Returns null if a side has too few points (caller falls
 * back to the inscribed extreme points).
 */
private fun circumscribedCorners(boundary: List<Offset>, rect: List<Offset>): List<Offset>? {
    val o = rect[0]
    val uLen = dist(rect[0], rect[1]); val vLen = dist(rect[0], rect[3])
    if (uLen < 1f || vLen < 1f) return null
    val ux = (rect[1].x - rect[0].x) / uLen; val uy = (rect[1].y - rect[0].y) / uLen
    val vx = (rect[3].x - rect[0].x) / vLen; val vy = (rect[3].y - rect[0].y) / vLen
    val left = ArrayList<Offset>(); val right = ArrayList<Offset>()
    val top = ArrayList<Offset>(); val bottom = ArrayList<Offset>()
    // Fit each side from its OUTER thirds only, skipping the middle of the edge. That dodges a centered
    // cutout (a notch or pill that dips into the screen) — its boundary would otherwise drag the line
    // fit inward — while still giving each side plenty of straight-edge points to fit.
    fun outer(t: Float) = t in 0.15f..0.40f || t in 0.60f..0.85f
    for (p in boundary) {
        val du = ((p.x - o.x) * ux + (p.y - o.y) * uy) / uLen // 0..1 across the rect
        val dv = ((p.x - o.x) * vx + (p.y - o.y) * vy) / vLen
        if (outer(dv)) { if (du < 0.12f) left += p else if (du > 0.88f) right += p }
        if (outer(du)) { if (dv < 0.12f) top += p else if (dv > 0.88f) bottom += p }
    }
    if (left.size < 5 || right.size < 5 || top.size < 5 || bottom.size < 5) return null
    // Each side's *slope* comes from a total-least-squares fit of its outer thirds (robust to the
    // notch and to pixel staircasing). Its *position*, though, is pinned to the outermost boundary
    // point along that side's outward normal — so the four lines circumscribe the WHOLE selection,
    // including green that bulges past the average edge (the horns either side of a notch). Pinning
    // to the extreme only translates the line (keeps the fitted slope), so it can't over-grow a
    // straight edge but guarantees the quad contains every knocked-out pixel.
    val lL = pinToExtreme(fitLine(left), boundary, -ux, -uy)
    val lR = pinToExtreme(fitLine(right), boundary, ux, uy)
    val lT = pinToExtreme(fitLine(top), boundary, -vx, -vy)
    val lB = pinToExtreme(fitLine(bottom), boundary, vx, vy)
    val tl = lineIntersect(lL, lT) ?: return null
    val tr = lineIntersect(lT, lR) ?: return null
    val br = lineIntersect(lR, lB) ?: return null
    val bl = lineIntersect(lB, lL) ?: return null
    // Reject a degenerate fit: if a side is occluded (one device overlapping another) its points are
    // sparse/skewed, the line fit goes off-axis, and two near-parallel side-lines intersect far away —
    // throwing a corner off to infinity. A real corner sits within a hair of the bounding [rect], so if
    // any corner lands well outside it the fit is unreliable; fall back (caller uses the rect instead).
    val rcx = (rect[0].x + rect[2].x) / 2f; val rcy = (rect[0].y + rect[2].y) / 2f
    val limit = dist(rect[0], rect[2]) // the rect's diagonal: a corner farther than this from centre is bogus
    for (p in listOf(tl, tr, br, bl)) if (hypot(p.x - rcx, p.y - rcy) > limit) return null
    return listOf(tl, tr, br, bl)
}

/**
 * Translates a fitted [line] (keeping its direction) outward until it touches the [boundary] point
 * farthest along the outward normal `(nx, ny)`. The line then has the fitted slope but sits at the
 * outer extreme of the selection, so the side it bounds contains every selected pixel on that side.
 */
private fun pinToExtreme(line: Pair<Offset, Offset>, boundary: List<Offset>, nx: Float, ny: Float): Pair<Offset, Offset> {
    val (p, d) = line
    var maxDist = -Float.MAX_VALUE
    for (q in boundary) {
        val dd = (q.x - p.x) * nx + (q.y - p.y) * ny
        if (dd > maxDist) maxDist = dd
    }
    return Offset(p.x + nx * maxDist, p.y + ny * maxDist) to d
}

/** Total-least-squares line fit: returns a point on the line and its (unit) direction. */
private fun fitLine(pts: List<Offset>): Pair<Offset, Offset> {
    val cx = pts.sumOf { it.x.toDouble() }.toFloat() / pts.size
    val cy = pts.sumOf { it.y.toDouble() }.toFloat() / pts.size
    var sxx = 0f; var syy = 0f; var sxy = 0f
    for (p in pts) { val dx = p.x - cx; val dy = p.y - cy; sxx += dx * dx; syy += dy * dy; sxy += dx * dy }
    val theta = 0.5f * atan2(2f * sxy, sxx - syy)
    return Offset(cx, cy) to Offset(cos(theta), sin(theta))
}

/** Intersection of two lines each given as (point, direction); null if near-parallel. */
private fun lineIntersect(l1: Pair<Offset, Offset>, l2: Pair<Offset, Offset>): Offset? {
    val (p1, d1) = l1; val (p2, d2) = l2
    val denom = d1.x * d2.y - d1.y * d2.x
    if (abs(denom) < 1e-4f) return null
    val t = ((p2.x - p1.x) * d2.y - (p2.y - p1.y) * d2.x) / denom
    return Offset(p1.x + d1.x * t, p1.y + d1.y * t)
}

/**
 * Single-screen convenience: warp [content] onto one screen quad given by its four corners (as
 * fractions of [frame]). See the multi-screen [DeviceImageMockup] for details.
 */
@Composable
fun DeviceImageMockup(
    frame: ImageBitmap,
    screenTopLeft: Offset,
    screenTopRight: Offset,
    screenBottomRight: Offset,
    screenBottomLeft: Offset,
    modifier: Modifier = Modifier,
    screenNativeWidth: Dp = 411.dp,
    screenColorTolerance: Float = 0.5f,
    rotation: ScreenRotation = ScreenRotation.None,
    content: @Composable () -> Unit,
) {
    DeviceImageMockup(
        frame = frame,
        regions = listOf(ScreenRegion(screenTopLeft, screenTopRight, screenBottomRight, screenBottomLeft)),
        screens = listOf(content),
        modifier = modifier,
        screenNativeWidth = screenNativeWidth,
        screenColorTolerance = screenColorTolerance,
        screenRotations = listOf(rotation),
    )
}

private fun dist(a: Offset, b: Offset): Float = hypot(a.x - b.x, a.y - b.y)

/**
 * If an oriented quad [c] (TL, TR, BR, BL) is within a few degrees of axis-aligned, snap it to its
 * axis-aligned bounding box. A flat, front-facing screen should warp with no perspective, but when it's
 * partly occluded by another device the corner fit comes out slightly skewed — enough to tilt the UI.
 * Genuinely tilted screens (a phone shown at an angle) are well past the tolerance and are left alone.
 */
private fun snapAxisAligned(c: List<Offset>): List<Offset> {
    val tol = 0.105f // ~6°
    val topAngle = atan2(c[1].y - c[0].y, c[1].x - c[0].x)        // 0 when the top edge is horizontal
    val leftAngle = atan2(c[3].y - c[0].y, c[3].x - c[0].x)       // PI/2 when the left edge points down
    if (abs(topAngle) > tol) return c
    if (abs(abs(leftAngle) - PI.toFloat() / 2f) > tol) return c
    val minX = minOf(c[0].x, c[1].x, c[2].x, c[3].x); val maxX = maxOf(c[0].x, c[1].x, c[2].x, c[3].x)
    val minY = minOf(c[0].y, c[1].y, c[2].y, c[3].y); val maxY = maxOf(c[0].y, c[1].y, c[2].y, c[3].y)
    return listOf(Offset(minX, minY), Offset(maxX, minY), Offset(maxX, maxY), Offset(minX, maxY))
}

/** Area of a screen's (fractional) corner quad via the shoelace formula — used to order draws by depth. */
private fun quadArea(r: ScreenRegion): Float {
    val x = floatArrayOf(r.topLeft.x, r.topRight.x, r.bottomRight.x, r.bottomLeft.x)
    val y = floatArrayOf(r.topLeft.y, r.topRight.y, r.bottomRight.y, r.bottomLeft.y)
    var a = 0f
    for (k in 0 until 4) { val j = (k + 1) % 4; a += x[k] * y[j] - x[j] * y[k] }
    return abs(a) / 2f
}
