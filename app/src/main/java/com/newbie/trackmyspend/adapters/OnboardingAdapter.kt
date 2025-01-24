package com.newbie.trackmyspend.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.newbie.trackmyspend.R

data class OnboardingItem(
    val number : Int,
    val image: Int ?= null,
    val title: String,
    val description: String,
    val backgroundColor : String = "#000fff")

class OnboardingAdapter(private val onboardingItems: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val numberView: TextView = view.findViewById(R.id.numberTextView)
        private val imageView: ImageView = view.findViewById(R.id.imageOnboarding)
        private val titleView: TextView = view.findViewById(R.id.textTitle)
        private val descriptionView: TextView = view.findViewById(R.id.textDescription)


        fun bind(item: OnboardingItem) {
            if (item.image != null) {
                imageView.setImageResource(item.image)
                imageView.visibility = View.VISIBLE
            } else imageView.visibility = View.VISIBLE
            numberView.text = item.number.toString()
            titleView.text = item.title
            descriptionView.text = item.description
            itemView.setBackgroundColor(Color.parseColor(item.backgroundColor))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.onboarding_adapter_child, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount(): Int = onboardingItems.size
}
