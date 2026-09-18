package dev.lucianosantos.storescreenshots

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * What a tilted device mockup is made of, and where the light falls on it.
 *
 * Only [ScreenshotStyle.mockupRotationX] or [ScreenshotStyle.mockupRotationY] brings any of this
 * into play: with the device face-on there is no side to see and nothing here changes a pixel. Tilt
 * it and the body is drawn as a solid — see the `3D perspective` section of the README — at which
 * point its depth, the shade of its rails and the direction of the light become visible.
 *
 * Every field is an *override*, and the defaults leave each form factor drawing itself: a null
 * colour means "the shade this device's own metrics give it", and a null [thicknessRatio] means
 * "however deep this device actually is". So `MockupMaterial()` renders an iPhone at an iPhone's
 * proportions and an Android tablet at a tablet's, and you only reach for this to deliberately
 * depart from that — a lighter rail against a dark banner, say, or a light thrown from the other
 * side to match a background.
 *
 * ```kotlin
 * style = ScreenshotStyle(
 *     mockupRotationY = -26f,
 *     mockupMaterial = MockupMaterial(lightAngle = 40f, railColor = Color(0xFFB0B4BA)),
 * )
 * ```
 */
@Immutable
data class MockupMaterial(
    /**
     * How deep the body is, as a fraction of its narrow side. Null uses the device's own measured
     * thickness — about `0.11` for a phone, a quarter of that for a tablet. Raising it exaggerates
     * the rail a tilt exposes; `0f` renders a device with no thickness at all.
     */
    val thicknessRatio: Float? = null,

    /**
     * Where the light comes from, in degrees around the canvas, measured clockwise from straight
     * up. The default throws it from the upper left, which is where most product photography puts
     * it.
     */
    val lightAngle: Float = DefaultLightAngle,

    /**
     * How high the light sits, in degrees: `0` is level with the screen, grazing the rails, and
     * `90` is straight in front of the device, flattening them out.
     */
    val lightElevation: Float = DefaultLightElevation,

    /** How much of a surface's colour survives where no light reaches it at all. */
    val ambient: Float = DefaultAmbient,

    /** The face of the side rails. Null keeps the enclosure's own. */
    val railColor: Color? = null,

    /** The bright machined line along a rail's outer edge. Null keeps the enclosure's own. */
    val edgeHighlightColor: Color? = null,

    /** The shade the body falls away to at its back. Null keeps the enclosure's own. */
    val backEdgeColor: Color? = null,

    /**
     * Strength of the sheen the renderer lays on the cover glass as the device turns. This is its
     * own layer, on top of and independent of [ScreenshotStyle.screenGlass] — a [GlassEffect]'s
     * reflex is drawn on the screen and warped along with it, and nothing here overrides it. `0f`
     * leaves the glass to that effect alone.
     */
    val glassSheen: Float = DefaultGlassSheen,
)

/** Light thrown from the upper left, a little above the device. */
internal const val DefaultLightAngle = -32f
internal const val DefaultLightElevation = 30f
internal const val DefaultAmbient = 0.42f
internal const val DefaultGlassSheen = 0.35f
