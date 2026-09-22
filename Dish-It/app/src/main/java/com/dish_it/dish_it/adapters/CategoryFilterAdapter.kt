package com.dish_it.dish_it.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.R
import com.dish_it.dish_it.models.CategoryFilter

class CategoryFilterAdapter(
    private val items: List<CategoryFilter>,
    private val onSelected: (CategoryFilter) -> Unit
) : RecyclerView.Adapter<CategoryFilterAdapter.CategoryViewHolder>() {

    // Index 0 ("All items") starts selected, matching the design.
    private var selectedPosition = 0

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: com.google.android.material.card.MaterialCardView =
            itemView.findViewById(R.id.card_category)
        val icon: ImageView = itemView.findViewById(R.id.img_category_icon)
        val count: TextView = itemView.findViewById(R.id.tv_category_count)
        val label: TextView = itemView.findViewById(R.id.tv_category_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_filter, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = items[position]
        holder.count.text = category.count.toString()
        holder.label.text = category.label
        holder.icon.setImageResource(category.iconRes)

        val isSelected = position == selectedPosition
        holder.card.setCardBackgroundColor(
            holder.itemView.context.getColor(
                if (isSelected) R.color.divider_light else R.color.card_white
            )
        )

        holder.itemView.setOnClickListener {
            val previouslySelected = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previouslySelected)
            notifyItemChanged(selectedPosition)
            onSelected(category)
        }
    }

    override fun getItemCount(): Int = items.size
}
