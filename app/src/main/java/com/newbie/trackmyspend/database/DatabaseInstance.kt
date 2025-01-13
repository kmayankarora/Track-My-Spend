package com.newbie.trackmyspend.database

import android.content.Context
import androidx.room.Room

object DatabaseInstance {
    @Volatile
    private var INSTANCE: ExpenseTransactionDatabase?= null

    fun getDatabase(context : Context) : ExpenseTransactionDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                ExpenseTransactionDatabase::class.java,
                "expense_transaction_db" // Name of the database file
            ).build()
            INSTANCE = instance
            instance
        }
    }
}