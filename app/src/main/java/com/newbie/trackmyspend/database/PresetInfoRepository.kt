package com.newbie.trackmyspend.database

import com.newbie.trackmyspend.model.PresetCompleteInfo
import com.newbie.trackmyspend.model.PresetInfo
import com.newbie.trackmyspend.model.PresetInfoDao
import kotlinx.coroutines.flow.Flow

class PresetInfoRepository(private val presetInfoDao: PresetInfoDao) {
    suspend fun saveOrModifyPreset(presetInfo: PresetInfo) = presetInfoDao.upsertPresetInfo(presetInfo)

    suspend fun clearAllPresets() = presetInfoDao.deleteAllPresets()

    suspend fun clearParticularPreset(presetInfo: PresetInfo) = presetInfoDao.deletePresetInfo(presetInfo)

    val allPresetInfo : Flow<List<PresetCompleteInfo>> = presetInfoDao.getAllPresets()
}