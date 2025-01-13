package com.newbie.trackmyspend

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.newbie.trackmyspend.adapters.TransactionAdapter
import com.newbie.trackmyspend.database.ExpenseInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityAllTransactionCategoryWiseBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AllTransactionCategoryWise : AppCompatActivity() {

    private lateinit var binding: ActivityAllTransactionCategoryWiseBinding
    private lateinit var expenseInfoViewModel: ExpenseInfoViewModel
    private lateinit var adapter : TransactionAdapter
    private var currentYear = 2024
    private var currentMonth = 12
    private var currentCategoryId = -1
    private var currentMonthlyLimit : Double = 0.0
    private var currentSum : Double = 0.0
    private var transactionType : ExpenseType = ExpenseType.SPEND
    val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAllTransactionCategoryWiseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getDateAndTime()
        setActionBar()
        getIntentData()
        expenseInfoViewModel = ViewModelProvider(this).get(ExpenseInfoViewModel::class.java)

        adapter = TransactionAdapter(this)
        //Toast.makeText(this, "" + currentCategoryId, Toast.LENGTH_SHORT).show()

        binding.recyclerViewId.apply {
            setHasFixedSize(true)
            adapter = this@AllTransactionCategoryWise.adapter
            layoutManager = LinearLayoutManager(context)
        }
        lifecycleScope.launch {
            expenseInfoViewModel.getAllTransactionsForParticularCategory(currentYear, currentMonth, currentCategoryId, transactionType).collectLatest { expenseList ->
                //Log.d("RecyclerViewDebug", "Emitted Data: $expenseList")
                currentSum = expenseList.sumOf { it.amount }
                binding.categoryAmountTextView2.text = currentSum.toString()
                if (currentMonthlyLimit != 0.0)
                    binding.spendTitleValueProgressbarId.progress = (currentSum / currentMonthlyLimit * 100).toInt()
                else binding.spendTitleValueProgressbarId.progress = 100
                adapter.submitList(expenseList)
            }
        }

    }

    private fun setActionBar() {
        setSupportActionBar(binding.toolbarId)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.baseline_close_24)
            supportActionBar!!.title = "Title"
        }

        binding.toolbarId.setNavigationOnClickListener { v: View? ->
            // Use OnBackPressedDispatcher for back button functionality
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getIntentData() {
        intent?.let{
            val transactionTypeString = it.getStringExtra("TRANSACTION_TYPE")
            transactionType = if (transactionTypeString != null) {
                ExpenseType.valueOf(transactionTypeString)
            } else {
                ExpenseType.SPEND // Default value in case of a null
            }
            currentYear = it.getIntExtra("CurrentYear", currentYear)
            currentMonth = it.getIntExtra("CurrentMonth", currentMonth)
            currentCategoryId = it.getIntExtra("CurrentCategory", currentCategoryId)
            val title = it.getStringExtra("CategoryTitle")
            currentMonthlyLimit = it.getDoubleExtra("CategoryMonthlyLimit",0.0)
            currentSum = it.getDoubleExtra("CategoryCurrentSum", 0.0)
            supportActionBar?.title = title //String.format(Locale.getDefault(), "%s (%02d/%02d)",title, currentMonth, currentYear)
            binding.currentPeriodValueId.text = String.format(Locale.getDefault(), "%s, %04d", monthNames[currentMonth-1], currentYear)
            binding.monthlyAmountLimitId.text = if (currentMonthlyLimit > 0.0) String.format(Locale.getDefault(),"%.0f", currentMonthlyLimit) else "Inf"
            binding.categoryAmountTextView2.text = String.format(Locale.getDefault(), "%.0f", currentSum)
            if (currentMonthlyLimit != 0.0)
                binding.spendTitleValueProgressbarId.progress = (currentSum / currentMonthlyLimit * 100).toInt()
            else binding.spendTitleValueProgressbarId.progress = 100
        }
    }

    private fun getDateAndTime() {
        val todayDate = Date()
        val calendar = Calendar.getInstance()

        calendar.time = todayDate
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1
    }
}