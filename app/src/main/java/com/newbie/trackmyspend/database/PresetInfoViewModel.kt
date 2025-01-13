package com.newbie.trackmyspend.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.newbie.trackmyspend.model.PresetInfo
import kotlinx.coroutines.launch

class PresetInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val presetInfoRepository : PresetInfoRepository

    init {
        val presetInfoDao = DatabaseInstance.getDatabase(application).presetInfoDao
        presetInfoRepository = PresetInfoRepository(presetInfoDao)
    }

    val allPreset = presetInfoRepository.allPresetInfo

    fun saveOrModifyPreset(presetInfo : PresetInfo) = viewModelScope.launch {
        presetInfoRepository.saveOrModifyPreset(presetInfo)
    }

    fun clearAllPreset() = viewModelScope.launch {
        presetInfoRepository.clearAllPresets()
    }

    fun clearParticularPreset(presetInfo: PresetInfo) = viewModelScope.launch {
        presetInfoRepository.clearParticularPreset(presetInfo)
    }
}