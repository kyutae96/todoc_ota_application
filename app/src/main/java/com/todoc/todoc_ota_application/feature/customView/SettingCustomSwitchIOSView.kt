package com.todoc.todoc_ota_application.feature.customView

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingCustomSwitchIOSView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var isChecked = false
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val animator = ValueAnimator()
    private var thumbPosition = 0f

    private val trackHeight = 28.dp()
    private val trackWidth = 48.dp()
    private val thumbSize = 24.dp()
    private val padding = 2.dp()

    init {
        isClickable = true
        setOnClickListener {
            toggle()
        }
        updateColors()
        thumbPosition = if (isChecked) 1f else 0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(trackWidth, trackHeight)
    }

    override fun onDraw(canvas: Canvas) {
        // 트랙
        val radius = trackHeight / 2f
        trackPaint.color = if (isChecked) Color.parseColor("#3065A6") else Color.parseColor("#D1D1D6")
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, trackPaint)

        // 썸
        val range = width - thumbSize - padding * 2
        val x = padding + thumbPosition * range
        canvas.drawOval(x, padding.toFloat(), x + thumbSize,
            (padding + thumbSize).toFloat(), thumbPaint)
    }

    private fun toggle() {
        isChecked = !isChecked
        animateThumb()
        updateColors()
        onCheckedChangeListener?.invoke(isChecked)
    }

    private fun animateThumb() {
        val start = thumbPosition
        val end = if (isChecked) 1f else 0f
        animator.cancel()
        animator.setFloatValues(start, end)
        animator.duration = 200
        animator.addUpdateListener {
            thumbPosition = it.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    private fun updateColors() {
        thumbPaint.color = Color.WHITE
        invalidate()
    }

    private var onCheckedChangeListener: ((Boolean) -> Unit)? = null
    fun setOnCheckedChangeSettingListener(listener: (Boolean) -> Unit) {
        onCheckedChangeListener = listener
    }

    fun setSettingChecked(checked: Boolean, animate: Boolean = true) {
        if (isChecked == checked) return
        isChecked = checked
        if (animate) animateThumb() else {
            thumbPosition = if (checked) 1f else 0f
            invalidate()
        }
        onCheckedChangeListener?.invoke(isChecked)
    }

    fun isSettingChecked(): Boolean = isChecked

    // dp 확장 함수
    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}