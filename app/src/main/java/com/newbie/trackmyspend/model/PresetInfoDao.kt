package com.newbie.trackmyspend.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetInfoDao {
    @Upsert
    suspend fun upsertPresetInfo(presetInfo: PresetInfo)

    @Delete
    suspend fun deletePresetInfo(presetInfo: PresetInfo)

    @Query("DELETE FROM presetTable")
    suspend fun deleteAllPresets()

    //@Query("SELECT * FROM presetTable ORDER BY id DESC")
    @Query("""
        SELECT 
            pt.id AS id,
            pt.presetName AS presetName,
            pt.transactionType AS transactionType,
            pt.amount AS amount,
            pt.category AS category,
            pt.transferType,
            pt.transferInfo,
            COALESCE(c.hexColorCode, "#000000") AS categoryHexCode,
            COALESCE(c.title, "Others") AS categoryTitle
        FROM presetTable pt
        LEFT JOIN category c
        ON pt.category = c.id
        ORDER BY pt.id DESC
    """)
    fun getAllPresets() : Flow<List<PresetCompleteInfo>>
}