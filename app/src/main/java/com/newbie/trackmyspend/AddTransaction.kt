package com.newbie.trackmyspend

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.newbie.trackmyspend.adapters.CategoryAdapter
import com.newbie.trackmyspend.database.CategoryInfoViewModel
import com.newbie.trackmyspend.database.ExpenseInfoViewModel
import com.newbie.trackmyspend.database.PresetInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityAddTransactionBinding
import com.newbie.trackmyspend.model.CategoryInfo
import com.newbie.trackmyspend.model.ExpenseInfo
import com.newbie.trackmyspend.model.PresetInfo
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AddTransaction : AppCompatActivity() {

    private lateinit var binding : ActivityAddTransactionBinding
    private lateinit var expenseInfoViewModel: ExpenseInfoViewModel
    private lateinit var categoryInfoViewModel: CategoryInfoViewModel
    private lateinit var presetInfoViewModel: PresetInfoViewModel

    private lateinit var categoryAdapter: CategoryAdapter
    private var currentCategoryPos : Int = -1
    private var currentCategoryId : Int = -1
    private var categoriesList: List<CategoryInfo> = emptyList()
    private var previousExpenseInfo : ExpenseInfo ?= null
    private var previousPresetInfo : PresetInfo ?= null

    private var currentYear = -1
    private var currentMonth = -1
    private var currentDate = -1
    private var currentHour = -1
    private var currentMinute = -1

    private var expenseType = ExpenseType.SPEND
    private var transferType = TransferType.FROM
    private var updatePresetFlag : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseInfoViewModel = ViewModelProvider(this)[ExpenseInfoViewModel::class.java]
        categoryInfoViewModel = ViewModelProvider(this)[CategoryInfoViewModel::class.java]
        presetInfoViewModel = ViewModelProvider(this)[PresetInfoViewModel::class.java]

        setActionBar()

        retrieveIntentData()
        setPreviousState()

        setToggleButtonGroup()
        setEditText()
        setToFromToggleButton()

        setInitialDateAndTime()
        setDateAndTimePicker()
        setRecyclerView()
        setAddCategoryButton()
        setButtonStates()

        binding.savePresetInfoId.setOnClickListener {
            val expenseAmount = binding.expenseAmountId.text.trim().toString()
            val amount = if (expenseAmount.isEmpty()) 0.0 else expenseAmount.toDouble()

            if (! updatePresetFlag && validateInputs(amount)) {
                showCustomPresetDialog(this)
            } else if (updatePresetFlag && validateInputs(amount)) {
                savePresetInfo(previousPresetInfo!!.presetName)
            }
            else {
                Toast.makeText(this, "Invalid amount" , Toast.LENGTH_SHORT).show()
            }
        }
        binding.saveExpenseInfoId.setOnClickListener {
            saveExpenseInfo()
        }
        binding.deletePresetInfoId.setOnClickListener {
            deletePresetInfoFromDB()
        }
        binding.deleteExpenseInfoId.setOnClickListener {
            deleteRecordInfoFromDB()
        }
    }

    private fun setButtonStates() {
        if (previousPresetInfo != null) {
            if (! updatePresetFlag) {
                binding.savePresetInfoId.visibility = View.GONE
                binding.deleteExpenseInfoId.visibility = View.VISIBLE
                binding.saveExpenseInfoId.visibility = View.VISIBLE
                binding.deletePresetInfoId.visibility = View.GONE
            } else {
                // modify the preset
                binding.savePresetInfoId.visibility = View.VISIBLE
                binding.deleteExpenseInfoId.visibility = View.GONE
                binding.saveExpenseInfoId.visibility = View.GONE
                binding.deletePresetInfoId.visibility = View.VISIBLE
            }
        } else if (previousExpenseInfo != null) {
            // old transaction
            binding.savePresetInfoId.visibility = View.GONE
            binding.deleteExpenseInfoId.visibility = View.VISIBLE
            binding.saveExpenseInfoId.visibility = View.VISIBLE
            binding.deletePresetInfoId.visibility = View.GONE
        } else {
            binding.deletePresetInfoId.visibility = View.GONE
            binding.deleteExpenseInfoId.visibility = View.GONE
            binding.savePresetInfoId.visibility = View.VISIBLE
            binding.saveExpenseInfoId.visibility = View.VISIBLE
        }
    }

    private fun retrieveIntentData() {

        intent?.let {
            updatePresetFlag = false
            if (it.hasExtra("TRANSACTION_ID") && it.hasExtra("TRANSACTION_AMOUNT")) {
                val transactionTypeString = it.getStringExtra("TRANSACTION_TYPE")
                val transactionType = if (transactionTypeString != null) {
                    ExpenseType.valueOf(transactionTypeString)
                } else {
                    ExpenseType.SPEND // Default value in case of a null
                }
                val transferTypeString = it.getStringExtra("TRANSACTION_TRANSFERTYPE")
                val transferType = if (transferTypeString != null) {
                    TransferType.valueOf(transferTypeString)
                } else {
                    TransferType.TO // Default value in case of a null
                }
                val transferInfo = it.getStringExtra("TRANSACTION_TRANSFERINFO") ?: ""

                var description = ""
                if (it.hasExtra("TRANSACTION_DESCRIPTION")) {
                    description = it.getStringExtra("TRANSACTION_DESCRIPTION") ?: ""
                }
                var datetimeLong = 0L
                if (it.hasExtra("TRANSACTION_DATETIME")) {
                    datetimeLong = it.getLongExtra("TRANSACTION_DATETIME", 0L)
                }
                var yearValue = 0
                if (it.hasExtra("TRANSACTION_YEAR")) {
                    yearValue = it.getIntExtra("TRANSACTION_YEAR", 2025)
                }
                var monthValue = 0
                if (it.hasExtra("TRANSACTION_MONTH")) {
                    monthValue = it.getIntExtra("TRANSACTION_MONTH", 1)
                }

                if (it.hasExtra("THIS_IS_PRESET")) {
                    val presetTitle : String = it.getStringExtra("PRESET_NAME").toString()
                    previousPresetInfo = PresetInfo(
                        it.getLongExtra("TRANSACTION_ID", 0L),
                        presetTitle,
                        transactionType,
                        it.getDoubleExtra("TRANSACTION_AMOUNT", 0.0),
                        it.getIntExtra("TRANSACTION_CATEGORY", -1),
                        transferType,
                        transferInfo
                    )
                    previousExpenseInfo = null
                    if (it.hasExtra("MODIFY_PRESET") && it.getBooleanExtra("MODIFY_PRESET", false)) {
                        updatePresetFlag = true
                    }
                }
                else {
                    previousPresetInfo = null
                    previousExpenseInfo = ExpenseInfo(
                        it.getLongExtra("TRANSACTION_ID", 0L),
                        transactionType,
                        it.getDoubleExtra("TRANSACTION_AMOUNT", 0.0),
                        it.getIntExtra("TRANSACTION_CATEGORY", -1),
                        transferType,
                        transferInfo,
                        description,
                        datetimeLong,
                        yearValue,
                        monthValue,
                    )
                }
                if (transactionType == ExpenseType.TRANSFER) {
                    binding.toFromLinearLayoutId.visibility = View.VISIBLE
                } else {
                    binding.toFromLinearLayoutId.visibility = View.INVISIBLE
                }
            } else {
                previousExpenseInfo = null
                previousPresetInfo = null
                updatePresetFlag = false
            }
        }
    }

    private fun setActionBar() {
        binding.presetNameTitle.visibility = View.INVISIBLE
        binding.dividerTopId.visibility = View.INVISIBLE

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

    private fun setPreviousState() {
        if (previousExpenseInfo != null) {
            //Toast.makeText(this, "let's see ", Toast.LENGTH_SHORT).show()
            supportActionBar?.title = "Update Record"
            binding.presetNameTitle.visibility = View.INVISIBLE
            binding.dividerTopId.visibility = View.INVISIBLE
            when(previousExpenseInfo!!.transactionType) {
                ExpenseType.SPEND -> {
                    expenseType = ExpenseType.SPEND
                    binding.toggleButtonGroup.check(R.id.spendToggleButtonId)
                    binding.toFromId.visibility = View.INVISIBLE
                    binding.transferToFromPersonId.visibility = View.INVISIBLE
                }
                ExpenseType.TRANSFER -> {
                    expenseType = ExpenseType.TRANSFER
                    binding.toFromId.visibility = View.VISIBLE
                    binding.transferToFromPersonId.visibility = View.VISIBLE
                    binding.toggleButtonGroup.check(R.id.transferToggleButtonId)
                    binding.transferToFromPersonId.setText(previousExpenseInfo!!.transferInfo)
                    binding.toFromId.isChecked = previousExpenseInfo!!.transferType == TransferType.TO
                    transferType = previousExpenseInfo!!.transferType ?: TransferType.FROM
                }
                ExpenseType.EARNED -> {
                    binding.toggleButtonGroup.check(R.id.earnedToggleButtonId)
                    expenseType = ExpenseType.EARNED
                    binding.toFromId.visibility = View.INVISIBLE
                    binding.transferToFromPersonId.visibility = View.INVISIBLE
                }
            }
            val amountString : String= String.format(Locale.getDefault(),"%.0f", previousExpenseInfo!!.amount)
            binding.expenseAmountId.setText(amountString)
            currentCategoryId = previousExpenseInfo!!.category
            binding.expenseDescriptionEditText.setText(previousExpenseInfo!!.description.toString())
        } else if (previousPresetInfo != null) {
            if (updatePresetFlag) {
                supportActionBar?.title = "Modify "
                binding.presetNameTitle.text = previousPresetInfo!!.presetName.toString()
                binding.presetNameTitle.visibility = View.VISIBLE
                binding.dividerTopId.visibility = View.VISIBLE
            } else {
                supportActionBar?.title = "Add Record"
                binding.presetNameTitle.text = ""
                binding.presetNameTitle.visibility = View.INVISIBLE
                binding.dividerTopId.visibility = View.INVISIBLE
            }
            when(previousPresetInfo!!.transactionType) {
                ExpenseType.SPEND -> {
                    expenseType = ExpenseType.SPEND
                    binding.toggleButtonGroup.check(R.id.spendToggleButtonId)
                    binding.toFromId.visibility = View.INVISIBLE
                    binding.transferToFromPersonId.visibility = View.INVISIBLE
                }
                ExpenseType.TRANSFER -> {
                    expenseType = ExpenseType.TRANSFER
                    binding.toFromId.visibility = View.VISIBLE
                    binding.transferToFromPersonId.visibility = View.VISIBLE
                    binding.toggleButtonGroup.check(R.id.transferToggleButtonId)
                    binding.transferToFromPersonId.setText(previousPresetInfo!!.transferInfo)
                    binding.toFromId.isChecked = previousPresetInfo!!.transferType == TransferType.TO
                    transferType = previousPresetInfo!!.transferType ?: TransferType.FROM
                }
                ExpenseType.EARNED -> {
                    binding.toggleButtonGroup.check(R.id.earnedToggleButtonId)
                    expenseType = ExpenseType.EARNED
                    binding.toFromId.visibility = View.INVISIBLE
                    binding.transferToFromPersonId.visibility = View.INVISIBLE
                }
            }
            val amountString = String.format(Locale.getDefault(),"%.0f", previousPresetInfo!!.amount)
            binding.expenseAmountId.setText(amountString)
            currentCategoryId = previousPresetInfo!!.category
        }
    }

    private fun setToggleButtonGroup() {
        binding.toggleButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.spendToggleButtonId -> {
                        binding.toFromLinearLayoutId.visibility = View.INVISIBLE
                        expenseType = ExpenseType.SPEND
                    }
                    R.id.earnedToggleButtonId -> {
                        expenseType = ExpenseType.EARNED
                        binding.toFromLinearLayoutId.visibility = View.INVISIBLE
                    }
                    R.id.transferToggleButtonId -> {
                        expenseType = ExpenseType.TRANSFER
                        binding.toFromLinearLayoutId.visibility = View.VISIBLE
                    }
                }
                binding.expenseAmountId.clearFocus()
                binding.transferToFromPersonId.clearFocus()
                binding.expenseDescriptionEditText.clearFocus()
            }
        }
    }

    private fun setEditText(){
        binding.expenseAmountId.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.expenseAmountId.windowToken, 0)
                binding.expenseAmountId.clearFocus()
                true
            } else {
                false
            }
        }
        binding.transferToFromPersonId.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.transferToFromPersonId.windowToken, 0)
                binding.transferToFromPersonId.clearFocus()
                true
            } else {
                false
            }
        }
        binding.expenseDescriptionEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.expenseDescriptionEditText.windowToken, 0)
                binding.expenseDescriptionEditText.clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun setToFromToggleButton() {
        binding.toFromId.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            transferType = if (!isChecked) {
                TransferType.FROM
            } else {
                TransferType.TO
            }
        }
    }

    private fun setRecyclerView(){
        categoryAdapter = CategoryAdapter(
            this@AddTransaction,
            {   selectedPositionId ->
                //Toast.makeText(this, "selected category = " + categories[selectedPosition], Toast.LENGTH_SHORT).show()
                currentCategoryPos = selectedPositionId //categoriesList[selectedPositionId].id
            },
            -1)
        //Toast.makeText(this, "current pos" + currentCategoryPos, Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            categoryInfoViewModel.allCategory.collectLatest { category ->
                val selectedIndex = category.indexOfFirst { it.id == currentCategoryId }
                categoryAdapter.setSelectedIndex(selectedIndex)
                currentCategoryPos = selectedIndex
                categoriesList = category

                categoryAdapter.submitList(category)
            }
        }
        binding.iconRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@AddTransaction, 2, GridLayoutManager.HORIZONTAL, false)
            adapter = this@AddTransaction.categoryAdapter
        }
    }

    private fun setInitialDateAndTime(){

        val calendar = Calendar.getInstance()
        if (previousExpenseInfo == null) {
            val todayDate = Date()
            calendar.time = todayDate
        } else {
//            currentYear = previousExpenseInfo!!.year
//            currentMonth = previousExpenseInfo!!.month
            calendar.timeInMillis = previousExpenseInfo!!.dateTime
        }
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth =
            calendar.get(Calendar.MONTH) + 1  // Months are 0-based, so add 1 to get 1-based month
        currentDate = calendar.get(Calendar.DAY_OF_MONTH)
        currentHour = calendar.get(Calendar.HOUR_OF_DAY)  // For 24-hour format
        currentMinute = calendar.get(Calendar.MINUTE)
        val formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", currentDate, currentMonth, currentYear)
        val formattedTime = String.format(Locale.getDefault(), "%02d : %02d", currentHour, currentMinute)

        binding.datePickerTextId.text = formattedDate
        binding.timePickerTextId.text = formattedTime
    }

    private fun setDateAndTimePicker() {
        val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        binding.datePickerTextId.setOnClickListener { v: View? ->
            binding.expenseAmountId.clearFocus()
            binding.transferToFromPersonId.clearFocus()
            binding.expenseDescriptionEditText.clearFocus()
            materialDatePicker.show(supportFragmentManager, materialDatePicker.toString())
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection: Long? ->
            // Convert selection (Long timestamp) to a formatted date
            val calendar = Calendar.getInstance()
            if (selection != null) {
                calendar.timeInMillis = selection
                currentDate = calendar.get(Calendar.DAY_OF_MONTH)
                currentMonth = calendar.get(Calendar.MONTH) + 1
                currentYear = calendar.get(Calendar.YEAR)
                val formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", currentDate, currentMonth, currentYear)
                binding.datePickerTextId.text = formattedDate
            }
            //val selectedDate = materialDatePicker.headerText
        }

        // Time picker
        val materialTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H) // Use 24-hour format (default is 12-hour)
            .setHour(currentHour) // Set the default hour
            .setMinute(currentMinute) // Set the default minute
            .setTitleText("Select Time")
            .build()

        binding.timePickerTextId.setOnClickListener { v: View? ->
            binding.expenseAmountId.clearFocus()
            binding.transferToFromPersonId.clearFocus()
            binding.expenseDescriptionEditText.clearFocus()
            materialTimePicker.show(supportFragmentManager, materialTimePicker.toString())
        }
        // Listen for selected time
        materialTimePicker.addOnPositiveButtonClickListener { view: View? ->
            val hour = materialTimePicker.hour
            val minute = materialTimePicker.minute

            // Format the selected time
            val selectedTime = String.format(Locale.getDefault(), "%02d : %02d", hour, minute)
            binding.timePickerTextId.text = selectedTime
        }
    }

    private fun validateInputs(expenseAmt: Double): Boolean {
        if (expenseAmt > 0) return true
        return false
    }

    private fun saveExpenseInfo() {
        val expenseAmount = binding.expenseAmountId.text.trim().toString()
        val amount = if (expenseAmount.isEmpty()) 0.0 else expenseAmount.toDouble()
        val description = binding.expenseDescriptionEditText.text?.trim().toString()
        val transferPerson = binding.transferToFromPersonId.text?.trim().toString()

        if (!validateInputs(amount)) {
            Snackbar.make(binding.mainId, "Invalid Amount", Snackbar.LENGTH_SHORT).show()
            return
        }

        val categoryId = if (currentCategoryPos == -1) -1 else categoriesList[currentCategoryPos].id

        var id : Long = 0L
        if (previousExpenseInfo != null) {
            id = previousExpenseInfo!!.id
        }

        val expenseInfo = ExpenseInfo(
            id,
            expenseType,
            amount,
            categoryId,
            if (expenseType == ExpenseType.TRANSFER) transferType else null,
            transferPerson,
            description,
            getTimeInMillis(currentYear, currentMonth -1, currentDate, currentHour, currentMinute),
            currentYear,
            currentMonth
        )
        expenseInfoViewModel.saveExpense(expenseInfo)

        finish()
    }

    fun getTimeInMillis(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month) // Month is 0-based, so no adjustment needed
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    private fun setAddCategoryButton() {
        binding.addCategoryButtonId.setOnClickListener {
            // Create an Intent to start SecondActivity
            val intent = Intent(this, CreateCategory::class.java)
            startActivity(intent)
        }
    }

    private fun savePresetInfo(presetTitle : String) : Boolean {
        val expenseAmount = binding.expenseAmountId.text.trim().toString()
        val amount = if (expenseAmount.isEmpty()) 0.0 else expenseAmount.toDouble()
        val transferPerson = binding.transferToFromPersonId.text?.trim().toString()

        if (!validateInputs(amount)){
            binding.expenseAmountId.error = "Invalid Amount"
            Snackbar.make(binding.mainId, "Invalid Amount", Snackbar.LENGTH_SHORT).show()
            return false
        }

        if (presetTitle.isEmpty()) {
            //dialogView.findViewById<EditText>(R.id.presetTitleEditTextId).error = "Empty Preset"
            Snackbar.make(binding.mainId, "Empty Preset Name", Snackbar.LENGTH_SHORT).show()
            return false
        }

        val categoryId = if (currentCategoryPos == -1) -1 else categoriesList[currentCategoryPos.toInt()].id

        val presetInfo : PresetInfo?
        if (previousPresetInfo == null) {
            presetInfo = PresetInfo(
                0L,
                presetTitle,
                expenseType,
                amount,
                categoryId,
                if (expenseType == ExpenseType.TRANSFER) transferType else null,
                transferPerson
            )
        } else {
            presetInfo = PresetInfo(
                previousPresetInfo!!.id,
                previousPresetInfo!!.presetName,
                expenseType,
                amount,
                categoryId,
                if (expenseType == ExpenseType.TRANSFER) transferType else null,
                transferPerson
            )
        }
        presetInfoViewModel.saveOrModifyPreset(presetInfo)
        if (updatePresetFlag) {
            finish()
        } else {
            Snackbar.make(binding.mainId, "Preset Saved 😊", Snackbar.LENGTH_SHORT).show()
        }
        return true
    }

    private fun showCustomPresetDialog(context : Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.preset_save_dialog, null)

        val expenseTypeValue = when(expenseType){
            ExpenseType.SPEND -> "SPEND"
            ExpenseType.EARNED -> "EARNED"
            ExpenseType.TRANSFER -> "TRANSFER"
            else -> "NULL"
        }
        val transferType = when(expenseType) {
            ExpenseType.TRANSFER -> {
                if (transferType == TransferType.TO) "TO" else "FROM"
            }
            else -> ""
        }
        val transferPerson = if (expenseType == ExpenseType.TRANSFER) binding.transferToFromPersonId.text else ""

        dialogView.findViewById<TextView>(R.id.transactionTypeValueId).text = expenseTypeValue
        val transactionTypeTextView = dialogView.findViewById<TextView>(R.id.transactionTypeTransferValueId)
        val transferTypeTextView = dialogView.findViewById<TextView>(R.id.transactionTypeTransferTypeValueId)

        if (transferPerson.isNotEmpty()) {
            transactionTypeTextView.text = transferPerson
            transferTypeTextView.text = transferType
            transactionTypeTextView.visibility = View.VISIBLE
            transferTypeTextView.visibility = View.VISIBLE
        } else {
            transactionTypeTextView.visibility = View.GONE
            transferTypeTextView.visibility = View.GONE
        }

        dialogView.findViewById<TextView>(R.id.transactionAmountValueId).text = binding.expenseAmountId.text
        dialogView.findViewById<TextView>(R.id.transactionCategoryValueId).text = if (currentCategoryPos >= 0) categoriesList[currentCategoryPos].title else "-"

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<TextView>(R.id.doPresetSavingId).setOnClickListener {
            val edittext = dialogView.findViewById<EditText>(R.id.presetTitleEditTextId)
            val title = edittext.text.trim().toString()
            if (title.isEmpty()) {
                edittext.error = "Empty Title"
            }
            if (title.isNotEmpty() && savePresetInfo(title)) {
                dialog.dismiss()
            }

        }
        dialogView.findViewById<TextView>(R.id.cancelPresetSavingId).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deletePresetInfoFromDB() {
        previousPresetInfo?.let {
            presetInfoViewModel.clearParticularPreset(it)
            Toast.makeText(this, "Preset Deleted 🔥🔥", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun deleteRecordInfoFromDB() {
        previousExpenseInfo?.let {
            expenseInfoViewModel.removeExpense(it)
            Toast.makeText(this, "Record Deleted 🔥🔥", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}