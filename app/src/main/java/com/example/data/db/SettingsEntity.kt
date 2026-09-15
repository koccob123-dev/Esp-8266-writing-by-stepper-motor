package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plotter_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val ipAddress: String = "192.168.4.1",
    val machineWidth: Float = 100f,
    val machineHeight: Float = 100f,
    val stepsPerMmX: Float = 64f,
    val stepsPerMmY: Float = 64f,
    val maxSpeed: Int = 60,
    val penUpAngle: Int = 45,
    val penDownAngle: Int = 90,
    val jogStepDistance: Float = 5f,
    val travelSpeed: Int = 80,
    val drawSpeed: Int = 40
)
