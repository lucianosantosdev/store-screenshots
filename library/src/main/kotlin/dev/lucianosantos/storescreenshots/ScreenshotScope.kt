package dev.lucianosantos.storescreenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Scope for fully custom screenshot layouts. Provides a [Mockup] composable that renders
 * the device bezel for the current form factor — you control everything else (background,
 * title, description, positioning).
 *
 * ```kotlin
 * @Test fun custom() = screenshot {
 *     Box(Modifier.fillMaxSize().background(Color.Red)) {
 *         Text("My custom title", Modifier.align(Alignment.TopCenter))
 *         Mockup(Modifier.align(Alignment.Center)) { HomeScreen() }
 *     }
 * }
 * ```
 */
class ScreenshotScope internal constructor(
    private val formFactor: FormFactor,
    private val style: ScreenshotStyle,
) {
    /**
     * Renders the device bezel (phone/tablet/watch/iPhone frame) with [content] inside.
     * Place it wherever you want in your custom layout. [rotationX], [rotationY], [rotationZ],
     * and [cameraDistance] tilt the device in 3D exactly like [DeviceMockup].
     */
    @Composable
    fun Mockup(
        modifier: Modifier = Modifier,
        rotationX: Float = 0f,
        rotationY: Float = 0f,
        rotationZ: Float = 0f,
        cameraDistance: Float = DefaultMockupCameraDistance,
        content: @Composable () -> Unit,
    ) {
        val customFrame = style.mockupFrame
        if (customFrame != null) {
            androidx.compose.foundation.layout.Box(
                modifier.mockup3dRotation(rotationX, rotationY, rotationZ, cameraDistance)
            ) { customFrame(content) }
        } else {
            DeviceMockup(
                formFactor = formFactor,
                modifier = modifier,
                showStatusBar = style.showStatusBar,
                statusBarClock = style.statusBarClock,
                rotationX = rotationX,
                rotationY = rotationY,
                rotationZ = rotationZ,
                cameraDistance = cameraDistance,
                content = content,
            )
        }
    }
}
