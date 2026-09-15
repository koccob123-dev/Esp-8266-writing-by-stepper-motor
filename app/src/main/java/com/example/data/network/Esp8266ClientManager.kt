package com.example.data.network

import com.example.data.model.DrawBatch
import com.example.data.model.MachineStatusResponse
import com.example.data.model.MoveRequest
import com.example.data.model.PenRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class Esp8266ClientManager {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(2, 30, TimeUnit.SECONDS))
        .retryOnConnectionFailure(true)
        .build()

    @Volatile
    private var currentIp: String = "192.168.4.1"

    @Volatile
    private var cachedService: Esp8266ApiService? = null

    private fun getService(ip: String): Esp8266ApiService {
        val sanitizedIp = ip.trim().removePrefix("http://").removePrefix("https://").removeSuffix("/")
        if (cachedService != null && currentIp == sanitizedIp) {
            return cachedService!!
        }

        synchronized(this) {
            if (cachedService != null && currentIp == sanitizedIp) {
                return cachedService!!
            }
            val baseUrl = "http://$sanitizedIp/"
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val service = retrofit.create(Esp8266ApiService::class.java)
            currentIp = sanitizedIp
            cachedService = service
            return service
        }
    }

    suspend fun getStatus(ip: String): Response<MachineStatusResponse> {
        return getService(ip).getStatus()
    }

    suspend fun home(ip: String): Response<ResponseBody> {
        return getService(ip).home()
    }

    suspend fun stop(ip: String): Response<ResponseBody> {
        return getService(ip).stop()
    }

    suspend fun setPen(ip: String, state: String): Response<ResponseBody> {
        return getService(ip).setPen(PenRequest(state))
    }

    suspend fun move(ip: String, deltaX: Float, deltaY: Float, speed: Int): Response<ResponseBody> {
        return getService(ip).move(MoveRequest(x = deltaX, y = deltaY, speed = speed))
    }

    suspend fun sendDrawBatch(ip: String, batch: DrawBatch): Response<ResponseBody> {
        return getService(ip).draw(batch)
    }

    suspend fun sendConfig(ip: String, config: Map<String, Any>): Response<ResponseBody> {
        return getService(ip).sendConfig(config)
    }

    suspend fun calibrate(ip: String): Response<ResponseBody> {
        return getService(ip).calibrate()
    }
}
