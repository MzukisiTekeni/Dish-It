package com.dish_it.dish_it

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.adapters.DietChipAdapter
import com.dish_it.dish_it.data.AppData
import com.dish_it.dish_it.data.FirebaseRepository
import com.dish_it.dish_it.models.ChipStyle
import com.dish_it.dish_it.models.DietChip
import com.dish_it.dish_it.util.NavTab
import com.dish_it.dish_it.util.bindBottomNav
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.FlexWrap

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        bindBottomNav(this, drawerLayout, NavTab.PROFILE)

        findViewById<ImageView>(R.id.btn_menu).setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }
        findViewById<View>(R.id.drawer_grocery_list).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START)
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }
        findViewById<View>(R.id.drawer_health_score).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START)
            startActivity(Intent(this, HealthScoreActivity::class.java))
        }

        // The profile card's numbers, name and email would normally come from
        // your logged-in user object - wire that in here once you have it,
        // the same way each screen's list data gets swapped for a real API call.

        findViewById<View>(R.id.btn_edit_avatar).setOnClickListener {
            // TODO: launch an image picker (ActivityResultContracts.PickVisualMedia
            // or similar) and load the result into img_avatar
            Toast.makeText(this, "Photo picker goes here", Toast.LENGTH_SHORT).show()
        }

        // Diet chips come from Firestore once loaded; until that load finishes (or for a
        // first-ever run with nothing saved yet) each row falls back to its demo defaults,
        // which then get written to Firestore so the next load has real data.
        val uid = FirebaseRepository.currentUserId
        if (uid == null) {
            setupChipRow(R.id.rv_diet_preferences, ChipStyle.PREFERENCE, emptyList(),
                listOf("Vegetarian", "High Protein", "Dairy free"),
                "Add a dietary preference", "e.g. Vegan, Keto, Low Carb")
            setupChipRow(R.id.rv_diet_avoid, ChipStyle.AVOID, emptyList(),
                listOf("Peanuts", "Mushrooms"),
                "Add an ingredient to avoid", "e.g. Shellfish, Gluten")
            setupChipRow(R.id.rv_diet_cuisines, ChipStyle.CUISINE, emptyList(),
                listOf("Italian", "Thai", "Mexican"),
                "Add a favourite cuisine", "e.g. Japanese, Indian")
        } else {
            FirebaseRepository.loadDietChips(uid) { stored ->
                setupChipRow(R.id.rv_diet_preferences, ChipStyle.PREFERENCE,
                    stored.filter { it.style == ChipStyle.PREFERENCE }.map { it.label },
                    listOf("Vegetarian", "High Protein", "Dairy free"),
                    "Add a dietary preference", "e.g. Vegan, Keto, Low Carb")
                setupChipRow(R.id.rv_diet_avoid, ChipStyle.AVOID,
                    stored.filter { it.style == ChipStyle.AVOID }.map { it.label },
                    listOf("Peanuts", "Mushrooms"),
                    "Add an ingredient to avoid", "e.g. Shellfish, Gluten")
                setupChipRow(R.id.rv_diet_cuisines, ChipStyle.CUISINE,
                    stored.filter { it.style == ChipStyle.CUISINE }.map { it.label },
                    listOf("Italian", "Thai", "Mexican"),
                    "Add a favourite cuisine", "e.g. Japanese, Indian")
            }
        }

        findViewById<View>(R.id.row_units).setOnClickListener { /* TODO: units picker */ }
        findViewById<View>(R.id.row_default_servings).setOnClickListener {
            showServingsPicker()
        }
        findViewById<View>(R.id.row_notifications).setOnClickListener { /* TODO: notification settings */ }
        findViewById<View>(R.id.row_theme).setOnClickListener { /* TODO: theme picker */ }
        findViewById<View>(R.id.row_language).setOnClickListener { /* TODO: language picker */ }
        findViewById<View>(R.id.row_sign_out).setOnClickListener {
            FirebaseRepository.signOut()
            AppData.clearLocalData()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * Builds one of the 3 chip rows: a FlexboxLayoutManager (so chips wrap
     * onto a new line instead of running off screen), a DietChipAdapter
     * seeded with `initial` labels, and a "+ Add" dialog that appends
     * whatever the person types.
     */
    /**
     * Builds one of the 3 chip rows. `storedLabels` is what Firestore already has for this
     * style (may be empty); `demoDefaults` is what a brand-new user with nothing saved yet
     * sees instead - those defaults get written to Firestore the first time so the row is
     * backed by real data from then on.
     */
    private fun setupChipRow(
        recyclerViewId: Int,
        style: ChipStyle,
        storedLabels: List<String>,
        demoDefaults: List<String>,
        addDialogTitle: String,
        addDialogHint: String
    ) {
        val uid = FirebaseRepository.currentUserId
        val usingDemoDefaults = storedLabels.isEmpty()
        val initialLabels = if (usingDemoDefaults) demoDefaults else storedLabels
        val chips = initialLabels.map { DietChip(it, style) }.toMutableList()

        if (usingDemoDefaults && uid != null) {
            chips.forEach { FirebaseRepository.addDietChip(uid, it) }
        }

        lateinit var adapter: DietChipAdapter

        adapter = DietChipAdapter(
            items = chips,
            onAddClicked = {
                val input = EditText(this).apply { hint = addDialogHint }
                AlertDialog.Builder(this)
                    .setTitle(addDialogTitle)
                    .setView(input)
                    .setPositiveButton("Add") { _, _ ->
                        val label = input.text.toString().trim()
                        if (label.isNotEmpty()) {
                            val chip = DietChip(label, style)
                            adapter.addChip(chip)
                            if (uid != null) FirebaseRepository.addDietChip(uid, chip)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onChipRemoved = { removed ->
                Toast.makeText(this, "Removed ${removed.label}", Toast.LENGTH_SHORT).show()
                if (uid != null) FirebaseRepository.removeDietChip(uid, removed)
            }
        )

        findViewById<RecyclerView>(recyclerViewId).apply {
            layoutManager = FlexboxLayoutManager(this@ProfileActivity).apply {
                flexWrap = FlexWrap.WRAP
            }
            this.adapter = adapter
        }
    }

    private fun showServingsPicker() {
        val options = arrayOf("1 person", "2 people", "4 people", "6 people")
        AlertDialog.Builder(this)
            .setTitle("Default Servings")
            .setItems(options) { _, which ->
                findViewById<android.widget.TextView>(R.id.tv_default_servings_value).text = options[which]
                // TODO: persist this choice (SharedPreferences/DataStore) and use it
                // to pre-scale ingredient amounts when a recipe screen opens.
            }
            .show()
    }
}