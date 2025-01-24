package com.newbie.trackmyspend

import ColorAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.newbie.trackmyspend.database.ClubInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityCreateClubBinding
import com.newbie.trackmyspend.model.ClubInfo
import com.newbie.trackmyspend.model.ColorProvider

class CreateClub : AppCompatActivity() {

    private lateinit var binding: ActivityCreateClubBinding
    private lateinit var clubInfoViewModel: ClubInfoViewModel
    private var previousClubData : ClubInfo ?= null
    private var colorList = ColorProvider.colorList
    private var preselectedColorIndex = 0
    private var selectedColorIndex = preselectedColorIndex


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateClubBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()
        retrieveIntentData()

        clubInfoViewModel = ViewModelProvider(this)[ClubInfoViewModel::class.java]

        setupEditText()
        setupPreviousValue()

        binding.colorImageViewId.setOnClickListener {
            showCustomColorDialog(this, preselectedColorIndex) { selectedColor ->
                this@CreateClub.selectedColorIndex = selectedColor
                preselectedColorIndex = selectedColor
            }
        }

        setupButtons()
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

    private fun setupEditText() {
        binding.clubTitleEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.clubTitleEditText.windowToken, 0)
                binding.clubTitleEditText.clearFocus()
                if (binding.clubTitleEditText.text?.trim()?.isNotBlank() == true)
                    binding.clubTextValueId1.text = binding.clubTitleEditText.text.toString()
                true
            } else {
                false
            }
        }
        binding.clubSubtitleEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.clubSubtitleEditText.windowToken, 0)
                binding.clubSubtitleEditText.clearFocus()
                if (binding.clubSubtitleEditText.text?.trim()?.isNotEmpty() == true)
                    binding.clubTextValueId2.text = binding.clubSubtitleEditText.text.toString()
                true
            } else {
                false
            }
        }
    }

    private fun setupButtons() {
        binding.clearClubValueId.setOnClickListener {
            binding.clubTitleEditText.setText("")
            binding.clubSubtitleEditText.setText("")
        }
        binding.saveClubId.setOnClickListener {
            saveClubInfo()
        }
        binding.clubIconImageView.setOnClickListener {
            showCustomColorDialog(this, preselectedColorIndex) { selectedColor ->
                this@CreateClub.selectedColorIndex = selectedColor
                preselectedColorIndex = selectedColor
            }
        }
        binding.deleteClubId.setOnClickListener {
            deleteClubInfo()
        }
    }

    private fun retrieveIntentData() {
        intent?.let {
            if (it.hasExtra("CLUB_ID")) {
                val id = it.getLongExtra("CLUB_ID", -1)
                val title = it.getStringExtra("CLUB_TITLE") ?: ""
                var subtitle = ""
                if (it.hasExtra("CLUB_SUBTITLE")) {
                    subtitle = it.getStringExtra("CLUB_SUBTITLE") ?: ""
                }
                val color = it.getStringExtra("CLUB_COLOR") ?: "#abc777"
                previousClubData = ClubInfo(
                    id = id,
                    title = title,
                    subtitle = if (subtitle.isEmpty()) null else subtitle,
                    hexColorCode = color
                )
            } else {
                previousClubData = null
            }
        }
    }

    private fun setupPreviousValue() {
        if (previousClubData != null) {
            binding.clubTitleEditText.setText(previousClubData!!.title.toString())
            binding.clubSubtitleEditText.setText(if(previousClubData!!.subtitle == null) "" else previousClubData!!.subtitle)
            binding.colorImageViewId.backgroundTintList = ColorStateList.valueOf(Color.parseColor(previousClubData!!.hexColorCode))
            binding.clubIconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(previousClubData!!.hexColorCode))
            preselectedColorIndex = colorList.indexOf(previousClubData!!.hexColorCode)
            selectedColorIndex = preselectedColorIndex
            if (previousClubData!!.title.isNotEmpty())
                binding.clubTextValueId1.text = previousClubData!!.title
            if (previousClubData!!.subtitle != null) {
                binding.clubTextValueId2.text = previousClubData!!.subtitle
            }
            supportActionBar!!.title = "Modify Club"
            binding.deleteClubId.visibility = View.VISIBLE
        } else {
            binding.clubTitleEditText.setText("")
            binding.clubSubtitleEditText.setText("")
            binding.colorImageViewId.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorList[0]))
            binding.clubIconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorList[0]))
            preselectedColorIndex = 0
            selectedColorIndex = 0
            supportActionBar!!.title = "Create Club"
            binding.deleteClubId.visibility = View.GONE
        }
    }

    private fun deleteClubInfo() {

    }

    private fun saveClubInfo() {
        val clubTitle = binding.clubTitleEditText.text?.trim().toString()
        val clubSubtitle = binding.clubSubtitleEditText.text?.trim().toString()
        if (clubTitle.isEmpty()) {
            Snackbar.make(binding.mainId, "Empty Title", Snackbar.LENGTH_SHORT).view
            return
        }
        val id : Long = 0
        val clubInfo = ClubInfo(
            id,
            title = clubTitle,
            subtitle = clubSubtitle.ifEmpty { null },
            hexColorCode = colorList[this@CreateClub.preselectedColorIndex]
        )
        clubInfoViewModel.saveOrModifyClub(clubInfo)
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
                this@CreateClub.selectedColorIndex = preselectedColorIndex
                this@CreateClub.preselectedColorIndex = preselectedColorIndex
                binding.colorImageViewId.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorList[this@CreateClub.preselectedColorIndex]))
                binding.clubIconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(
                    colorList[this@CreateClub.preselectedColorIndex]
                ))
//                binding.colorCodeTextViewId.text = colorList[this@CreateCategory.preselectedColorIndex]
            } // User canceled
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.selectColorSelectionId).setOnClickListener {
            onColorSelected(selectedColorIndex) // Pass the selected color
            this@CreateClub.preselectedColorIndex = selectedColorIndex
            binding.colorImageViewId.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorList[selectedColorIndex]))
            binding.clubIconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorList[selectedColorIndex]))
            //Log.i("colors selected ", colorList[selectedColorIndex] + "  " + selectedColorIndex + "  " + this@CreateCategory.preselectedColorIndex)
//            binding.colorCodeTextViewId.text = colorList[this@CreateCategory.selectedColorIndex]
            dialog.dismiss()
        }

        dialog.show()
    }

}