package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.JobPreflightInfo
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StepperGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun QueuePreflightDialog(
    preflight: JobPreflightInfo,
    isWritingActive: Boolean,
    writingProgress: Float,
    writingStatusText: String,
    isConnected: Boolean = true,
    onStartWriting: () -> Unit,
    onCancelWriting: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isWritingActive) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .testTag("queue_preflight_dialog"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "JOB PRE-FLIGHT CHECK",
                            fontSize = 11.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = preflight.title,
                            fontSize = 16.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isWritingActive) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("dismiss_queue_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricRow(
                        label = "Number of Strokes",
                        value = "${preflight.totalPaths} strokes"
                    )
                    MetricRow(
                        label = "Number of Points",
                        value = "${preflight.totalPoints} points"
                    )
                    MetricRow(
                        label = "Estimated Distance",
                        value = String.format(Locale.US, "%.1f mm (Draw: %.1f mm)", preflight.totalDistanceMm, preflight.drawDistanceMm)
                    )
                    val minutes = preflight.estimatedDurationSeconds / 60
                    val seconds = preflight.estimatedDurationSeconds % 60
                    val timeStr = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
                    MetricRow(
                        label = "Estimated Time",
                        value = timeStr,
                        highlightColor = StepperGreen
                    )
                    MetricRow(
                        label = "Machine Width",
                        value = "${preflight.machineWidthMm.toInt()} mm"
                    )
                    MetricRow(
                        label = "Machine Height",
                        value = "${preflight.machineHeightMm.toInt()} mm"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Active Plotting Status / Progress
                if (isWritingActive) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = writingStatusText,
                            fontSize = 12.sp,
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { writingProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("writing_progress_bar"),
                            color = StepperGreen,
                            trackColor = DarkSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onCancelWriting,
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cancel_writing_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("ABORT PLOTTING", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Start Writing Confirmation Button
                    if (!isConnected) {
                        Text(
                            text = "⚠ Machine is disconnected. Connect to ESP8266 to start.",
                            color = Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = onStartWriting,
                        enabled = isConnected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StepperGreen,
                            disabledContainerColor = DarkSurfaceElevated,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_writing_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isConnected) DarkBackground else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isConnected) "START WRITING" else "DISCONNECTED",
                            color = if (isConnected) DarkBackground else TextMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    highlightColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = highlightColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
