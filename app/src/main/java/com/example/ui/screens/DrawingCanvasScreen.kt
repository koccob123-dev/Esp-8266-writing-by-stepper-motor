package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SettingsEntity
import com.example.engine.StrokePath
import com.example.engine.StrokePoint
import com.example.ui.components.MachineBedPreview
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

@Composable
fun DrawingCanvasScreen(
    strokes: List<StrokePath>,
    settings: SettingsEntity,
    onStrokeDrawn: (List<StrokePoint>) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onSendToMachine: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showTravelPreview by remember { mutableStateOf(true) }

    val totalPoints = strokes.sumOf { it.size }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Tool Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOUCH VECTOR CANVAS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                        Text(
                            text = "Draw vectors • Machine: ${settings.machineWidth.toInt()}x${settings.machineHeight.toInt()} mm",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Stroke stats badge
                    Box(
                        modifier = Modifier
                            .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${strokes.size} strokes (${totalPoints} pts)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StepperGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Interactive Machine Bed Touch Canvas
        MachineBedPreview(
            machineWidthMm = settings.machineWidth,
            machineHeightMm = settings.machineHeight,
            strokes = strokes,
            isInteractiveDrawing = true,
            onStrokeDrawn = onStrokeDrawn,
            showTravelMoves = showTravelPreview,
            modifier = Modifier.fillMaxWidth()
        )

        // Required Action Buttons: UNDO, REDO, CLEAR, PREVIEW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // UNDO
            OutlinedButton(
                onClick = onUndo,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("canvas_undo_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("UNDO", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            // REDO
            OutlinedButton(
                onClick = onRedo,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("canvas_redo_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("REDO", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            // CLEAR
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("canvas_clear_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed)
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("CLEAR", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            // PREVIEW (Rapid travel moves toggle)
            OutlinedButton(
                onClick = { showTravelPreview = !showTravelPreview },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("canvas_preview_button"),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (showTravelPreview) NeonCyan else DarkOutline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (showTravelPreview) NeonCyan else TextMuted
                )
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PREVIEW", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        // SEND TO MACHINE Button
        Button(
            onClick = onSendToMachine,
            colors = ButtonDefaults.buttonColors(
                containerColor = StepperGreen,
                disabledContainerColor = DarkSurfaceElevated,
                disabledContentColor = TextMuted
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("send_canvas_to_machine_button"),
            enabled = strokes.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = if (strokes.isNotEmpty()) DarkBackground else TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SEND TO MACHINE",
                color = if (strokes.isNotEmpty()) DarkBackground else TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
