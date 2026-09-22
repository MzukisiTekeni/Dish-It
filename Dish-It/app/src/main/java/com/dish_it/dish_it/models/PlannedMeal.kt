package com.dish_it.dish_it.models

enum class MealType { BREAKFAST, LUNCH, DINNER }

/** One meal slot on one day - `recipe` is null when nothing's planned for it. */
data class PlannedMeal(
    val type: MealType,
    val recipe: Recipe?
)
