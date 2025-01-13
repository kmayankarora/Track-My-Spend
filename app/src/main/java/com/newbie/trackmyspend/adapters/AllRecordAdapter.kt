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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.newbie.trackmyspend.AddTransaction
import com.newbie.trackmyspend.ExpenseType
import com.newbie.trackmyspend.R
import com.newbie.trackmyspend.TransferType
import com.newbie.trackmyspend.model.TransactionExpenseInfo
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AllRecordAdapter(private val context : Context) :
    ListAdapter<TransactionExpenseInfo, AllRecordAdapter.TransactionViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionExpenseInfo>() {
            override fun areItemsTheSame(oldItem: TransactionExpenseInfo, newItem: TransactionExpenseInfo): Boolean {
                // Compare items by unique identifier or content
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TransactionExpenseInfo, newItem: TransactionExpenseInfo): Boolean {
                return oldItem == newItem
            }
        }
    }

    class TransactionViewHolder(private val context: Context, view : View) : RecyclerView.ViewHolder(view) {

        private var title : TextView = view.findViewById(R.id.categoryTitleTextView)
        private var amount : TextView = view.findViewById(R.id.amountValueId)
        private var date : TextView = view.findViewById(R.id.dateValueId)
        private var drawable : ImageView = view.findViewById(R.id.signDrawableId)

        private fun convertToDateTime(timestamp : Long) : String{
            val formatter = DateTimeFormatter.ofPattern("dd/MM")
                .withZone(ZoneId.systemDefault()) // Use the system's time zone
            return formatter.format(Instant.ofEpochMilli(timestamp))
        }

        fun bind(expenseInfo: TransactionExpenseInfo) {
            title.text = if (expenseInfo.categoryTitle.isEmpty()) "" else expenseInfo.categoryTitle.toString()
            val expenseAmtString = String.format(Locale.getDefault(),"%.0f", expenseInfo.amount)
            amount.text = expenseAmtString
            if (expenseInfo.transactionType == ExpenseType.TRANSFER) {
                val color = ContextCompat.getColor(context, R.color.normal_text_color)
                amount.setTextColor(color)
                drawable.imageTintList = ColorStateList.valueOf(color)
                if (expenseInfo.transferType == TransferType.TO) {
                    drawable.setImageResource(R.drawable.baseline_remove_24)
                } else {
                    drawable.setImageResource(R.drawable.baseline_add_24)
                }
            } else if (expenseInfo.transactionType == ExpenseType.SPEND) {
                amount.setTextColor(Color.RED)
                drawable.setImageResource(R.drawable.baseline_remove_24)
                drawable.imageTintList = ColorStateList.valueOf(Color.RED)
            } else {
                amount.setTextColor(Color.MAGENTA)
                drawable.setImageResource(R.drawable.baseline_add_24)
                drawable.imageTintList = ColorStateList.valueOf(Color.MAGENTA)
            }
            date.text = convertToDateTime(expenseInfo.dateTime)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.all_transaction_report_card_rv_child, parent, false)
        return TransactionViewHolder(context, view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}