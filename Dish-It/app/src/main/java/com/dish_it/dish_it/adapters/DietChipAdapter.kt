package com.dish_it.dish_it.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.R
import com.dish_it.dish_it.models.ChipStyle
import com.dish_it.dish_it.models.DietChip

private const val TYPE_CHIP = 0
private const val TYPE_ADD = 1

/**
 * Powers all 3 chip rows on My Profile (Dietary Preferences, Ingredients to
 * avoid, Favourite Cuisines) - same adapter, just a different `items` list
 * and ChipStyle per row. The list always renders one MORE item than
 * `items.size`: the last position is always the dashed "+ Add" chip
 * (item_diet_chip_add.xml), which is why getItemCount() is items.size + 1
 * and getItemViewType() checks `position == items.size`.
 *
 * Pair this with a FlexboxLayoutManager (set in ProfileActivity.kt), not a
 * LinearLayoutManager - Flexbox is what makes the chips WRAP onto a new
 * line instead of running off the edge of the screen once there are more
 * than fit on one row.
 */
class DietChipAdapter(
    private val items: MutableList<DietChip>,
    private val onAddClicked: () -> Unit,
    private val onChipRemoved: (DietChip) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.img_chip_icon)
        val label: TextView = itemView.findViewById(R.id.tv_chip_label)
    }

    class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int =
        if (position == items.size) TYPE_ADD else TYPE_CHIP

    override fun getItemCount(): Int = items.size + 1 // +1 for the trailing "Add" chip

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ADD) {
            AddViewHolder(inflater.inflate(R.layout.item_diet_chip_add, parent, false))
        } else {
            ChipViewHolder(inflater.inflate(R.layout.item_diet_chip, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddViewHolder) {
            holder.itemView.setOnClickListener { onAddClicked() }
            return
        }

        val chip = items[position]
        val h = holder as ChipViewHolder
        h.label.text = chip.label

        val (bgColorRes, textColorRes, iconRes) = when (chip.style) {
            ChipStyle.PREFERENCE -> Triple(R.color.chip_green_bg, R.color.chip_green_text, R.drawable.ic_check)
            ChipStyle.AVOID -> Triple(R.color.chip_red_bg, R.color.chip_red_text, R.drawable.ic_block)
            ChipStyle.CUISINE -> Triple(R.color.tag_lunch_bg, R.color.tag_lunch_text, R.drawable.ic_globe)
        }
        val textColor = ContextCompat.getColor(h.itemView.context, textColorRes)
        h.itemView.backgroundTintList = ContextCompat.getColorStateList(h.itemView.context, bgColorRes)
        h.label.setTextColor(textColor)
        h.icon.setImageResource(iconRes)
        h.icon.imageTintList = ContextCompat.getColorStateList(h.itemView.context, textColorRes)

        // Long-press removes the chip - immediate, no confirmation dialog,
        // since adding it back is one tap away via "+ Add".
        h.itemView.setOnLongClickListener {
            val removed = items.removeAt(h.bindingAdapterPosition)
            notifyItemRemoved(h.bindingAdapterPosition)
            onChipRemoved(removed)
            true
        }
    }

    fun addChip(chip: DietChip) {
        items.add(chip)
        notifyItemInserted(items.size - 1) // the new chip lands just before the "+ Add" chip
    }
}
