package com.newbie.trackmyspend

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.newbie.trackmyspend.adapters.AllRecordAdapter
import com.newbie.trackmyspend.database.ExpenseInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityAllRecordTransactionListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

class AllRecordTransactionList : AppCompatActivity() {

    private lateinit var binding : ActivityAllRecordTransactionListBinding
    private lateinit var expenseInfoViewModel: ExpenseInfoViewModel
    private lateinit var adapter : AllRecordAdapter
    private var currentYear = 2024
    private var currentMonth = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAllRecordTransactionListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()
        getIntentData()

        adapter = AllRecordAdapter(this)
        expenseInfoViewModel = ViewModelProvider(this).get(ExpenseInfoViewModel::class.java)

        binding.ledgerRecyclerViewId.apply {
            setHasFixedSize(true)
            adapter = this@AllRecordTransactionList.adapter
            layoutManager = LinearLayoutManager(context)
        }
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
            expenseInfoViewModel.getAllTransactions(currentYear, currentMonth, null).collectLatest { expenseList ->
                //Log.d("RecyclerViewDebug", "Emitted Data: $expenseList")
                if (expenseList.isEmpty()) {
                    binding.messageId.visibility = View.VISIBLE
                    binding.addRecordTextView.visibility = View.VISIBLE
                    binding.ledgerRecyclerViewId.visibility = View.GONE
                } else {
                    binding.messageId.visibility = View.GONE
                    binding.addRecordTextView.visibility = View.GONE
                    binding.ledgerRecyclerViewId.visibility = View.VISIBLE
                    adapter.submitList(expenseList)
                }
                val spendValue = expenseList.filter { it.transactionType == ExpenseType.SPEND }.sumOf { it.amount }
                val earnedValue = expenseList.filter { it.transactionType == ExpenseType.EARNED }.sumOf { it.amount }
                val transferToValue = expenseList.filter { it.transactionType == ExpenseType.TRANSFER && it.transferType == TransferType.TO }.sumOf { it.amount }
                val transferFromValue = expenseList.filter { it.transactionType == ExpenseType.TRANSFER && it.transferType == TransferType.FROM }.sumOf { it.amount }
                binding.spendAmountValueId.text = String.format(Locale.getDefault(), "%.0f", abs(spendValue))
                binding.earnedAmountValueId.text = String.format(Locale.getDefault(), "%.0f", abs(earnedValue))
                binding.transferToAmountValueId.text = String.format(Locale.getDefault(), "%.0f", abs(transferToValue))
                binding.transferFromAmountValueId.text = String.format(Locale.getDefault(), "%.0f", abs(transferFromValue))
                val finalValue = (-spendValue + earnedValue -transferToValue + transferFromValue)
                binding.finalAmountValueId.text = String.format(Locale.getDefault(), "%.0f", abs(finalValue))
                binding.finalSumSignValueId.setImageResource(
                    if (finalValue > 0) R.drawable.baseline_add_24 else R.drawable.baseline_remove_24
                )
            }
        }
    }
}