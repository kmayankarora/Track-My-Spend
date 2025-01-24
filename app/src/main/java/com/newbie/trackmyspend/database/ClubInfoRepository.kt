package com.newbie.trackmyspend.database

import com.newbie.trackmyspend.model.ClubInfo
import com.newbie.trackmyspend.model.ClubInfoDao
import kotlinx.coroutines.flow.Flow

class ClubInfoRepository(private val clubDao: ClubInfoDao) {
    suspend fun saveOrModifyClub(clubInfo: ClubInfo) = clubDao.upsertClubInfo(clubInfo)

    val allClubs : Flow<List<ClubInfo>> = clubDao.getAllClubInfo()

    suspend fun deleteClub(clubInfo: ClubInfo) = clubDao.deleteClub(clubInfo)
}