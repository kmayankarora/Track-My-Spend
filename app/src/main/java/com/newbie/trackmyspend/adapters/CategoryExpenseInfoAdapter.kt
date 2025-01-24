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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.translationMatrix
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.newbie.trackmyspend.AllTransactionCategoryWise
import com.newbie.trackmyspend.ExpenseType
import com.newbie.trackmyspend.R
import com.newbie.trackmyspend.model.CategoryExpenseInfo
import java.util.Locale
import kotlin.math.ceil


class CategoryExpenseInfoAdapter(
    private val context: Context,
    private var year : Int,
    private var month : Int,
    private var transactionType: ExpenseType,
    private val getTransactionType: () -> ExpenseType) :
    ListAdapter<CategoryExpenseInfo, CategoryExpenseInfoAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CategoryExpenseInfo>() {
            override fun areItemsTheSame(oldItem: CategoryExpenseInfo, newItem: CategoryExpenseInfo): Boolean {
                // Unique comparison (e.g., ID)
                return oldItem.categoryId == newItem.categoryId
            }

            override fun areContentsTheSame(oldItem: CategoryExpenseInfo, newItem: CategoryExpenseInfo): Boolean {
                // Compare content equality
                return oldItem == newItem
            }
        }
    }

    // ViewHolder Class
    class ViewHolder(
        private val context: Context,
        private val year : Int,
        private val month : Int,
        private val transactionType: ExpenseType,
        private val getTransactionType: () -> ExpenseType,
        itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val categoryText: TextView = itemView.findViewById(R.id.categoryTitleTextView)
        private val categoryAmount: TextView = itemView.findViewById(R.id.categoryAmountTextView)
        private val categoryAmount2: TextView = itemView.findViewById(R.id.categoryAmountTextView2)
        private val categoryColor: ImageView = itemView.findViewById(R.id.categoryIconImageView)
        private val categoryProgress: ProgressBar = itemView.findViewById(R.id.spendTitleValueProgressbarId)
        private val factorValue : ImageView = itemView.findViewById(R.id.factorValueId)
        private val monthlyLimit : TextView = itemView.findViewById(R.id.monthlyAmountLimitId)
        private var redColor = Color.parseColor("#FF0000")
        private var yellowColor = Color.parseColor("#FFFF00")
        private var greenColor = Color.parseColor("#00FF00")
        private var lighestGreenColor = Color.parseColor("#a9ffa9")

        fun getGreenToYellowToRedColor(percentage: Float): Int {
            // Define the start and end colors
            // Handle percentage for green to yellow (0% to 50%)
            if (percentage <= 0.7f) {
                //val alpha : Float = (percentage) / 0.6f * 255;
                //Log.i("pushing it " , "" + alpha)
                return Color.argb(0,0,0,0)
            }
            else if (percentage <= 0.85f) {
                val ratio = (percentage - 0.7f) / 0.15f  // Normalize for first half
                return interpolateColor(greenColor, yellowColor, ratio)
            }
            // Handle percentage for yellow to red (50% to 100%)
            else {
                val ratio = (percentage - 0.85f) / 0.15f  // Normalize for second half
                return interpolateColor(yellowColor, redColor, ratio)
            }
        }

        fun interpolateColor(startColor: Int, endColor: Int, ratio: Float): Int {
            val startRed = Color.red(startColor)
            val startGreen = Color.green(startColor)
            val startBlue = Color.blue(startColor)

            val endRed = Color.red(endColor)
            val endGreen = Color.green(endColor)
            val endBlue = Color.blue(endColor)

            val red = (startRed + (endRed - startRed) * ratio).toInt()
            val green = (startGreen + (endGreen - startGreen) * ratio).toInt()
            val blue = (startBlue + (endBlue - startBlue) * ratio).toInt()

            return Color.rgb(red, green, blue)
        }

        fun bind(expense: CategoryExpenseInfo) {
            categoryText.text = expense.categoryTitle.toString()
            val totalExpenseAmtString = String.format(Locale.getDefault(), "%.0f", expense.totalAmount.toBigDecimal())
            categoryAmount.text = totalExpenseAmtString
            categoryAmount2.text = totalExpenseAmtString
            categoryColor.imageTintList = ColorStateList.valueOf(Color.parseColor(expense.categoryHexColorCode))
            monthlyLimit.text = if (expense.categoryMonthlyLimit != 0.0) String.format(Locale.getDefault(), "%.0f", expense.categoryMonthlyLimit.toBigDecimal()) else "Inf"
            var percent : Double = if (expense.categoryMonthlyLimit != 0.0) (1.0 * expense.totalAmount / expense.categoryMonthlyLimit * 100) else 100.0
            percent = if (percent > 100) 100.0 else percent
            categoryProgress.progress = (percent).toInt()
            //val colorWithAlpha = ColorUtils.setAlphaComponent(color, (1.0 * percent /100 * 255).toInt())
            //categoryProgress.progressTintList = ColorStateList.valueOf(colorWithAlpha)
            //monthlyLimit.text = getColor(percent).toString()
            //categoryProgress.progressTintList = ColorStateList.valueOf(getGreenToYellowToRedColor(percent/100.0.toFloat()))
            factorValue.imageTintList = ColorStateList.valueOf(getGreenToYellowToRedColor((1.0f * percent / 100).toFloat()))

            itemView.setOnClickListener {
                Toast.makeText(context, "percentage " + percent, Toast.LENGTH_SHORT).show()
                val intent = Intent(context, AllTransactionCategoryWise::class.java).apply {
                    // Pass the product info to CreateProductItem activity via intent extras
                    putExtra("TRANSACTION_TYPE", getTransactionType().name)
                    putExtra("CurrentYear", year)
                    putExtra("CurrentMonth", month)
                    putExtra("CurrentCategory", expense.categoryId)
                    putExtra("CategoryTitle", expense.categoryTitle)
                    putExtra("CategoryMonthlyLimit", expense.categoryMonthlyLimit)
                    putExtra("CategoryCurrentSum", expense.totalAmount)
                }
                // Start CreateProductItem activity with the product info
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_expense_info_rv_child_layout, parent, false)
        return ViewHolder(context, year, month, transactionType, getTransactionType,  view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun submitList(list: List<CategoryExpenseInfo>?) {
        super.submitList(list)
    }

    fun updateYearMonthTransaction(year: Int, month: Int) {
        this.year = year
        this.month = month
    }

}
