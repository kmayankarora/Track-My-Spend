package com.newbie.trackmyspend

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.newbie.trackmyspend.database.ExpenseInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityGraphRecordsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class GraphRecords : AppCompatActivity() {

    private lateinit var binding : ActivityGraphRecordsBinding
    private lateinit var expenseInfoViewModel: ExpenseInfoViewModel
    private var currentYear = 2024
    private var currentMonth = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGraphRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        expenseInfoViewModel = ViewModelProvider(this).get(ExpenseInfoViewModel::class.java)

        setActionBar()
        getIntentData()
        fetchTransactions()
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

    private fun getIntentData() {
        intent?.let{
            currentYear = it.getIntExtra("CurrentYear", currentYear)
            currentMonth = it.getIntExtra("CurrentMonth", currentMonth)
        }
    }

    private fun fetchTransactions() {
        lifecycleScope.launch {
            expenseInfoViewModel.getTransactionStatus(currentYear, currentMonth).collectLatest { expenseList ->

            }
        }
        lifecycleScope.launch {
            expenseInfoViewModel.getCategoryStatus(currentYear, currentMonth, ExpenseType.SPEND)
                .debounce(300)
                .collectLatest { transactionList ->
            }
        }
    }
}