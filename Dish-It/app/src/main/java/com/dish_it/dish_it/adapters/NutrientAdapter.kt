package com.dish_it.dish_it.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.R
import com.dish_it.dish_it.models.Nutrient

class NutrientAdapter(private val items: List<Nutrient>) :
    RecyclerView.Adapter<NutrientAdapter.NutrientViewHolder>() {

    class NutrientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_nutrient_name)
        val value: TextView = itemView.findViewById(R.id.tv_nutrient_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutrientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nutrient_row, parent, false)
        return NutrientViewHolder(view)
    }

    override fun onBindViewHolder(holder: NutrientViewHolder, position: Int) {
        holder.name.text = items[position].name
        holder.value.text = items[position].value
    }

    override fun getItemCount(): Int = items.size
}
