package dev.lucianosantos.storescreenshots.frames

import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Measuring plumbing for [IPhone17ProMaxBezelComparisonTest]: pulling the same features out of the
 * committed Simulator capture and out of the bezel this library draws.
 *
 * Neither image can be measured by colour alone — the black bezel, an unlit screen and empty space
 * are all the same pixel — so both are composited onto [Key], a magenta that appears nowhere in a
 * titanium-and-glass phone. Everything below then works off "is this pixel part of the device",
 * which is what the geometry actually turns on.
 */
internal object BezelReference {

    /** The backdrop both images are flattened onto. Any pixel still this colour is not the device. */
    const val Key = 0xFFFF00FF.toInt()

    /** Flattens [image]'s alpha onto [Key] so the two images can be measured the same way. */
    fun onKey(image: BufferedImage): BufferedImage {
        val out = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        val kr = (Key ushr 16) and 0xFF
        val kg = (Key ushr 8) and 0xFF
        val kb = Key and 0xFF
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val argb = image.getRGB(x, y)
                val a = ((argb ushr 24) and 0xFF) / 255f
                fun blend(shift: Int, key: Int) =
                    (((argb ushr shift) and 0xFF) * a + key * (1 - a)).toInt().coerceIn(0, 255)
                out.setRGB(x, y, (0xFF shl 24) or (blend(16, kr) shl 16) or (blend(8, kg) shl 8) or blend(0, kb))
            }
        }
        return out
    }

    /**
     * True where the pixel belongs to the device. A pixel half-covered by an anti-aliased edge is
     * half-keyed, so the test counts it as device only once it is mostly device.
     */
    fun isDevice(image: BufferedImage, x: Int, y: Int): Boolean {
        val v = image.getRGB(x, y)
        val r = (v ushr 16) and 0xFF
        val g = (v ushr 8) and 0xFF
        val b = v and 0xFF
        // Keyed pixels are magenta: red and blue high, green low. Anything with green in it, or
        // without both of the other two, is the device.
        return !(r > 128 && b > 128 && g < 128)
    }

    /** True where the pixel is the lit display: part of the device, and near-white. */
    fun isScreen(image: BufferedImage, x: Int, y: Int): Boolean {
        val v = image.getRGB(x, y)
        val r = (v ushr 16) and 0xFF
        val g = (v ushr 8) and 0xFF
        val b = v and 0xFF
        return g > 190 && r > 190 && b > 190 && abs(r - g) < 40 && abs(g - b) < 40
    }

    /**
     * Fraction of the two silhouettes' combined area that both cover. A body that is the right size
     * but a point out of place scores well below 1.0 here, which is the point: it is the one number
     * that catches "the shape moved" without naming a feature first.
     */
    fun silhouetteIou(a: BufferedImage, b: BufferedImage): Float {
        var intersection = 0
        var union = 0
        for (y in 0 until minOf(a.height, b.height)) {
            for (x in 0 until minOf(a.width, b.width)) {
                val inA = isDevice(a, x, y)
                val inB = isDevice(b, x, y)
                if (inA && inB) intersection++
                if (inA || inB) union++
            }
        }
        return if (union == 0) 1f else intersection.toFloat() / union
    }

    private fun rowEdges(image: BufferedImage, y: Int): Pair<Int, Int>? {
        val l = (0 until image.width).firstOrNull { isDevice(image, it, y) } ?: return null
        val r = (image.width - 1 downTo 0).first { isDevice(image, it, y) }
        return l to r
    }

    /**
     * The enclosure's own bounds, ignoring the side buttons: the left and right edge each row
     * *usually* has. The buttons only ever touch a few hundred rows out of three thousand, so the
     * commonest edge is the body's.
     */
    fun bodyBounds(image: BufferedImage): Rect {
        val left = mutableMapOf<Int, Int>()
        val right = mutableMapOf<Int, Int>()
        var top = Int.MAX_VALUE
        var bottom = Int.MIN_VALUE
        for (y in 0 until image.height) {
            val (l, r) = rowEdges(image, y) ?: continue
            left[l] = (left[l] ?: 0) + 1
            right[r] = (right[r] ?: 0) + 1
            if (y < top) top = y
            if (y > bottom) bottom = y
        }
        return Rect(left.maxByOrNull { it.value }!!.key, top, right.maxByOrNull { it.value }!!.key, bottom)
    }

    /** Bounds of the lit display inside the device, or null if nothing in the frame is lit. */
    fun screenBounds(image: BufferedImage): Rect? {
        var l = Int.MAX_VALUE; var r = Int.MIN_VALUE; var t = Int.MAX_VALUE; var b = Int.MIN_VALUE
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                if (isScreen(image, x, y)) {
                    if (x < l) l = x
                    if (x > r) r = x
                    if (y < t) t = y
                    if (y > b) b = y
                }
            }
        }
        return if (l == Int.MAX_VALUE) null else Rect(l, t, r, b)
    }

    /**
     * The cutout at the top of the display: the unlit island near the top of [screen]. Searched only
     * in the middle half of the display's width, so the display's own rounded corners — unlit too —
     * are never mistaken for it.
     */
    fun islandBounds(image: BufferedImage, screen: Rect): Rect? {
        var l = Int.MAX_VALUE; var r = Int.MIN_VALUE; var t = Int.MAX_VALUE; var b = Int.MIN_VALUE
        val bottom = minOf(screen.top + screen.height / 8, image.height - 1)
        val from = screen.left + screen.width / 4
        val to = screen.right - screen.width / 4
        for (y in screen.top..bottom) {
            for (x in from..to) {
                if (!isScreen(image, x, y)) {
                    if (x < l) l = x
                    if (x > r) r = x
                    if (y < t) t = y
                    if (y > b) b = y
                }
            }
        }
        return if (l == Int.MAX_VALUE) null else Rect(l, t, r, b)
    }

    /** How far the silhouette reaches past [body] on each side — the side buttons' protrusion. */
    fun protrusions(image: BufferedImage, body: Rect): Pair<Int, Int> {
        var left = 0
        var right = 0
        for (y in body.top..body.bottom) {
            val (l, r) = rowEdges(image, y) ?: continue
            if (body.left - l > left) left = body.left - l
            if (r - body.right > right) right = r - body.right
        }
        return left to right
    }

    /**
     * Every contiguous stretch of rows where the silhouette reaches past [body] — one per side
     * button, top-most first, as (top row relative to the body's top, height in rows).
     */
    fun buttonRuns(image: BufferedImage, body: Rect, onLeft: Boolean): List<Pair<Int, Int>> {
        val rows = (body.top..body.bottom).filter { y ->
            val (l, r) = rowEdges(image, y) ?: return@filter false
            if (onLeft) l < body.left - 1 else r > body.right + 1
        }
        if (rows.isEmpty()) return emptyList()
        val runs = mutableListOf(mutableListOf(rows.first()))
        rows.drop(1).forEach { y ->
            // A one-row gap is anti-aliasing at a button's rounded end, not two buttons.
            if (y - runs.last().last() <= 2) runs.last() += y else runs += mutableListOf(y)
        }
        return runs.map { (it.first() - body.top) to (it.last() - it.first() + 1) }
    }

    /**
     * How rounded [box]'s corners are, as the radius of the circle with the same diagonal reach.
     *
     * Walking in along the diagonal from a corner is the one probe that is stable on both images: it
     * crosses the curve at right angles, so a pixel of anti-aliasing costs a pixel of radius rather
     * than skewing a fit, and it needs no assumption about where the curve ends. A circular corner
     * of radius R sits `R(√2 − 1)` from the corner along that diagonal, which is what this inverts.
     * iOS draws continuous curves rather than arcs, so the number is a like-for-like comparison of
     * two shapes, not either one's true radius.
     *
     * All four corners are probed and averaged, so a frame that rounds only some of them is caught.
     */
    fun cornerRadius(image: BufferedImage, box: Rect, inside: (BufferedImage, Int, Int) -> Boolean): Float {
        val corners = listOf(
            Triple(box.left, box.top, 1 to 1),
            Triple(box.right, box.top, -1 to 1),
            Triple(box.left, box.bottom, 1 to -1),
            Triple(box.right, box.bottom, -1 to -1),
        )
        val radii = corners.map { (cx, cy, step) ->
            val (sx, sy) = step
            var i = 0
            while (i < box.width / 2 && !inside(image, cx + sx * i, cy + sy * i)) i++
            // The probe advances a pixel in each axis per step, so it has travelled i√2 pixels.
            (i * sqrt(2f)) / (sqrt(2f) - 1)
        }
        return radii.sum() / radii.size
    }
}
