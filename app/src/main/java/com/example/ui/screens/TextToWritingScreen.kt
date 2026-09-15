package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.engine.VectorFont
import com.example.ui.components.MachineBedPreview
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StepperGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TextToWritingScreen(
    textInput: String,
    selectedFont: VectorFont,
    fontSizeMm: Float,
    letterSpacingMm: Float,
    lineSpacingMm: Float,
    writingSpeed: Int,
    startX: Float,
    startY: Float,
    textStrokes: List<StrokePath>,
    settings: SettingsEntity,
    onTextChanged: (String) -> Unit,
    onFontChanged: (VectorFont) -> Unit,
    onFontSizeChanged: (Float) -> Unit,
    onLetterSpacingChanged: (Float) -> Unit,
    onLineSpacingChanged: (Float) -> Unit,
    onWritingSpeedChanged: (Int) -> Unit,
    onPositionChanged: (Float, Float) -> Unit,
    onClearText: () -> Unit,
    onSendToMachine: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showPreviewExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Large Text Editor Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("text_input_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VECTOR TEXT WRITING",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                    }

                    Text(
                        text = "${textStrokes.size} strokes",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = StepperGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Large Text Editor
                OutlinedTextField(
                    value = textInput,
                    onValueChange = onTextChanged,
                    label = { Text("Enter text to write (e.g. HELLO)") },
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkOutline,
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("plot_text_input_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Font Selection
                Text(
                    text = "STROKE FONT STYLE",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VectorFont.values().forEach { font ->
                        val isSelected = font == selectedFont
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFontChanged(font) },
                            label = {
                                Text(
                                    text = font.displayName.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = DarkBackground,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NeonCyan else DarkOutline
                            ),
                            modifier = Modifier.testTag("font_chip_${font.name}")
                        )
                    }
                }
            }
        }

        // Live Vector Bed Preview
        if (showPreviewExpanded) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("text_preview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREVIEW (${settings.machineWidth.toInt()}x${settings.machineHeight.toInt()} mm)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Text(
                            text = "Pos: (${startX.toInt()}, ${startY.toInt()} mm)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    MachineBedPreview(
                        machineWidthMm = settings.machineWidth,
                        machineHeightMm = settings.machineHeight,
                        strokes = textStrokes,
                        showTravelMoves = true
                    )
                }
            }
        }

        // Controls: Text size, Letter spacing, Line spacing, X position, Y position, Speed
        Card(
            modifier = Modifier.fillMaxWidth().testTag("text_controls_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "WRITING PARAMETERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )

                // Text Size
                ParameterSlider(
                    label = "Text Size",
                    valueStr = String.format(Locale.US, "%.1f mm", fontSizeMm),
                    value = fontSizeMm,
                    onValueChange = onFontSizeChanged,
                    range = 4f..35f,
                    testTag = "slider_font_size"
                )

                // Letter Spacing
                ParameterSlider(
                    label = "Letter Spacing",
                    valueStr = String.format(Locale.US, "%.1f mm", letterSpacingMm),
                    value = letterSpacingMm,
                    onValueChange = onLetterSpacingChanged,
                    range = 0.5f..8f,
                    testTag = "slider_letter_spacing"
                )

                // Line Spacing
                ParameterSlider(
                    label = "Line Spacing",
                    valueStr = String.format(Locale.US, "%.1f mm", lineSpacingMm),
                    value = lineSpacingMm,
                    onValueChange = onLineSpacingChanged,
                    range = 1f..15f,
                    testTag = "slider_line_spacing"
                )

                // X Position
                ParameterSlider(
                    label = "X Position",
                    valueStr = String.format(Locale.US, "%.0f mm", startX),
                    value = startX,
                    onValueChange = { onPositionChanged(it, startY) },
                    range = 0f..(settings.machineWidth - 10f).coerceAtLeast(10f),
                    testTag = "slider_x_pos"
                )

                // Y Position
                ParameterSlider(
                    label = "Y Position",
                    valueStr = String.format(Locale.US, "%.0f mm", startY),
                    value = startY,
                    onValueChange = { onPositionChanged(startX, it) },
                    range = 0f..(settings.machineHeight - 10f).coerceAtLeast(10f),
                    testTag = "slider_y_pos"
                )

                // Speed
                ParameterSlider(
                    label = "Speed",
                    valueStr = "$writingSpeed %",
                    value = writingSpeed.toFloat(),
                    onValueChange = { onWritingSpeedChanged(it.toInt()) },
                    range = 10f..100f,
                    testTag = "slider_writing_speed"
                )
            }
        }

        // Action Buttons: PREVIEW, CLEAR, SEND TO MACHINE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // PREVIEW Button
            OutlinedButton(
                onClick = { showPreviewExpanded = !showPreviewExpanded },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("preview_text_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showPreviewExpanded) "HIDE PREVIEW" else "PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // CLEAR Button
            OutlinedButton(
                onClick = onClearText,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("clear_text_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "CLEAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // SEND TO MACHINE Button
        Button(
            onClick = onSendToMachine,
            enabled = textStrokes.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = StepperGreen,
                disabledContainerColor = DarkSurfaceElevated,
                disabledContentColor = TextMuted
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("send_to_machine_text_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = if (textStrokes.isNotEmpty()) DarkBackground else TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SEND TO MACHINE",
                color = if (textStrokes.isNotEmpty()) DarkBackground else TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun ParameterSlider(
    label: String,
    valueStr: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    testTag: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = valueStr,
                fontSize = 11.sp,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = DarkSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth().testTag(testTag)
        )
    }
}
