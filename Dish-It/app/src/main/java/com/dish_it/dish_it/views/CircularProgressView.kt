package com.dish_it.dish_it.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * A plain ring/donut progress indicator - Android has no built-in circular
 * ProgressBar you can style like this, so it's a small custom View instead.
 *
 * Draws two arcs on top of each other: a full circle "track" (the grey
 * background ring), then a "progress" arc on top of it that only sweeps as
 * far around as `progress` says. Both are just drawArc() calls - there is
 * no bitmap, image, or XML shape involved.
 *
 * Usage (see HealthScoreActivity.kt):
 *   ring.progress = 85f        // 0f..100f - percentage of the ring to fill
 *   ring.progressColor = ContextCompat.getColor(this, R.color.health_high)
 */
class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    /** 0f..100f. Setting this redraws the view immediately. */
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 100f)
            invalidate()
        }

    var trackColor: Int = Color.parseColor("#EEDEDC") // matches @color/divider_light
        set(value) { field = value; invalidate() }

    var progressColor: Int = Color.parseColor("#3E8E4F") // matches @color/health_high
        set(value) { field = value; invalidate() }

    var ringStrokeWidthDp: Float = 8f
        set(value) { field = value; invalidate() }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val arcBounds = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val strokePx = ringStrokeWidthDp * resources.displayMetrics.density
        trackPaint.strokeWidth = strokePx
        trackPaint.color = trackColor
        progressPaint.strokeWidth = strokePx
        progressPaint.color = progressColor

        // Inset by half the stroke width so the ring isn't clipped at the
        // view's edges (a stroked arc is drawn centred ON the path, so half
        // of it would otherwise fall outside the view's bounds).
        val inset = strokePx / 2f
        arcBounds.set(inset, inset, width - inset, height - inset)

        // Full grey circle first...
        canvas.drawArc(arcBounds, -90f, 360f, false, trackPaint)
        // ...then the coloured arc on top, starting from the top (-90°)
        // and sweeping clockwise proportionally to progress.
        val sweepAngle = 360f * (progress / 100f)
        canvas.drawArc(arcBounds, -90f, sweepAngle, false, progressPaint)
    }
}
