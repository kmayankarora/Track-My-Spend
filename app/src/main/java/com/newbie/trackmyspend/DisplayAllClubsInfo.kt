package com.newbie.trackmyspend

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.newbie.trackmyspend.adapters.ClubManageAdapter
import com.newbie.trackmyspend.database.ClubInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityDisplayAllClubsInfoBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DisplayAllClubsInfo : AppCompatActivity() {

    private lateinit var binding : ActivityDisplayAllClubsInfoBinding
    private lateinit var clubInfoViewModel: ClubInfoViewModel
    private lateinit var clubAdapter: ClubManageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDisplayAllClubsInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clubInfoViewModel = ViewModelProvider(this)[ClubInfoViewModel::class.java]
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
            // Use OnBackPressedDispatcher for back button functionality
            onBackPressedDispatcher.onBackPressed()
        }
    }
    private fun setupRecyclerView() {
        clubAdapter = ClubManageAdapter(this, false)
        binding.allClubListRvId.apply {
            setHasFixedSize(true)
            adapter = this@DisplayAllClubsInfo.clubAdapter
            layoutManager = GridLayoutManager(this@DisplayAllClubsInfo, 1, GridLayoutManager.VERTICAL, false)
        }
        lifecycleScope.launch {
            clubInfoViewModel.allClubs.collectLatest { clubs ->
                if (clubs.isEmpty()) {
                    //binding.messageId.visibility = View.VISIBLE
                    //binding.presetRecyclerViewId.visibility = View.GONE
                } else {
                    //binding.messageId.visibility = View.GONE
                    //binding.presetRecyclerViewId.visibility = View.VISIBLE
                    clubAdapter.submitList(clubs)
                }
            }
        }
    }
}