package com.dish_it.dish_it.models

data class Nutrient(
    val name: String,
    val value: String // pre-formatted, e.g. "523.65 g" - keeps the adapter trivial
)
