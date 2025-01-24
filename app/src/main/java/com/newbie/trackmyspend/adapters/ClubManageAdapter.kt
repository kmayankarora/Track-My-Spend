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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.newbie.trackmyspend.CreateCategory
import com.newbie.trackmyspend.CreateClub
import com.newbie.trackmyspend.DisplayParticularClubTransactions
import com.newbie.trackmyspend.ExpenseType
import com.newbie.trackmyspend.R
import com.newbie.trackmyspend.TransferType
import com.newbie.trackmyspend.model.ClubInfo
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ClubManageAdapter(private val context : Context, private val editMode: Boolean) :
    ListAdapter<ClubInfo, ClubManageAdapter.ClubViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ClubInfo>() {
            override fun areItemsTheSame(oldItem: ClubInfo, newItem: ClubInfo): Boolean {
                // Compare items by unique identifier or content
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ClubInfo, newItem: ClubInfo): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ClubViewHolder(private val context: Context, private val editMode: Boolean, view : View) : RecyclerView.ViewHolder(view) {

        val titleTextView: TextView = view.findViewById(R.id.clubTitleId)
        val iconTextView : ImageView = view.findViewById(R.id.iconImageView)
        val subtitleTextView: TextView = view.findViewById(R.id.clubSubtitleId)

        fun bind(clubInfo: ClubInfo) {
            titleTextView.text = clubInfo.title.toString()
            subtitleTextView.text = if (clubInfo.subtitle != null) clubInfo.subtitle.toString() else "-----"
            iconTextView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(clubInfo.hexColorCode.toString()))
            itemView.setOnClickListener {
                if (editMode) {
                    val intent = Intent(context, CreateClub::class.java).apply {
                        putExtra("CLUB_TITLE", clubInfo.title)
                        putExtra("CLUB_SUBTITLE", clubInfo.subtitle)
                        putExtra("CLUB_COLOR", clubInfo.hexColorCode)
                        putExtra("CLUB_ID", clubInfo.id)
                    }
                    context.startActivity(intent)
                } else {
                    val intent = Intent(context, DisplayParticularClubTransactions::class.java).apply {
                        putExtra("CLUB_TITLE", clubInfo.title)
                        if (clubInfo.subtitle != null)
                            putExtra("CLUB_SUBTITLE", clubInfo.subtitle)
                        putExtra("CLUB_COLOR", clubInfo.hexColorCode)
                        putExtra("CLUB_ID", clubInfo.id)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClubViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.club_rv_child_layout, parent, false)
        return ClubViewHolder(context, editMode, view)
    }

    override fun onBindViewHolder(holder: ClubViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}