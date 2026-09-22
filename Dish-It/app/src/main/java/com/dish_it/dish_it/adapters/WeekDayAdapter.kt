package com.dish_it.dish_it.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.R
import com.dish_it.dish_it.data.AppData
import com.dish_it.dish_it.models.MealType
import com.dish_it.dish_it.util.colorRes
import com.dish_it.dish_it.util.iconRes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Powers rv_week_days on Meal Planner's Week tab - one card per day, tap to jump to that Day view. */
class WeekDayAdapter(
    private val days: List<Calendar>,
    private val onDayClicked: (Calendar) -> Unit
) : RecyclerView.Adapter<WeekDayAdapter.DayViewHolder>() {

    private val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tv_week_day_date)
        val todayBadge: View = itemView.findViewById(R.id.tv_today_badge)
        val breakfastIcon: ImageView = itemView.findViewById(R.id.img_week_breakfast)
        val breakfastLabel: TextView = itemView.findViewById(R.id.tv_week_breakfast)
        val lunchIcon: ImageView = itemView.findViewById(R.id.img_week_lunch)
        val lunchLabel: TextView = itemView.findViewById(R.id.tv_week_lunch)
        val dinnerIcon: ImageView = itemView.findViewById(R.id.img_week_dinner)
        val dinnerLabel: TextView = itemView.findViewById(R.id.tv_week_dinner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_week_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.date.text = dateFormat.format(day.time)
        holder.todayBadge.visibility = if (isSameDay(day, Calendar.getInstance())) View.VISIBLE else View.GONE

        val plans = AppData.plansFor(day)
        bindRow(holder.breakfastIcon, holder.breakfastLabel, MealType.BREAKFAST, plans[0].recipe?.title)
        bindRow(holder.lunchIcon, holder.lunchLabel, MealType.LUNCH, plans[1].recipe?.title)
        bindRow(holder.dinnerIcon, holder.dinnerLabel, MealType.DINNER, plans[2].recipe?.title)

        holder.itemView.setOnClickListener { onDayClicked(day) }
    }

    private fun bindRow(icon: ImageView, label: TextView, type: MealType, title: String?) {
        icon.setImageResource(type.iconRes())
        icon.setColorFilter(icon.context.getColor(type.colorRes()))
        if (title != null) {
            label.text = title
            label.setTextColor(label.context.getColor(R.color.text_dark))
        } else {
            label.text = "Not planned"
            label.setTextColor(label.context.getColor(R.color.text_gray))
        }
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    override fun getItemCount(): Int = days.size
}
