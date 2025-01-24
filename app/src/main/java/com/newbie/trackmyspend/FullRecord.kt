package com.newbie.trackmyspend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.newbie.trackmyspend.databinding.ActivityFullRecordBinding
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FullRecord : AppCompatActivity() {

    private lateinit var binding : ActivityFullRecordBinding
    private var currentYear = 2024
    private var currentMonth = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFullRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()
        getDateAndTime()
        setupNumberPicker()
        setupArrowButtons()
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

    private fun getDateAndTime() {
        val todayDate = Date()
        val calendar = Calendar.getInstance()

        calendar.time = todayDate

        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1
    }

    private fun setupNumberPicker(){
        // Months for the picker
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "July", "Aug", "Sept", "Oct", "Nov", "Dec"
        )

        // Configure Month Picker
        binding.monthNumberPickerId.apply {
            minValue = 0
            maxValue = months.size - 1
            displayedValues = months
            value = currentMonth - 1
            wrapSelectorWheel = true
        }

        binding.yearNumberPickerId.apply {
            minValue = currentYear - 5
            maxValue = currentYear + 2
            value = currentYear
            wrapSelectorWheel = false
        }

        val updateDate = {
            currentMonth = binding.monthNumberPickerId.value
            currentYear = binding.yearNumberPickerId.value
            binding.monthYearDisplayId.text = String.format(Locale.getDefault(), "%s, %4d", months[currentMonth], currentYear) //"Selected Date: $selectedMonth $selectedYear"
            val monthYear = String.format(Locale.getDefault(), "%02d/%4d", currentMonth + 1, currentYear)
            binding.monthIndicatorText1.text = monthYear
            binding.monthIndicatorText2.text = monthYear
            binding.monthIndicatorText3.text = monthYear
        }
        binding.monthNumberPickerId.setOnValueChangedListener { _, _, _ -> updateDate() }
        binding.yearNumberPickerId.setOnValueChangedListener { _, _, _ -> updateDate() }

        updateDate()
    }

    private fun setupArrowButtons() {
        binding.clubDetailsInfo.setOnClickListener {
            val intent = Intent(this, DisplayAllClubsInfo::class.java)
            startActivity(intent)
        }

        binding.allRecordId.setOnClickListener {
            val intent = Intent(this, AllTransactionList::class.java)
            intent.putExtra("CurrentYear", currentYear)
            intent.putExtra("CurrentMonth", currentMonth + 1)
            startActivity(intent)
        }
        binding.categoryWiseRecordsId.setOnClickListener {
            val intent = Intent(this, CategoryWiseTransactions::class.java)
            intent.putExtra("CurrentYear", currentYear)
            intent.putExtra("CurrentMonth", currentMonth + 1)
            startActivity(intent)
        }
        binding.reportCardId.setOnClickListener {
            val intent = Intent(this, AllRecordTransactionList::class.java)
            intent.putExtra("CurrentYear", currentYear)
            intent.putExtra("CurrentMonth", currentMonth + 1)
            startActivity(intent)
        }
        binding.graphCardArrowButtonId.setOnClickListener {
            val intent = Intent(this, GraphRecords  ::class.java)
            intent.putExtra("CurrentYear", currentYear)
            intent.putExtra("CurrentMonth", currentMonth + 1)
            startActivity(intent)
        }
    }
}