package com.newbie.trackmyspend.adapters

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

class TransactionAdapter(private val context : Context) :
    ListAdapter<TransactionExpenseInfo, TransactionAdapter.TransactionViewHolder>(DIFF_CALLBACK) {

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

        private var categoryColor : ImageView = view.findViewById(R.id.categoryImageView)
        private var amountTextView : TextView = view.findViewById(R.id.amountTextView)
        private var categoryTextView : TextView = view.findViewById(R.id.categoryTextView)
        private var transactionTypeTextView : TextView = view.findViewById(R.id.transactionTypeEntryId)
        private var transferTypeTextView : TextView = view.findViewById(R.id.transferToFromPersonId)
        private var dateTimeTextView : TextView = view.findViewById(R.id.timeOfEntryId)

        private fun convertToDateTime(timestamp : Long) : String{
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")
                .withZone(ZoneId.systemDefault()) // Use the system's time zone
            return formatter.format(Instant.ofEpochMilli(timestamp))
        }

        fun bind(expenseInfo: TransactionExpenseInfo) {
            itemView.setOnClickListener {
                val intent = Intent(context, AddTransaction::class.java).apply {
                    putExtra("TRANSACTION_TYPE", expenseInfo.transactionType.name)
                    putExtra("TRANSACTION_AMOUNT", expenseInfo.amount)
                    putExtra("TRANSACTION_CATEGORY", expenseInfo.category)
                    putExtra("TRANSACTION_ID", expenseInfo.id)
                    putExtra("TRANSACTION_DATETIME", expenseInfo.dateTime)
                    putExtra("TRANSACTION_DESCRIPTION", expenseInfo.description)
                    putExtra("TRANSACTION_TRANSFERINFO", expenseInfo.transferInfo)
                    putExtra("TRANSACTION_TRANSFERTYPE", expenseInfo.transferType?.name)
                    putExtra("TRANSACTION_MONTH", expenseInfo.month)
                    putExtra("TRANSACTION_YEAR", expenseInfo.year)
                    putExtra("THIS_IS_TRANSACTION", true)
                    if (expenseInfo.clubId != null) {
                        Toast.makeText(context, "ClubId " + expenseInfo.clubId, Toast.LENGTH_SHORT).show()
                        putExtra("CLUB_ID", expenseInfo.clubId)
                    }
                }
                context.startActivity(intent)
            }
            val amountString = String.format(Locale.getDefault(),"%.0f", expenseInfo.amount)
            amountTextView.text = amountString
            categoryTextView.text = expenseInfo.categoryTitle
            transferTypeTextView.visibility = View.INVISIBLE
            if (expenseInfo.transactionType == ExpenseType.TRANSFER) {
                var value : String = ""
                value += if (expenseInfo.transferType == TransferType.TO) {
                    "Transfer To"
                } else {
                    "Transfer From"
                }
                transferTypeTextView.text = expenseInfo.transferInfo
                transactionTypeTextView.text = value
                transferTypeTextView.visibility = View.VISIBLE
            } else if (expenseInfo.transactionType == ExpenseType.SPEND) {
                transactionTypeTextView.text = "Spend"
            } else {
                transactionTypeTextView.text = "Earned"
            }
            categoryColor.imageTintList = ColorStateList.valueOf(Color.parseColor(expenseInfo.categoryHexColorCode))
            dateTimeTextView.text = convertToDateTime(expenseInfo.dateTime)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_rv_child, parent, false)
        return TransactionViewHolder(context, view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


}