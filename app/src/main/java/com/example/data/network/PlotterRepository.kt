package com.example.data.network

import com.example.data.model.ConnectionStatus
import com.example.data.model.DrawBatch
import com.example.data.model.DrawCommand
import com.example.data.model.MachineStatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

class PlotterRepository(
    private val clientManager: Esp8266ClientManager = Esp8266ClientManager()
) {
    suspend fun pingAndGetStatus(ip: String): Result<Pair<MachineStatusResponse, Long>> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val response = clientManager.getStatus(ip)
            val latency = System.currentTimeMillis() - start
            if (response.isSuccessful && response.body() != null) {
                Result.success(Pair(response.body()!!, latency))
            } else {
                Result.failure(Exception("HTTP error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Connection timed out. Ensure phone is connected to ESP8266 Wi-Fi hotspot."))
        } catch (e: ConnectException) {
            Result.failure(Exception("Could not connect to $ip. Check hotspot connection."))
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.localizedMessage ?: "Unknown I/O error"}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun home(ip: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resp = clientManager.home(ip)
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Home failed: HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stop(ip: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resp = clientManager.stop(ip)
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Stop failed: HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPen(ip: String, isDown: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val state = if (isDown) "down" else "up"
            val resp = clientManager.setPen(ip, state)
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Pen $state failed: HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun move(ip: String, deltaX: Float, deltaY: Float, speed: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resp = clientManager.move(ip, deltaX, deltaY, speed)
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Move failed: HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun calibrate(ip: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resp = clientManager.calibrate(ip)
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Calibrate failed: HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendConfig(ip: String, config: Map<String, Any>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val resp = clientManager.sendConfig(ip, config)
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Config upload failed: HTTP ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends drawing commands in safe chunks (e.g. 50 commands per HTTP POST)
     * so that the ESP8266's limited memory buffer does not overflow.
     * Reports progress via [onProgress] (0.0f .. 1.0f).
     */
    suspend fun sendDrawingInBatches(
        ip: String,
        commands: List<DrawCommand>,
        chunkSize: Int = 40,
        onProgress: (sentCount: Int, totalCount: Int) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (commands.isEmpty()) return@withContext Result.success(Unit)

        val total = commands.size
        val chunks = commands.chunked(chunkSize)
        var sentSoFar = 0

        for ((index, chunk) in chunks.withIndex()) {
            try {
                val resp = clientManager.sendDrawBatch(ip, DrawBatch(chunk))
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed sending batch ${index + 1}/${chunks.size}: HTTP ${resp.code()}"))
                }
                sentSoFar += chunk.size
                onProgress(sentSoFar, total)
                // Short pacing delay between batches for ESP8266 Wi-Fi stack
                if (index < chunks.size - 1) {
                    delay(30)
                }
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Transmission error on batch ${index + 1}: ${e.localizedMessage}"))
            }
        }
        Result.success(Unit)
    }
}
