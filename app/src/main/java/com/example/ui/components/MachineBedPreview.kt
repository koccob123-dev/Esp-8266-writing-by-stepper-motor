package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.engine.PlotterCoordinates
import com.example.engine.StrokePath
import com.example.engine.StrokePoint
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RapidTravelYellow
import com.example.ui.theme.ServoAmber
import com.example.ui.theme.StepperGreen
import kotlin.math.roundToInt

@Composable
fun MachineBedPreview(
    machineWidthMm: Float,
    machineHeightMm: Float,
    toolX: Float = 0f,
    toolY: Float = 0f,
    isPenDown: Boolean = false,
    strokes: List<StrokePath> = emptyList(),
    isInteractiveDrawing: Boolean = false,
    onStrokeDrawn: ((List<StrokePoint>) -> Unit)? = null,
    showTravelMoves: Boolean = true,
    modifier: Modifier = Modifier
) {
    val currentPoints = remember { mutableStateListOf<StrokePoint>() }
    var bedSize by remember { mutableStateOf(Size.Zero) }

    val bedAspectRatio = if (machineHeightMm > 0f) machineWidthMm / machineHeightMm else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(bedAspectRatio)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkBackground)
            .border(1.5.dp, DarkOutline, RoundedCornerShape(12.dp))
            .padding(2.dp)
            .testTag("machine_bed_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isInteractiveDrawing && onStrokeDrawn != null) {
                        Modifier.pointerInput(machineWidthMm, machineHeightMm) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPoints.clear()
                                    val pt = PlotterCoordinates.canvasToMachine(
                                        offset,
                                        bedSize,
                                        machineWidthMm,
                                        machineHeightMm
                                    )
                                    currentPoints.add(pt)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val pt = PlotterCoordinates.canvasToMachine(
                                        change.position,
                                        bedSize,
                                        machineWidthMm,
                                        machineHeightMm
                                    )
                                    currentPoints.add(pt)
                                },
                                onDragEnd = {
                                    if (currentPoints.isNotEmpty()) {
                                        onStrokeDrawn(currentPoints.toList())
                                    }
                                    currentPoints.clear()
                                },
                                onDragCancel = {
                                    currentPoints.clear()
                                }
                            )
                        }
                    } else Modifier
                )
        ) {
            bedSize = size
            val w = size.width
            val h = size.height

            // Draw technical XY grid (10mm minor, 50mm major)
            val stepX = w / (machineWidthMm / 10f)
            val stepY = h / (machineHeightMm / 10f)

            var xGrid = 0f
            var mmX = 0
            while (xGrid <= w) {
                val isMajor = (mmX % 50 == 0)
                drawLine(
                    color = if (isMajor) Color(0xFF233245) else Color(0xFF141C26),
                    start = Offset(xGrid, 0f),
                    end = Offset(xGrid, h),
                    strokeWidth = if (isMajor) 1.5f else 0.8f
                )
                xGrid += stepX
                mmX += 10
            }

            var yGrid = 0f
            var mmY = 0
            while (yGrid <= h) {
                val isMajor = (mmY % 50 == 0)
                drawLine(
                    color = if (isMajor) Color(0xFF233245) else Color(0xFF141C26),
                    start = Offset(0f, yGrid),
                    end = Offset(w, yGrid),
                    strokeWidth = if (isMajor) 1.5f else 0.8f
                )
                yGrid += stepY
                mmY += 10
            }

            // Draw outer perimeter border
            drawRect(
                color = Color(0xFF2C3E55),
                size = size,
                style = Stroke(width = 2f)
            )

            // Draw rapid travel moves (between ends of strokes and starts of next)
            if (showTravelMoves && strokes.size > 1) {
                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                var prevEnd: Offset? = null

                for (stroke in strokes) {
                    if (stroke.isEmpty()) continue
                    val firstPt = PlotterCoordinates.machineToCanvas(stroke.first(), size, machineWidthMm, machineHeightMm)
                    if (prevEnd != null) {
                        drawLine(
                            color = RapidTravelYellow.copy(alpha = 0.45f),
                            start = prevEnd,
                            end = firstPt,
                            strokeWidth = 1f,
                            pathEffect = dashedEffect
                        )
                    }
                    prevEnd = PlotterCoordinates.machineToCanvas(stroke.last(), size, machineWidthMm, machineHeightMm)
                }
            }

            // Draw completed toolpath strokes
            for (stroke in strokes) {
                if (stroke.size < 2) continue
                val path = Path()
                val first = PlotterCoordinates.machineToCanvas(stroke.first(), size, machineWidthMm, machineHeightMm)
                path.moveTo(first.x, first.y)

                for (i in 1 until stroke.size) {
                    val pt = PlotterCoordinates.machineToCanvas(stroke[i], size, machineWidthMm, machineHeightMm)
                    path.lineTo(pt.x, pt.y)
                }

                drawPath(
                    path = path,
                    color = NeonCyan,
                    style = Stroke(
                        width = 3.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Draw current in-progress touch stroke
            if (currentPoints.size >= 2) {
                val activePath = Path()
                val first = PlotterCoordinates.machineToCanvas(currentPoints.first(), size, machineWidthMm, machineHeightMm)
                activePath.moveTo(first.x, first.y)

                for (i in 1 until currentPoints.size) {
                    val pt = PlotterCoordinates.machineToCanvas(currentPoints[i], size, machineWidthMm, machineHeightMm)
                    activePath.lineTo(pt.x, pt.y)
                }

                drawPath(
                    path = activePath,
                    color = StepperGreen,
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Draw live pen tool position crosshair
            val toolOffset = PlotterCoordinates.machineToCanvas(
                StrokePoint(toolX, toolY),
                size,
                machineWidthMm,
                machineHeightMm
            )

            // Crosshair lines
            val crosshairSize = 14f
            drawLine(
                color = if (isPenDown) ServoAmber else StepperGreen,
                start = Offset(toolOffset.x - crosshairSize, toolOffset.y),
                end = Offset(toolOffset.x + crosshairSize, toolOffset.y),
                strokeWidth = 2f
            )
            drawLine(
                color = if (isPenDown) ServoAmber else StepperGreen,
                start = Offset(toolOffset.x, toolOffset.y - crosshairSize),
                end = Offset(toolOffset.x, toolOffset.y + crosshairSize),
                strokeWidth = 2f
            )

            // Center targeting reticle circle
            drawCircle(
                color = if (isPenDown) ServoAmber else StepperGreen,
                radius = 7f,
                center = toolOffset,
                style = Stroke(width = 2f)
            )

            if (isPenDown) {
                drawCircle(
                    color = ServoAmber,
                    radius = 3.5f,
                    center = toolOffset
                )
            }
        }
    }
}
