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
import com.newbie.trackmyspend.AddTransaction
import com.newbie.trackmyspend.ExpenseType
import com.newbie.trackmyspend.R
import com.newbie.trackmyspend.TransferType
import com.newbie.trackmyspend.model.PresetCompleteInfo
import java.util.Locale

class PresetInfoAdapter(private val context: Context, private val editPreset : Boolean) :
    ListAdapter<PresetCompleteInfo, PresetInfoAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PresetCompleteInfo>() {
            override fun areItemsTheSame(oldItem: PresetCompleteInfo, newItem: PresetCompleteInfo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PresetCompleteInfo, newItem: PresetCompleteInfo): Boolean {
                // Compare content equality
                //Log.i("maybe i am the culprit", "find out")
                return oldItem == newItem
            }
        }
    }

    // ViewHolder Class
    class ViewHolder(private val context: Context, private val editPreset : Boolean, itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val presetTextView: TextView = itemView.findViewById(R.id.presetNameId)
        private val expenseTypeTextView: TextView = itemView.findViewById(R.id.expenseTypeId)
        private val transferTypeTextView: TextView = itemView.findViewById(R.id.transferTypeId)
        private val transferPersonTextView: TextView = itemView.findViewById(R.id.transferPersonId)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTypeId)
        private val amountTextView : TextView = itemView.findViewById(R.id.amountId)
        private val iconImageView : ImageView = itemView.findViewById(R.id.iconImageView)

        fun bind(presetCompleteInfo: PresetCompleteInfo) {
            presetTextView.text = presetCompleteInfo.presetName

            val expenseTypeValue = when(presetCompleteInfo.transactionType){
                ExpenseType.SPEND -> "Spend"
                ExpenseType.EARNED -> "Earned"
                ExpenseType.TRANSFER -> "Transfer"
            }
            val transferType = when(presetCompleteInfo.transactionType) {
                ExpenseType.TRANSFER -> {
                    if (presetCompleteInfo.transferType == TransferType.TO) "To" else "From"
                }
                else -> ""
            }
            val transferPerson = if (presetCompleteInfo.transactionType == ExpenseType.TRANSFER) presetCompleteInfo.transferInfo else ""
            expenseTypeTextView.text = expenseTypeValue
            transferTypeTextView.text = transferType
            transferPersonTextView.text = transferPerson
            categoryTextView.text = presetCompleteInfo.categoryTitle
            val amount : String= String.format(Locale.getDefault(),"%.0f", presetCompleteInfo.amount.toBigDecimal())
            amountTextView.text = amount
            iconImageView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(presetCompleteInfo.categoryHexCode))

            itemView.setOnClickListener {
                val intent = Intent(context, AddTransaction::class.java).apply {
                    putExtra("TRANSACTION_TYPE", presetCompleteInfo.transactionType.name)
                    putExtra("TRANSACTION_AMOUNT", presetCompleteInfo.amount)
                    putExtra("TRANSACTION_CATEGORY", presetCompleteInfo.category)
                    putExtra("TRANSACTION_ID", presetCompleteInfo.id)
                    putExtra("TRANSACTION_TRANSFERINFO", presetCompleteInfo.transferInfo)
                    putExtra("TRANSACTION_TRANSFERTYPE", presetCompleteInfo.transferType?.name)
                    putExtra("PRESET_NAME", presetCompleteInfo.presetName)
                    putExtra("THIS_IS_PRESET", true)
                    putExtra("MODIFY_PRESET", editPreset)
                }
                // Start CreateProductItem activity with the product info
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.preset_info_rv_child, parent, false)
        return ViewHolder(context, editPreset, view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun submitList(list: List<PresetCompleteInfo>?) {
        super.submitList(list)
    }

}