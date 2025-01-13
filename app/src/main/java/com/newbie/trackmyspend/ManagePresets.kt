package com.newbie.trackmyspend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.newbie.trackmyspend.adapters.PresetInfoAdapter
import com.newbie.trackmyspend.database.PresetInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityManagePresetsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManagePresets : AppCompatActivity() {

    private lateinit var binding: ActivityManagePresetsBinding
    private lateinit var presetInfoViewModel: PresetInfoViewModel
    private lateinit var presetAdapter : PresetInfoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityManagePresetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presetInfoViewModel = ViewModelProvider(this)[PresetInfoViewModel::class.java]
        setActionBar()
        setupRecyclerView()

    }
    private fun setActionBar() {
        setSupportActionBar(binding.toolbarId)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.baseline_close_24)
        }
        binding.toolbarId.setNavigationOnClickListener { v: View? ->
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {

        presetAdapter = PresetInfoAdapter(this, true)

        binding.presetRecyclerViewId.apply {
            setHasFixedSize(true)
            adapter = this@ManagePresets.presetAdapter
            layoutManager = GridLayoutManager(this@ManagePresets, 2, GridLayoutManager.VERTICAL, false)
        }

        lifecycleScope.launch {
            presetInfoViewModel.allPreset.collectLatest { presetList ->
                if (presetList.isEmpty()) {
                    binding.messageId.visibility = View.VISIBLE
                    binding.presetRecyclerViewId.visibility = View.GONE
                } else {
                    binding.messageId.visibility = View.GONE
                    binding.presetRecyclerViewId.visibility = View.VISIBLE
                    presetAdapter.submitList(presetList)
                }
            }
        }
    }

}