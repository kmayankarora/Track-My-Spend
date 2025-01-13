package com.newbie.trackmyspend.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.newbie.trackmyspend.ExpenseType
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseInfoDao {
    @Upsert
    suspend fun upsertExpenseInfo(expense: ExpenseInfo)

    @Query("SELECT * FROM expenses ORDER BY dateTime DESC")
    fun getAllExpenses(): Flow<List<ExpenseInfo>>

    @Query("SELECT * FROM expenses WHERE year = :year AND month = :month ORDER BY dateTime DESC")
    fun getExpensesByMonth(year: Int, month: Int): Flow<List<ExpenseInfo>>

    @Query("SELECT * FROM expenses WHERE year = :year AND month = :month AND category = :type ORDER BY dateTime DESC")
    fun getExpensesByCategory(year: Int, month: Int, type : Int): Flow<List<ExpenseInfo>>

    @Query("""
        SELECT transactionType, SUM(amount) as totalAmount, transferType
        FROM expenses
        WHERE year = :year AND month = :month
        GROUP BY transactionType, transferType
        ORDER BY 
            transactionType ASC,
            CASE 
                WHEN transferType is NULL THEN 1
                WHEN transferType = 'FROM' THEN 2
                WHEN transferType = 'TO' THEN 3
                ELSE 4
            END
    """)
    fun getSumByTransactionType(year: Int, month: Int): Flow<List<TransactionInfo>>


//    @Query("""
//        SELECT
//            COALESCE(c.id, -1) AS categoryId,  -- Use -1 or another value for missing categories
//            COALESCE(c.title, 'Others') AS categoryTitle,  -- Label deleted categories as 'Others'
//            COALESCE(c.monthlyLimit, 0.0) AS categoryMonthlyLimit,  -- Default monthly limit as 0 for 'Others'
//            COALESCE(c.hexColorCode, '#000000') AS categoryHexColorCode,  -- Default hex color for 'Others'
//            SUM(e.amount) AS totalAmount
//        FROM expenses e
//        LEFT JOIN category c ON e.category = c.id AND e.year = :year AND e.month = :month
//        GROUP BY COALESCE(c.id, -1)
//        ORDER BY totalAmount DESC
//    """)
    @Query("""
        SELECT 
            COALESCE(c.id, -1) AS categoryId,  -- Use -1 or another value for missing categories
            COALESCE(c.title, 'Others') AS categoryTitle,  -- Label deleted categories as 'Others'
            COALESCE(c.monthlyLimit, 0.0) AS categoryMonthlyLimit,  -- Default monthly limit as 0 for 'Others'
            COALESCE(c.hexColorCode, '#000000') AS categoryHexColorCode,  -- Default hex color for 'Others'
            SUM(e.amount) AS totalAmount  -- Sum of amounts for 'SPEND' transactions
        FROM expenses e
        LEFT JOIN category c ON e.category = c.id 
        WHERE e.transactionType = :transactionType  -- Only include 'SPEND' transactions
            AND e.year = :year AND e.month = :month
        GROUP BY COALESCE(c.id, -1)
        ORDER BY totalAmount DESC
    """)
    fun getSumByCategoryType(year: Int, month: Int, transactionType: ExpenseType): Flow<List<CategoryExpenseInfo>>

//    @Query("""
//        SELECT
//            e.id AS id,
//            e.transactionType AS transactionType,
//            e.amount AS amount,
//            e.category AS category,
//            e.transferType,
//            e.transferInfo,
//            e.description,
//            e.dateTime,
//            e.year,
//            e.month,
//            COALESCE(c.hexColorCode, "#000000") AS categoryHexColorCode,
//            COALESCE(c.title, "Others") AS categoryTitle
//        FROM expenses e
//        LEFT JOIN category c
//        ON e.category = c.id
//        WHERE e.year = :year AND e.month = :month
//        ORDER BY e.dateTime DESC
//    """)
    @Query("""
        SELECT 
            e.id AS id,
            e.transactionType AS transactionType,
            e.amount AS amount,
            e.category AS category,
            e.transferType,
            e.transferInfo,
            e.description,
            e.dateTime,
            e.year,
            e.month,
            COALESCE(c.hexColorCode, "#000000") AS categoryHexColorCode,
            COALESCE(c.title, "Others") AS categoryTitle
        FROM expenses e
        LEFT JOIN category c
        ON e.category = c.id 
        WHERE e.year = :year 
          AND e.month = :month
          AND (:transactionType IS NULL OR e.transactionType = :transactionType)
        ORDER BY e.dateTime DESC, e.id DESC
    """)
    fun getAllTransaction(year: Int, month: Int, transactionType : ExpenseType?): Flow<List<TransactionExpenseInfo>>

//    @Query("""
//        SELECT
//            e.id AS id,
//            e.transactionType AS transactionType,
//            e.amount AS amount,
//            e.category AS category,
//            e.transferType,
//            e.transferInfo,
//            e.description,
//            e.dateTime,
//            e.year,
//            e.month,
//            COALESCE(c.hexColorCode, "#000000") AS categoryHexColorCode,
//            COALESCE(c.title, "Others") AS categoryTitle
//        FROM expenses e
//        LEFT JOIN category c
//        ON e.category = c.id
//        WHERE e.year = :year AND e.month = :month AND e.category = :categoryId
//        ORDER BY e.dateTime DESC
//    """)
//    @Query("""
//        SELECT
//            e.id AS id,
//            e.transactionType AS transactionType,
//            e.amount AS amount,
//            e.category AS category,
//            e.transferType,
//            e.transferInfo,
//            e.description,
//            e.dateTime,
//            e.year,
//            e.month,
//            COALESCE(c.hexColorCode, "#000000") AS categoryHexColorCode,
//            COALESCE(c.title, "Others") AS categoryTitle
//        FROM expenses e
//        LEFT JOIN category c
//        ON e.category = c.id
//        WHERE e.year = :year
//          AND e.month = :month
//          AND (
//            :categoryId = -1
//            OR e.category = :categoryId
//          )
//          AND (c.id IS NULL OR e.category = c.id)
//        ORDER BY e.dateTime DESC
//    """)
@Query("""
    SELECT 
        e.id AS id,
        e.transactionType AS transactionType,
        e.amount AS amount,
        e.category AS category,
        e.transferType,
        e.transferInfo,
        e.description,
        e.dateTime,
        e.year,
        e.month,
        COALESCE(c.hexColorCode, "#000000") AS categoryHexColorCode,
        COALESCE(c.title, "Others") AS categoryTitle
    FROM expenses e
    LEFT JOIN category c
    ON e.category = c.id 
    WHERE e.year = :year 
      AND e.month = :month 
      AND e.transactionType = :transactionType
      AND (
        :categoryId != -1 
        OR c.id IS NULL
      )
      AND (:categoryId = -1 OR e.category = :categoryId)
    ORDER BY e.dateTime DESC, e.id DESC
""")
    fun getAllTransactionForParticularCategory(year: Int, month: Int, categoryId : Int, transactionType: ExpenseType): Flow<List<TransactionExpenseInfo>>



    // Delete a specific expense
    @Delete
    suspend fun deleteExpense(expense: ExpenseInfo)

    // Delete all expenses
    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM expenses WHERE year = :year AND month = :month")
    suspend fun deleteExpenseForMonthYear(year: Int, month: Int)
}