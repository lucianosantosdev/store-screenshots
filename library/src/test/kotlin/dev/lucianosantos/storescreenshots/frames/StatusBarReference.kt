package dev.lucianosantos.storescreenshots.frames

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

/**
 * Image plumbing for [IosStatusBarComparisonTest]: loading the committed iPhone 17 capture and
 * measuring the same features out of it and out of the status bar this library draws.
 *
 * Everything works on plain AWT images rather than Android bitmaps — the reference PNG and the
 * Roborazzi output are both real files on disk, and `ImageIO` reads them without dragging
 * Robolectric's bitmap emulation into the measurements.
 */
internal object StatusBarReference {

    /**
     * Loads one of the committed captures: `iphone17_statusbar.png`, the top strip of an iPhone 17's
     * framebuffer at @3x, or `ipad13_statusbar.png`, the same for a 13-inch iPad Air at @2x. Both
     * were taken from the Xcode Simulator with the status bar overridden to Apple's usual state:
     *
     * ```
     * xcrun simctl boot "iPhone 17"
     * xcrun simctl status_bar booted override --time 9:41 \
     *     --dataNetwork wifi --wifiMode active --wifiBars 3 \
     *     --cellularMode active --cellularBars 4 \
     *     --batteryState discharging --batteryLevel 100
     * xcrun simctl io booted screenshot screen.png    # crop the top strip, full width
     * ```
     *
     * The iPad is captured in dark appearance so its glyphs are white, and `--cellularMode` is left
     * off: a Wi-Fi iPad shows no cellular bars, which is what its slot's screenshots depict.
     *
     * Each capture's width is its device's screen width in points times its scale — 1206 px at @3x
     * is [IPhone17Metrics.ScreenWidth], 2048 px at @2x is [IPadAir13Metrics.ScreenWidth] — so a
     * capture's pixels map onto the metrics directly.
     */
    fun load(name: String): BufferedImage {
        val stream = StatusBarReference::class.java.getResourceAsStream("/reference/$name")
            ?: error("Missing /reference/$name — see the KDoc on StatusBarReference")
        return stream.use { ImageIO.read(it) }
    }

    fun read(file: File): BufferedImage = ImageIO.read(file) ?: error("Could not decode $file")

    /** Drops any alpha onto black, so a capture and a render can be compared channel for channel. */
    fun opaque(image: BufferedImage): BufferedImage {
        val out = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val argb = image.getRGB(x, y)
                val a = (argb ushr 24) and 0xFF
                out.setRGB(
                    x, y,
                    if (a == 255) argb or (0xFF shl 24) else scale(argb, a / 255f),
                )
            }
        }
        return out
    }

    private fun scale(color: Int, alpha: Float): Int {
        fun ch(shift: Int) = (((color ushr shift) and 0xFF) * alpha).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    fun rgb(image: BufferedImage, x: Int, y: Int): Triple<Int, Int, Int> {
        val v = image.getRGB(x, y)
        return Triple((v ushr 16) and 0xFF, (v ushr 8) and 0xFF, v and 0xFF)
    }

    fun luma(image: BufferedImage, x: Int, y: Int): Float {
        val (r, g, b) = rgb(image, x, y)
        return (r + g + b) / 3f
    }

    /**
     * Intersection-over-union of the "ink" — pixels brighter than [threshold] — in [region] of both
     * images. For a glyph, being a pixel out matters far less than being the wrong shape, and IoU
     * says exactly that: 1.0 is identical, and it falls off with area the two do not share.
     */
    fun inkIou(a: BufferedImage, b: BufferedImage, region: Rect, threshold: Float = 128f): Float {
        var intersection = 0
        var union = 0
        for (y in region.top..region.bottom) {
            for (x in region.left..region.right) {
                val inA = luma(a, x, y) > threshold
                val inB = luma(b, x, y) > threshold
                if (inA && inB) intersection++
                if (inA || inB) union++
            }
        }
        return if (union == 0) 1f else intersection.toFloat() / union
    }

    /** Bounding box of pixels brighter than [threshold] inside [region], or null if there are none. */
    fun inkBounds(image: BufferedImage, region: Rect, threshold: Float = 128f): Rect? {
        var l = Int.MAX_VALUE; var r = Int.MIN_VALUE; var t = Int.MAX_VALUE; var b = Int.MIN_VALUE
        for (y in region.top..region.bottom) {
            for (x in region.left..region.right) {
                if (luma(image, x, y) > threshold) {
                    l = min(l, x); r = max(r, x); t = min(t, y); b = max(b, y)
                }
            }
        }
        return if (l == Int.MAX_VALUE) null else Rect(l, t, r, b)
    }
}

/** An inclusive pixel rectangle. */
internal data class Rect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left + 1
    val height: Int get() = bottom - top + 1
    val centerX: Int get() = (left + right) / 2
    val centerY: Int get() = (top + bottom) / 2
}
