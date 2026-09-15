package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

object PlotterCoordinates {

    /**
     * Converts raw touch offset coordinates on a canvas into machine coordinates in millimeters.
     */
    fun canvasToMachine(
        touchOffset: Offset,
        canvasSize: Size,
        machineWidthMm: Float,
        machineHeightMm: Float
    ): StrokePoint {
        if (canvasSize.width <= 0f || canvasSize.height <= 0f) {
            return StrokePoint(0f, 0f)
        }
        val xMm = (touchOffset.x / canvasSize.width * machineWidthMm).coerceIn(0f, machineWidthMm)
        val yMm = (touchOffset.y / canvasSize.height * machineHeightMm).coerceIn(0f, machineHeightMm)
        return StrokePoint(xMm, yMm)
    }

    /**
     * Converts machine coordinates in millimeters back to canvas offset for rendering preview.
     */
    fun machineToCanvas(
        point: StrokePoint,
        canvasSize: Size,
        machineWidthMm: Float,
        machineHeightMm: Float
    ): Offset {
        if (machineWidthMm <= 0f || machineHeightMm <= 0f) return Offset.Zero
        val px = (point.x / machineWidthMm) * canvasSize.width
        val py = (point.y / machineHeightMm) * canvasSize.height
        return Offset(px, py)
    }
}
