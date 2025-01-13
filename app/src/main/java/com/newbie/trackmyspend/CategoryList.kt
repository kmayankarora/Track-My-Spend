package com.newbie.trackmyspend

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.newbie.trackmyspend.adapters.CategoryListAdapter
import com.newbie.trackmyspend.database.CategoryInfoViewModel
import com.newbie.trackmyspend.databinding.ActivityCategoryListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryList : AppCompatActivity() {

    private lateinit var binding:ActivityCategoryListBinding
    private lateinit var adapter : CategoryListAdapter
    private lateinit var categoryInfoViewModel: CategoryInfoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryInfoViewModel = ViewModelProvider(this).get(CategoryInfoViewModel::class.java)


        val toolbar: MaterialToolbar = findViewById(R.id.toolbarId)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true);
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.baseline_close_24); // Optional: Custom back icon
        }
        binding.toolbarId.setNavigationOnClickListener { v: View? ->
            // Use OnBackPressedDispatcher for back button functionality
            onBackPressedDispatcher.onBackPressed()
        }

        setRecyclerView()
        setFAB()
    }
    private fun setRecyclerView() {

        adapter = CategoryListAdapter(this);

        binding.categoryListRv.apply {
            setHasFixedSize(true)
            adapter = this@CategoryList.adapter
            layoutManager = GridLayoutManager(this@CategoryList, 2, GridLayoutManager.VERTICAL, false)
        }

        lifecycleScope.launch {
            categoryInfoViewModel.allCategory.collectLatest { categories ->
                adapter.submitList(categories)
            }
        }
    }
    private fun setFAB() {
        binding.addCategoryFabId.setOnClickListener {
            // Create an Intent to start SecondActivity
            val intent = Intent(this, CreateCategory::class.java)
            startActivity(intent)
        }
    }
}