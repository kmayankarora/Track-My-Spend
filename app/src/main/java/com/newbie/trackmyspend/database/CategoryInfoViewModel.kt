package com.newbie.trackmyspend.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.newbie.trackmyspend.model.CategoryInfo
import kotlinx.coroutines.launch

class CategoryInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val categoryInfoRepository : CategoryInfoRepository

    init {
        val categoryDao = DatabaseInstance.getDatabase(application).categoryDao
        categoryInfoRepository = CategoryInfoRepository(categoryDao)
    }

    fun saveOrModifyCategory(categoryInfo: CategoryInfo) = viewModelScope.launch {
        categoryInfoRepository.saveOrModifyCategory(categoryInfo)
    }

    fun deleteCategory(categoryInfo: CategoryInfo) = viewModelScope.launch {
        categoryInfoRepository.deleteCategory(categoryInfo)
    }

    val allCategory = categoryInfoRepository.allCategory
}