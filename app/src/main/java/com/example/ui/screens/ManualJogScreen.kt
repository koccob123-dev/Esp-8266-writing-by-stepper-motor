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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.JogDirection
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
fun ManualJogScreen(
    connectionStatus: ConnectionStatus,
    posX: Float,
    posY: Float,
    isPenDown: Boolean,
    machineState: String,
    settings: SettingsEntity,
    onJog: (JogDirection) -> Unit,
    onHome: () -> Unit,
    onSetPen: (Boolean) -> Unit,
    onEmergencyStop: () -> Unit,
    onStepDistanceSelected: (Float) -> Unit,
    onSpeedChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status & Coordinates HUD
        Card(
            modifier = Modifier.fillMaxWidth().testTag("manual_hud_card"),
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
                    Text(
                        text = "MANUAL CONTROL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )

                    StatusChip(status = connectionStatus, machineState = machineState)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(title = "X AXIS", value = String.format(Locale.US, "%.1f mm", posX), modifier = Modifier.weight(1f))
                    MetricBox(title = "Y AXIS", value = String.format(Locale.US, "%.1f mm", posY), modifier = Modifier.weight(1f))
                    MetricBox(
                        title = "PEN",
                        value = if (isPenDown) "DOWN" else "UP",
                        color = if (isPenDown) ServoAmber else StepperGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // CNC Jog Cross Pad (UP, DOWN, LEFT, RIGHT, HOME)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("manual_jog_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "STEPPER JOG",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // UP (Y-)
                LargeJogPadButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    label = "UP",
                    testTag = "jog_up_button",
                    onClick = { onJog(JogDirection.UP) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // LEFT, HOME, RIGHT
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LargeJogPadButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = "LEFT",
                        testTag = "jog_left_button",
                        onClick = { onJog(JogDirection.LEFT) }
                    )

                    // HOME Center Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(2.dp, StepperGreen, CircleShape)
                            .clickable(onClick = onHome)
                            .testTag("jog_center_home_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = StepperGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "HOME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StepperGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    LargeJogPadButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        label = "RIGHT",
                        testTag = "jog_right_button",
                        onClick = { onJog(JogDirection.RIGHT) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // DOWN (Y+)
                LargeJogPadButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    label = "DOWN",
                    testTag = "jog_down_button",
                    onClick = { onJog(JogDirection.DOWN) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Movement Distance: 1 mm, 5 mm, 10 mm
                Text(
                    text = "MOVEMENT DISTANCE",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(1f, 5f, 10f).forEach { dist ->
                        val selected = kotlin.math.abs(settings.jogStepDistance - dist) < 0.1f
                        FilterChip(
                            selected = selected,
                            onClick = { onStepDistanceSelected(dist) },
                            label = {
                                Text(
                                    text = "${dist.toInt()} mm",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
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
                                selected = selected,
                                borderColor = if (selected) NeonCyan else DarkOutline
                            ),
                            modifier = Modifier.testTag("manual_step_${dist.toInt()}mm")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Speed Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SPEED",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "${settings.travelSpeed} %",
                        fontSize = 12.sp,
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = settings.travelSpeed.toFloat(),
                    onValueChange = { onSpeedChanged(it.toInt()) },
                    valueRange = 10f..100f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("manual_speed_slider")
                )
            }
        }

        // Pen Controls: PEN UP & PEN DOWN
        Card(
            modifier = Modifier.fillMaxWidth().testTag("manual_pen_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "PEN SERVO CONTROL",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isPenDown) DarkSurfaceElevated else DarkSurfaceVariant)
                            .border(1.5.dp, if (!isPenDown) StepperGreen else DarkOutline, RoundedCornerShape(12.dp))
                            .clickable { onSetPen(false) }
                            .testTag("manual_pen_up_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = StepperGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PEN UP (${settings.penUpAngle}°)",
                                color = if (!isPenDown) StepperGreen else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPenDown) DarkSurfaceElevated else DarkSurfaceVariant)
                            .border(1.5.dp, if (isPenDown) ServoAmber else DarkOutline, RoundedCornerShape(12.dp))
                            .clickable { onSetPen(true) }
                            .testTag("manual_pen_down_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = ServoAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PEN DOWN (${settings.penDownAngle}°)",
                                color = if (isPenDown) ServoAmber else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // STOP Button (Large & Highly Visible)
        LargeEmergencyStopCard(
            onStopClicked = onEmergencyStop
        )

        // Machine Bed Visualizer
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
                Text(
                    text = "TOOL POSITION (X: ${posX.toInt()} mm, Y: ${posY.toInt()} mm)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

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
private fun LargeJogPadButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.5.dp, DarkOutline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextPrimary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    color: Color = NeonCyan,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkOutline, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color, fontFamily = FontFamily.Monospace)
        }
    }
}
