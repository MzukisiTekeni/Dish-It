package com.dish_it.dish_it

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.adapters.BulletTextAdapter
class RecipeDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)
        val ingredients = listOf(
            "2 Eggs",
            "1/2 Chopped onion",
            "2 Tomatoes",
            "1/2 Cup Parsley",
            "Any seasoning of your choice"
        )
        findViewById<RecyclerView>(R.id.rv_ingredients).adapter =
            BulletTextAdapter(ingredients)
        val instructions = listOf(
            "First fry your onion",
            "Add Chopped tomatoes fry until ready",
            "Put aside and fry your eggs after washing the pan",
            "Serve up to two people"
        )
        findViewById<RecyclerView>(R.id.rv_instructions).adapter =
            BulletTextAdapter(instructions)
    }
}