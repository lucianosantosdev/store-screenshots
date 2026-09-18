package dev.lucianosantos.storescreenshots.frames

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * The 3D model a solid mockup is built on, held to what it claims.
 *
 * Plain JVM — no Robolectric, no renderer — like [dev.lucianosantos.storescreenshots.FormFactorSizeTest].
 * What it cannot check is that the model matches Compose's own `graphicsLayer`; that is measured by
 * `CameraModelProbe` and gated by [SolidFrontFaceMatchesFlatTiltTest]. What it does check is that
 * the model is self-consistent, that its sign conventions are the documented ones, and that the one
 * property the renderer's painter order depends on actually holds.
 *
 * Floats are compared with a delta throughout: `sin`/`cos` route to `Math`, not `StrictMath`, so a
 * last-bit difference across platforms is allowed for. Nothing here depends on one.
 */
class DeviceProjectionTest {

    private val width = 411f * 3f
    private val height = 822f * 3f
    private val thickness = width * 0.11f
    private val pivot = Offset(600f, 900f)
    private val density = 3f

    private fun tilt(x: Float = 0f, y: Float = 0f, z: Float = 0f, camera: Float = 12f) =
        MockupTilt(rotationX = x, rotationY = y, rotationZ = z, cameraDistance = camera)

    private val samples = sampleRoundRectRing(width, height, 42f * 3f, cornerSamplesFor(42f * 3f))

    private fun ring(t: MockupTilt, thickness: Float = this.thickness): ProjectedRing? =
        projectRing(
            samples = samples,
            thickness = thickness,
            tilt = t,
            cameraPx = t.cameraPx(density),
            pivot = pivot,
        )

    // ---- the camera model, restated ------------------------------------------------------------

    @Test
    fun `camera distance is the value times density times 72`() {
        assertEquals(12f * 3f * 72f, tilt().cameraPx(3f), 1e-3f)
        assertEquals(2592f, tilt().cameraPx(3f), 1e-3f)
    }

    // ---- identity -------------------------------------------------------------------------------

    @Test
    fun `no tilt projects the front face onto its own footprint`() {
        val quad = requireNotNull(projectFrontQuad(width, height, tilt(), tilt().cameraPx(density), pivot))
        val expected = floatArrayOf(
            pivot.x - width / 2f, pivot.y - height / 2f,
            pivot.x + width / 2f, pivot.y - height / 2f,
            pivot.x + width / 2f, pivot.y + height / 2f,
            pivot.x - width / 2f, pivot.y + height / 2f,
        )
        expected.indices.forEach { assertEquals(expected[it], quad[it], 1e-3f) }
    }

    @Test
    fun `no tilt leaves every side wall edge-on, so none is drawn`() {
        assertEquals(emptyList<Int>(), ring(tilt())!!.visibleSegments())
        assertEquals(emptySet<String>(), visibleRails(tilt()))
    }

    @Test
    fun `an in-plane spin alone still shows no thickness`() {
        assertTrue(tilt(z = -6f).isFlat)
        assertEquals(emptyList<Int>(), ring(tilt(z = -6f))!!.visibleSegments())
    }

    // ---- sign conventions -----------------------------------------------------------------------

    /**
     * A positive `rotationY` brings the device's left edge toward the viewer, which puts the camera
     * off to the device's left — so it is the *left* wall that comes into view, and the right wall
     * that turns away. Note this is the opposite of what the old `mockupRotationY = -26f` comment in
     * the README claimed; the probe settled it.
     */
    @Test
    fun `turning the left edge forward reveals the left rail`() {
        assertEquals(setOf("left"), visibleRails(tilt(y = 26f)))
        assertEquals(setOf("right"), visibleRails(tilt(y = -26f)))
    }

    /**
     * A positive `rotationX` tips the top of the device away, which puts the camera below it — so
     * the *bottom* wall comes into view. It takes a steeper angle than the side rails do; see
     * [a rail appears only once the tilt passes the angle that edge is already viewed from].
     */
    @Test
    fun `tipping the top back reveals the bottom rail`() {
        assertEquals(setOf("bottom"), visibleRails(tilt(x = 50f)))
        assertEquals(setOf("top"), visibleRails(tilt(x = -50f)))
    }

    /**
     * The marketing angle the example ships: a firm Y turn, a slight X tip, a slight in-plane spin.
     * The Y turn is well past the threshold so the right rail shows; the 8 degrees of X is nowhere
     * near it, so no horizontal rail does — which is what the rendered mockup should look like.
     */
    @Test
    fun `the marketing angle reveals the right rail and no horizontal one`() {
        assertEquals(setOf("right"), visibleRails(tilt(x = 8f, y = -26f, z = -6f)))
    }

    /** Steepen both axes and the corresponding pair of rails, and the corner joining them, appear. */
    @Test
    fun `a steep combined tilt reveals both of its rails`() {
        assertEquals(setOf("right", "bottom"), visibleRails(tilt(x = 40f, y = -40f, z = -6f)))
    }

    /**
     * Which rail a tilt reveals is not simply "the one it turns toward" — perspective gets a vote.
     * The camera already looks at the device's bottom edge from `atan(halfHeight / cameraDistance)`
     * above it, so the body has to tip past *that* before its bottom wall turns into view at all. A
     * phone is twice as tall as it is wide, so its side rails appear from about 13 degrees while its
     * top and bottom rails need about 25 — which is why an 8-degree X tip shows no horizontal rail
     * and a 26-degree Y turn shows a broad one.
     *
     * The flat `graphicsLayer` path projects through the same camera, so this is not a choice the
     * solid renderer makes; it is what the perspective already was.
     */
    @Test
    fun `a rail appears only once the tilt passes the angle that edge is already viewed from`() {
        val camera = tilt().cameraPx(density)
        val sideThreshold = Math.toDegrees(kotlin.math.atan((width / 2f / camera).toDouble())).toFloat()
        val endThreshold = Math.toDegrees(kotlin.math.atan((height / 2f / camera).toDouble())).toFloat()
        assertEquals(13.4f, sideThreshold, 0.2f)
        assertEquals(25.4f, endThreshold, 0.2f)

        fun sideVisible(angle: Float) = "left" in visibleRails(tilt(y = angle))
        fun endVisible(angle: Float) = "bottom" in visibleRails(tilt(x = angle))

        assertFalse(sideVisible(sideThreshold - 3f))
        assertTrue(sideVisible(sideThreshold + 3f))
        assertFalse(endVisible(endThreshold - 3f))
        assertTrue(endVisible(endThreshold + 3f))
    }

    /** The unrotated outward normals are still the ones a rounded rectangle should have. */
    @Test
    fun `the outline carries the four edge normals and sweeps every corner`() {
        val samples = sampleRoundRectRing(width, height, 42f * 3f, 8)
        listOf(0f to -1f, 1f to 0f, 0f to 1f, -1f to 0f).forEach { (nx, ny) ->
            assertTrue(
                "no edge facing ($nx, $ny)",
                samples.any { abs(it.nx - nx) < 1e-3f && abs(it.ny - ny) < 1e-3f },
            )
        }
        // Four arcs of 8 steps (9 samples each, endpoints included) plus one edge end per corner.
        assertEquals(4 * 9 + 4, samples.size)
    }

    // ---- the property the painter order rests on ------------------------------------------------

    /**
     * The renderer draws the rails first and the live screen last, and never sorts the two against
     * each other. That is only safe because a visible side wall of a *convex* body whose front face
     * still points at the camera can never project inside that face. This walks the supported range
     * and checks it numerically, so a change to the culling or the sampling that broke it would
     * fail here rather than as a rail drawn across someone's UI.
     *
     * The face to test against is the projected front *ring* — the rounded outline the bezel clips
     * itself to — not the sharp-cornered quad the screen is warped onto. Near a rounded corner the
     * ring is inset from the quad, so a rail that correctly sits outside the device can still be
     * inside the quad, and testing the quad would call that a failure.
     */
    @Test
    fun `no visible rail ever projects inside the front face`() {
        var checked = 0
        for (rx in -60..60 step 10) for (ry in -60..60 step 10) {
            val t = tilt(x = rx.toFloat(), y = ry.toFloat())
            val r = ring(t) ?: continue
            val face = r.front.toList()
            r.visibleSegments().forEach { i ->
                val j = (i + 1) % r.front.size
                listOf(r.back[j], r.back[i]).forEach { p ->
                    assertTrue(
                        "at rotX=$rx rotY=$ry a visible rail vertex $p landed inside the device face",
                        !strictlyInside(face, p),
                    )
                    checked++
                }
            }
        }
        assertTrue("the sweep never found a visible rail to check", checked > 1000)
    }

    // ---- the homography reproduces the full projection ------------------------------------------

    @Test
    fun `the four-corner homography reproduces interior points exactly`() {
        val t = tilt(x = 8f, y = -26f, z = -6f)
        val camera = t.cameraPx(density)
        val quad = projectFrontQuad(width, height, t, camera, pivot)!!
        val src = floatArrayOf(0f, 0f, width, 0f, width, height, 0f, height)
        val h = requireNotNull(homography(src, quad))

        // Points spread across the face, in the same native coordinates the renderer warps from.
        listOf(0.5f to 0.5f, 0.1f to 0.9f, 0.73f to 0.21f, 0.99f to 0.5f).forEach { (u, v) ->
            val native = applyHomography(h, width * u, height * v)!!
            val direct = project(
                t.rotate(Vec3(width * (u - 0.5f), height * (v - 0.5f), 0f)), camera, pivot,
            )!!
            assertEquals(direct.x, native.x, 1e-2f)
            assertEquals(direct.y, native.y, 1e-2f)
        }
    }

    @Test
    fun `the homography of an untilted face is the identity translation`() {
        val src = floatArrayOf(0f, 0f, 10f, 0f, 10f, 20f, 0f, 20f)
        val dst = floatArrayOf(5f, 7f, 15f, 7f, 15f, 27f, 5f, 27f)
        val h = homography(src, dst)!!
        val p = applyHomography(h, 5f, 10f)!!
        assertEquals(10f, p.x, 1e-3f)
        assertEquals(17f, p.y, 1e-3f)
    }

    // ---- physical sanity ------------------------------------------------------------------------

    /** Turn the device further and more of its side shows. Cheap, and it catches a flipped sign. */
    @Test
    fun `the rail grows as the tilt steepens`() {
        val areas = listOf(20f, 30f, 40f, 50f).map { angle ->
            val t = tilt(y = angle)
            val r = ring(t)!!
            r.visibleSegments().sumOf { i ->
                val j = (i + 1) % r.front.size
                abs(signedArea(r.front[i], r.front[j], r.back[j], r.back[i])).toDouble()
            }
        }
        areas.zipWithNext { smaller, larger ->
            assertTrue("visible rail area did not grow: $areas", larger > smaller)
        }
    }

    /** The back of the body is always further from the camera than its front, at every tilt. */
    @Test
    fun `the back of the body stays behind its front`() {
        val r = ring(tilt(x = 8f, y = -26f, z = -6f))!!
        r.front.indices.forEach { assertTrue(r.backDepth[it] < r.frontDepth[it]) }
    }

    // ---- guardrails -----------------------------------------------------------------------------

    @Test
    fun `extreme angles are clamped rather than projected`() {
        val clamped = tilt(x = 120f, y = -200f).clamped()
        assertEquals(MaxTiltDegrees, clamped.rotationX, 1e-3f)
        assertEquals(-MaxTiltDegrees, clamped.rotationY, 1e-3f)
        assertFalse(tilt(y = 89.9f).frontFaceVisible())
        assertTrue(tilt(y = 45f).frontFaceVisible())
    }

    @Test
    fun `degenerate input returns null instead of NaN`() {
        assertNull(projectFrontQuad(0f, height, tilt(y = 26f), 2592f, pivot))
        assertNull(projectFrontQuad(width, height, tilt(y = 89.9f), 2592f, pivot))
        // A point that has come up through the near plane cannot be projected.
        assertNull(project(Vec3(0f, 0f, 900f), 1000f, pivot))
        assertNull(homography(FloatArray(8), FloatArray(8)))
    }

    @Test
    fun `a body with no thickness has no visible rail`() {
        assertEquals(emptyList<Int>(), ring(tilt(x = 8f, y = -26f), thickness = 0f)!!.visibleSegments())
    }

    @Test
    fun `the same input projects to the same pixels twice`() {
        val t = tilt(x = 8f, y = -26f, z = -6f)
        val a = ring(t)!!
        val b = ring(t)!!
        a.front.indices.forEach {
            assertEquals(a.front[it], b.front[it])
            assertEquals(a.back[it], b.back[it])
        }
    }

    @Test
    fun `corner sampling depends only on the radius`() {
        assertEquals(cornerSamplesFor(126f), cornerSamplesFor(126f))
        assertTrue(cornerSamplesFor(1f) >= 8)
        assertTrue(cornerSamplesFor(100_000f) <= 64)
    }

    // ---- the bodies the frames extrude ----------------------------------------------------------

    /**
     * Each frame's solid carries exactly the hardware its flat bezel draws — no more, and no less.
     * The iPad's power button is the one that lies along the *top* rail rather than a side, and it
     * is the reason [RailEdge] has four values instead of a `isLeft` flag. No golden can show it: an
     * iPad fills its canvas, and the tilt it would take to turn its top rail into view pushes the
     * device off the edge. So it is pinned here instead.
     */
    @Test
    fun `each device extrudes the buttons its bezel draws`() {
        val phone = androidPhoneBody(411.dp, 822.dp)
        assertEquals(
            "the phone frame draws two buttons on the left and one on the right",
            listOf(RailEdge.Left, RailEdge.Left, RailEdge.Right),
            phone.buttons.map { it.edge },
        )

        val iPhone = iPhoneBody(IPhone17ProMaxMetrics, 470.dp, 986.dp)
        assertEquals(
            "an iPhone has the Action and two volume buttons on the left, and the side button opposite",
            listOf(RailEdge.Left, RailEdge.Left, RailEdge.Left, RailEdge.Right),
            iPhone.buttons.map { it.edge },
        )

        val iPad = iPadBody(1138.4f.dp, 1484.dp)
        assertEquals(
            "an iPad has two volume buttons on its right edge and the power button along its top",
            listOf(RailEdge.Right, RailEdge.Right, RailEdge.Top),
            iPad.buttons.map { it.edge },
        )

        assertTrue(
            "the tablet frame draws no buttons, so its solid must not invent any",
            androidTabletBody(820.dp, 1300.dp).buttons.isEmpty(),
        )
    }

    /**
     * A tablet is proportionally thinner than a phone, but only about half as thick relative to its
     * own width — not a quarter.
     *
     * Worth pinning because the figure has an easy way of going wrong: a device's thickness ratio is
     * against its *width*, the short side in portrait, and a tablet's long side is half as long
     * again. Dividing by that instead halves the ratio and leaves a tilted tablet with a rail too
     * thin to see, which is what this originally shipped with. The bound below is loose enough to
     * allow a re-measure and tight enough to catch that slip.
     */
    @Test
    fun `thickness comes from each device's own proportions`() {
        val phone = androidPhoneBody(411.dp, 822.dp)
        val tablet = androidTabletBody(820.dp, 1300.dp)
        val iPad = iPadBody(1138.4f.dp, 1484.dp)
        assertEquals(411f * 0.110f, phone.thickness.value, 0.1f)
        assertEquals(820f * 0.048f, tablet.thickness.value, 0.1f)

        fun ratio(body: DeviceBody) = body.thickness.value / minOf(body.width.value, body.height.value)
        assertTrue("a phone should be about a ninth as deep as it is wide", ratio(phone) in 0.09f..0.13f)
        listOf("Android tablet" to tablet, "iPad" to iPad).forEach { (name, slab) ->
            assertTrue(
                "$name is ${ratio(slab)} of its width deep, which is not a tablet's proportions",
                ratio(slab) in 0.02f..0.07f,
            )
            assertTrue(
                "$name should be thinner than a phone, relative to its own width",
                ratio(slab) < ratio(phone),
            )
        }
    }

    // ---- helpers --------------------------------------------------------------------------------

    /**
     * Which of the four straight rails a tilt turns toward the viewer.
     *
     * Named by geometry rather than by normal direction: a rounded corner sweeps its normal through
     * a full quarter turn, so "some visible segment faces up" is true of almost any tilt once a
     * corner is in view. The straight edges are the ones a reader means by "the top rail".
     */
    private fun visibleRails(t: MockupTilt): Set<String> {
        val r = ring(t)!!
        val visible = r.visibleSegments().toSet()
        return straightEdges().filterValues { it in visible }.keys
    }

    /** Segment index of each straight edge, found by both its endpoints lying on that edge's line. */
    private fun straightEdges(): Map<String, Int> {
        val hw = width / 2f
        val hh = height / 2f
        val on = { a: Float, b: Float -> abs(a - b) < 1e-2f }
        val found = mutableMapOf<String, Int>()
        samples.indices.forEach { i ->
            val a = samples[i]
            val b = samples[(i + 1) % samples.size]
            when {
                on(a.y, -hh) && on(b.y, -hh) && abs(a.x - b.x) > 1f -> found["top"] = i
                on(a.x, hw) && on(b.x, hw) && abs(a.y - b.y) > 1f -> found["right"] = i
                on(a.y, hh) && on(b.y, hh) && abs(a.x - b.x) > 1f -> found["bottom"] = i
                on(a.x, -hw) && on(b.x, -hw) && abs(a.y - b.y) > 1f -> found["left"] = i
            }
        }
        assertEquals("expected to find all four straight edges", 4, found.size)
        return found
    }

    /**
     * True when [p] sits at least half a pixel inside every edge of a convex [polygon]. The slack
     * runs inward on purpose: a rail that only just came into view shares its inner edge with the
     * front face, and a vertex sitting exactly on that shared edge is touching, not overlapping.
     */
    private fun strictlyInside(polygon: List<Offset>, p: Offset): Boolean {
        // The ring is walked clockwise in screen space, so an interior point sits on the same side
        // of every edge; which side that is comes from the polygon's own winding.
        var area = 0f
        polygon.indices.forEach { i ->
            val a = polygon[i]
            val b = polygon[(i + 1) % polygon.size]
            area += a.x * b.y - b.x * a.y
        }
        val winding = if (area < 0f) -1f else 1f
        polygon.indices.forEach { i ->
            val a = polygon[i]
            val b = polygon[(i + 1) % polygon.size]
            val edge = kotlin.math.hypot(b.x - a.x, b.y - a.y)
            if (edge < 1e-3f) return@forEach
            val cross = (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x)
            if (cross / edge * winding <= 0.5f) return false
        }
        return true
    }
}
