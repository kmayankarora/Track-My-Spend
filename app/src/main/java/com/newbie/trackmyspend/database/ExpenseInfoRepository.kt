package com.newbie.trackmyspend.database

import com.newbie.trackmyspend.ExpenseType
import com.newbie.trackmyspend.model.CategoryExpenseInfo
import com.newbie.trackmyspend.model.ExpenseInfo
import com.newbie.trackmyspend.model.ExpenseInfoDao
import com.newbie.trackmyspend.model.TransactionExpenseInfo
import com.newbie.trackmyspend.model.TransactionInfo
import kotlinx.coroutines.flow.Flow

class ExpenseInfoRepository(private val expenseInfoDao: ExpenseInfoDao) {

    // Insert or update a single expense transaction
    suspend fun saveExpense(expenseInfo: ExpenseInfo) = expenseInfoDao.upsertExpenseInfo(expenseInfo)

    // Retrieve all expense transactions as a Flow
    val allExpenses: Flow<List<ExpenseInfo>> = expenseInfoDao.getAllExpenses()
    fun getAggregatedTransactionInfo(year: Int, month: Int) : Flow<List<TransactionInfo>> =
        expenseInfoDao.getSumByTransactionType(year, month)

    // Retrieve expense transactions for a specific month and year
    fun getExpensesForMonth(year: Int, month: Int): Flow<List<ExpenseInfo>> =
        expenseInfoDao.getExpensesByMonth(year, month)

    fun getAggregatedCategoryInfo(year: Int, month: Int, transactionType: ExpenseType) : Flow<List<CategoryExpenseInfo>> =
        expenseInfoDao.getSumByCategoryType(year, month, transactionType)

    fun getAllTransactionsForMonth(year : Int, month : Int, transactionType : ExpenseType?): Flow<List<TransactionExpenseInfo>> =
        expenseInfoDao.getAllTransaction(year, month, transactionType)

    fun getAllTransactionsForMonthForParticularCategory(year : Int, month : Int, categoryId : Int, transactionType: ExpenseType): Flow<List<TransactionExpenseInfo>> =
        expenseInfoDao.getAllTransactionForParticularCategory(year, month, categoryId, transactionType)

    fun getAllTransactionForParticularClub(clubId : Long) : Flow<List<TransactionExpenseInfo>> =
        expenseInfoDao.getAllTransactionForParticularClub(clubId)


    // Delete all expenses
    suspend fun clearAllExpenses() = expenseInfoDao.deleteAllExpenses()

    // Delete expenses for a specific month and year
    suspend fun clearExpensesForMonth(year: Int, month: Int) =
        expenseInfoDao.deleteExpenseForMonthYear(year, month)

    // Delete a specific expense transaction
    suspend fun removeExpense(expenseInfo: ExpenseInfo) = expenseInfoDao.deleteExpense(expenseInfo)
}
