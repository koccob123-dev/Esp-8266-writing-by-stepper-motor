package com.example.engine

import com.example.data.model.DrawCommand
import com.example.data.model.JobPreflightInfo
import kotlin.math.hypot
import kotlin.math.sqrt

object PathOptimizer {

    /**
     * Ramer-Douglas-Peucker (RDP) path simplification.
     * Removes redundant/jittery points along a stroke within epsilon tolerance (in mm).
     */
    fun simplifyPath(points: List<StrokePoint>, epsilonMm: Float = 0.4f): List<StrokePoint> {
        if (points.size <= 2) return points

        var maxDistance = 0f
        var index = 0
        val start = points.first()
        val end = points.last()

        for (i in 1 until points.size - 1) {
            val dist = perpendicularDistance(points[i], start, end)
            if (dist > maxDistance) {
                maxDistance = dist
                index = i
            }
        }

        return if (maxDistance > epsilonMm) {
            val left = simplifyPath(points.subList(0, index + 1), epsilonMm)
            val right = simplifyPath(points.subList(index, points.size), epsilonMm)
            left.dropLast(1) + right
        } else {
            listOf(start, end)
        }
    }

    private fun perpendicularDistance(pt: StrokePoint, lineStart: StrokePoint, lineEnd: StrokePoint): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y
        val lineLen = hypot(dx, dy)
        if (lineLen == 0f) return hypot(pt.x - lineStart.x, pt.y - lineStart.y)

        val numerator = kotlin.math.abs(dy * pt.x - dx * pt.y + lineEnd.x * lineStart.y - lineEnd.y * lineStart.x)
        return numerator / lineLen
    }

    /**
     * Optimizes stroke order using nearest-neighbor heuristic to reduce pen-up rapid travel.
     */
    fun optimizeStrokeOrder(strokes: List<StrokePath>): List<StrokePath> {
        if (strokes.size <= 1) return strokes

        val remaining = strokes.filter { it.isNotEmpty() }.toMutableList()
        val ordered = mutableListOf<StrokePath>()

        var currentPos = StrokePoint(0f, 0f)

        while (remaining.isNotEmpty()) {
            var bestIndex = -1
            var bestDist = Float.MAX_VALUE
            var reverseBest = false

            for (i in remaining.indices) {
                val stroke = remaining[i]
                val distToStart = hypot(stroke.first().x - currentPos.x, stroke.first().y - currentPos.y)
                val distToEnd = hypot(stroke.last().x - currentPos.x, stroke.last().y - currentPos.y)

                if (distToStart < bestDist) {
                    bestDist = distToStart
                    bestIndex = i
                    reverseBest = false
                }
                if (distToEnd < bestDist) {
                    bestDist = distToEnd
                    bestIndex = i
                    reverseBest = true
                }
            }

            if (bestIndex != -1) {
                val chosen = remaining.removeAt(bestIndex)
                val strokeToAdd = if (reverseBest) chosen.reversed() else chosen
                ordered.add(strokeToAdd)
                currentPos = strokeToAdd.last()
            } else {
                break
            }
        }

        return ordered
    }

    /**
     * Computes pre-flight job stats (distances, time estimate, bounds) for confirmation before writing.
     */
    fun calculatePreflight(
        title: String,
        strokes: List<StrokePath>,
        machineWidthMm: Float,
        machineHeightMm: Float,
        drawSpeedMmS: Float = 30f,
        travelSpeedMmS: Float = 60f,
        penServoDelayS: Float = 0.25f
    ): JobPreflightInfo {
        val commands = StrokeFontEngine.strokesToCommands(strokes)
        var drawDist = 0f
        var travelDist = 0f
        var penLifts = 0
        var prevX = 0f
        var prevY = 0f
        var prevPen = 0

        var minX = Float.MAX_VALUE
        var maxX = 0f
        var minY = Float.MAX_VALUE
        var maxY = 0f

        for (cmd in commands) {
            val dist = hypot(cmd.x - prevX, cmd.y - prevY)
            if (cmd.pen == 1 && prevPen == 1) {
                drawDist += dist
            } else if (dist > 0f) {
                travelDist += dist
            }
            if (cmd.pen == 0 && prevPen == 1) {
                penLifts++
            }

            minX = minOf(minX, cmd.x)
            maxX = maxOf(maxX, cmd.x)
            minY = minOf(minY, cmd.y)
            maxY = maxOf(maxY, cmd.y)

            prevX = cmd.x
            prevY = cmd.y
            prevPen = cmd.pen
        }

        if (minX == Float.MAX_VALUE) minX = 0f
        if (minY == Float.MAX_VALUE) minY = 0f

        val totalDist = drawDist + travelDist
        val safeDrawSpeed = if (drawSpeedMmS <= 0f) 30f else drawSpeedMmS
        val safeTravelSpeed = if (travelSpeedMmS <= 0f) 60f else travelSpeedMmS

        val estimatedSeconds = ((drawDist / safeDrawSpeed) + (travelDist / safeTravelSpeed) + (penLifts * penServoDelayS * 2)).toInt()

        return JobPreflightInfo(
            title = title,
            totalPaths = strokes.size,
            totalPoints = commands.size,
            drawDistanceMm = drawDist,
            travelDistanceMm = travelDist,
            totalDistanceMm = totalDist,
            estimatedDurationSeconds = maxOf(1, estimatedSeconds),
            machineWidthMm = machineWidthMm,
            machineHeightMm = machineHeightMm,
            boundsMinX = minX,
            boundsMaxX = maxX,
            boundsMinY = minY,
            boundsMaxY = maxY,
            commands = commands
        )
    }
}
