package com.newbie.trackmyspend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.newbie.trackmyspend.adapters.CategoryExpenseInfoAdapter
import com.newbie.trackmyspend.adapters.TransactionAdapter
import com.newbie.trackmyspend.database.ExpenseInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityCategoryWiseTransactionsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryWiseTransactions : AppCompatActivity() {

    private lateinit var binding : ActivityCategoryWiseTransactionsBinding
    private lateinit var expenseInfoViewModel: ExpenseInfoViewModel
    private lateinit var adapter : CategoryExpenseInfoAdapter
    private var currentTransactionType : ExpenseType = ExpenseType.SPEND
    private var currentYear = 2024
    private var currentMonth = 12


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoryWiseTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()
        getIntentData()
        expenseInfoViewModel = ViewModelProvider(this).get(ExpenseInfoViewModel::class.java)

        adapter = CategoryExpenseInfoAdapter(
            this,
            currentYear,
            currentMonth,
            ExpenseType.SPEND,
            getTransactionType = {currentTransactionType})

        binding.recyclerViewId.apply {
            setHasFixedSize(true)
            adapter = this@CategoryWiseTransactions.adapter
            layoutManager = LinearLayoutManager(context)
        }
        setupSpinner()
        fetchTransactions()
        binding.addRecordTextView.setOnClickListener {
            val intent = Intent(this, AddTransaction::class.java)
            startActivity(intent)
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

    private fun getIntentData() {
        intent?.let{
            currentYear = it.getIntExtra("CurrentYear", currentYear)
            currentMonth = it.getIntExtra("CurrentMonth", currentMonth)
        }
    }

    private fun fetchTransactions() {
        lifecycleScope.launch {
            expenseInfoViewModel.getCategoryStatus(currentYear, currentMonth, currentTransactionType).collectLatest { expenseList ->
                if (expenseList.isEmpty()) {
                    binding.recyclerViewId.visibility = View.GONE
                    binding.messageId.visibility = View.VISIBLE
                    binding.addRecordTextView.visibility = View.VISIBLE
                } else {
                    binding.recyclerViewId.visibility = View.VISIBLE
                    binding.messageId.visibility = View.GONE
                    binding.addRecordTextView.visibility = View.GONE
                    adapter.submitList(expenseList)
                }
            }
        }
    }

    private fun setupSpinner(){
        val transationTypeList = listOf("Spend", "Earned", "Transfer")
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, transationTypeList)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.transactionTypeSpinner.adapter = adapter
        binding.transactionTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = transationTypeList[position]
                when(position) {
                    0 -> currentTransactionType = ExpenseType.SPEND
                    1 -> currentTransactionType = ExpenseType.EARNED
                    2 -> currentTransactionType = ExpenseType.TRANSFER
                    else -> currentTransactionType = ExpenseType.SPEND
                }
                fetchTransactions()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                currentTransactionType = ExpenseType.SPEND
                fetchTransactions()
            }
        }
    }
}