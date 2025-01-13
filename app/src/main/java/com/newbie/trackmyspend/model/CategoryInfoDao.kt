package com.newbie.trackmyspend.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryInfoDao {
    @Upsert
    suspend fun upsertCategoryInfo(category: CategoryInfo)

    @Query("SELECT * FROM category ORDER BY id ASC")
    fun getAllCategoryInfo(): Flow<List<CategoryInfo>>

    @Query("DELETE FROM category")
    suspend fun deleteAllCategories()

    @Delete
    suspend fun deleteCategory(category: CategoryInfo)
}