package com.dish_it.dish_it.util

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.dish_it.dish_it.R

/**
 * Turns a plain horizontal RecyclerView into a swipeable, auto-advancing
 * carousel with page-indicator dots underneath it - used for "Popular
 * recipes" on Find Recipes.
 *
 * Three things it's responsible for, that a plain RecyclerView doesn't do
 * on its own:
 *  1. Snapping - each swipe lands cleanly on one full card, via PagerSnapHelper.
 *  2. Auto-advancing - every `intervalMs`, scrolls to the next card, looping
 *     back to the first after the last.
 *  3. Dots - builds one dot per item, and keeps the "active" one in sync
 *     with whichever card is actually on screen (from swiping OR auto-advance).
 */
class CarouselAutoScroller(
    private val recyclerView: RecyclerView,
    private val dotsContainer: LinearLayout,
    private val intervalMs: Long = 4000,
    private val onPageChanged: (Int) -> Unit = {}
) {
    private val handler = Handler(Looper.getMainLooper())
    private val snapHelper = PagerSnapHelper()
    private var itemCount = 0
    private var currentPosition = 0

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (itemCount > 1) {
                currentPosition = (currentPosition + 1) % itemCount
                recyclerView.smoothScrollToPosition(currentPosition)
            }
            handler.postDelayed(this, intervalMs)
        }
    }

    /** Call once, right after the RecyclerView's adapter/data has been set. */
    fun setup(itemCount: Int) {
        this.itemCount = itemCount
        currentPosition = 0

        // PagerSnapHelper can only ever be attached to a RecyclerView once -
        // attaching it again on every reload would crash, so guard against that.
        if (recyclerView.onFlingListener == null) {
            snapHelper.attachToRecyclerView(recyclerView)
        }

        buildDots(itemCount)
        updateDots(0)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                val snapView = snapHelper.findSnapView(layoutManager) ?: return
                val position = layoutManager.getPosition(snapView)
                if (position != RecyclerView.NO_POSITION) {
                    currentPosition = position
                    updateDots(position)
                }
            }
        })
    }

    /** Start auto-advancing - call from onResume(). */
    fun start() {
        handler.removeCallbacks(autoScrollRunnable)
        handler.postDelayed(autoScrollRunnable, intervalMs)
    }

    /** Stop auto-advancing - call from onPause(), so it doesn't run against a hidden screen. */
    fun stop() {
        handler.removeCallbacks(autoScrollRunnable)
    }

    private fun buildDots(count: Int) {
        dotsContainer.removeAllViews()
        val density = dotsContainer.context.resources.displayMetrics.density
        val sizePx = (8 * density).toInt()
        val marginPx = (6 * density).toInt()

        for (i in 0 until count) {
            val dot = View(dotsContainer.context)
            val params = LinearLayout.LayoutParams(sizePx, sizePx)
            if (i != count - 1) params.marginEnd = marginPx
            dot.layoutParams = params
            dot.setBackgroundResource(R.drawable.bg_dot_inactive)
            dotsContainer.addView(dot)
        }
    }

    private fun updateDots(activeIndex: Int) {
        for (i in 0 until dotsContainer.childCount) {
            dotsContainer.getChildAt(i)
                .setBackgroundResource(if (i == activeIndex) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
        }
        onPageChanged(activeIndex)
    }
}
