package com.dish_it.dish_it.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.R
import com.dish_it.dish_it.models.ShoppingGroup
import com.dish_it.dish_it.models.ShoppingItem

/**
 * Powers rv_shopping_groups on Shopping List - one card per recipe, each
 * expandable/collapsible to reveal that recipe's ingredients underneath.
 *
 * Only ONE group is ever expanded at a time (an "accordion") - that's why
 * expand state is tracked here as a single `expandedPosition`, rather than
 * each ShoppingGroup carrying its own isExpanded flag. Opening a new group
 * automatically closes whichever one was previously open.
 *
 * The ingredient rows INSIDE a group are deliberately NOT a nested
 * RecyclerView. A recipe's ingredient list is short and doesn't change
 * shape once bound, so it's simpler (and avoids the classic headaches of
 * putting a RecyclerView inside a RecyclerView) to just build plain rows
 * in a loop every time a group is bound.
 */
class ShoppingGroupAdapter(
    private val groups: List<ShoppingGroup>,
    private val onCheckedChanged: (ShoppingItem) -> Unit,
    private val onEditClicked: (ShoppingItem) -> Unit
) : RecyclerView.Adapter<ShoppingGroupAdapter.GroupViewHolder>() {

    // Starts with the first group open, matching the old default - -1 would
    // mean "none expanded" if you'd rather start fully collapsed instead.
    private var expandedPosition: Int = if (groups.isNotEmpty()) 0 else -1

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val header: View = itemView.findViewById(R.id.group_header)
        val chevron: ImageView = itemView.findViewById(R.id.img_chevron)
        val title: TextView = itemView.findViewById(R.id.tv_group_title)
        val count: TextView = itemView.findViewById(R.id.tv_group_count)
        val itemsContainer: LinearLayout = itemView.findViewById(R.id.items_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]

        holder.title.text = group.recipeTitle
        holder.count.text = "${group.items.size} item${if (group.items.size == 1) "" else "s"}"

        applyExpandedState(holder, position == expandedPosition)
        buildIngredientRows(holder, group)

        holder.header.setOnClickListener {
            val clickedPosition = holder.bindingAdapterPosition
            if (clickedPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val previouslyExpanded = expandedPosition
            // Tapping the already-open group closes it; tapping any other
            // group opens IT and closes whatever was open before.
            expandedPosition = if (expandedPosition == clickedPosition) -1 else clickedPosition

            if (previouslyExpanded != -1) notifyItemChanged(previouslyExpanded)
            notifyItemChanged(clickedPosition)
        }
    }

    private fun applyExpandedState(holder: GroupViewHolder, expanded: Boolean) {
        holder.itemsContainer.visibility = if (expanded) View.VISIBLE else View.GONE
        holder.chevron.rotation = if (expanded) 90f else 0f
    }

    private fun buildIngredientRows(holder: GroupViewHolder, group: ShoppingGroup) {
        holder.itemsContainer.removeAllViews()
        val inflater = LayoutInflater.from(holder.itemsContainer.context)

        for (item in group.items) {
            val row = inflater.inflate(R.layout.item_shopping_item, holder.itemsContainer, false)
            val checkbox = row.findViewById<CheckBox>(R.id.checkbox_item)
            val name = row.findViewById<TextView>(R.id.tv_item_name)
            val editButton = row.findViewById<ImageView>(R.id.btn_edit_item)

            // Same "clear the listener before setting state" pattern as
            // before - avoids programmatically setting isChecked looking
            // like a real tap and re-firing the callback.
            checkbox.setOnCheckedChangeListener(null)
            name.text = item.name
            checkbox.isChecked = item.isChecked
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                onCheckedChanged(item)
            }

            editButton.setOnClickListener { onEditClicked(item) }

            holder.itemsContainer.addView(row)
        }
    }

    override fun getItemCount(): Int = groups.size
}
