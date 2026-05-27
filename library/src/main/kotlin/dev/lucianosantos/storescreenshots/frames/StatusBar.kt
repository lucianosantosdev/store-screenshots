package dev.lucianosantos.storescreenshots.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun StatusBar(clock: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = clock, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.SignalCellular4Bar, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Icon(Icons.Filled.Wifi, null, tint = Color.White, modifier = Modifier.size(14.dp))
            BatteryIcon()
        }
    }
}

@Composable
private fun BatteryIcon() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 22.dp, height = 11.dp)
                .border(1.dp, Color.White, RoundedCornerShape(3.dp))
                .padding(1.5.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .fillMaxHeight()
                    .background(Color.White, RoundedCornerShape(1.dp))
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 1.dp)
                .width(1.5.dp)
                .height(5.dp)
                .background(Color.White, RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
        )
    }
}
