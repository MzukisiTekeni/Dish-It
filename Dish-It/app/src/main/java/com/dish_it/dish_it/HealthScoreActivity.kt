package com.dish_it.dish_it

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.adapters.NutrientAdapter
import com.dish_it.dish_it.adapters.PopularRecipeAdapter
import com.dish_it.dish_it.data.AppData
import com.dish_it.dish_it.models.Nutrient
import com.dish_it.dish_it.models.Recipe
import com.dish_it.dish_it.network.RetrofitProvider
import com.dish_it.dish_it.util.CarouselAutoScroller
import com.dish_it.dish_it.util.NavTab
import com.dish_it.dish_it.util.bindBottomNav
import com.dish_it.dish_it.views.CircularProgressView
import kotlinx.coroutines.launch

class HealthScoreActivity : AppCompatActivity() {

    private var carouselScroller: CarouselAutoScroller? = null
    private var savedRecipes: List<Recipe> = emptyList()

    // Avoids a slow/stale network response overwriting the screen after the
    // person has already swiped on to a different recipe.
    private var currentlyShownRecipeId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_score)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        // Health Score lives in the drawer, not the bottom bar, so no
        // bottom tab lights up here - but the 4 tabs still need to work
        // when tapped, which is what bindBottomNav also wires up.
        bindBottomNav(this, drawerLayout, NavTab.NONE)

        findViewById<ImageView>(R.id.btn_menu).setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }
        findViewById<View>(R.id.drawer_grocery_list).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START)
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }
        findViewById<View>(R.id.drawer_health_score).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START) // already here
        }

        savedRecipes = AppData.savedRecipes
        val emptyState = findViewById<View>(R.id.tv_empty_state)
        val contentGroup = findViewById<View>(R.id.content_group)

        if (savedRecipes.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            contentGroup.visibility = View.GONE
            return
        }

        emptyState.visibility = View.GONE
        contentGroup.visibility = View.VISIBLE
        setupCarousel()
    }

    private fun setupCarousel() {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_saved_recipes)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = PopularRecipeAdapter(savedRecipes) { recipe ->
            startActivity(
                Intent(this, RecipeDetailsActivity::class.java)
                    .putExtra(RecipeDetailsActivity.EXTRA_RECIPE_ID, recipe.id)
            )
        }

        // onPageChanged fires for the initial card too (position 0), which is
        // what loads the very first recipe's score - no separate call needed.
        val scroller = CarouselAutoScroller(
            recyclerView = recyclerView,
            dotsContainer = findViewById(R.id.carousel_dots),
            intervalMs = 8000, // slower than Find Recipes' default (4000ms) - there's
                                // more to read here (score + nutrients) before advancing
            onPageChanged = { position -> loadHealthDataFor(savedRecipes[position]) }
        )
        carouselScroller = scroller
        scroller.setup(savedRecipes.size)
    }

    override fun onResume() {
        super.onResume()
        carouselScroller?.start()
    }

    override fun onPause() {
        super.onPause()
        carouselScroller?.stop()
    }

    /** Fetches this recipe's health score + nutrients from Spoonacular and displays them. */
    private fun loadHealthDataFor(recipe: Recipe) {
        currentlyShownRecipeId = recipe.id
        val nutrientsList = findViewById<RecyclerView>(R.id.rv_nutrients)
        val loadingLabel = findViewById<TextView>(R.id.tv_nutrients_loading)

        // A recipe with no real Spoonacular id (shouldn't normally happen -
        // every saved recipe comes from a real API result) has nothing to
        // fetch, so don't even try.
        if (recipe.id <= 0) {
            showScore(0.0)
            nutrientsList.adapter = NutrientAdapter(emptyList())
            loadingLabel.visibility = View.VISIBLE
            loadingLabel.text = "No health data available for this recipe."
            return
        }

        loadingLabel.visibility = View.VISIBLE
        loadingLabel.text = "Loading nutrition info..."
        nutrientsList.adapter = NutrientAdapter(emptyList())

        lifecycleScope.launch {
            try {
                val info = RetrofitProvider.api.getRecipeInformation(
                    id = recipe.id,
                    apiKey = BuildConfig.SPOONACULAR_API_KEY,
                    includeNutrition = true
                )

                // The person may have already swiped to a different card by
                // the time this response comes back - if so, ignore it.
                if (recipe.id != currentlyShownRecipeId) return@launch

                showScore((info.healthScore ?: 0.0) / 10.0)

                val nutrients = info.nutrition?.nutrients
                    ?.map { Nutrient(it.name, "%.2f %s".format(it.amount, it.unit)) }
                    ?: emptyList()
                nutrientsList.adapter = NutrientAdapter(nutrients)
                loadingLabel.visibility = if (nutrients.isEmpty()) View.VISIBLE else View.GONE
                loadingLabel.text = "No nutrition info available for this recipe."
            } catch (e: Exception) {
                if (recipe.id != currentlyShownRecipeId) return@launch
                Toast.makeText(this@HealthScoreActivity, "Couldn't load health data: ${e.message}", Toast.LENGTH_SHORT).show()
                loadingLabel.visibility = View.VISIBLE
                loadingLabel.text = "Couldn't load nutrition info."
            }
        }
    }

    /** score is 0.0..10.0, matching what's shown as "x.x/10" on screen. */
    private fun showScore(score: Double) {
        val clamped = score.coerceIn(0.0, 10.0)

        // Same High/Medium/Low thresholds as the legend under the ring.
        val (colorRes, label) = when {
            clamped >= 7.0 -> R.color.health_high to "Very Healthy"
            clamped >= 4.0 -> R.color.health_medium to "Moderately Healthy"
            else -> R.color.health_low to "Needs Improvement"
        }
        val color = ContextCompat.getColor(this, colorRes)

        findViewById<CircularProgressView>(R.id.health_ring).apply {
            progress = (clamped / 10.0 * 100).toFloat() // ring's scale is 0..100
            progressColor = color
        }
        findViewById<TextView>(R.id.tv_score).apply {
            text = "%.1f".format(clamped)
            setTextColor(color)
        }
        findViewById<TextView>(R.id.tv_score_label).apply {
            text = label
            setTextColor(color)
        }
    }
}
