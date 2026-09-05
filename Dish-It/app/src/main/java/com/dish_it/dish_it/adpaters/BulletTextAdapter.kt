package com.dish_it.dish_it.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.R
// The constructor takes the list of strings this adapter will display.
// Nothing more is needed for something this simple.
class BulletTextAdapter(private val items: List<String>) :
    RecyclerView.Adapter<BulletTextAdapter.BulletViewHolder>() {
    // Step A: the ViewHolder. Its ONLY job is to find and hold
// the TextView from item_bullet_text.xml, once.
    class BulletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.tv_bullet_text)
    }
    // Step B: called only when a brand new row view needs to be built
// (RecyclerView will call this just enough times to fill the screen).
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BulletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bullet_text, parent, false)
        return BulletViewHolder(view)
    }
    // Step C: called every time a row (new OR recycled) needs today's data.
// "position" tells you which item in the list this row should show.
    override fun onBindViewHolder(holder: BulletViewHolder, position: Int) {
        holder.text.text = items[position]
    }
    // Step D: tells RecyclerView how many rows exist in total.
    override fun getItemCount(): Int = items.size
}