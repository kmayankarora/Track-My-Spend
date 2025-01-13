package com.newbie.trackmyspend.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.newbie.trackmyspend.model.CategoryInfo
import com.newbie.trackmyspend.model.CategoryInfoDao
import com.newbie.trackmyspend.model.ExpenseInfo
import com.newbie.trackmyspend.model.ExpenseInfoDao
import com.newbie.trackmyspend.model.PresetInfo
import com.newbie.trackmyspend.model.PresetInfoDao

@Database(
    entities = [ExpenseInfo::class, CategoryInfo::class, PresetInfo::class],
    version = 1
)
abstract class ExpenseTransactionDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "ExpenseTransactionDB"
    }
    abstract val dao: ExpenseInfoDao
    abstract val categoryDao : CategoryInfoDao
    abstract val presetInfoDao : PresetInfoDao
}