package com.av.mediajourney.common;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zzw on 2019-06-27
 * Des:
 */
public class CommonGLRender implements GLSurfaceView.Renderer, View.OnTouchListener {

    private Context context;

    private IGLRender render;

    public CommonGLRender(Context context,IGLRender render) {
        this.context = context;
        this.render = render;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        render.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        render.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        render.draw();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return render != null && render.onTouch(v, event);
    }
}
