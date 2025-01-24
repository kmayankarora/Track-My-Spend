package com.newbie.trackmyspend.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubInfoDao {
    @Upsert
    suspend fun upsertClubInfo(club : ClubInfo)

    @Query("SELECT * FROM club_info ORDER BY id DESC")
    fun getAllClubInfo() : Flow<List<ClubInfo>>

    @Query("DELETE FROM club_info")
    suspend fun deleteAllClubs()

    @Delete
    suspend fun deleteClub(club : ClubInfo)
}