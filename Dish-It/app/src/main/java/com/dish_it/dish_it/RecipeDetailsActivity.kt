package com.dish_it.dish_it

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dish_it.dish_it.adapters.BulletTextAdapter
import com.dish_it.dish_it.data.AppData
import com.dish_it.dish_it.models.Recipe
import com.dish_it.dish_it.network.RecipeInfoDto
import com.dish_it.dish_it.network.RetrofitProvider
import com.dish_it.dish_it.util.NavTab
import com.dish_it.dish_it.util.bindBottomNav
import kotlinx.coroutines.launch

class RecipeDetailsActivity : AppCompatActivity() {

    companion object {
        /** Key used to pass a Spoonacular recipe id into this screen via Intent extra. */
        const val EXTRA_RECIPE_ID = "extra_recipe_id"
    }

    // Whatever's currently on screen - kept as fields so the Save button
    // (which the person can tap at any point) always has something to save,
    // without re-fetching or re-reading views.
    private var currentRecipe: Recipe? = null
    private var currentIngredients: List<String> = emptyList()       // full text, for the on-screen list
    private var currentIngredientNames: List<String> = emptyList()   // just the product, for Shopping List

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        // A recipe can be opened from Home, Recipe Book, or Meal Planner,
        // so no single bottom tab "owns" this screen - none light up.
        bindBottomNav(this, findViewById(android.R.id.content), NavTab.NONE)

        // Back button
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Save button: adds the current recipe to Recipe Book, and its
        // ingredients to the Shopping List (skipping ones already there).
        findViewById<TextView>(R.id.btn_save).setOnClickListener {
            saveCurrentRecipe()
        }

        // Recipe Book and Meal Planner still use local dummy Recipe objects
        // without a real Spoonacular id (id defaults to 0), so this screen
        // only fetches real data when a genuine id was actually passed in -
        // otherwise it falls back to the original hardcoded example.
        val recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, 0)
        if (recipeId > 0) {
            loadRecipeFromApi(recipeId)
        } else {
            showDummyRecipe()
        }
    }

    private fun loadRecipeFromApi(recipeId: Int) {
        lifecycleScope.launch {
            try {
                val info = RetrofitProvider.api.getRecipeInformation(
                    id = recipeId,
                    apiKey = BuildConfig.SPOONACULAR_API_KEY
                )
                showRecipe(info)
            } catch (e: Exception) {
                Toast.makeText(this@RecipeDetailsActivity, "Couldn't load this recipe: ${e.message}", Toast.LENGTH_LONG).show()
                showDummyRecipe() // better to show SOMETHING than a blank screen
            }
        }
    }

    private fun showRecipe(info: RecipeInfoDto) {
        findViewById<TextView>(R.id.tv_recipe_name).text = info.title
        Glide.with(this)
            .load(info.image)
            .centerCrop()
            .into(findViewById(R.id.img_recipe_banner))

        // preparationMinutes/cookingMinutes often come back as -1 or null when
        // Spoonacular doesn't have that split - fall back to the total time.
        val total = info.readyInMinutes ?: 0
        val prep = info.preparationMinutes?.takeIf { it > 0 } ?: total
        val cook = info.cookingMinutes?.takeIf { it > 0 } ?: total

        findViewById<TextView>(R.id.tv_prep_time).text = "Preparation: $prep minutes"
        findViewById<TextView>(R.id.tv_cook_time).text = "Cooking: $cook minutes"
        findViewById<TextView>(R.id.tv_total_time).text = "Total: $total minutes"
        findViewById<TextView>(R.id.tv_serving).text = "Serving: ${info.servings ?: 1} person"

        val ingredients = info.extendedIngredients?.map { it.original } ?: emptyList()
        findViewById<RecyclerView>(R.id.rv_ingredients).adapter = BulletTextAdapter(ingredients)

        // Shopping List wants just the product ("Eggs"), not the full
        // instruction ("2 large eggs") - Spoonacular gives us both separately.
        currentIngredientNames = info.extendedIngredients
            ?.mapNotNull { it.name?.trim()?.takeIf { name -> name.isNotEmpty() } }
            ?.map { it.replaceFirstChar { c -> c.uppercase() } }
            ?: emptyList()

        val steps = info.analyzedInstructions
            ?.firstOrNull()
            ?.steps
            ?.sortedBy { it.number }
            ?.map { it.step }
            ?: emptyList()
        findViewById<RecyclerView>(R.id.rv_instructions).adapter = BulletTextAdapter(
            steps.ifEmpty { listOf("No step-by-step instructions were provided for this recipe.") }
        )

        // Remember what's on screen so the Save button has something to work with.
        currentIngredients = ingredients
        currentRecipe = Recipe(
            title = info.title,
            imageUrl = info.image ?: "",
            timeMinutes = total,
            servings = info.servings ?: 1,
            rating = (info.spoonacularScore ?: 0.0) / 20.0,
            tag = info.dishTypes?.firstOrNull()?.replaceFirstChar { it.uppercase() },
            id = info.id
        )
    }

    /** The screen's original placeholder content - shown when no real recipe id was passed in. */
    private fun showDummyRecipe() {
        val ingredients = listOf(
            "2 Eggs",
            "1/2 Chopped onion",
            "2 Tomatoes",
            "1/2 Cup Parsley",
            "Any seasoning of your choice"
        )
        findViewById<RecyclerView>(R.id.rv_ingredients).adapter = BulletTextAdapter(ingredients)

        val instructions = listOf(
            "First fry your onion",
            "Add Chopped tomatoes fry until ready",
            "Put aside and fry your eggs after washing the pan",
            "Serve up to two people"
        )
        findViewById<RecyclerView>(R.id.rv_instructions).adapter = BulletTextAdapter(instructions)

        currentIngredients = ingredients
        currentIngredientNames = listOf("Eggs", "Onion", "Tomatoes", "Parsley", "Seasoning")
        currentRecipe = Recipe(
            title = "Simple Breakfast",
            imageUrl = "",
            timeMinutes = 8,
            servings = 1,
            rating = 0.0,
            tag = "Breakfast"
        )
    }

    private fun saveCurrentRecipe() {
        val recipe = currentRecipe ?: return
        val wasAlreadySaved = AppData.isRecipeSaved(recipe.id)
        val addedCount = AppData.saveRecipeAndAddIngredients(recipe, currentIngredientNames)

        val message = when {
            wasAlreadySaved -> "Already in your Recipe Book - added $addedCount new item(s) to Shopping List"
            else -> "Saved to Recipe Book - added $addedCount item(s) to Shopping List"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
