package com.dish_it.dish_it.models

/**
 * One collapsible section on Shopping List - all the ingredients that came
 * from a single saved recipe. Built at display time in ShoppingListActivity
 * by grouping the flat AppData.shoppingItems list by recipeTitle - this
 * isn't stored anywhere itself.
 *
 * Note there's no `isExpanded` field here any more - only one group can be
 * open at a time, and ShoppingGroupAdapter tracks THAT as a single position
 * rather than each group tracking its own state.
 */
data class ShoppingGroup(
    val recipeTitle: String,
    val items: List<ShoppingItem>
)
