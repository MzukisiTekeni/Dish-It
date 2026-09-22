package com.dish_it.dish_it.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.R

/**
 * Powers both rv_ingredients and rv_instructions on activity_recipe_details.xml.
 * Each item is just a line of text with a bullet dot in front of it.
 */
class BulletTextAdapter(private val items: List<String>) :
    RecyclerView.Adapter<BulletTextAdapter.BulletViewHolder>() {

    class BulletViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.tv_bullet_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BulletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bullet_text, parent, false)
        return BulletViewHolder(view)
    }

    override fun onBindViewHolder(holder: BulletViewHolder, position: Int) {
        holder.text.text = items[position]
    }

    override fun getItemCount(): Int = items.size
}
