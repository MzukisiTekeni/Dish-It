package com.dish_it.dish_it

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.adapters.PopularRecipeAdapter
import com.dish_it.dish_it.adapters.RecipeListAdapter
import com.dish_it.dish_it.models.Recipe
import com.dish_it.dish_it.network.RetrofitProvider
import com.dish_it.dish_it.network.toRecipe
import com.dish_it.dish_it.util.CarouselAutoScroller
import com.dish_it.dish_it.util.NavTab
import com.dish_it.dish_it.util.bindBottomNav
import kotlinx.coroutines.launch

class FindRecipesActivity : AppCompatActivity() {

    private lateinit var carouselScroller: CarouselAutoScroller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_recipes)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        // Find Recipes IS this app's Home screen, so Home lights up.
        bindBottomNav(this, drawerLayout, NavTab.HOME)

        // Hamburger icon opens the drawer
        findViewById<ImageView>(R.id.btn_menu).setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }

        // Each drawer row: navigate, then close the drawer behind you
        findViewById<View>(R.id.drawer_grocery_list).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START)
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }
        findViewById<View>(R.id.drawer_health_score).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START)
            startActivity(Intent(this, HealthScoreActivity::class.java))
        }

        // --- Popular recipes (horizontal carousel) - a few random real
        // recipes, fetched once when this screen opens ---
        findViewById<RecyclerView>(R.id.rv_popular).layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        carouselScroller = CarouselAutoScroller(
            recyclerView = findViewById(R.id.rv_popular),
            dotsContainer = findViewById(R.id.carousel_dots)
        )
        loadPopularRecipes()

        // --- Search results (vertical list) - EMPTY until the person
        // actually searches for something. ---
        findViewById<RecyclerView>(R.id.rv_search_results).adapter =
            RecipeListAdapter(emptyList()) { recipe -> openRecipeDetails(recipe) }

        // --- Search bar ---
        // Two ways to trigger a search, since different keyboards/devices
        // handle the "Search" IME action inconsistently: tapping the
        // magnifying-glass icon always works, and pressing the keyboard's
        // Search/Done/Enter key is handled as a fallback.
        val searchField = findViewById<EditText>(R.id.et_search)

        findViewById<ImageView>(R.id.btn_search).setOnClickListener {
            runSearchFromField(searchField)
        }

        searchField.setOnEditorActionListener { _, actionId, event ->
            val isSearchTrigger =
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            if (isSearchTrigger) {
                runSearchFromField(searchField)
                true
            } else {
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::carouselScroller.isInitialized) carouselScroller.start()
    }

    override fun onPause() {
        super.onPause()
        if (::carouselScroller.isInitialized) carouselScroller.stop()
    }

    private fun runSearchFromField(searchField: EditText) {
        val query = searchField.text.toString().trim()
        if (query.isNotEmpty()) {
            searchRecipes(query)
        } else {
            Toast.makeText(this, "Type something to search for", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openRecipeDetails(recipe: Recipe) {
        startActivity(
            Intent(this, RecipeDetailsActivity::class.java)
                .putExtra(RecipeDetailsActivity.EXTRA_RECIPE_ID, recipe.id)
        )
    }

    /** Fills the "Popular recipes" carousel with a handful of random recipes. */
    private fun loadPopularRecipes() {
        lifecycleScope.launch {
            try {
                // Ask for more than we need, since we're about to filter most
                // of them out - Spoonacular's /recipes/random has no "minimum
                // rating" parameter, so the filtering happens client-side below.
                // A bigger batch means better odds of finding 5 that qualify.
                val response = RetrofitProvider.api.getRandomRecipes(
                    apiKey = BuildConfig.SPOONACULAR_API_KEY,
                    number = 50
                )
                val allFetched = response.recipes.map { it.toRecipe() }
                val highlyRated = allFetched.filter { it.rating >= 4.0 }.take(5)

                // 4.5+ is a real bar - a batch of 50 can still come back with
                // none that clear it. Rather than leave the carousel empty
                // (which looks broken), fall back to the best-rated ones
                // actually available this time.
                val popular = highlyRated.ifEmpty {
                    allFetched.sortedByDescending { it.rating }.take(5)
                }

                findViewById<RecyclerView>(R.id.rv_popular).adapter =
                    PopularRecipeAdapter(popular) { recipe -> openRecipeDetails(recipe) }
                carouselScroller.setup(popular.size)
            } catch (e: Exception) {
                Toast.makeText(this@FindRecipesActivity, "Couldn't load popular recipes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Runs a real Spoonacular search and fills the Search Results list with it. */
    private fun searchRecipes(query: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitProvider.api.searchRecipes(
                    apiKey = BuildConfig.SPOONACULAR_API_KEY,
                    query = query
                )
                val results = response.results.map { it.toRecipe() }
                findViewById<RecyclerView>(R.id.rv_search_results).adapter =
                    RecipeListAdapter(results) { recipe -> openRecipeDetails(recipe) }
                if (results.isEmpty()) {
                    Toast.makeText(this@FindRecipesActivity, "No recipes found for \"$query\"", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FindRecipesActivity, "Search failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
