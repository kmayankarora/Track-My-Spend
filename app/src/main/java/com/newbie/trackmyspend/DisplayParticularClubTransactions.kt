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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.newbie.trackmyspend.adapters.ClubManageAdapter
import com.newbie.trackmyspend.adapters.TransactionAdapter
import com.newbie.trackmyspend.database.ExpenseInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityDisplayParticularClubTransactionsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DisplayParticularClubTransactions : AppCompatActivity() {

    private lateinit var binding : ActivityDisplayParticularClubTransactionsBinding
    private lateinit var clubAdapter: TransactionAdapter
    private lateinit var expenseInfoViewModel: ExpenseInfoViewModel
    private var clubId : Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDisplayParticularClubTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        expenseInfoViewModel = ViewModelProvider(this).get(ExpenseInfoViewModel::class.java)
        setActionBar()
        retrieveIntentData()
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
    private fun retrieveIntentData() {
        intent?.let {
            if (it.hasExtra("CLUB_ID")) {
                clubId = it.getLongExtra("CLUB_ID", -1L)
            }
            if (it.hasExtra("CLUB_TITLE")) {
                binding.toolbarId.title = it.getStringExtra("CLUB_TITLE")
            }
            if (it.hasExtra("CLUB_SUBTITLE")) {
                val subtitle = it.getStringExtra("CLUB_SUBTITLE").toString()
                if (subtitle.isNotEmpty()) {
                    binding.toolbarId.subtitle = subtitle
                }
            }
        }
    }

    private fun setupRecyclerView() {
        clubAdapter = TransactionAdapter(this)
        binding.allClubListRvId.apply {
            setHasFixedSize(true)
            adapter = this@DisplayParticularClubTransactions.clubAdapter
            layoutManager = LinearLayoutManager(this@DisplayParticularClubTransactions)//GridLayoutManager(this@ManageClubs, 1, GridLayoutManager.VERTICAL, false)
        }
        lifecycleScope.launch {
            expenseInfoViewModel.getAllTransactionForParticularClub(clubId).collectLatest { clubs ->
                Toast.makeText(this@DisplayParticularClubTransactions, "got some data " + clubs.size, Toast.LENGTH_SHORT).show()
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