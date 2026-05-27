package dev.lucianosantos.storescreenshots

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Visual styling for the frame banner around a screenshot. Pass to [ScreenshotRule] or
 * [StoreScreenshotsTest] to customize:
 *
 * - [mockupPosition] — where the device sits vertically.
 * - [mockupOffset] — visual nudge applied to the device after positioning. Use to crop the
 *   device into a canvas edge, peek it off-screen, or compensate for non-default padding.
 *   This is a [Modifier.offset]-style visual shift; layout sizes don't change.
 * - [fontFamily] — applied to the default title/description Text composables. Has no effect
 *   when [title] / [description] composables override the default rendering.
 * - [background] — full-canvas composable rendered underneath everything. When set, the
 *   `backgroundColor` from [Screenshot] is ignored.
 * - [title] / [description] — replace the default Text composables. The current text string
 *   (resolved per-locale from the annotation) is passed in.
 *
 * Defaults preserve the look of every form factor in the library before this API existed.
 */
@Immutable
data class ScreenshotStyle(
    val mockupPosition: MockupPosition = MockupPosition.Bottom,
    val mockupOffset: DpOffset = DpOffset(0.dp, 0.dp),
    val mockupRotation: Float = 0f,
    val fontFamily: FontFamily = FontFamily.Default,
    val titleFontFamily: FontFamily = FontFamily.Default,
    val descriptionFontFamily: FontFamily = FontFamily.Default,
    val showStatusBar: Boolean = true,
    val statusBarClock: String = "12:00",
    val background: (@Composable () -> Unit)? = null,
    val title: (@Composable (text: String) -> Unit)? = null,
    val description: (@Composable (text: String) -> Unit)? = null,
)
