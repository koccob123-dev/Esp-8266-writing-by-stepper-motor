package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SettingsEntity
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StepperGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    currentSettings: SettingsEntity,
    onSaveSettings: (SettingsEntity) -> Unit,
    onSyncToMachine: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var ipAddress by remember(currentSettings) { mutableStateOf(currentSettings.ipAddress) }
    var machineWidth by remember(currentSettings) { mutableStateOf(currentSettings.machineWidth.toString()) }
    var machineHeight by remember(currentSettings) { mutableStateOf(currentSettings.machineHeight.toString()) }
    var stepsPerMmX by remember(currentSettings) { mutableStateOf(currentSettings.stepsPerMmX.toString()) }
    var stepsPerMmY by remember(currentSettings) { mutableStateOf(currentSettings.stepsPerMmY.toString()) }
    var maxSpeed by remember(currentSettings) { mutableStateOf(currentSettings.maxSpeed.toString()) }
    var penUpAngle by remember(currentSettings) { mutableStateOf(currentSettings.penUpAngle.toString()) }
    var penDownAngle by remember(currentSettings) { mutableStateOf(currentSettings.penDownAngle.toString()) }
    var travelSpeed by remember(currentSettings) { mutableStateOf(currentSettings.travelSpeed.toString()) }
    var drawSpeed by remember(currentSettings) { mutableStateOf(currentSettings.drawSpeed.toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settings Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("settings_form_card"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLOTTER CONFIGURATION",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                }

                Text(
                    text = "Hardware parameters are persisted locally in Room database and applied across all drawing, text, and jog calculations.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // ESP8266 IP
                SettingsField(
                    label = "ESP8266 Hotspot IP Address",
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    keyboardType = KeyboardType.Text,
                    helperText = "Default: 192.168.4.1 (ESP8266 Access Point)",
                    testTag = "settings_ip_field"
                )

                // Bed Dimensions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsField(
                        label = "Bed Width (mm)",
                        value = machineWidth,
                        onValueChange = { machineWidth = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "settings_width_field"
                    )

                    SettingsField(
                        label = "Bed Height (mm)",
                        value = machineHeight,
                        onValueChange = { machineHeight = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "settings_height_field"
                    )
                }

                // Steps per mm (28BYJ-48 motors)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsField(
                        label = "Steps / mm X",
                        value = stepsPerMmX,
                        onValueChange = { stepsPerMmX = it },
                        keyboardType = KeyboardType.Number,
                        helperText = "28BYJ-48 stepper",
                        modifier = Modifier.weight(1f),
                        testTag = "settings_steps_x_field"
                    )

                    SettingsField(
                        label = "Steps / mm Y",
                        value = stepsPerMmY,
                        onValueChange = { stepsPerMmY = it },
                        keyboardType = KeyboardType.Number,
                        helperText = "28BYJ-48 stepper",
                        modifier = Modifier.weight(1f),
                        testTag = "settings_steps_y_field"
                    )
                }

                // Servo angles (MG90S)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsField(
                        label = "Pen UP Angle (°)",
                        value = penUpAngle,
                        onValueChange = { penUpAngle = it },
                        keyboardType = KeyboardType.Number,
                        helperText = "Default: 45°",
                        modifier = Modifier.weight(1f),
                        testTag = "settings_pen_up_field"
                    )

                    SettingsField(
                        label = "Pen DOWN Angle (°)",
                        value = penDownAngle,
                        onValueChange = { penDownAngle = it },
                        keyboardType = KeyboardType.Number,
                        helperText = "Default: 90°",
                        modifier = Modifier.weight(1f),
                        testTag = "settings_pen_down_field"
                    )
                }

                // Speeds
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsField(
                        label = "Travel Speed (%)",
                        value = travelSpeed,
                        onValueChange = { travelSpeed = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "settings_travel_speed_field"
                    )

                    SettingsField(
                        label = "Draw Speed (%)",
                        value = drawSpeed,
                        onValueChange = { drawSpeed = it },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                        testTag = "settings_draw_speed_field"
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val def = SettingsEntity()
                    ipAddress = def.ipAddress
                    machineWidth = def.machineWidth.toString()
                    machineHeight = def.machineHeight.toString()
                    stepsPerMmX = def.stepsPerMmX.toString()
                    stepsPerMmY = def.stepsPerMmY.toString()
                    maxSpeed = def.maxSpeed.toString()
                    penUpAngle = def.penUpAngle.toString()
                    penDownAngle = def.penDownAngle.toString()
                    travelSpeed = def.travelSpeed.toString()
                    drawSpeed = def.drawSpeed.toString()
                },
                modifier = Modifier.weight(1f).testTag("restore_defaults_button"),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("DEFAULTS", color = TextPrimary, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    val updated = currentSettings.copy(
                        ipAddress = ipAddress.trim(),
                        machineWidth = machineWidth.toFloatOrNull() ?: currentSettings.machineWidth,
                        machineHeight = machineHeight.toFloatOrNull() ?: currentSettings.machineHeight,
                        stepsPerMmX = stepsPerMmX.toFloatOrNull() ?: currentSettings.stepsPerMmX,
                        stepsPerMmY = stepsPerMmY.toFloatOrNull() ?: currentSettings.stepsPerMmY,
                        maxSpeed = maxSpeed.toIntOrNull() ?: currentSettings.maxSpeed,
                        penUpAngle = penUpAngle.toIntOrNull() ?: currentSettings.penUpAngle,
                        penDownAngle = penDownAngle.toIntOrNull() ?: currentSettings.penDownAngle,
                        travelSpeed = travelSpeed.toIntOrNull() ?: currentSettings.travelSpeed,
                        drawSpeed = drawSpeed.toIntOrNull() ?: currentSettings.drawSpeed
                    )
                    onSaveSettings(updated)
                },
                modifier = Modifier.weight(1.5f).height(48.dp).testTag("save_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = StepperGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE SETTINGS", color = DarkBackground, fontWeight = FontWeight.Black, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            }
        }

        if (onSyncToMachine != null) {
            OutlinedButton(
                onClick = onSyncToMachine,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("sync_to_esp8266_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
            ) {
                Text("UPLOAD CONFIG TO ESP8266 (/api/config)", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    testTag: String
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 11.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
            modifier = Modifier.fillMaxWidth().testTag(testTag)
        )
        if (helperText != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = helperText,
                fontSize = 9.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
