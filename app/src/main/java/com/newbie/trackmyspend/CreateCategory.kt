package com.newbie.trackmyspend

import ColorAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

import com.newbie.trackmyspend.database.CategoryInfoViewModel
import com.newbie.trackmyspend.database.ExpenseInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityCreateCategoryBinding
import com.newbie.trackmyspend.model.CategoryInfo
import com.newbie.trackmyspend.model.ColorProvider
import java.util.Locale

class CreateCategory : AppCompatActivity() {
    private lateinit var binding:ActivityCreateCategoryBinding
    private lateinit var categoryInfoViewModel: CategoryInfoViewModel
    private var previousCategoryData : CategoryInfo ?= null
    private var colorList = ColorProvider.colorList
    private var preselectedColorIndex = 0
    private var selectedColorIndex = preselectedColorIndex

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()
        retrieveIntentData()



        categoryInfoViewModel = ViewModelProvider(this).get(CategoryInfoViewModel::class.java)

        binding.saveCategoryId.setOnClickListener {
            saveCategoryInfo()
        }

        binding.colorImageViewId.setOnClickListener {
            showCustomColorDialog(this, preselectedColorIndex) { selectedColor ->
                this@CreateCategory.selectedColorIndex = selectedColor
                preselectedColorIndex = selectedColor
            }
        }
        binding.deleteCategoryId.setOnClickListener {
            deleteCategoryInfo()
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

    private fun retrieveIntentData(){
        intent?.let {
            if (it.hasExtra("CATEGORY_ID")) {
                val id = it.getIntExtra("CATEGORY_ID", -1)
                val title = it.getStringExtra("CATEGORY_TITLE") ?: ""
                val monthly_limit = it.getDoubleExtra("CATEGORY_MONTHLY_LIMIT", 0.0)
                val color = it.getStringExtra("CATEGORY_COLOR") ?: "#abc777"

                selectedColorIndex = colorList.indexOf(color)
                preselectedColorIndex = selectedColorIndex
                previousCategoryData = CategoryInfo(id, title, monthly_limit, color)
                binding.categoryTitleEditText.setText(title)
                val amountLimitString = String.format(Locale.getDefault(), "%.0f", monthly_limit.toBigDecimal())
                binding.monthlyAmountLimitEditText.setText(amountLimitString)
//                binding.colorCodeTextViewId.text = color
                binding.deleteCategoryId.visibility = View.VISIBLE
                binding.colorImageViewId.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
            } else {
                binding.deleteCategoryId.visibility = View.INVISIBLE
                previousCategoryData = null
                preselectedColorIndex = 0
                selectedColorIndex = 0
//                binding.colorCodeTextViewId.text = colorList.get(preselectedColorIndex)
                binding.colorImageViewId.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorList.get(preselectedColorIndex)))
            }
        }
    }
    private fun deleteCategoryInfo() {
        previousCategoryData?.let { categoryInfoViewModel.deleteCategory(it) }
        finish()
    }

    private fun saveCategoryInfo() {
        val categoryTitle = binding.categoryTitleEditText.text?.trim().toString()
        //val categoryColorHexCode = "#" + binding.iconColorId.text.trim().toString()
        val monthlyLimit = binding.monthlyAmountLimitEditText.text?.trim().toString()

        if (categoryTitle.isEmpty()  || monthlyLimit.isEmpty()) {
            Toast.makeText(this, "Inputs are empty", Toast.LENGTH_SHORT).show()
            binding.categoryTitleEditText.error = "Empty Input"
            binding.monthlyAmountLimitEditText.error = "Empty Input"
            //Snackbar.make(binding.mainId, "Either Value is empty or wrong color code", Snackbar.LENGTH_SHORT).view
            return
        }
        //Toast.makeText(this, "selected Color << " + colorList[preselectedColorIndex].toString(), Toast.LENGTH_SHORT).show()
        if (categoryTitle.startsWith("Other", true)) {
            //Snackbar.make(binding.mainId, "'Other...' is a reserved category", Snackbar.LENGTH_SHORT).view
            binding.categoryTitleEditText.requestFocus()
            binding.categoryTitleEditText.error = "Other... is a reserved category"
            return
        }

        val id : Int = previousCategoryData?.id ?: 0
        val categoryInfo = CategoryInfo(
            id,
            title = categoryTitle,
            hexColorCode = colorList[this@CreateCategory.preselectedColorIndex],
            monthlyLimit = monthlyLimit.toDouble()
        )
        categoryInfoViewModel.saveOrModifyCategory(categoryInfo)

        finish()
    }

    private fun showCustomColorDialog(
        context: Context,
        preselectedColorIndex: Int? = null,
        onColorSelected: (Int) -> Unit
    ) {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.color_picker_dialog, null)

        // Initialize RecyclerView
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvColors)
        recyclerView.layoutManager = GridLayoutManager(context, 5)

        // Create Dialog
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Set Adapter
        val adapter = ColorAdapter(colorList, selectedColorIndex) { selectedColor ->
            onColorSelected(selectedColor)
            //dialog.dismiss()
        }
        recyclerView.adapter = adapter

        dialogView.findViewById<TextView>(R.id.cancelColorSelectionId).setOnClickListener {
            Log.i("colors selected ", colorList[selectedColorIndex] + "  " + selectedColorIndex + " " + preselectedColorIndex)
            if (preselectedColorIndex != null) {
                this@CreateCategory.selectedColorIndex = preselectedColorIndex
                this@CreateCategory.preselectedColorIndex = preselectedColorIndex
                binding.colorImageViewId.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorList[this@CreateCategory.preselectedColorIndex]))
//                binding.colorCodeTextViewId.text = colorList[this@CreateCategory.preselectedColorIndex]
            } // User canceled
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.selectColorSelectionId).setOnClickListener {
            onColorSelected(selectedColorIndex) // Pass the selected color
            this@CreateCategory.preselectedColorIndex = selectedColorIndex
            binding.colorImageViewId.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorList[selectedColorIndex]))
            Log.i("colors selected ", colorList[selectedColorIndex] + "  " + selectedColorIndex + "  " + this@CreateCategory.preselectedColorIndex)
//            binding.colorCodeTextViewId.text = colorList[this@CreateCategory.selectedColorIndex]
            dialog.dismiss()
        }

        dialog.show()
    }

}