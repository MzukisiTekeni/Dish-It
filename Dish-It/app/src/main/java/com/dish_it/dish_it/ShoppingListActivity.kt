package com.dish_it.dish_it

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.adapters.ShoppingGroupAdapter
import com.dish_it.dish_it.data.AppData
import com.dish_it.dish_it.models.ShoppingGroup
import com.dish_it.dish_it.util.NavTab
import com.dish_it.dish_it.util.bindBottomNav

class ShoppingListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        bindBottomNav(this, drawerLayout, NavTab.NONE)

        findViewById<ImageView>(R.id.btn_menu).setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }
        findViewById<View>(R.id.drawer_grocery_list).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START) // already here
        }
        findViewById<View>(R.id.drawer_health_score).setOnClickListener {
            drawerLayout.closeDrawer(Gravity.START)
            startActivity(Intent(this, HealthScoreActivity::class.java))
        }

        recyclerView = findViewById(R.id.rv_shopping_groups)
        emptyState = findViewById(R.id.tv_empty_state)

        showGroups()
    }

    // Re-reads AppData every time this screen opens, so it always reflects
    // whatever's been saved since the last time you were here.
    private fun showGroups() {
        val groups = AppData.shoppingItems
            .groupBy { it.recipeTitle }
            .map { (title, items) -> ShoppingGroup(recipeTitle = title, items = items) }

        recyclerView.adapter = ShoppingGroupAdapter(
            groups = groups,
            onCheckedChanged = { item ->
                // item.isChecked was already updated in place by the adapter - it's the
                // same object stored in AppData.shoppingItems, not a copy - so this just
                // needs to push that change up to Firestore.
                AppData.setShoppingItemChecked(item, item.isChecked)
            },
            onEditClicked = { item ->
                // TODO: show a dialog to rename `item`; AppData.removeShoppingItem(item)
                // is already wired up for a "remove" action once that dialog exists.
            }
        )

        recyclerView.visibility = if (groups.isEmpty()) View.GONE else View.VISIBLE
        emptyState.visibility = if (groups.isEmpty()) View.VISIBLE else View.GONE
    }
}
