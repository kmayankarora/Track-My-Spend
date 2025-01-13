package com.newbie.trackmyspend

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.newbie.trackmyspend.databinding.ActivityMainBinding
import com.newbie.trackmyspend.databinding.ActivityPreferencesBinding
import com.newbie.trackmyspend.model.SharedDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class Preferences : AppCompatActivity() {

    private lateinit var binding: ActivityPreferencesBinding
    private lateinit var preferencesManager : SharedDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()
        preferencesManager = SharedDataStore.getInstance(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val showEarningsInReportcard = preferencesManager.getShowEarningInReportCardStatus.first() // Assuming this is a boolean value in your datastore
                val showTransferFromInReportcard = preferencesManager.getShowTransferFromInReportCardStatus.first() // Assuming this is a boolean value in your datastore
                val monthlyExpenseLimitValue = preferencesManager.targetMonthlyExpenseFlow.first()
                val monthlyEarningLimitValue = preferencesManager.targetMonthlyEarningFlow.first()
                binding.earningToggleSwitch.isChecked = showEarningsInReportcard
                binding.transferFromToggleSwitch.isChecked = showTransferFromInReportcard

                val monthlyExpense = String.format(Locale.getDefault(),"%.0f", monthlyExpenseLimitValue)
                val monthlyEarning = String.format(Locale.getDefault(),"%.0f", monthlyEarningLimitValue)
                binding.earningTargetEditText.setText(monthlyEarning)
                binding.expenseTargetEditText.setText(monthlyExpense)
            }
        }

        binding.saveTargetPrefBtnId.setOnClickListener {
            savePreferences()
        }

    }

    private fun setActionBar() {
        setSupportActionBar(binding.toolbarId)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.baseline_close_24)
        }

        binding.toolbarId.setNavigationOnClickListener { v: View? ->
            // Use OnBackPressedDispatcher for back button functionality
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun savePreferences(){
        val expenseMonthlyTargetText = binding.expenseTargetEditText.text?.trim().toString()
        val earningMonthlyTargetText = binding.earningTargetEditText.text?.trim().toString()
        val expenseValue = if(expenseMonthlyTargetText.isEmpty()) 0.0 else expenseMonthlyTargetText.toDouble()
        val earningValue = if(earningMonthlyTargetText.isEmpty()) 0.0 else earningMonthlyTargetText.toDouble()
        val showEarningStatus = binding.earningToggleSwitch.isChecked
        val showTransferFromStatus = binding.transferFromToggleSwitch.isChecked

        if (expenseValue > 0.0 && earningValue > 0.0) {
            // Save a value
            lifecycleScope.launch {
                preferencesManager.saveTargetMonthlyExpense(expenseValue)
            }
            lifecycleScope.launch {
                preferencesManager.saveTargetMonthlyEarning(earningValue)
            }
            lifecycleScope.launch {
                preferencesManager.setShowEarningInReportCardStatus(showEarningStatus)
            }
            lifecycleScope.launch {
                preferencesManager.setShowTransferFromInReportCardStatus(showTransferFromStatus)
            }
            finish()
        } else {
            Snackbar.make(binding.mainId, "Wrong Input passed", Snackbar.LENGTH_SHORT).show()
        }
    }
}