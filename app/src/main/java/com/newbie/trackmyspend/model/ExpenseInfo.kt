package com.newbie.trackmyspend.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.newbie.trackmyspend.ExpenseType
import com.newbie.trackmyspend.TransferType

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["year", "month"], name = "index_year_month")]
)
data class ExpenseInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val transactionType : ExpenseType,
    val amount: Double,
    val category: Int = -1,
    val transferType : TransferType ?= null,
    val transferInfo : String = "",
    val description: String = "",
    val dateTime: Long,
    val year: Int,
    val month: Int
)

data class TransactionInfo(
    val transactionType: ExpenseType,
    val totalAmount : Double,
    val transferType: TransferType?
)

data class CategoryExpenseInfo(
    val categoryId : Int,
    val categoryTitle : String,
    val categoryMonthlyLimit: Double,
    val categoryHexColorCode: String,
    val totalAmount : Double
)

data class TransactionExpenseInfo(
    val id: Long = 0L,
    val transactionType : ExpenseType,
    val amount: Double,
    val category: Int = -1,
    val transferType : TransferType ?= null,
    val transferInfo : String = "",
    val description: String = "",
    val dateTime: Long,
    val year: Int,
    val month: Int,
    val categoryHexColorCode: String,
    val categoryTitle: String
)

