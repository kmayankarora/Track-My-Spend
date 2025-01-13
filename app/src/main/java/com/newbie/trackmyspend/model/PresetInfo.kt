package com.newbie.trackmyspend.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.newbie.trackmyspend.ExpenseType
import com.newbie.trackmyspend.TransferType

@Entity(
    tableName = "presetTable",
    indices = [Index(value = ["presetName"], unique = true)]
)
data class PresetInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val presetName: String,
    val transactionType: ExpenseType,
    val amount: Double,
    val category: Int = -1,
    val transferType: TransferType?= null,
    val transferInfo: String = ""
)

data class PresetCompleteInfo(
    val id: Long = 0L,
    val presetName : String,
    val transactionType : ExpenseType,
    val amount: Double,
    val category: Int = -1,
    val transferType : TransferType?= null,
    val transferInfo : String = "",
    val categoryTitle : String,
    val categoryHexCode : String
)
