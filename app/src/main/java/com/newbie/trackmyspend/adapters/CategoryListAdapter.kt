package com.newbie.trackmyspend.adapters

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.newbie.trackmyspend.CreateCategory
import com.newbie.trackmyspend.R
import com.newbie.trackmyspend.model.CategoryInfo
import java.util.Locale

class CategoryListAdapter(private val context : Context) :
    ListAdapter<CategoryInfo, CategoryListAdapter.CategoryViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryInfo>() {
            override fun areItemsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                // Compare items by unique identifier or content
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CategoryInfo, newItem: CategoryInfo): Boolean {
                return oldItem == newItem
            }
        }
    }

    class CategoryViewHolder(private val context: Context, view : View) : RecyclerView.ViewHolder(view) {

        private val titleTextView: TextView = view.findViewById(R.id.categoryTitleTextView)
        private val iconImageView: ImageView = view.findViewById(R.id.categoryIconImageView)
        private val monthlyTextView: TextView = view.findViewById(R.id.monthlyLimitId)

        fun bind(categoryInfo: CategoryInfo) {
            titleTextView.text = categoryInfo.title.toString()
            iconImageView.imageTintList = ColorStateList.valueOf(Color.parseColor(categoryInfo.hexColorCode))
            val monthlyLimitString = String.format(Locale.getDefault(), "%.0f", categoryInfo.monthlyLimit.toBigDecimal())
            monthlyTextView.text = monthlyLimitString //.toPlainString().toString()

            itemView.setOnClickListener {
                val intent = Intent(context, CreateCategory::class.java).apply {
                    putExtra("CATEGORY_TITLE", categoryInfo.title)
                    putExtra("CATEGORY_ID", categoryInfo.id)
                    putExtra("CATEGORY_COLOR", categoryInfo.hexColorCode)
                    putExtra("CATEGORY_MONTHLY_LIMIT", categoryInfo.monthlyLimit)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_list_rv_child_layout, parent, false)
        return CategoryViewHolder(context, view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun submitList(list: List<CategoryInfo>?) {
        super.submitList(list)
    }
}