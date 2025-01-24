package com.newbie.trackmyspend.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension to create a DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class SharedDataStore private constructor(private val dataStore: DataStore<Preferences>) {

    companion object {
        @Volatile
        private var INSTANCE: SharedDataStore? = null

        // Get Singleton Instance
        fun getInstance(context: Context): SharedDataStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharedDataStore(context.dataStore)
                    .also { INSTANCE = it }
            }
        }

        // Keys for preferences
        private val TARGET_MONTHLY_EXPENSE_KEY = doublePreferencesKey("target_monthly_expense")
        private val TARGET_MONTHLY_EARNING_KEY = doublePreferencesKey("target_monthly_earning")
        private val SHOW_EARNING_IN_REPORTCARD = booleanPreferencesKey("show_earning_reportcard")
        private val SHOW_TRANSFER_FROM_IN_REPORTCARD = booleanPreferencesKey("show_transfer_from_reportcard")
    }

    // Save target monthly expense
    suspend fun saveTargetMonthlyExpense(value: Double) {
        dataStore.edit { preferences ->
            preferences[TARGET_MONTHLY_EXPENSE_KEY] = value
        }
    }

    // Retrieve target monthly expense as Flow
    val targetMonthlyExpenseFlow: Flow<Double> = dataStore.data.map { preferences ->
        preferences[TARGET_MONTHLY_EXPENSE_KEY] ?: 20000.0 // Default value is 10000
    }

    // Save target monthly earning
    suspend fun saveTargetMonthlyEarning(value: Double) {
        dataStore.edit { preferences ->
            preferences[TARGET_MONTHLY_EARNING_KEY] = value
        }
    }

    // Retrieve target monthly earning as Flow
    val targetMonthlyEarningFlow: Flow<Double> = dataStore.data.map { preferences ->
        preferences[TARGET_MONTHLY_EARNING_KEY] ?: 20000.0 // Default value is 10000
    }

    suspend fun setShowEarningInReportCardStatus(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_EARNING_IN_REPORTCARD] = value
        }
    }

    // Retrieve target monthly earning as Flow
    val getShowEarningInReportCardStatus: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_EARNING_IN_REPORTCARD] ?: true // Default value is 10000
    }

    suspend fun setShowTransferFromInReportCardStatus(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_TRANSFER_FROM_IN_REPORTCARD] = value
        }
    }

    // Retrieve target monthly earning as Flow
    val getShowTransferFromInReportCardStatus: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_TRANSFER_FROM_IN_REPORTCARD] ?: true // Default value is 10000
    }


}
