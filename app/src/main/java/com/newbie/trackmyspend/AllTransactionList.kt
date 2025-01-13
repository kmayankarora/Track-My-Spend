package com.newbie.trackmyspend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.newbie.trackmyspend.adapters.TransactionAdapter
import com.newbie.trackmyspend.database.ExpenseInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityAllTransactionListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class AllTransactionList : AppCompatActivity() {

    private lateinit var binding : ActivityAllTransactionListBinding
    private lateinit var expenseInfoViewModel: ExpenseInfoViewModel
    private lateinit var adapter : TransactionAdapter
    private var currentYear = 2024
    private var currentMonth = 12
    private var transactionType : ExpenseType ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAllTransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getDateAndTime()
        setActionBar()
        getIntentData()
        expenseInfoViewModel = ViewModelProvider(this).get(ExpenseInfoViewModel::class.java)

        adapter = TransactionAdapter(this)


        binding.recyclerViewId.apply {
            setHasFixedSize(true)
            adapter = this@AllTransactionList.adapter
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

    private fun getDateAndTime() {
        val todayDate = Date()
        val calendar = Calendar.getInstance()

        calendar.time = todayDate

        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1
    }

    private fun setupSpinner(){
        val transationTypeList = listOf("All", "Spend", "Earned", "Transfer")
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, transationTypeList)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.transactionSpinner.adapter = adapter
        binding.transactionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = transationTypeList[position]
                when(position) {
                    1 -> transactionType = ExpenseType.SPEND
                    2 -> transactionType = ExpenseType.EARNED
                    3 -> transactionType = ExpenseType.TRANSFER
                    else -> transactionType = null
                }
                fetchTransactions()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                transactionType = null
                fetchTransactions()
            }
        }
    }

    private fun fetchTransactions() {
        lifecycleScope.launch {
            expenseInfoViewModel.getAllTransactions(currentYear, currentMonth, transactionType).collectLatest { expenseList ->
                //Log.d("RecyclerViewDebug", "Emitted Data: $expenseList")
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
}