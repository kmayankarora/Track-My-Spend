package com.newbie.trackmyspend.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.newbie.trackmyspend.ExpenseType
import com.newbie.trackmyspend.model.CategoryExpenseInfo
import com.newbie.trackmyspend.model.ExpenseInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExpenseInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val expenseRepository: ExpenseInfoRepository

    init {
        val expenseInfoDao = DatabaseInstance.getDatabase(application).dao
        expenseRepository = ExpenseInfoRepository(expenseInfoDao)
    }

    // Save or update a single expense transaction
    fun saveExpense(expenseInfo: ExpenseInfo) = viewModelScope.launch {
        expenseRepository.saveExpense(expenseInfo)
    }

    // Remove a specific expense transaction
    fun removeExpense(expenseInfo: ExpenseInfo) = viewModelScope.launch {
        expenseRepository.removeExpense(expenseInfo)
    }

    // Clear all expenses
    fun clearAllExpenses() = viewModelScope.launch {
        expenseRepository.clearAllExpenses()
    }

    // Clear expenses for a specific month and year
    fun clearExpensesForMonth(year: Int, month: Int) = viewModelScope.launch {
        expenseRepository.clearExpensesForMonth(year, month)
    }

    // Fetch all expenses as Flow
    val allExpenses = expenseRepository.allExpenses

    // Fetch expenses for a specific year and month
    fun getExpensesForMonth(year: Int, month: Int) =
        expenseRepository.getExpensesForMonth(year, month)

    fun getTransactionStatus(year: Int, month : Int) = expenseRepository.getAggregatedTransactionInfo(year, month)
    fun getCategoryStatus(year: Int, month : Int, transactionType: ExpenseType) : Flow<List<CategoryExpenseInfo>> {
        return expenseRepository.getAggregatedCategoryInfo(year, month, transactionType)
    }
    fun getAllTransactions(year : Int, month : Int, transactionType : ExpenseType?) = expenseRepository.getAllTransactionsForMonth(year, month, transactionType)

    fun getAllTransactionsForParticularCategory(year: Int, month: Int, categoryId : Int, transactionType: ExpenseType)
        = expenseRepository.getAllTransactionsForMonthForParticularCategory(year, month, categoryId, transactionType)
}
