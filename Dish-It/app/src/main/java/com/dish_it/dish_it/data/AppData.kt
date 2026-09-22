package com.dish_it.dish_it.data

import com.dish_it.dish_it.models.MealType
import com.dish_it.dish_it.models.PlannedMeal
import com.dish_it.dish_it.models.Recipe
import com.dish_it.dish_it.models.ShoppingItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * A single, app-wide store for anything that needs to be shared BETWEEN
 * screens - saved recipes (Recipe Book), the shopping list, and now the
 * meal plan (Meal Planner).
 *
 * Why an `object` and not a database: this is intentionally the simplest
 * thing that could work. `object` makes it a singleton - there's only ever
 * one AppData for the whole app, so every Activity that reads
 * `AppData.savedRecipes` sees the exact same list, including changes made
 * by a completely different Activity moments earlier.
 *
 * The trade-off: none of this survives the app's process being killed -
 * unlike a real local database (Room) or DataStore, which would write to
 * disk and survive that. That's a deliberate v1 simplification, not an
 * oversight - swapping this for Room later wouldn't change how any
 * Activity calls it, only what's inside this file.
 */
object AppData {

    val savedRecipes = mutableListOf<Recipe>()

    // Starts genuinely empty now - Shopping List is grouped by recipe, so an
    // item with no recipe behind it wouldn't have anywhere to belong.
    // Everything here comes from tapping "+ Save" on a real recipe.
    val shoppingItems = mutableListOf<ShoppingItem>()

    // Meal Planner's data: which recipe (if any) is assigned to each meal
    // type, on each date. Keyed by a plain "yyyy-MM-dd" string rather than
    // by Calendar itself, since two different Calendar instances for the
    // same day aren't `==` to each other - a String is a much simpler key.
    private val mealPlan = mutableMapOf<String, MutableMap<MealType, Recipe>>()
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private fun dateKey(calendar: Calendar): String = dateKeyFormat.format(calendar.time)

    /**
     * Pulls everything this signed-in user has stored in Firestore into the
     * in-memory lists above. Call this once, right after a successful login,
     * before any screen reads AppData. `onComplete` fires once all three
     * loads have finished (in any order).
     */
    fun loadFromFirestore(uid: String, onComplete: () -> Unit) {
        savedRecipes.clear()
        shoppingItems.clear()
        mealPlan.clear()

        var pending = 3
        fun oneDone() {
            pending--
            if (pending == 0) onComplete()
        }

        FirebaseRepository.loadSavedRecipes(uid) { recipes ->
            savedRecipes.addAll(recipes)
            oneDone()
        }
        FirebaseRepository.loadShoppingItems(uid) { items ->
            shoppingItems.addAll(items)
            oneDone()
        }
        FirebaseRepository.loadMealPlan(uid) { plan ->
            plan.forEach { (date, meals) -> mealPlan[date] = meals.toMutableMap() }
            oneDone()
        }
    }

    /** Clears everything in memory - call this on sign-out so the next user starts blank. */
    fun clearLocalData() {
        savedRecipes.clear()
        shoppingItems.clear()
        mealPlan.clear()
    }

    /** True if a recipe with this Spoonacular id has already been saved. */
    fun isRecipeSaved(recipeId: Int): Boolean =
        savedRecipes.any { it.id == recipeId }

    /**
     * Saves `recipe` to Recipe Book (does nothing if it's already there)
     * and adds each of `ingredients` to the shopping list, grouped under
     * this recipe's title - skipping any ingredient that's already listed
     * under THIS SAME recipe (compared trimmed and case-insensitively).
     *
     * Duplicates are only skipped within the same recipe's group on
     * purpose: if "Eggs" is needed for two different saved recipes, it's
     * genuinely useful to see it listed under both, since each group is
     * that recipe's own shopping checklist.
     *
     * Returns how many ingredients were actually NEW, so the caller can
     * show a meaningful "X items added" message.
     */
    fun saveRecipeAndAddIngredients(recipe: Recipe, ingredients: List<String>): Int {
        val uid = FirebaseRepository.currentUserId

        if (!isRecipeSaved(recipe.id)) {
            savedRecipes.add(recipe)
            if (uid != null) FirebaseRepository.saveRecipe(uid, recipe)
        }

        var addedCount = 0
        for (ingredient in ingredients) {
            val cleaned = ingredient.trim()
            if (cleaned.isEmpty()) continue

            val alreadyInThisGroup = shoppingItems.any {
                it.recipeTitle == recipe.title && it.name.trim().equals(cleaned, ignoreCase = true)
            }
            if (!alreadyInThisGroup) {
                val item = ShoppingItem(name = cleaned, isChecked = false, recipeTitle = recipe.title)
                shoppingItems.add(item)
                if (uid != null) FirebaseRepository.upsertShoppingItem(uid, item)
                addedCount++
            }
        }
        return addedCount
    }

    /** Flips `item.isChecked` and pushes the change to Firestore for the signed-in user. */
    fun setShoppingItemChecked(item: ShoppingItem, checked: Boolean) {
        item.isChecked = checked
        val uid = FirebaseRepository.currentUserId
        if (uid != null) FirebaseRepository.upsertShoppingItem(uid, item)
    }

    /** Removes `item` from the shopping list, locally and in Firestore. */
    fun removeShoppingItem(item: ShoppingItem) {
        shoppingItems.remove(item)
        val uid = FirebaseRepository.currentUserId
        if (uid != null) FirebaseRepository.deleteShoppingItem(uid, item.id)
    }

    /** Assigns `recipe` to `type` on `date` - overwrites whatever was there before. */
    fun setPlannedMeal(date: Calendar, type: MealType, recipe: Recipe) {
        val key = dateKey(date)
        mealPlan.getOrPut(key) { mutableMapOf() }[type] = recipe
        val uid = FirebaseRepository.currentUserId
        if (uid != null) FirebaseRepository.setPlannedMeal(uid, key, type, recipe)
    }

    /** Removes whatever's planned for `type` on `date`, if anything. */
    fun clearPlannedMeal(date: Calendar, type: MealType) {
        val key = dateKey(date)
        mealPlan[key]?.remove(type)
        val uid = FirebaseRepository.currentUserId
        if (uid != null) FirebaseRepository.clearPlannedMeal(uid, key, type)
    }

    /** What's planned for one specific meal type on one date - null if nothing is. */
    fun plannedMeal(date: Calendar, type: MealType): Recipe? =
        mealPlan[dateKey(date)]?.get(type)

    /**
     * All 3 meal slots for one date, bundled together - this is what
     * WeekDayAdapter and MonthDayAdapter (and MealPlannerActivity's Day
     * tab) actually read from, so all three tabs always agree with
     * each other.
     */
    fun plansFor(date: Calendar): List<PlannedMeal> = listOf(
        PlannedMeal(MealType.BREAKFAST, plannedMeal(date, MealType.BREAKFAST)),
        PlannedMeal(MealType.LUNCH, plannedMeal(date, MealType.LUNCH)),
        PlannedMeal(MealType.DINNER, plannedMeal(date, MealType.DINNER))
    )
}
