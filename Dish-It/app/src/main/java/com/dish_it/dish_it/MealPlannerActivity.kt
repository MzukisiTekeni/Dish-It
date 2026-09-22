package com.dish_it.dish_it

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dish_it.dish_it.adapters.MonthDayAdapter
import com.dish_it.dish_it.adapters.RecipeListAdapter
import com.dish_it.dish_it.adapters.WeekDayAdapter
import com.dish_it.dish_it.data.AppData
import com.dish_it.dish_it.models.MealType
import com.dish_it.dish_it.models.PlannedMeal
import com.dish_it.dish_it.util.NavTab
import com.dish_it.dish_it.util.bindBottomNav
import com.dish_it.dish_it.util.label
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MealPlannerActivity : AppCompatActivity() {

    private enum class Tab { DAY, WEEK, MONTH }

    // Three independent "which date(s) am I looking at" states - moving
    // through months on the Month tab shouldn't silently change what's
    // selected on the Day tab, and same for the Week tab's own browsing.
    private val selectedDate: Calendar = Calendar.getInstance()
    private val visibleWeekAnchor: Calendar = Calendar.getInstance()
    private val visibleMonth: Calendar = Calendar.getInstance()

    private val dayDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    private val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private lateinit var dayContent: View
    private lateinit var weekContent: View
    private lateinit var monthContent: View
    private lateinit var dayToggle: TextView
    private lateinit var weekToggle: TextView
    private lateinit var monthToggle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_planner)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        bindBottomNav(this, drawerLayout, NavTab.MEAL_PLANNER)

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

        dayContent = findViewById(R.id.day_content)
        weekContent = findViewById(R.id.week_content)
        monthContent = findViewById(R.id.month_content)
        dayToggle = findViewById(R.id.toggle_day)
        weekToggle = findViewById(R.id.toggle_week)
        monthToggle = findViewById(R.id.toggle_month)

        dayToggle.setOnClickListener { showTab(Tab.DAY) }
        weekToggle.setOnClickListener { showTab(Tab.WEEK) }
        monthToggle.setOnClickListener { showTab(Tab.MONTH) }

        findViewById<View>(R.id.btn_prev_day).setOnClickListener {
            selectedDate.add(Calendar.DAY_OF_MONTH, -1)
            refreshDayTab()
        }
        findViewById<View>(R.id.btn_next_day).setOnClickListener {
            selectedDate.add(Calendar.DAY_OF_MONTH, 1)
            refreshDayTab()
        }
        findViewById<View>(R.id.btn_prev_week).setOnClickListener {
            visibleWeekAnchor.add(Calendar.DAY_OF_MONTH, -7)
            refreshWeekTab()
        }
        findViewById<View>(R.id.btn_next_week).setOnClickListener {
            visibleWeekAnchor.add(Calendar.DAY_OF_MONTH, 7)
            refreshWeekTab()
        }
        findViewById<View>(R.id.btn_prev_month).setOnClickListener {
            visibleMonth.add(Calendar.MONTH, -1)
            refreshMonthTab()
        }
        findViewById<View>(R.id.btn_next_month).setOnClickListener {
            visibleMonth.add(Calendar.MONTH, 1)
            refreshMonthTab()
        }

        // The month grid is 7 columns wide - GridLayoutManager needs setting
        // once here; app:layoutManager in XML can't express a column count.
        findViewById<RecyclerView>(R.id.rv_month_days).layoutManager = GridLayoutManager(this, 7)

        showTab(Tab.DAY)
        refreshDayTab()
        refreshWeekTab()
        refreshMonthTab()
    }

    private fun showTab(tab: Tab) {
        dayContent.visibility = if (tab == Tab.DAY) View.VISIBLE else View.GONE
        weekContent.visibility = if (tab == Tab.WEEK) View.VISIBLE else View.GONE
        monthContent.visibility = if (tab == Tab.MONTH) View.VISIBLE else View.GONE

        val selections = listOf(dayToggle to (tab == Tab.DAY), weekToggle to (tab == Tab.WEEK), monthToggle to (tab == Tab.MONTH))
        for ((toggle, isSelected) in selections) {
            toggle.setBackgroundResource(if (isSelected) R.drawable.bg_pill_active else 0)
            toggle.setTextColor(getColor(if (isSelected) R.color.maroon_dark else R.color.text_gray))
        }
    }

    /** Refreshes all 3 tabs' data - call after anything that changes the meal plan. */
    private fun refreshAllTabs() {
        refreshDayTab()
        refreshWeekTab()
        refreshMonthTab()
    }

    // ---------- Day tab ----------

    private fun refreshDayTab() {
        findViewById<TextView>(R.id.tv_selected_date).text = dayDateFormat.format(selectedDate.time)

        val plans = AppData.plansFor(selectedDate)
        bindMealSlot(R.id.slot_breakfast, R.id.empty_breakfast, plans[0])
        bindMealSlot(R.id.slot_lunch, R.id.empty_lunch, plans[1])
        bindMealSlot(R.id.slot_dinner, R.id.empty_dinner, plans[2])
    }

    /**
     * Each meal slot is really TWO stacked views in the same FrameLayout -
     * the recipe card (item_recipe_horizontal) and a dashed "Add X" empty
     * state (item_meal_slot_empty) - only one is ever shown at a time,
     * decided here by whether this slot actually has a recipe planned.
     */
    private fun bindMealSlot(slotId: Int, emptyId: Int, plan: PlannedMeal) {
        val slot = findViewById<View>(slotId)
        val empty = findViewById<View>(emptyId)
        val recipe = plan.recipe

        if (recipe == null) {
            slot.visibility = View.GONE
            empty.visibility = View.VISIBLE
            empty.findViewById<TextView>(R.id.tv_empty_label).text = "Add ${plan.type.label()}"
            empty.setOnClickListener { showRecipePicker(plan.type) }
            return
        }

        slot.visibility = View.VISIBLE
        empty.visibility = View.GONE

        slot.findViewById<TextView>(R.id.tv_recipe_title).text = recipe.title
        slot.findViewById<TextView>(R.id.tv_time).text = "${recipe.timeMinutes} Minutes"
        slot.findViewById<TextView>(R.id.tv_servings).text = recipe.servings.toString()
        slot.findViewById<TextView>(R.id.tv_rating).text = "%.1f".format(recipe.rating)

        val tag = slot.findViewById<TextView>(R.id.tv_recipe_tag)
        tag.visibility = View.VISIBLE
        tag.text = recipe.tag
        tag.setBackgroundResource(
            when (recipe.tag) {
                "Breakfast" -> R.drawable.bg_tag_breakfast
                "Lunch" -> R.drawable.bg_tag_lunch
                "Dinner" -> R.drawable.bg_tag_dinner
                else -> R.drawable.bg_tag_dessert
            }
        )

        Glide.with(slot.context)
            .load(recipe.imageUrl)
            .centerCrop()
            .into(slot.findViewById(R.id.img_recipe_thumb))

        // Tap the card to view it; long-press to change or clear what's planned.
        slot.setOnClickListener {
            startActivity(
                Intent(this, RecipeDetailsActivity::class.java)
                    .putExtra(RecipeDetailsActivity.EXTRA_RECIPE_ID, recipe.id)
            )
        }
        slot.setOnLongClickListener {
            showRecipePicker(plan.type)
            true
        }
    }

    /**
     * The actual "choose it from Recipe Book" flow: shows every saved
     * recipe using the exact same row layout + adapter Recipe Book itself
     * uses (item_recipe_horizontal.xml / RecipeListAdapter), so a recipe
     * looks identical here as it does there - photo, time, servings,
     * rating, tag all visible before picking, not just a title in a list.
     * Picking one assigns it to `type` on `selectedDate` and refreshes
     * every tab, since Week/Month need to show the change too.
     */
    private fun showRecipePicker(type: MealType) {
        val recipes = AppData.savedRecipes
        if (recipes.isEmpty()) {
            Toast.makeText(this, "Save a recipe in Recipe Book first, then it'll show up here", Toast.LENGTH_LONG).show()
            return
        }

        val view = layoutInflater.inflate(R.layout.dialog_recipe_picker, null)
        view.findViewById<TextView>(R.id.tv_picker_title).text = "Choose a ${type.label()} recipe"

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .create()

        view.findViewById<RecyclerView>(R.id.rv_picker_recipes).adapter =
            RecipeListAdapter(recipes) { recipe ->
                AppData.setPlannedMeal(selectedDate, type, recipe)
                refreshAllTabs()
                dialog.dismiss()
            }

        dialog.show()
    }

    // ---------- Week tab ----------

    private fun refreshWeekTab() {
        val startOfWeek = (visibleWeekAnchor.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val days = (0 until 7).map { offset ->
            (startOfWeek.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
        }
        val endOfWeek = days.last()

        findViewById<TextView>(R.id.tv_week_label).text = formatWeekRange(startOfWeek, endOfWeek)

        findViewById<RecyclerView>(R.id.rv_week_days).adapter = WeekDayAdapter(days) { clickedDay ->
            selectedDate.time = clickedDay.time
            showTab(Tab.DAY)
            refreshDayTab()
        }
    }

    private fun formatWeekRange(start: Calendar, end: Calendar): String {
        val sameMonth = start.get(Calendar.MONTH) == end.get(Calendar.MONTH) &&
            start.get(Calendar.YEAR) == end.get(Calendar.YEAR)
        val startFormat = SimpleDateFormat(if (sameMonth) "d" else "d MMM", Locale.getDefault())
        val endFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        return "${startFormat.format(start.time)} - ${endFormat.format(end.time)}"
    }

    // ---------- Month tab ----------

    private fun refreshMonthTab() {
        findViewById<TextView>(R.id.tv_month_label).text = monthLabelFormat.format(visibleMonth.time)

        val firstOfMonth = (visibleMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        // Calendar.DAY_OF_WEEK is 1=Sunday..7=Saturday, and the header row
        // (Sun Mon Tue...) starts on Sunday too, so this is exactly how many
        // blank cells need to come before day 1 for the grid to line up.
        val leadingBlanks = firstOfMonth.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        val cells = mutableListOf<Calendar?>()
        repeat(leadingBlanks) { cells.add(null) }
        for (day in 1..daysInMonth) {
            cells.add((firstOfMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) })
        }
        while (cells.size % 7 != 0) cells.add(null) // pad the last row to stay 7-wide

        findViewById<RecyclerView>(R.id.rv_month_days).adapter = MonthDayAdapter(cells) { clickedDay ->
            selectedDate.time = clickedDay.time
            showTab(Tab.DAY)
            refreshDayTab()
        }
    }
}
