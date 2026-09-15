package com.example.data.network

import com.example.data.model.DrawBatch
import com.example.data.model.MachineStatusResponse
import com.example.data.model.MoveRequest
import com.example.data.model.PenRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Esp8266ApiService {
    @GET("/api/status")
    suspend fun getStatus(): Response<MachineStatusResponse>

    @POST("/api/home")
    suspend fun home(): Response<ResponseBody>

    @POST("/api/stop")
    suspend fun stop(): Response<ResponseBody>

    @POST("/api/pen")
    suspend fun setPen(@Body request: PenRequest): Response<ResponseBody>

    @POST("/api/move")
    suspend fun move(@Body request: MoveRequest): Response<ResponseBody>

    @POST("/api/draw")
    suspend fun draw(@Body request: DrawBatch): Response<ResponseBody>

    @POST("/api/config")
    suspend fun sendConfig(@Body config: Map<String, Any>): Response<ResponseBody>

    @POST("/api/calibrate")
    suspend fun calibrate(): Response<ResponseBody>
}
