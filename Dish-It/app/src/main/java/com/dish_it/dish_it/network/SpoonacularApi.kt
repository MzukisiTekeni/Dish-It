package com.dish_it.dish_it.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Describes the Spoonacular endpoints this app needs. Retrofit generates
 * the real networking code for this interface automatically - you never
 * implement these functions yourself.
 */
interface SpoonacularApi {

    /** Powers the search bar - only called once the person actually searches. */
    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String,
        @Query("number") number: Int = 10,
        @Query("addRecipeInformation") addRecipeInformation: Boolean = true
    ): SearchResponse

    /** Powers the "Popular recipes" carousel shown before any search happens. */
    @GET("recipes/random")
    suspend fun getRandomRecipes(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int = 5
    ): RandomRecipesResponse

    /** Powers Recipe Details AND Health Score - the FULL recipe: ingredients, steps, everything. */
    @GET("recipes/{id}/information")
    suspend fun getRecipeInformation(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String,
        @Query("includeNutrition") includeNutrition: Boolean = true
    ): RecipeInfoDto
}

// --- Response shapes, matching Spoonacular's JSON field-for-field ---

data class SearchResponse(
    val results: List<RecipeDto>
)

data class RandomRecipesResponse(
    val recipes: List<RecipeDto>
)

/**
 * One recipe, as returned by search/random. Both endpoints return recipes
 * in (roughly) this same shape, so one DTO covers both - fields that don't
 * apply to a given endpoint just come back null and are ignored.
 */
data class RecipeDto(
    val id: Int,
    val title: String,
    val image: String?,           // full photo URL - already hosted by Spoonacular
    val readyInMinutes: Int?,
    val servings: Int?,
    val spoonacularScore: Double?, // 0-100
    val dishTypes: List<String>?  // e.g. ["breakfast", "brunch"]
)

/**
 * The FULL recipe, as returned by /recipes/{id}/information - this is what
 * Recipe Details is built from. Has everything RecipeDto has, plus the
 * actual ingredients and cooking steps.
 */
data class RecipeInfoDto(
    val id: Int,
    val title: String,
    val image: String?,
    val servings: Int?,
    val readyInMinutes: Int?,
    val preparationMinutes: Int?,   // often -1 (Spoonacular doesn't always know this split out)
    val cookingMinutes: Int?,       // same as above
    val spoonacularScore: Double?,  // 0-100 - same field complexSearch/random return
    val healthScore: Double?,       // 0-100 - powers the Health Score screen's ring
    val dishTypes: List<String>?,   // e.g. ["breakfast", "brunch"]
    val extendedIngredients: List<IngredientDto>?,
    val analyzedInstructions: List<InstructionGroupDto>?,
    val nutrition: NutritionDto?    // only populated when includeNutrition=true was passed
)

data class NutritionDto(
    val nutrients: List<NutrientDto>?
)

data class NutrientDto(
    val name: String,
    val amount: Double,
    val unit: String
)

data class IngredientDto(
    val original: String,  // full display text, e.g. "2 large eggs" - for Recipe Details
    val name: String?      // just the product, e.g. "eggs" - for the Shopping List
)

data class InstructionGroupDto(
    val steps: List<InstructionStepDto>
)

data class InstructionStepDto(
    val number: Int,
    val step: String
)
