import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.newbie.trackmyspend.R

class ColorAdapter(
    private val colors: List<String>, // List of colors
    private var preselectedColorIndex: Int?, // Index of the preselected color
    private val onColorSelected: (Int) -> Unit // Callback to return the position of selected color
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    // Initially set the selected position to preselectedColorIndex or RecyclerView.NO_POSITION if null
    private var selectedPosition = preselectedColorIndex ?: RecyclerView.NO_POSITION

    // ViewHolder for each color item
    inner class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorView: ImageView = view.findViewById(R.id.colorItem)
        val backgroundView: ImageView = view.findViewById(R.id.selectionLayoutId)

        init {
            view.setOnClickListener {
                // Save the current position and update the selected position
                val previousPosition = selectedPosition
                selectedPosition = bindingAdapterPosition

                // Notify previous and current selected positions
                if (previousPosition != RecyclerView.NO_POSITION) notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                // Update the preselected color index
                preselectedColorIndex = selectedPosition

                // Notify the caller with the selected position
                onColorSelected(selectedPosition)
            }
        }
    }

    // Create a ViewHolder for each color item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
        return ColorViewHolder(view)
    }

    // Bind each color item to the ViewHolder
    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]

        // Set the color as the background tint of the ImageView
        holder.colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))

        // Highlight the selected item by checking if it's the currently selected position
        if (position == selectedPosition) {
            holder.backgroundView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))//ContextCompat.getDrawable(holder.itemView.context, R.drawable.selected_border)
        } else {
            holder.backgroundView.backgroundTintList = ColorStateList.valueOf(Color.argb(0,0,0,0))//null
        }
    }

    // Return the total count of colors in the list
    override fun getItemCount() = colors.size
}
