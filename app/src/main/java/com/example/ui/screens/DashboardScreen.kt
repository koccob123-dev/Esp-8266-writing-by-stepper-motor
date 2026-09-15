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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SettingsEntity
import com.example.data.model.ConnectionStatus
import com.example.ui.components.LargeEmergencyStopCard
import com.example.ui.components.MachineBedPreview
import com.example.ui.components.StatusChip
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ServoAmber
import com.example.ui.theme.StepperGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun DashboardScreen(
    connectionStatus: ConnectionStatus,
    posX: Float,
    posY: Float,
    isPenDown: Boolean,
    machineState: String,
    settings: SettingsEntity,
    onConnect: (String) -> Unit,
    onTestConnection: (String) -> Unit,
    onDisconnect: () -> Unit,
    onHome: () -> Unit,
    onEmergencyStop: () -> Unit,
    onSetPen: (Boolean) -> Unit,
    onCalibrate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var ipInput by remember(settings.ipAddress) { mutableStateOf(settings.ipAddress) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. ESP8266 Wi-Fi Hotspot Connection Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("connection_card"),
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
                            imageVector = if (connectionStatus is ConnectionStatus.Connected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (connectionStatus is ConnectionStatus.Connected) StepperGreen else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ESP8266 WI-FI LINK",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                    }

                    StatusChip(status = connectionStatus, machineState = machineState)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Wi-Fi Credentials Notice Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkOutline, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ESP8266 Wi-Fi Hotspot",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "SSID: WriteBot  •  Password: 12345678  •  HTTP cleartext allowed",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("ESP8266 IP Address (default 192.168.4.1)") },
                    singleLine = true,
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
                    modifier = Modifier.fillMaxWidth().testTag("ip_address_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (connectionStatus is ConnectionStatus.Connected) {
                        OutlinedButton(
                            onClick = onDisconnect,
                            modifier = Modifier.weight(1f).testTag("disconnect_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmergencyRed))
                        ) {
                            Text("DISCONNECT", color = EmergencyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { onConnect(ipInput) },
                            modifier = Modifier.weight(1f).testTag("connect_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("CONNECT", color = DarkBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { onTestConnection(ipInput) },
                        modifier = Modifier.weight(1f).testTag("test_connection_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TEST CONNECTION", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // 2. Machine Telemetry HUD Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("telemetry_card"),
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
                        text = "MACHINE TELEMETRY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                    Text(
                        text = "STATE: $machineState",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (machineState == "DRAWING") ServoAmber else StepperGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // X Position
                    TelemetryMetricBox(
                        title = "X POSITION",
                        value = String.format(Locale.US, "%.1f", posX),
                        unit = "mm",
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )

                    // Y Position
                    TelemetryMetricBox(
                        title = "Y POSITION",
                        value = String.format(Locale.US, "%.1f", posY),
                        unit = "mm",
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )

                    // Pen Status
                    TelemetryMetricBox(
                        title = "PEN STATUS",
                        value = if (isPenDown) "DOWN" else "UP",
                        unit = if (isPenDown) "${settings.penDownAngle}°" else "${settings.penUpAngle}°",
                        accentColor = if (isPenDown) ServoAmber else StepperGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Machine Commands: HOME, STOP, PEN UP, PEN DOWN, CALIBRATE
                Text(
                    text = "MACHINE COMMANDS",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        icon = Icons.Default.Home,
                        label = "HOME",
                        tint = StepperGreen,
                        onClick = onHome,
                        modifier = Modifier.weight(1f),
                        testTag = "action_home_button"
                    )

                    ActionButton(
                        icon = Icons.Default.Edit,
                        label = "PEN UP",
                        tint = StepperGreen,
                        onClick = { onSetPen(false) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_pen_up_button"
                    )

                    ActionButton(
                        icon = Icons.Default.Edit,
                        label = "PEN DOWN",
                        tint = ServoAmber,
                        onClick = { onSetPen(true) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_pen_down_button"
                    )

                    ActionButton(
                        icon = Icons.Default.Build,
                        label = "CALIBRATE",
                        tint = TextSecondary,
                        onClick = onCalibrate,
                        modifier = Modifier.weight(1f),
                        testTag = "action_calibrate_button"
                    )
                }
            }
        }

        // 3. STOP Button (Highly visible Emergency Stop)
        LargeEmergencyStopCard(
            onStopClicked = onEmergencyStop
        )

        // 4. Live Machine Bed Preview
        Card(
            modifier = Modifier.fillMaxWidth().testTag("dashboard_bed_preview_card"),
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
                        text = "LIVE BED POSITION (${settings.machineWidth.toInt()}x${settings.machineHeight.toInt()} mm)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Text(
                        text = "X: ${posX.toInt()} Y: ${posY.toInt()}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                MachineBedPreview(
                    machineWidthMm = settings.machineWidth,
                    machineHeightMm = settings.machineHeight,
                    toolX = posX,
                    toolY = posY,
                    isPenDown = isPenDown,
                    showTravelMoves = false
                )
            }
        }
    }
}

@Composable
private fun TelemetryMetricBox(
    title: String,
    value: String,
    unit: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkOutline, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = accentColor
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkOutline, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
        }
    }
}
