package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.ui.JogDirection
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StepperGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun JogPad(
    currentStepMm: Float,
    onStepDistanceSelected: (Float) -> Unit,
    currentSpeed: Int,
    onSpeedChanged: (Int) -> Unit,
    onJog: (JogDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MANUAL JOG CONTROL",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan
                )

                Text(
                    text = "STEP: ${currentStepMm.toInt()} mm",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CNC Cross Jog Layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // UP (Y-)
                JogButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    label = "Y-",
                    testTag = "jog_up",
                    onClick = { onJog(JogDirection.UP) }
                )

                // LEFT, HOME, RIGHT
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JogButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = "X-",
                        testTag = "jog_left",
                        onClick = { onJog(JogDirection.LEFT) }
                    )

                    // HOME center button
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(2.dp, StepperGreen, CircleShape)
                            .clickable { onJog(JogDirection.HOME) }
                            .testTag("jog_home"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home Plotter",
                                tint = StepperGreen,
                                modifier = Modifier.size(24.dp)
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

                    JogButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        label = "X+",
                        testTag = "jog_right",
                        onClick = { onJog(JogDirection.RIGHT) }
                    )
                }

                // DOWN (Y+)
                JogButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    label = "Y+",
                    testTag = "jog_down",
                    onClick = { onJog(JogDirection.DOWN) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step Distance Selectors
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
                    val selected = kotlin.math.abs(currentStepMm - dist) < 0.1f
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
                            selectedLabelColor = Color(0xFF090D12),
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = if (selected) NeonCyan else DarkOutline
                        ),
                        modifier = Modifier.testTag("step_distance_${dist.toInt()}mm")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Speed Control Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "JOG SPEED",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "$currentSpeed %",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            Slider(
                value = currentSpeed.toFloat(),
                onValueChange = { onSpeedChanged(it.toInt()) },
                valueRange = 10f..100f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = DarkSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().testTag("jog_speed_slider")
            )
        }
    }
}

@Composable
private fun JogButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.5.dp, DarkOutline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
