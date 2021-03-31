package com.av.mediajourney.common

import android.view.MotionEvent
import android.view.View

interface IGLRender {
    fun onSurfaceCreated()
    fun onSurfaceChanged(width: Int, height: Int)
    fun draw()
    fun onTouch(v: View?, event: MotionEvent?): Boolean
}