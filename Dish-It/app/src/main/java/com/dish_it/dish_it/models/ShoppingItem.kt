package com.dish_it.dish_it.models

import java.util.UUID

data class ShoppingItem(
    val name: String = "",
    var isChecked: Boolean = false,
    val recipeTitle: String = "",
    // Firestore document id for this item - generated once when the item is
    // first created, then reused for every update/delete against Firestore.
    val id: String = UUID.randomUUID().toString()
)
