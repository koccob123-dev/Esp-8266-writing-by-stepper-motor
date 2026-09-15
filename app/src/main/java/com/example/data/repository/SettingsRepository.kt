package com.example.data.repository

import com.example.data.db.SettingsDao
import com.example.data.db.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingsDao: SettingsDao) {

    val settingsFlow: Flow<SettingsEntity> = settingsDao.getSettingsFlow().map { entity ->
        entity ?: SettingsEntity()
    }

    suspend fun getSettings(): SettingsEntity {
        return settingsDao.getSettings() ?: SettingsEntity().also {
            settingsDao.insertOrUpdate(it)
        }
    }

    suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.insertOrUpdate(settings.copy(id = 1))
    }
}
