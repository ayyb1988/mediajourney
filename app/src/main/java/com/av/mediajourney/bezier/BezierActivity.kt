package com.av.mediajourney.bezier

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.av.mediajourney.R
import com.av.mediajourney.common.CommonGLRender

class BezierActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beziercurve)
        val llRoot: LinearLayout = findViewById(R.id.ll_root)

        val bezierRender = BezierRender(this)
//        val bezierRender = BezierCurveLineRender(this)
        val render = CommonGLRender(this, bezierRender)
        val glSurfaceView = GLSurfaceView(this)
        glSurfaceView.apply {

            setEGLContextClientVersion(2)
            setRenderer(render)
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
            setOnTouchListener(render)
        }
        llRoot.addView(glSurfaceView)
    }
}