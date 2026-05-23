package dev.lucianosantos.storescreenshots.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PageBackground = Brush.verticalGradient(
    colors = listOf(Color(0xFF7C3AED), Color(0xFF3730A3))
)
private val CardColor = Color(0xFF1F1147)
private val AccentColor = Color(0xFFFBBF24)

@Composable
fun CounterScreen(count: Int) {
    Box(
        modifier = Modifier.fillMaxSize().background(PageBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppBadge()
            Spacer(Modifier.height(40.dp))
            CounterCard(count = count)
            Spacer(Modifier.height(24.dp))
            ActionRow()
        }
    }
}

@Composable
private fun AppBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.FlashOn,
            contentDescription = null,
            tint = AccentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Counter Demo",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CounterCard(count: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(CardColor)
            .padding(horizontal = 40.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 96.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "TAPS",
            color = AccentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )
    }
}

@Composable
private fun ActionRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircleButton(filled = false) {
            Icon(Icons.Filled.Remove, contentDescription = null, tint = Color.White)
        }
        CircleButton(filled = true) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = CardColor)
        }
    }
}

@Composable
private fun CircleButton(filled: Boolean, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(if (filled) AccentColor else Color.White.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
