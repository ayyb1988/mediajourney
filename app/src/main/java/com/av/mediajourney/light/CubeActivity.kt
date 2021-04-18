package com.av.mediajourney.skybox

import android.annotation.SuppressLint
import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import com.av.mediajourney.R

class CubeActivity : Activity() {

    lateinit var glSurfaceView: GLSurfaceView
    lateinit var cubeRender: CubeRender;

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beziercurve)
        val llRoot: LinearLayout = findViewById(R.id.ll_root)
        glSurfaceView = GLSurfaceView(this)

        glSurfaceView.setEGLContextClientVersion(2)
        cubeRender = CubeRender(this);
        glSurfaceView.setRenderer(cubeRender)
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)


        glSurfaceView.setOnTouchListener(object : OnTouchListener {
            var lastX = 0f;
            var lastY = 0f;
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                if (event == null) {
                    return false
                }
                if (MotionEvent.ACTION_DOWN == event.action) {
                    lastX = event.x;
                    lastY = event.y;
                } else if (MotionEvent.ACTION_MOVE == event.action) {
                    val deltaX = event.x - lastX
                    val deltaY = event.y - lastY

                    lastX = event.x
                    lastY = event.y

                    glSurfaceView.queueEvent {
                        cubeRender.handleTouchMove(deltaX, deltaY)
                    }
                }
                return true
            }
        })

        llRoot.addView(glSurfaceView)
    }

    override fun onResume() {
        super.onResume()
        if (glSurfaceView != null) {
            glSurfaceView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (glSurfaceView != null) {
            glSurfaceView.onPause()
        }
    }
}