package com.newbie.trackmyspend.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.newbie.trackmyspend.model.ClubInfo
import kotlinx.coroutines.launch

class ClubInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val clubInfoRepository : ClubInfoRepository

    init {
        val clubDao = DatabaseInstance.getDatabase(application).clubInfoDao
        clubInfoRepository = ClubInfoRepository(clubDao)
    }

    fun saveOrModifyClub(clubInfo: ClubInfo) = viewModelScope.launch {
        clubInfoRepository.saveOrModifyClub(clubInfo)
    }

    fun deleteClub(clubInfo: ClubInfo) = viewModelScope.launch {
        clubInfoRepository.deleteClub(clubInfo)
    }

    val allClubs = clubInfoRepository.allClubs
}