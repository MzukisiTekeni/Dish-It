package com.dish_it.dish_it.data

import com.dish_it.dish_it.models.MealType
import com.dish_it.dish_it.models.PlannedMeal
import com.dish_it.dish_it.models.Recipe
import java.util.Calendar

/**
 * Stand-in for a real backend/local database of meal plans. Given a date,
 * deterministically returns what's "planned" for it - the Day, Week, and
 * Month tabs all call this same function, so they always agree with each
 * other. Swap the body of plansFor() for a real repository/API call once
 * meal planning is backed by something persistent - nothing that READS
 * from this object would need to change, only what's inside it.
 */
object MealPlanDemoData {

    private val breakfasts = listOf(
        Recipe("English Breakfast", "https://example.com/1.jpg", 20, 2, 4.5, tag = "Breakfast"),
        Recipe("Avocado Toast", "https://example.com/2.jpg", 10, 1, 4.1, tag = "Breakfast"),
        Recipe("Simple Breakfast", "https://example.com/3.jpg", 8, 1, 4.2, tag = "Breakfast")
    )
    private val lunches = listOf(
        Recipe("Green Steak Salad", "https://example.com/4.jpg", 30, 2, 4.8, tag = "Lunch"),
        Recipe("Chicken Wrap", "https://example.com/5.jpg", 20, 1, 4.3, tag = "Lunch")
    )
    private val dinners = listOf(
        Recipe("Grilled Salmon", "https://example.com/6.jpg", 35, 2, 4.6, tag = "Dinner"),
        Recipe("Veggie Pasta", "https://example.com/7.jpg", 25, 3, 4.2, tag = "Dinner")
    )

    /** A stable integer for a given date, used to deterministically vary the demo data. */
    private fun dayKey(calendar: Calendar): Int =
        calendar.get(Calendar.YEAR) * 400 + calendar.get(Calendar.DAY_OF_YEAR)

    fun plansFor(calendar: Calendar): List<PlannedMeal> {
        val key = dayKey(calendar)

        fun pick(type: MealType, options: List<Recipe>, seed: Int): Recipe? {
            // Roughly 1 in 4 slots is deliberately left unplanned, so the
            // "Add a meal" empty state actually shows up somewhere in the demo.
            if ((key + seed) % 4 == 0) return null
            return options[(key + seed) % options.size]
        }

        return listOf(
            PlannedMeal(MealType.BREAKFAST, pick(MealType.BREAKFAST, breakfasts, 0)),
            PlannedMeal(MealType.LUNCH, pick(MealType.LUNCH, lunches, 1)),
            PlannedMeal(MealType.DINNER, pick(MealType.DINNER, dinners, 2))
        )
    }
}
