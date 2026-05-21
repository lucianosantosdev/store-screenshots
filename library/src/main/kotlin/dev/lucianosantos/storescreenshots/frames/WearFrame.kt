package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Round watch frame. Play Store wants a 384x384 PNG; this fills the canvas with a circular
 * black background and renders [content] inside.
 *
 * Title/description are intentionally omitted — Wear Play Store screenshots are square and
 * do not have room for the marketing banner that the phone/tablet frames use.
 */
@Composable
fun WearFrame(
    backgroundColor: Color = Color.Black,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .size(227.dp)
                .clip(CircleShape)
                .background(backgroundColor)
        ) {
            content()
        }
    }
}
