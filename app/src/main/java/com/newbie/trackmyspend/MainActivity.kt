package com.newbie.trackmyspend

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.newbie.trackmyspend.adapters.CategoryExpenseInfoAdapter
import androidx.core.graphics.ColorUtils
import com.newbie.trackmyspend.adapters.PresetInfoAdapter
import com.newbie.trackmyspend.database.ExpenseInfoViewModel
import com.newbie.trackmyspend.database.PresetInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityMainBinding
import com.newbie.trackmyspend.model.SharedDataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var expenseInfoViewModel: ExpenseInfoViewModel
    private lateinit var presetInfoViewModel: PresetInfoViewModel

    private lateinit var presetAdapter : PresetInfoAdapter

    private lateinit var preferencesManager : SharedDataStore
    private var currentYear = 2024
    private var currentMonth = 12
    private var spendingAmounts = doubleArrayOf(0.0, 0.0);
    private var color = Color.parseColor("#FF0000")

    val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarId)
        setSupportActionBar(toolbar)

        expenseInfoViewModel = ViewModelProvider(this)[ExpenseInfoViewModel::class.java]
        preferencesManager = SharedDataStore.getInstance(this)
        presetInfoViewModel = ViewModelProvider(this)[PresetInfoViewModel::class.java]

        val todayDate = Date()
        val calendar = Calendar.getInstance()

        calendar.time = todayDate

        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentPeriodValue = String.format(Locale.getDefault(), "%s, %04d", monthNames[currentMonth-1], currentYear)
        binding.currentPeriodValueId.text = currentPeriodValue

        val plus_drawable = ContextCompat.getDrawable(this, R.drawable.baseline_add_24)
        val minus_drawable = ContextCompat.getDrawable(this, R.drawable.baseline_remove_24)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val transactionFlow = expenseInfoViewModel.getTransactionStatus(currentYear, currentMonth)

                // Combine the transaction data and target expense
                combine(
                    preferencesManager.getShowEarningInReportCardStatus, // Flow<Boolean>
                    preferencesManager.getShowTransferFromInReportCardStatus,
                    transactionFlow,
                    preferencesManager.targetMonthlyExpenseFlow
                ) {  showEarnings, showTransferFrom, transactions, expense ->
                    // Process transaction data
                    var earnedAmt = 0.0
                    var spendAmt = 0.0
                    var transferToAmt = 0.0
                    var transferFromAmt = 0.0

                    if (transactions.isNotEmpty()) {
                        for (transaction in transactions) {
                            when (transaction.transactionType) {
                                ExpenseType.EARNED -> {
                                    earnedAmt += transaction.totalAmount // Accumulate Earned Amount
                                }
                                ExpenseType.SPEND -> {
                                    spendAmt += transaction.totalAmount // Accumulate Spend Amount
                                }
                                ExpenseType.TRANSFER -> {
                                    when (transaction.transferType) {
                                        TransferType.TO -> {
                                            transferToAmt += transaction.totalAmount // Accumulate Transfer-To Amount
                                        }
                                        TransferType.FROM -> {
                                            transferFromAmt += transaction.totalAmount // Accumulate Transfer-From Amount
                                        }
                                        null -> {
                                            transferToAmt += 0.0
                                            transferFromAmt += 0.0
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Update the spending amounts array
                    spendingAmounts[0] = spendAmt
                    spendingAmounts[1] = transferToAmt

                    // Calculate the final amount
                    var finalAmt = earnedAmt + transferFromAmt - spendAmt - transferToAmt
                    if (!showEarnings) {
                        finalAmt -= earnedAmt
                        binding.EarnedTitleValueId.text = "N/A"
                    } else {
                        val earnedAmtString= String.format(Locale.getDefault(),"%.0f", earnedAmt)
                        binding.EarnedTitleValueId.text = earnedAmtString
                    }
                    if (!showTransferFrom) {
                        finalAmt -= transferFromAmt
                        binding.TransferFromTitleValueId.text = "N/A"
                    } else {
                        val transferAmtString= String.format(Locale.getDefault(),"%.0f", transferFromAmt)
                        binding.TransferFromTitleValueId.text = transferAmtString
                    }

                    var factor : Double = spendAmt / expense
                    factor = if (factor >= 1) 1.0 else factor
                    val colorWithAlpha = if (factor > 0.6) ColorUtils.setAlphaComponent(color, (factor * 255).toInt()) else Color.argb(0,0,0,0)
                    binding.warningSignalId.imageTintList = ColorStateList.valueOf(colorWithAlpha)

                    val spendAmtTitle = String.format(Locale.getDefault(),"%.0f", spendAmt)
                    binding.SpendTitleValueId.text = spendAmtTitle

                    val transferToAmtTitle = String.format(Locale.getDefault(),"%.0f", transferToAmt)
                    binding.TransferToTitleValueId.text = transferToAmtTitle

                    val finalSumAmtString = String.format(Locale.getDefault(),"%.0f", abs(finalAmt))
                    binding.finalSumValueId.text = finalSumAmtString
                    binding.finalSumSignId.setImageResource(if (finalAmt > 0) R.drawable.baseline_add_24 else R.drawable.baseline_remove_24)
//                    binding.finalSumValueId.setCompoundDrawablesRelativeWithIntrinsicBounds(
//                        if (finalAmt > 0) plus_drawable else minus_drawable,
//                        null,
//                        null,
//                        null
//                    )

                }.collect{ }
            }
        }
        setupFABbuttons()
        setupPresetRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.primary_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.all_category_list_menu -> {
                val intent = Intent(this, CategoryList::class.java)
                startActivity(intent)
                true
            }
            R.id.target_menu -> {
                val intent = Intent(this, Preferences::class.java)
                startActivity(intent)
                true
            }
            R.id.manage_preset_list_menu -> {
                val intent = Intent(this, ManagePresets::class.java)
                //val intent = Intent(this, FullRecord::class.java)
                startActivity(intent)
                true
            }
            R.id.all_clubs_list_menu -> {
                val intent = Intent(this, ManageClubs::class.java)
                //val intent = Intent(this, FullRecord::class.java)
                startActivity(intent)
                true
            }
            R.id.onboarding_view_id -> {
                val intent = Intent(this, Onboarding::class.java)
                //val intent = Intent(this, FullRecord::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupFABbuttons() {
        val button : FloatingActionButton = findViewById(R.id.buttonId)
        button.setOnClickListener {
            // Create an Intent to start SecondActivity
            val intent = Intent(this, AddTransaction::class.java)
            startActivity(intent)
        }
        binding.fullRecordId.setOnClickListener{
            val intent = Intent(this, FullRecord::class.java)
            startActivity(intent)
        }
        binding.allCategoryRecordId.setOnClickListener {
            val intent = Intent(this, CategoryWiseTransactions::class.java)
            intent.putExtra("CurrentYear", currentYear)
            intent.putExtra("CurrentMonth", currentMonth)
            startActivity(intent)
        }
    }

    private fun setupPresetRecyclerView() {

        presetAdapter = PresetInfoAdapter(this, false)

        binding.presetRecyclerViewId.apply {
            setHasFixedSize(true)
            adapter = this@MainActivity.presetAdapter
            layoutManager = GridLayoutManager(this@MainActivity, 2, GridLayoutManager.VERTICAL, false)
        }

        lifecycleScope.launch {
            presetInfoViewModel.allPreset.collectLatest { presetList ->
                presetAdapter.submitList(presetList)

                if (presetList.isEmpty()) {
                    // Hide RecyclerView and show placeholder
                    binding.presetRecyclerViewId.visibility = View.GONE
                    binding.placeholderPresetImageViewId.visibility = View.VISIBLE
                } else {
                    // Show RecyclerView and hide placeholder
                    binding.presetRecyclerViewId.visibility = View.VISIBLE
                    binding.placeholderPresetImageViewId.visibility = View.GONE
                }
            }
        }
    }
}