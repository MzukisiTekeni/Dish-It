package com.dish_it.dish_it.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.R
import com.dish_it.dish_it.data.AppData
import com.dish_it.dish_it.util.colorRes
import java.util.Calendar

/**
 * Powers rv_month_days on Meal Planner's Month tab - a 7-column calendar
 * grid (paired with GridLayoutManager(context, 7) in MealPlannerActivity).
 * `days` includes `null` entries for the blank cells before day 1 and
 * after the month's last day, so the grid lines up under the right weekday.
 */
class MonthDayAdapter(
    private val days: List<Calendar?>,
    private val onDayClicked: (Calendar) -> Unit
) : RecyclerView.Adapter<MonthDayAdapter.DayViewHolder>() {

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayNumber: TextView = itemView.findViewById(R.id.tv_month_day_number)
        val dotsRow: LinearLayout = itemView.findViewById(R.id.dots_row)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_month_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val context = holder.itemView.context
        val day = days[position]
        holder.dotsRow.removeAllViews()

        // Blank cell (padding before day 1 / after the month's last day) -
        // reset everything a recycled view might still be carrying, then bail.
        if (day == null) {
            holder.dayNumber.text = ""
            holder.dayNumber.setBackgroundResource(0)
            holder.itemView.setOnClickListener(null)
            holder.itemView.isClickable = false
            return
        }

        holder.dayNumber.text = day.get(Calendar.DAY_OF_MONTH).toString()

        val isToday = isSameDay(day, Calendar.getInstance())
        if (isToday) {
            holder.dayNumber.setBackgroundResource(R.drawable.bg_pill_solid)
            holder.dayNumber.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.maroon_primary))
            holder.dayNumber.setTextColor(context.getColor(R.color.card_white))
        } else {
            holder.dayNumber.setBackgroundResource(0)
            holder.dayNumber.setTextColor(context.getColor(R.color.text_dark))
        }

        val density = context.resources.displayMetrics.density
        val dotSize = (4 * density).toInt()
        val dotMargin = (2 * density).toInt()
        AppData.plansFor(day).filter { it.recipe != null }.forEach { plan ->
            val dot = View(context)
            dot.layoutParams = LinearLayout.LayoutParams(dotSize, dotSize).apply { marginEnd = dotMargin }
            dot.setBackgroundResource(R.drawable.bg_dot_active)
            dot.backgroundTintList = ColorStateList.valueOf(context.getColor(plan.type.colorRes()))
            holder.dotsRow.addView(dot)
        }

        holder.itemView.isClickable = true
        holder.itemView.setOnClickListener { onDayClicked(day) }
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    override fun getItemCount(): Int = days.size
}
