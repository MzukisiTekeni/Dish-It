package com.dish_it.dish_it.models

data class Recipe(
    // Defaults on every field (not just `tag` and `id`) are needed so Firestore's
    // toObject(Recipe::class.java) can deserialize with its no-arg-constructor
    // requirement - not just a "why not" cleanup.
    val title: String = "",
    val imageUrl: String = "",   // or a @DrawableRes Int if loading from local drawables
    val timeMinutes: Int = 0,
    val servings: Int = 0,
    val rating: Double = 0.0,
    val tag: String? = null, // e.g. "Breakfast" / "Lunch" / "Dinner" / "Dessert" - null hides the chip
    val id: Int = 0          // Spoonacular's recipe id - 0 for the app's local dummy recipes
)