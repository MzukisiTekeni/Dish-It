package com.dish_it.dish_it.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // remove if you're not using Glide - swap for your own image loader
import com.dish_it.dish_it.R
import com.dish_it.dish_it.models.Recipe

/**
 * Powers rv_popular on activity_find_recipes.xml (item_popular_recipe.xml).
 */
class PopularRecipeAdapter(
    private val items: List<Recipe>,
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<PopularRecipeAdapter.PopularViewHolder>() {

    class PopularViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.img_popular)
        val title: TextView = itemView.findViewById(R.id.tv_popular_title)
        val time: TextView = itemView.findViewById(R.id.tv_popular_time)
        val servings: TextView = itemView.findViewById(R.id.tv_popular_servings)
        val rating: TextView = itemView.findViewById(R.id.tv_popular_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular_recipe, parent, false)
        return PopularViewHolder(view)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        val recipe = items[position]
        holder.title.text = recipe.title
        holder.time.text = "${recipe.timeMinutes} Minutes"
        holder.servings.text = recipe.servings.toString()
        holder.rating.text = "%.1f".format(recipe.rating)

        Glide.with(holder.image.context)
            .load(recipe.imageUrl)
            .centerCrop()
            .into(holder.image)

        holder.itemView.setOnClickListener { onClick(recipe) }
    }

    override fun getItemCount(): Int = items.size
}
