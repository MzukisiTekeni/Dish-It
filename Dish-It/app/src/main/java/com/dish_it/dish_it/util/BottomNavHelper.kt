package com.dish_it.dish_it.util

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.dish_it.dish_it.FindRecipesActivity
import com.dish_it.dish_it.MealPlannerActivity
import com.dish_it.dish_it.ProfileActivity
import com.dish_it.dish_it.R
import com.dish_it.dish_it.RecipeBookActivity

/**
 * Which bottom-nav tab (if any) represents the current screen.
 * Health Score and Grocery List live in the drawer now, not the bottom
 * bar, so a screen reached from the drawer passes NONE - no tab lights up.
 */
enum class NavTab { HOME, RECIPE_BOOK, MEAL_PLANNER, PROFILE, NONE }

/**
 * Call this once in onCreate(), AFTER setContentView(), on every screen
 * that includes layout_bottom_nav.xml. It does two things:
 *   1. Colours the icon+label for `active` maroon, leaves the rest grey.
 *   2. Wires each tab so tapping it opens that screen.
 *
 * rootView: any view inside the inflated layout (e.g. the whole
 * ConstraintLayout you pass to setContentView) - used to find the
 * nav_* ids via findViewById, the same way you'd look up any view.
 */
fun bindBottomNav(activity: Activity, rootView: View, active: NavTab) {
    val maroon = ContextCompat.getColor(activity, R.color.maroon_primary)
    val grey = ContextCompat.getColor(activity, R.color.text_gray)

    fun style(iconId: Int, labelId: Int, isActive: Boolean) {
        val color = if (isActive) maroon else grey
        rootView.findViewById<ImageView>(iconId).setColorFilter(color)
        rootView.findViewById<TextView>(labelId).setTextColor(color)
    }

    style(R.id.nav_home_icon, R.id.nav_home_label, active == NavTab.HOME)
    style(R.id.nav_recipe_book_icon, R.id.nav_recipe_book_label, active == NavTab.RECIPE_BOOK)
    style(R.id.nav_meal_planner_icon, R.id.nav_meal_planner_label, active == NavTab.MEAL_PLANNER)
    style(R.id.nav_profile_icon, R.id.nav_profile_label, active == NavTab.PROFILE)

    // Tapping a tab that isn't already active opens that screen.
    // Swap FindRecipesActivity for whatever your real "Home" screen ends up being.
    rootView.findViewById<View>(R.id.nav_home).setOnClickListener {
        if (active != NavTab.HOME) activity.startActivity(Intent(activity, FindRecipesActivity::class.java))
    }
    rootView.findViewById<View>(R.id.nav_recipe_book).setOnClickListener {
        if (active != NavTab.RECIPE_BOOK) activity.startActivity(Intent(activity, RecipeBookActivity::class.java))
    }
    rootView.findViewById<View>(R.id.nav_meal_planner).setOnClickListener {
        if (active != NavTab.MEAL_PLANNER) activity.startActivity(Intent(activity, MealPlannerActivity::class.java))
    }
    rootView.findViewById<View>(R.id.nav_profile).setOnClickListener {
        if (active != NavTab.PROFILE) activity.startActivity(Intent(activity, ProfileActivity::class.java))
    }
}
