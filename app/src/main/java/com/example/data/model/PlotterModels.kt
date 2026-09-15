package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Single machine toolpath point.
 * [pen]: 0 = UP (rapid travel without drawing), 1 = DOWN (drawing contact).
 */
@JsonClass(generateAdapter = true)
data class DrawCommand(
    @Json(name = "x") val x: Float,
    @Json(name = "y") val y: Float,
    @Json(name = "pen") val pen: Int
)

@JsonClass(generateAdapter = true)
data class DrawBatch(
    @Json(name = "commands") val commands: List<DrawCommand>
)

@JsonClass(generateAdapter = true)
data class MoveRequest(
    @Json(name = "x") val x: Float,
    @Json(name = "y") val y: Float,
    @Json(name = "speed") val speed: Int
)

@JsonClass(generateAdapter = true)
data class PenRequest(
    @Json(name = "state") val state: String // "up" or "down"
)

@JsonClass(generateAdapter = true)
data class MachineStatusResponse(
    @Json(name = "status") val status: String? = "idle",
    @Json(name = "x") val x: Float? = 0f,
    @Json(name = "y") val y: Float? = 0f,
    @Json(name = "pen") val pen: String? = "up",
    @Json(name = "speed") val speed: Int? = 50,
    @Json(name = "message") val message: String? = null
)

sealed class ConnectionStatus {
    object Disconnected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    data class Connected(val ip: String, val latencyMs: Long? = null) : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

/**
 * High-level machine operational state.
 */
enum class MachineState {
    IDLE,
    DRAWING,
    HOMING,
    STOPPED,
    PAUSED,
    ERROR
}

/**
 * Pre-flight statistics displayed in Queue system before execution.
 */
data class JobPreflightInfo(
    val title: String,
    val totalPaths: Int,
    val totalPoints: Int,
    val drawDistanceMm: Float,
    val travelDistanceMm: Float,
    val totalDistanceMm: Float,
    val estimatedDurationSeconds: Int,
    val machineWidthMm: Float,
    val machineHeightMm: Float,
    val boundsMinX: Float,
    val boundsMaxX: Float,
    val boundsMinY: Float,
    val boundsMaxY: Float,
    val commands: List<DrawCommand>
)
