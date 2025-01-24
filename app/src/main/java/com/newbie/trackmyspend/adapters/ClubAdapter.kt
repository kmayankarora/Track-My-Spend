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
import com.newbie.trackmyspend.model.ClubInfo


class ClubAdapter(
    private val context: Context,
    private val onClubSelected: (Int) -> Unit,
    private val defaultSelected : Int = -1
) : ListAdapter<ClubInfo, ClubAdapter.ClubViewHolder>(DIFF_CALLBACK) {

    private var selectedPosition = if (defaultSelected != -1) defaultSelected else RecyclerView.NO_POSITION // Tracks the selected item
    private val selectedColor = ContextCompat.getColor(context, R.color.card_view_dark_bg)
    private val defaultColor = ContextCompat.getColor(context, R.color.card_view_light_bg)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ClubInfo>() {
            override fun areItemsTheSame(oldItem: ClubInfo, newItem: ClubInfo): Boolean {
                // Compare items by unique identifier or content
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ClubInfo, newItem: ClubInfo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ClubViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.clubTitleId)
        val iconTextView : ImageView = view.findViewById(R.id.iconImageView)
        val subtitleTextView: TextView = view.findViewById(R.id.clubSubtitleId)
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
                onClubSelected(selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClubViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.club_rv_child_layout, parent, false) // Inflate your item layout
        return ClubViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClubViewHolder, position: Int) {
        val item = getItem(position)
        holder.titleTextView.text = item.title

        holder.subtitleTextView.text = if (item.subtitle != null) item.subtitle.toString() else ""
        holder.subtitleTextView.visibility = if (item.subtitle == null) View.GONE else View.VISIBLE

        holder.iconTextView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.hexColorCode.toString()))
        // Highlight selected item with a different background
        if (position == selectedPosition) {
            holder.constraintLayout.setBackgroundColor(selectedColor) // Selected item background
        } else {
            holder.constraintLayout.setBackgroundColor(defaultColor) // Default background
        }
    }

    override fun submitList(list: List<ClubInfo>?) {
        super.submitList(list)
    }

    public fun setSelectedIndex(value : Int) {
        selectedPosition = value
    }
}
