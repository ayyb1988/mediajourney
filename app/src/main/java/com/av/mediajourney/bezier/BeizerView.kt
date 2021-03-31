package com.av.mediajourney.bezier

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class BeizerView : View {

    var path = Path()
    val paint = Paint()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePath()

        paint.isAntiAlias = true
        paint.strokeWidth = 5f
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
    }

    private fun updatePath() {
        path.reset()
        path.moveTo(10f, 1500f)
        path.cubicTo(300f, 650f, 800f, 100f, 1050f, 1500f)
        path.moveTo(10f, 100f)
        path.close()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.save()
        canvas?.drawPath(path, paint)
        super.dispatchDraw(canvas)
        canvas?.restore()
    }
}