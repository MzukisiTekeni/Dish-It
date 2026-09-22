package com.dish_it.dish_it.data

import com.dish_it.dish_it.models.ChipStyle
import com.dish_it.dish_it.models.DietChip
import com.dish_it.dish_it.models.MealType
import com.dish_it.dish_it.models.Recipe
import com.dish_it.dish_it.models.ShoppingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Everything that talks to Firebase (Authentication + Cloud Firestore) lives
 * here - Activities and AppData never call FirebaseAuth/FirebaseFirestore
 * directly, they go through this object instead.
 *
 * Firestore layout:
 *   usernames/{username}          -> { uid, email }   (lets Login look an email up by username,
 *                                                        and lets Registration check "is this
 *                                                        username taken?" before creating a user)
 *   users/{uid}                   -> { fullName, username, email, defaultServings, createdAt }
 *   users/{uid}/savedRecipes/{id} -> one Recipe
 *   users/{uid}/shoppingItems/{id}-> one ShoppingItem
 *   users/{uid}/mealPlan/{yyyy-MM-dd} -> { breakfast: {...}?, lunch: {...}?, dinner: {...}? }
 *   users/{uid}/dietChips/{id}    -> { label, style }
 */
object FirebaseRepository {

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    private fun userDoc(uid: String) = db.collection("users").document(uid)
    private fun savedRecipesCol(uid: String) = userDoc(uid).collection("savedRecipes")
    private fun shoppingItemsCol(uid: String) = userDoc(uid).collection("shoppingItems")
    private fun mealPlanCol(uid: String) = userDoc(uid).collection("mealPlan")
    private fun dietChipsCol(uid: String) = userDoc(uid).collection("dietChips")

    // ----------------------------------------------------------------
    // Auth
    // ----------------------------------------------------------------

    /**
     * Registers a brand-new account. Checks the `usernames` collection first
     * so two people can't sign up with the same username, then creates the
     * Firebase Auth user, then writes their profile to `users/{uid}` and
     * reserves `usernames/{username}`.
     */
    fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val usernameKey = username.trim().lowercase()
        db.collection("usernames").document(usernameKey).get()
            .addOnSuccessListener { existing ->
                if (existing.exists()) {
                    onError("That username is already taken.")
                    return@addOnSuccessListener
                }

                auth.createUserWithEmailAndPassword(email.trim(), password)
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid ?: run {
                            onError("Something went wrong creating your account.")
                            return@addOnSuccessListener
                        }

                        val profile = hashMapOf(
                            "fullName" to fullName.trim(),
                            "username" to username.trim(),
                            "email" to email.trim(),
                            "defaultServings" to "2 people",
                            "createdAt" to System.currentTimeMillis()
                        )

                        val usernameLookup = hashMapOf(
                            "uid" to uid,
                            "email" to email.trim()
                        )

                        userDoc(uid).set(profile)
                            .addOnSuccessListener {
                                db.collection("usernames").document(usernameKey).set(usernameLookup)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { e ->
                                        onError(e.message ?: "Couldn't reserve username.")
                                    }
                            }
                            .addOnFailureListener { e ->
                                onError(e.message ?: "Couldn't save your profile.")
                            }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Couldn't create your account.")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Couldn't reach the server.")
            }
    }

    /**
     * Logs in with the app's "Username" field: looks up the email that
     * belongs to that username, then signs in with Firebase Auth's
     * email/password flow (Firebase Auth itself only knows email/password).
     */
    fun login(
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val usernameKey = username.trim().lowercase()
        db.collection("usernames").document(usernameKey).get()
            .addOnSuccessListener { doc ->
                val email = doc.getString("email")
                if (email == null) {
                    onError("No account found for that username.")
                    return@addOnSuccessListener
                }

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Incorrect username or password.")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Couldn't reach the server.")
            }
    }

    fun signOut() = auth.signOut()

    // ----------------------------------------------------------------
    // Saved recipes
    // ----------------------------------------------------------------

    fun saveRecipe(uid: String, recipe: Recipe) {
        savedRecipesCol(uid).document(recipe.id.toString()).set(recipe)
    }

    fun loadSavedRecipes(uid: String, onResult: (List<Recipe>) -> Unit) {
        savedRecipesCol(uid).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.documents.mapNotNull {
                    it.toObject(Recipe::class.java)
                })
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    // ----------------------------------------------------------------
    // Shopping list
    // ----------------------------------------------------------------

    fun upsertShoppingItem(uid: String, item: ShoppingItem) {
        shoppingItemsCol(uid).document(item.id).set(item)
    }

    fun deleteShoppingItem(uid: String, itemId: String) {
        shoppingItemsCol(uid).document(itemId).delete()
    }

    fun loadShoppingItems(uid: String, onResult: (List<ShoppingItem>) -> Unit) {
        shoppingItemsCol(uid).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.documents.mapNotNull {
                    it.toObject(ShoppingItem::class.java)
                })
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    // ----------------------------------------------------------------
    // Meal plan - one document per date, holding up to 3 recipes
    // ----------------------------------------------------------------

    fun setPlannedMeal(uid: String, dateKey: String, type: MealType, recipe: Recipe) {
        val field = mapOf(type.name.lowercase() to recipe)
        mealPlanCol(uid).document(dateKey).set(field, SetOptions.merge())
    }

    fun clearPlannedMeal(uid: String, dateKey: String, type: MealType) {
        mealPlanCol(uid).document(dateKey)
            .update(
                type.name.lowercase(),
                com.google.firebase.firestore.FieldValue.delete()
            )
    }

    /** Loads every stored day at once - fine for a single-user meal plan's size. */
    fun loadMealPlan(
        uid: String,
        onResult: (Map<String, Map<MealType, Recipe>>) -> Unit
    ) {
        mealPlanCol(uid).get()
            .addOnSuccessListener { snapshot ->
                val result = mutableMapOf<String, Map<MealType, Recipe>>()

                for (doc in snapshot.documents) {
                    val dayMap = mutableMapOf<MealType, Recipe>()

                    for (type in MealType.entries) {
                        val recipeMap = doc.get(type.name.lowercase())

                        if (recipeMap is Map<*, *>) {
                            val recipe = mapToRecipe(recipeMap)

                            if (recipe != null) {
                                dayMap[type] = recipe
                            }
                        }
                    }

                    if (dayMap.isNotEmpty()) {
                        result[doc.id] = dayMap
                    }
                }

                onResult(result)
            }
            .addOnFailureListener { onResult(emptyMap()) }
    }

    private fun mapToRecipe(map: Map<*, *>): Recipe? {
        return try {
            val title = map["title"] as? String ?: return null

            Recipe(
                title = title,
                imageUrl = map["imageUrl"] as? String ?: "",
                timeMinutes = (map["timeMinutes"] as? Long)?.toInt() ?: 0,
                servings = (map["servings"] as? Long)?.toInt() ?: 0,
                rating = (map["rating"] as? Double) ?: 0.0,
                tag = map["tag"] as? String,
                id = (map["id"] as? Long)?.toInt() ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }

    // ----------------------------------------------------------------
    // Diet chips (My Profile: preferences / avoid / cuisines)
    // ----------------------------------------------------------------

    fun addDietChip(uid: String, chip: DietChip) {
        val doc = dietChipsCol(uid).document()

        doc.set(
            mapOf(
                "label" to chip.label,
                "style" to chip.style.name
            )
        )
    }

    fun removeDietChip(uid: String, chip: DietChip) {
        dietChipsCol(uid)
            .whereEqualTo("label", chip.label)
            .whereEqualTo("style", chip.style.name)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach {
                    it.reference.delete()
                }
            }
    }

    fun loadDietChips(uid: String, onResult: (List<DietChip>) -> Unit) {
        dietChipsCol(uid).get()
            .addOnSuccessListener { snapshot ->
                val chips = snapshot.documents.mapNotNull { doc ->
                    val label = doc.getString("label")
                        ?: return@mapNotNull null

                    val styleName = doc.getString("style")
                        ?: return@mapNotNull null

                    val style = try {
                        ChipStyle.valueOf(styleName)
                    } catch (e: Exception) {
                        return@mapNotNull null
                    }

                    DietChip(label, style)
                }

                onResult(chips)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}