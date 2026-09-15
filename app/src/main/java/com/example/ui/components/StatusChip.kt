package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionStatus
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ServoAmber
import com.example.ui.theme.StepperGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StatusChip(
    status: ConnectionStatus,
    machineState: String = "IDLE",
    modifier: Modifier = Modifier
) {
    val (dotColor, label, detail) = when (status) {
        is ConnectionStatus.Connected -> {
            val stateColor = when (machineState.uppercase()) {
                "DRAWING" -> NeonCyan
                "HOMING" -> ServoAmber
                "STOPPED", "ERROR" -> EmergencyRed
                else -> StepperGreen
            }
            Triple(stateColor, machineState.uppercase(), "${status.ip} ${status.latencyMs?.let { "• ${it}ms" } ?: ""}")
        }
        is ConnectionStatus.Connecting -> Triple(ServoAmber, "CONNECTING", "Establishing link...")
        is ConnectionStatus.Disconnected -> Triple(TextMuted, "DISCONNECTED", "Offline")
        is ConnectionStatus.Error -> Triple(EmergencyRed, "ERROR", "Offline")
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkOutline, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(if (status is ConnectionStatus.Connected || status is ConnectionStatus.Connecting) dotColor.copy(alpha = alpha) else dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = dotColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        if (detail.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($detail)",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
