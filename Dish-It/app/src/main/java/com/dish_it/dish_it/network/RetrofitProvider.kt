package com.dish_it.dish_it.network

import com.dish_it.dish_it.models.Recipe
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * One shared Retrofit client for the whole app. `by lazy` means this isn't
 * actually built until the first time RetrofitProvider.api is used.
 */
object RetrofitProvider {

    val api: SpoonacularApi by lazy {
        // Prints every request/response to Logcat - filter by tag "OkHttp" to see them.
        // Remove this (and the addInterceptor line) once you don't need it any more.
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpoonacularApi::class.java)
    }
}

/**
 * Turns what Spoonacular sent (RecipeDto) into what your adapters/layouts
 * already know how to display (Recipe). This is the one place that
 * translates between "the API's shape" and "this app's shape".
 */
fun RecipeDto.toRecipe(): Recipe = Recipe(
    title = title,
    imageUrl = image ?: "",
    timeMinutes = readyInMinutes ?: 0,
    servings = servings ?: 1,
    rating = (spoonacularScore ?: 0.0) / 20.0, // Spoonacular's 0-100 -> a 0-5 star scale
    tag = dishTypes?.firstOrNull()?.replaceFirstChar { it.uppercase() },
    id = id
)