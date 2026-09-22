package com.dish_it.dish_it.util

import com.dish_it.dish_it.R
import com.dish_it.dish_it.models.MealType

/**
 * How each meal type is displayed - kept as extension functions here
 * (rather than fields on the MealType enum itself) so models/ stays plain
 * data with no Android resource references, matching the rest of the app.
 */
fun MealType.label(): String = when (this) {
    MealType.BREAKFAST -> "Breakfast"
    MealType.LUNCH -> "Lunch"
    MealType.DINNER -> "Dinner"
}

fun MealType.iconRes(): Int = when (this) {
    MealType.BREAKFAST -> R.drawable.ic_breakfast
    MealType.LUNCH -> R.drawable.ic_lunch
    MealType.DINNER -> R.drawable.ic_dinner
}

fun MealType.colorRes(): Int = when (this) {
    MealType.BREAKFAST -> R.color.tag_breakfast_text
    MealType.LUNCH -> R.color.tag_lunch_text
    MealType.DINNER -> R.color.tag_dinner_text
}
