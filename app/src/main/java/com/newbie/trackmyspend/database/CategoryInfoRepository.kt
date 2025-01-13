package com.newbie.trackmyspend.database

import com.newbie.trackmyspend.model.CategoryInfo
import com.newbie.trackmyspend.model.CategoryInfoDao
import kotlinx.coroutines.flow.Flow

class CategoryInfoRepository(private val categoryDao : CategoryInfoDao) {
    suspend fun saveOrModifyCategory(categoryInfo: CategoryInfo) = categoryDao.upsertCategoryInfo(categoryInfo)

    val allCategory:Flow<List<CategoryInfo>> = categoryDao.getAllCategoryInfo()

    suspend fun deleteCategory(categoryInfo: CategoryInfo) = categoryDao.deleteCategory(categoryInfo)

}