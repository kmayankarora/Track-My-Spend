package com.newbie.trackmyspend

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.newbie.trackmyspend.adapters.CategoryAdapter
import com.newbie.trackmyspend.adapters.ClubAdapter
import com.newbie.trackmyspend.database.CategoryInfoViewModel
import com.newbie.trackmyspend.database.ClubInfoViewModel
import com.newbie.trackmyspend.database.ExpenseInfoViewModel
import com.newbie.trackmyspend.database.PresetInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityAddTransactionBinding
import com.newbie.trackmyspend.model.CategoryInfo
import com.newbie.trackmyspend.model.ClubInfo
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
    private lateinit var clubInfoViewModel: ClubInfoViewModel


    //private var currentCategoryPos : Int = -1
    //private var currentCategoryId : Int = -1

    private var previousExpenseInfo : ExpenseInfo ?= null
    private var previousPresetInfo : PresetInfo ?= null

    private lateinit var categoryAdapter: CategoryAdapter
    private var categoriesList: List<CategoryInfo> = emptyList()
    private var temporarySelectedCategoryIndex : Int = -1
    private var currentSelectedCategoryIndex : Int = -1
    private var previouslySelectedCategoryIndex : Int = -1
    private var currentSelectedCategoryTableId : Long = -1

    private lateinit var clubAdapter: ClubAdapter
    private var clubList : List<ClubInfo> = emptyList()
    private var temporarySelectedClubIndex : Int = -1
    private var currentSelectedClubIndex : Int = -1
    private var previouslySelectedClubIndex : Int = -1
    private var currentSelectedClubTableId : Long = -1

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
        clubInfoViewModel = ViewModelProvider(this)[ClubInfoViewModel::class.java]

        setActionBar()
        retrieveIntentData()
        setPreviousState()

        setToggleButtonGroup()
        setEditText()
        setToFromToggleButton()

        setInitialDateAndTime()
        setDateAndTimePicker()
        //setRecyclerView()
        setButtonStates()

        setUpClubButtons()
        setUpCategoryButtons()
        createClubRecyclerView()
        createCategoryRecyclerView()

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
                    var clubId : Long = -1L
                    if (it.hasExtra("CLUB_ID")) {
                        clubId = it.getLongExtra("CLUB_ID", -1L)
                    }
                    previousPresetInfo = null
                    previousExpenseInfo = ExpenseInfo(
                        it.getLongExtra("TRANSACTION_ID", 0L),
                        transactionType,
                        it.getDoubleExtra("TRANSACTION_AMOUNT", 0.0),
                        it.getIntExtra("TRANSACTION_CATEGORY", -1),
                        transferType,
                        transferInfo,
                        description,
                        if (clubId < 0) null else clubId,
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
        binding.presetNameTitle.visibility = View.GONE
        binding.dividerTopId.visibility = View.GONE

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
            supportActionBar?.title = "Update Record"
            binding.presetNameTitle.visibility = View.GONE
            binding.dividerTopId.visibility = View.GONE
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
            if (previousExpenseInfo!!.amount > 0) binding.saveExpenseInfoId.isEnabled = true
            currentSelectedCategoryTableId = if (previousExpenseInfo!!.category != null) previousExpenseInfo!!.category.toLong() else -1L
            binding.expenseDescriptionEditText.setText(previousExpenseInfo!!.description.toString())
            currentSelectedClubTableId = if (previousExpenseInfo!!.clubId == null) -1 else previousExpenseInfo!!.clubId!!
        } else if (previousPresetInfo != null) {
            if (updatePresetFlag) {
                supportActionBar?.title = "Modify "
                binding.presetNameTitle.text = previousPresetInfo!!.presetName.toString()
                binding.presetNameTitle.visibility = View.VISIBLE
                binding.dividerTopId.visibility = View.VISIBLE
            } else {
                supportActionBar?.title = "Add Record"
                binding.presetNameTitle.text = ""
                binding.presetNameTitle.visibility = View.GONE
                binding.dividerTopId.visibility = View.GONE
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
            if (previousPresetInfo!!.amount > 0) binding.saveExpenseInfoId.isEnabled = true
            currentSelectedCategoryTableId = previousPresetInfo!!.category.toLong()
            currentSelectedClubTableId = -1L
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

    /*private fun setRecyclerView(){
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
    }*/

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

        val categoryId = if (currentSelectedCategoryIndex == -1) -1 else categoriesList[currentSelectedCategoryIndex].id

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
            if (currentSelectedClubIndex != -1) clubList[currentSelectedClubIndex].id else null,
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

        val categoryId = if (currentSelectedCategoryIndex == -1) -1 else categoriesList[currentSelectedCategoryIndex.toInt()].id

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
        dialogView.findViewById<TextView>(R.id.transactionCategoryValueId).text = if (currentSelectedCategoryIndex >= 0) categoriesList[currentSelectedCategoryIndex].title else "-"

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

    private fun setUpClubButtons(){
        binding.addClubValueId.setOnClickListener {
            // Create an Intent to start SecondActivity
            val intent = Intent(this, CreateClub::class.java)
            startActivity(intent)
        }
        binding.clubSelectedValueViewId.setOnClickListener {
            setClubDisplayDialog(this@AddTransaction)
        }
        binding.clearClubValueId.setOnClickListener {
            currentSelectedClubIndex = -1
            previouslySelectedClubIndex = -1
            binding.clubTextValueId2.visibility = View.INVISIBLE
            binding.clubTextValueId1.visibility = View.INVISIBLE
            binding.clubIconImageView.visibility = View.INVISIBLE
            binding.clubNoneTitleId.visibility = View.VISIBLE
            binding.clearClubValueId.visibility = View.GONE
            binding.addClubValueId.visibility = View.VISIBLE
        }
    }

    private fun setClubDisplayDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.club_selector_dialog, null)
        val clubRecyclerView : RecyclerView = dialogView.findViewById<RecyclerView>(R.id.clubRecyclerViewId)

        clubRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@AddTransaction, 1, GridLayoutManager.VERTICAL, false)
            adapter = this@AddTransaction.clubAdapter
            clubAdapter.setSelectedIndex(currentSelectedClubIndex)
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        dialogView.findViewById<TextView>(R.id.selectClubSelectionId).setOnClickListener {
            if (temporarySelectedClubIndex == -1) {
                currentSelectedClubIndex = temporarySelectedClubIndex
                previouslySelectedClubIndex = temporarySelectedClubIndex
                binding.clubTextValueId2.visibility = View.INVISIBLE
                binding.clubTextValueId1.visibility = View.INVISIBLE
                binding.clubIconImageView.visibility = View.INVISIBLE
                binding.clubNoneTitleId.visibility = View.VISIBLE
                binding.clearClubValueId.visibility = View.GONE
                binding.addClubValueId.visibility = View.VISIBLE
            } else {
                previouslySelectedClubIndex = temporarySelectedClubIndex
                currentSelectedClubIndex = temporarySelectedClubIndex
                binding.clubIconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(clubList[currentSelectedClubIndex].hexColorCode))
                binding.clubTextValueId1.text = clubList[currentSelectedClubIndex].title
                binding.clubTextValueId2.text = if (clubList[currentSelectedClubIndex].subtitle != null) clubList[currentSelectedClubIndex].subtitle else "----"
                //binding.clubSelectedValueViewId.visibility = View.VISIBLE
                binding.clubTextValueId2.visibility = if (clubList[currentSelectedClubIndex].subtitle != null) View.VISIBLE else View.GONE
                binding.clubTextValueId1.visibility = View.VISIBLE
                binding.clubIconImageView.visibility = View.VISIBLE
                binding.clubNoneTitleId.visibility = View.INVISIBLE
                binding.clearClubValueId.visibility = View.VISIBLE
                binding.addClubValueId.visibility = View.GONE
            }
            dialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.cancelClubSelectionId).setOnClickListener {
            currentSelectedClubIndex = previouslySelectedClubIndex
            temporarySelectedClubIndex = previouslySelectedClubIndex
            if (temporarySelectedClubIndex == -1) {
                binding.clubTextValueId2.visibility = View.INVISIBLE
                binding.clubTextValueId1.visibility = View.INVISIBLE
                binding.clubIconImageView.visibility = View.INVISIBLE
                binding.clubNoneTitleId.visibility = View.VISIBLE
                binding.clearClubValueId.visibility = View.GONE
                binding.addClubValueId.visibility = View.VISIBLE
            } else {
                binding.clubIconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(clubList[currentSelectedClubIndex].hexColorCode))
                binding.clubTextValueId1.text = clubList[currentSelectedClubIndex].title
                binding.clubTextValueId2.visibility = View.VISIBLE
                binding.clubTextValueId1.visibility = View.VISIBLE
                binding.clubIconImageView.visibility = View.VISIBLE
                binding.clubNoneTitleId.visibility = View.INVISIBLE
                binding.clearClubValueId.visibility = View.VISIBLE
                binding.addClubValueId.visibility = View.GONE
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun createClubRecyclerView() {
        clubAdapter = ClubAdapter(
            this@AddTransaction,
            {   selectedPositionId ->
                //Toast.makeText(this, "selected category = " + categories[selectedPosition], Toast.LENGTH_SHORT).show()
                temporarySelectedClubIndex = selectedPositionId //categoriesList[selectedPositionId].id
            },
            -1)
        lifecycleScope.launch {
            clubInfoViewModel.allClubs.collectLatest { clubs ->
                //Log.i("clubs", clubs.toString())
                val selectedIndex = clubs.indexOfFirst { it.id == currentSelectedClubTableId }
                //Log.i("Pocket chnage", "currentSelected  = " + currentSelectedClubTableId)
                //Log.i("chekc ohh AOT" , "selectedIndex = " + selectedIndex + ", previouslyselectedindex = " + previouslySelectedClubIndex)
                clubAdapter.setSelectedIndex(selectedIndex)
                currentSelectedClubIndex = selectedIndex
                previouslySelectedClubIndex = selectedIndex
                temporarySelectedClubIndex = selectedIndex
                clubList = clubs

                if (temporarySelectedClubIndex == -1) {
                    binding.clubTextValueId2.visibility = View.INVISIBLE
                    binding.clubTextValueId1.visibility = View.INVISIBLE
                    binding.clubIconImageView.visibility = View.INVISIBLE
                    binding.clubNoneTitleId.visibility = View.VISIBLE
                    binding.clearClubValueId.visibility = View.GONE
                    binding.addClubValueId.visibility = View.VISIBLE
                } else {
                    binding.clubIconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(clubList[currentSelectedClubIndex].hexColorCode))
                    binding.clubTextValueId1.text = clubList[currentSelectedClubIndex].title
                    binding.clubTextValueId2.text = if (clubList[currentSelectedClubIndex].subtitle != null) clubList[currentSelectedClubIndex].subtitle else ""
                    binding.clubTextValueId2.visibility = View.VISIBLE
                    binding.clubTextValueId1.visibility = View.VISIBLE
                    binding.clubIconImageView.visibility = View.VISIBLE
                    binding.clubNoneTitleId.visibility = View.INVISIBLE
                    binding.clearClubValueId.visibility = View.VISIBLE
                    binding.addClubValueId.visibility = View.GONE
                }

                clubAdapter.submitList(clubList)
            }
        }

    }

    private fun setUpCategoryButtons() {
        binding.addCategoryButtonId.setOnClickListener {
            // Create an Intent to start SecondActivity
            val intent = Intent(this, CreateCategory::class.java)
            startActivity(intent)
        }
        binding.categorySelectedValueViewId.setOnClickListener {
            setCategoryDisplayDialog(this@AddTransaction)
        }
        binding.clearCategoryButtonId.setOnClickListener {
            currentSelectedCategoryIndex = -1
            previouslySelectedCategoryIndex = -1
            binding.categoryTitleValueId.visibility = View.INVISIBLE
            binding.categoryIconImageView.visibility = View.INVISIBLE
            binding.categoryNoneId.visibility = View.VISIBLE
            binding.clearCategoryButtonId.visibility = View.GONE
            binding.addCategoryButtonId.visibility = View.VISIBLE
        }
    }

    private fun setCategoryDisplayDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.club_selector_dialog, null)
        val clubRecyclerView : RecyclerView = dialogView.findViewById<RecyclerView>(R.id.clubRecyclerViewId)
        val dialogViewTitleView : TextView = dialogView.findViewById(R.id.clubDialogTitleId)
        dialogViewTitleView.text = "Pick a Category"

        clubRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@AddTransaction, 1, GridLayoutManager.VERTICAL, false)
            adapter = this@AddTransaction.categoryAdapter
            categoryAdapter.setSelectedIndex(currentSelectedCategoryIndex)
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        dialogView.findViewById<TextView>(R.id.selectClubSelectionId).setOnClickListener {
            if (temporarySelectedCategoryIndex == -1) {
                currentSelectedCategoryIndex = temporarySelectedCategoryIndex
                previouslySelectedCategoryIndex = temporarySelectedCategoryIndex

                binding.categoryTitleValueId.visibility = View.INVISIBLE
                binding.categoryIconImageView.visibility = View.INVISIBLE
                binding.categoryNoneId.visibility = View.VISIBLE
                binding.clearCategoryButtonId.visibility = View.GONE
                binding.addCategoryButtonId.visibility = View.VISIBLE
            } else {
                previouslySelectedCategoryIndex = temporarySelectedCategoryIndex
                currentSelectedCategoryIndex = temporarySelectedCategoryIndex
                binding.categoryIconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(categoriesList[currentSelectedCategoryIndex].hexColorCode))
                binding.categoryTitleValueId.text = categoriesList[currentSelectedCategoryIndex].title
                //binding.clubTextValueId2.text = if (clubList[currentSelectedClubIndex].subtitle != null) clubList[currentSelectedClubIndex].subtitle else ""
                //binding.clubSelectedValueViewId.visibility = View.VISIBLE
                //binding.clubTextValueId2.visibility = View.VISIBLE
                binding.categoryTitleValueId.visibility = View.VISIBLE
                binding.categoryIconImageView.visibility = View.VISIBLE
                binding.categoryNoneId.visibility = View.INVISIBLE
                binding.clearCategoryButtonId.visibility = View.VISIBLE
                binding.addCategoryButtonId.visibility = View.GONE
            }
            dialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.cancelClubSelectionId).setOnClickListener {
            currentSelectedCategoryIndex = previouslySelectedCategoryIndex
            temporarySelectedCategoryIndex = previouslySelectedCategoryIndex
            if (temporarySelectedCategoryIndex == -1) {
                binding.categoryTitleValueId.visibility = View.INVISIBLE
                binding.categoryIconImageView.visibility = View.INVISIBLE
                binding.categoryNoneId.visibility = View.VISIBLE
                binding.clearCategoryButtonId.visibility = View.GONE
                binding.addCategoryButtonId.visibility = View.VISIBLE
            } else {
                binding.categoryIconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(categoriesList[currentSelectedCategoryIndex].hexColorCode))
                binding.categoryTitleValueId.text = categoriesList[currentSelectedCategoryIndex].title
                binding.categoryTitleValueId.visibility = View.VISIBLE
                binding.categoryIconImageView.visibility = View.VISIBLE
                binding.categoryNoneId.visibility = View.INVISIBLE
                binding.clearCategoryButtonId.visibility = View.VISIBLE
                binding.addCategoryButtonId.visibility = View.GONE
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun createCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter(
            this@AddTransaction,
            {   selectedPositionId ->
                //Toast.makeText(this, "selected category = " + categories[selectedPosition], Toast.LENGTH_SHORT).show()
                temporarySelectedCategoryIndex = selectedPositionId //categoriesList[selectedPositionId].id
            },
            -1)
        lifecycleScope.launch {
            categoryInfoViewModel.allCategory.collectLatest { category ->
                //Log.i("clubs", clubs.toString())
                val selectedIndex = category.indexOfFirst { it.id == currentSelectedCategoryTableId.toInt() }
                //Log.i("Pocket chnage", "currentSelected  = " + currentSelectedClubTableId)
                //Log.i("chekc ohh AOT" , "selectedIndex = " + selectedIndex + ", previouslyselectedindex = " + previouslySelectedClubIndex)
                categoryAdapter.setSelectedIndex(selectedIndex)
                currentSelectedCategoryIndex = selectedIndex
                previouslySelectedCategoryIndex = selectedIndex
                temporarySelectedCategoryIndex = selectedIndex
                categoriesList = category

                if (temporarySelectedCategoryIndex == -1) {
                    binding.categoryTitleValueId.visibility = View.INVISIBLE
                    binding.categoryIconImageView.visibility = View.INVISIBLE
                    binding.categoryNoneId.visibility = View.VISIBLE
                    binding.clearCategoryButtonId.visibility = View.GONE
                    binding.addCategoryButtonId.visibility = View.VISIBLE
                } else {
                    binding.categoryIconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(categoriesList[currentSelectedCategoryIndex].hexColorCode))
                    binding.categoryTitleValueId.text = categoriesList[currentSelectedCategoryIndex].title
                    binding.categoryTitleValueId.visibility = View.VISIBLE
                    binding.categoryIconImageView.visibility = View.VISIBLE
                    binding.categoryNoneId.visibility = View.INVISIBLE
                    binding.clearCategoryButtonId.visibility = View.VISIBLE
                    binding.addCategoryButtonId.visibility = View.GONE
                }
                categoryAdapter.submitList(categoriesList)
            }
        }

    }
}