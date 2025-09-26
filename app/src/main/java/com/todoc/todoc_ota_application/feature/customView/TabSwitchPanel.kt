package com.todoc.todoc_ota_application.feature.customView

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.graphics.*
import android.icu.lang.UCharacter.VerticalOrientation
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.animation.addListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class TabSwitchPanel @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    var corner = 24.toFloat().dp
    var stripHeight = 35.toFloat().dp        // 상단 탭 높이
    var selectedColor = 0xFF3065A6.toInt()
    var unselectedColor = 0xFFCCCCCC.toInt()
    var textSelectedColor = Color.WHITE
    var textUnselectedColor = Color.BLACK
    var textSizeSp = 16f

    var leftText = "1";  set(v){ field=v; invalidate() }
    var rightText = "2"; set(v){ field=v; invalidate() }

    var selectedIndex = 0
        private set

    var onCheckedChange: ((Int)->Unit)? = null


    val linearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        clipToPadding = false
        isClickable = false
        isFocusable = false
        gravity = Gravity.CENTER
        setPadding(16.dpI, 12.dpI, 16.dpI, 16.dpI)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = sp(textSizeSp)
        isFakeBoldText = true
    }
    private val textBounds = Rect()
    private var progress = 0f // 0(left)~1(right)
    private var downTime = 0L

    init {
        setWillNotDraw(false)
        // 탭 스트립 높이만큼 상단 패딩 부여 (컨텐츠는 그 아래에 배치됨)
        setPadding(16.dpI, (stripHeight + 16f.dp).toInt(), 16.dpI, 16.dpI)
        // 내부 컨테이너 추가
        super.addView(linearLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child === linearLayout) {
            super.addView(child, index, params) // 내부 컨테이너는 그대로
        } else {
            // 외부에서 넣는 자식은 컨텐츠 영역으로
            linearLayout.addView(child, index, params)
        }
    }
    override fun onFinishInflate() {
        super.onFinishInflate()
        // 이미 addView를 오버라이드했으므로 보통 필요 없지만, 혹시 모를 케이스 대비
        val toMove = mutableListOf<View>()
        for (i in 0 until super.getChildCount()) {
            val v = getChildAt(i)
            if (v !== linearLayout) toMove += v
        }
        toMove.forEach {
            super.removeView(it)
            linearLayout.addView(it)
        }
    }

    /** 레이아웃 리소스를 한 번에 컨테이너에 인플레이트 */
    fun setContent(@androidx.annotation.LayoutRes layoutRes: Int) {
        linearLayout.removeAllViews()
        LayoutInflater.from(context).inflate(layoutRes, linearLayout, true)
        requestLayout()
    }

    fun setSelectedIndex(index: Int, animate: Boolean = true) {
        val end = if (index <= 0) 0f else 1f
        if (!animate) {
            progress = end; selectedIndex = if (end == 0f) 0 else 1
            onCheckedChange?.invoke(selectedIndex); invalidate(); return
        }
        ValueAnimator.ofFloat(progress, end).apply {
            duration = 180
            addUpdateListener { progress = it.animatedValue as Float; invalidate() }
            addListener(onEnd = {
                selectedIndex = if (end == 0f) 0 else 1
                onCheckedChange?.invoke(selectedIndex)
            })
            start()
        }
    }
    override fun generateDefaultLayoutParams(): LayoutParams =
        MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams =
        MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams =
        if (p is MarginLayoutParams) MarginLayoutParams(p)
        else MarginLayoutParams(p ?: generateDefaultLayoutParams())

    override fun checkLayoutParams(p: LayoutParams?): Boolean = p is MarginLayoutParams
    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val wMode = MeasureSpec.getMode(wSpec)
        val wSize = MeasureSpec.getSize(wSpec)
        val hMode = MeasureSpec.getMode(hSpec)
        val hSize = MeasureSpec.getSize(hSpec)

        // 자식 측정 (마진/패딩 고려)
        measureChildWithMargins(linearLayout, wSpec, paddingLeft + paddingRight, hSpec, paddingTop + paddingBottom)

        val childLp = linearLayout.layoutParams as MarginLayoutParams
        val desiredW = paddingLeft + paddingRight + childLp.leftMargin + childLp.rightMargin + linearLayout.measuredWidth
        val desiredH = paddingTop + paddingBottom + childLp.topMargin + childLp.bottomMargin + linearLayout.measuredHeight

        val measuredW = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> desiredW.coerceAtMost(if (wMode == MeasureSpec.AT_MOST) wSize else desiredW)
            else -> desiredW
        }
        val measuredH = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> desiredH.coerceAtMost(if (hMode == MeasureSpec.AT_MOST) hSize else desiredH)
            else -> desiredH
        }

        setMeasuredDimension(measuredW, measuredH)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val lp = linearLayout.layoutParams as MarginLayoutParams
        val cl = paddingLeft + lp.leftMargin
        val ct = paddingTop + lp.topMargin
        val cr = r - l - paddingRight - lp.rightMargin
        val cb = b - t - paddingBottom - lp.bottomMargin
        linearLayout.layout(cl, ct, cr, cb)
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val r = corner.coerceAtMost(min(w, h) / 2f)

        // 큰 파란 패널
        paint.color = selectedColor
        canvas.drawRoundRect(0f, 0f, w, h, r, r, paint)

        val stripPath = Path().apply {
            val radii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f)
            addRoundRect(RectF(0f, 0f, w, stripHeight), radii, Path.Direction.CW)
        }
        paint.color = unselectedColor
        canvas.drawPath(stripPath, paint)

        // 선택 탭 캡(파란색) – 회색을 덮어써서 탭 효과
        val half = w / 2f
        val capLeft = lerp(0f, half, progress)
        val capRight = lerp(half, w, progress)
        paint.color = selectedColor
        canvas.drawRoundRect(capLeft, 0f, capRight, stripHeight + r * 0.8f, r, r, paint)

        // 라벨(좌/우)
        // 좌
        val leftSelected = progress < 0.5f
        textPaint.color = if (leftSelected) textSelectedColor else textUnselectedColor
        drawCenterText(canvas, leftText, half * 0.5f, stripHeight * 0.62f)

        // 우
        val rightSelected = !leftSelected
        textPaint.color = if (rightSelected) textSelectedColor else textUnselectedColor
        drawCenterText(canvas, rightText, half + half * 0.5f, stripHeight * 0.62f)
    }

    private fun drawCenterText(c: Canvas, text: String, cx: Float, cy: Float) {
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val base = cy - textBounds.exactCenterY()
        c.drawText(text, cx, base, textPaint)
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { downTime = System.currentTimeMillis(); return true }
            MotionEvent.ACTION_UP -> {
                val isTap = System.currentTimeMillis() - downTime < 250
                if (isTap) setSelectedIndex(if (event.x < width/2f) 0 else 1, true)
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private val Int.dpI get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dp get() = this * resources.displayMetrics.density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity

    fun isLeftSelected()  = selectedIndex == 0
    fun isRightSelected() = selectedIndex == 1
    fun selectedLabel()   = if (selectedIndex == 0) leftText else rightText

}
