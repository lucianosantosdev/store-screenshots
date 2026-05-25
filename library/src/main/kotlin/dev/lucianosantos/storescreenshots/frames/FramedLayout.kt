package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lucianosantos.storescreenshots.MockupPosition
import dev.lucianosantos.storescreenshots.ScreenshotStyle

/**
 * Shared layout used by [PhoneFrame], [TabletFrame], and [AppleFrame]. Renders the background
 * (color or composable override), the title/description (Text or composable override), and
 * the device [mockup] composable, arranged according to `style.mockupPosition`.
 */
@Composable
internal fun FramedLayout(
    title: String,
    description: String,
    backgroundColor: Color,
    contentColor: Color,
    style: ScreenshotStyle,
    horizontalPadding: Dp,
    verticalPadding: Dp,
    titleFontSize: TextUnit = 30.sp,
    descriptionFontSize: TextUnit = 16.sp,
    mockup: @Composable ColumnScope.(externalModifier: Modifier) -> Unit,
) {
    val offsetModifier = Modifier.offset(
        x = style.mockupOffset.x,
        y = style.mockupOffset.y,
    )
    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        style.background?.invoke()

        val titleSlot: @Composable () -> Unit = {
            if (title.isNotEmpty()) {
                val override = style.title
                if (override != null) {
                    override(title)
                } else {
                    Text(
                        text = title,
                        color = contentColor,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = style.fontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        val descriptionSlot: @Composable () -> Unit = {
            if (description.isNotEmpty()) {
                val override = style.description
                if (override != null) {
                    override(description)
                } else {
                    Text(
                        text = description,
                        color = contentColor.copy(alpha = 0.85f),
                        fontSize = descriptionFontSize,
                        fontFamily = style.fontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (style.mockupPosition) {
                MockupPosition.Top -> {
                    mockup(offsetModifier)
                    Spacer(Modifier.height(24.dp))
                    titleSlot()
                    if (title.isNotEmpty() && description.isNotEmpty()) Spacer(Modifier.height(12.dp))
                    descriptionSlot()
                }
                MockupPosition.Middle -> {
                    titleSlot()
                    Spacer(Modifier.weight(1f))
                    mockup(offsetModifier)
                    Spacer(Modifier.weight(1f))
                    descriptionSlot()
                }
                MockupPosition.Bottom -> {
                    titleSlot()
                    if (title.isNotEmpty() && description.isNotEmpty()) Spacer(Modifier.height(12.dp))
                    descriptionSlot()
                    Spacer(Modifier.height(24.dp))
                    mockup(offsetModifier)
                }
            }
        }
    }
}
