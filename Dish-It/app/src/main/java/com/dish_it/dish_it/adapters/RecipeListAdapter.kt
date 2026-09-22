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
 * Powers rv_search_results on activity_find_recipes.xml, and the recipe/meal
 * rows on Recipe Book and Meal Planner (all use item_recipe_horizontal.xml).
 */
class RecipeListAdapter(
    private val items: List<Recipe>,
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.img_recipe_thumb)
        val title: TextView = itemView.findViewById(R.id.tv_recipe_title)
        val tag: TextView = itemView.findViewById(R.id.tv_recipe_tag)
        val time: TextView = itemView.findViewById(R.id.tv_time)
        val servings: TextView = itemView.findViewById(R.id.tv_servings)
        val rating: TextView = itemView.findViewById(R.id.tv_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_horizontal, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = items[position]
        holder.title.text = recipe.title
        holder.time.text = "${recipe.timeMinutes} Minutes"
        holder.servings.text = recipe.servings.toString()
        holder.rating.text = "%.1f".format(recipe.rating)

        // Tag chip only shows up when this recipe actually has one -
        // Search Results passes null, Recipe Book / Meal Planner pass a value.
        if (recipe.tag != null) {
            holder.tag.visibility = View.VISIBLE
            holder.tag.text = recipe.tag
            val bg = when (recipe.tag) {
                "Breakfast" -> R.drawable.bg_tag_breakfast
                "Lunch" -> R.drawable.bg_tag_lunch
                "Dinner" -> R.drawable.bg_tag_dinner
                "Dessert" -> R.drawable.bg_tag_dessert
                else -> R.drawable.bg_tag_breakfast
            }
            holder.tag.setBackgroundResource(bg)
        } else {
            holder.tag.visibility = View.GONE
        }

        Glide.with(holder.image.context)
            .load(recipe.imageUrl)
            .centerCrop()
            .into(holder.image)

        holder.itemView.setOnClickListener { onClick(recipe) }
    }

    override fun getItemCount(): Int = items.size
}
