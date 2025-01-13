package com.newbie.trackmyspend.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.newbie.trackmyspend.R
import com.newbie.trackmyspend.model.CategoryExpenseInfo
import com.newbie.trackmyspend.model.CategoryInfo


class CategoryAdapter(
    private val context: Context,
    private val onCategorySelected: (Int) -> Unit, // Callback for the selected position
    private val defaultSelected : Int = -1
) : ListAdapter<CategoryInfo, CategoryAdapter.CategoryViewHolder>(DIFF_CALLBACK) {

    private var selectedPosition = if (defaultSelected != -1) defaultSelected else RecyclerView.NO_POSITION // Tracks the selected item
    private val selectedColor = ContextCompat.getColor(context, R.color.card_view_dark_bg)
    private val defaultColor = ContextCompat.getColor(context, R.color.card_view_light_bg)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryInfo>() {
            override fun areItemsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                // Compare items by unique identifier or content
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryTextView: TextView = view.findViewById(R.id.iconTextView)
        val iconTextView : ImageView = view.findViewById(R.id.iconImageView)
        val constraintLayout: ConstraintLayout = view.findViewById(R.id.constraintLayoutId)

        init {
            view.setOnClickListener {
                var previousPosition = selectedPosition
                selectedPosition = bindingAdapterPosition

                // Notify adapter to refresh the previous and current selected items
                if (previousPosition != RecyclerView.NO_POSITION) notifyItemChanged(previousPosition)
                if (selectedPosition == previousPosition) {
                    previousPosition = -1
                    selectedPosition = -1
                }
                notifyItemChanged(selectedPosition)

                // Trigger callback with the selected position
                onCategorySelected(selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_rv_child_layout, parent, false) // Inflate your item layout
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.categoryTextView.text = item.title
        holder.iconTextView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.hexColorCode.toString()))
        // Highlight selected item with a different background
        if (position == selectedPosition) {
            holder.constraintLayout.setBackgroundColor(selectedColor) // Selected item background

        } else {
            holder.constraintLayout.setBackgroundColor(defaultColor) // Default background
        }
    }

    override fun submitList(list: List<CategoryInfo>?) {
        super.submitList(list)
    }

    public fun setSelectedIndex(value : Int) {
        selectedPosition = value
    }
}
