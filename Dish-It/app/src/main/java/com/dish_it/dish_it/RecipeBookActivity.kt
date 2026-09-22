package com.dish_it.dish_it

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.adapters.CategoryFilterAdapter
import com.dish_it.dish_it.adapters.RecipeListAdapter
import com.dish_it.dish_it.data.AppData
import com.dish_it.dish_it.models.CategoryFilter
import com.dish_it.dish_it.models.Recipe
import com.dish_it.dish_it.util.NavTab
import com.dish_it.dish_it.util.bindBottomNav

class RecipeBookActivity : AppCompatActivity() {

    private lateinit var recipesHeader: TextView
    private lateinit var recipesList: RecyclerView
    private lateinit var emptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_book)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        bindBottomNav(this, drawerLayout, NavTab.RECIPE_BOOK)

        findViewById<ImageView>(R.id.btn_menu).setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }
        findViewById<View>(R.id.drawer_grocery_list).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START)
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }
        findViewById<View>(R.id.drawer_health_score).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START)
            startActivity(Intent(this, HealthScoreActivity::class.java))
        }

        recipesHeader = findViewById(R.id.tv_recipes_header)
        recipesList = findViewById(R.id.rv_recipes)
        emptyState = findViewById(R.id.tv_empty_state)

        // --- Category filter chips - counts are computed from AppData.savedRecipes
        // every time this screen opens, so they're always accurate. ---
        val saved = AppData.savedRecipes
        val categories = listOf(
            CategoryFilter("All items", saved.size, R.drawable.ic_recipe_book),
            CategoryFilter("Breakfast", saved.count { it.tag == "Breakfast" }, R.drawable.ic_breakfast),
            CategoryFilter("Lunch", saved.count { it.tag == "Lunch" }, R.drawable.ic_lunch),
            CategoryFilter("Dinner", saved.count { it.tag == "Dinner" }, R.drawable.ic_dinner),
            CategoryFilter("Dessert", saved.count { it.tag == "Dessert" }, R.drawable.ic_dessert)
        )
        findViewById<RecyclerView>(R.id.rv_categories).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = CategoryFilterAdapter(categories) { selected ->
                recipesHeader.text = "Recipes (${selected.label})"
                showRecipes(if (selected.label == "All items") saved else saved.filter { it.tag == selected.label })
            }
        }

        // --- Recipe list: starts showing everything that's been saved ---
        showRecipes(saved)
    }

    private fun showRecipes(recipes: List<Recipe>) {
        recipesList.adapter = RecipeListAdapter(recipes) { recipe ->
            startActivity(
                Intent(this, RecipeDetailsActivity::class.java)
                    .putExtra(RecipeDetailsActivity.EXTRA_RECIPE_ID, recipe.id)
            )
        }
        recipesList.visibility = if (recipes.isEmpty()) View.GONE else View.VISIBLE
        emptyState.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
    }
}
