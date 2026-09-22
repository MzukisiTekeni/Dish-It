package com.dish_it.dish_it.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.R
import com.dish_it.dish_it.models.ShoppingItem

class ShoppingItemAdapter(
    private val items: MutableList<ShoppingItem>,
    private val onCheckedChanged: (ShoppingItem) -> Unit,
    private val onEditClicked: (ShoppingItem) -> Unit
) : RecyclerView.Adapter<ShoppingItemAdapter.ShoppingViewHolder>() {

    class ShoppingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_item)
        val name: TextView = itemView.findViewById(R.id.tv_item_name)
        val editButton: ImageView = itemView.findViewById(R.id.btn_edit_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping_item, parent, false)
        return ShoppingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingViewHolder, position: Int) {
        val item = items[position]

        // Setting the checkbox's state can itself fire a "checked changed"
        // callback, which would re-trigger this same bind - clear the
        // listener first so restoring state on a recycled row doesn't
        // accidentally look like the user just tapped it.
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.name.text = item.name
        holder.checkbox.isChecked = item.isChecked

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            item.isChecked = isChecked
            onCheckedChanged(item)
        }

        holder.editButton.setOnClickListener { onEditClicked(item) }
    }

    override fun getItemCount(): Int = items.size
}
