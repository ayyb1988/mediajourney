package com.av.mediajourney.opengl.gpuimage;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import com.av.mediajourney.R;
import com.av.mediajourney.opengl.MyRender2;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY;

public class GpuImageActivity extends Activity {

    private static final String TAG = "TextureActivity";
    @BindView(R.id.glSView)
    GLSurfaceView glSView;
    private boolean isSupportEs3;
    private GPUImageRender myRender2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_surface);
        ButterKnife.bind(this);

        // Check if the system supports OpenGL ES 3.0.
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager
                .getDeviceConfigurationInfo();
        isSupportEs3 =
                configurationInfo.reqGlEsVersion >= 0x30000
                        || Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86");

        if (isSupportEs3) {
            glSView.setEGLContextClientVersion(3);

            glSView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR|GLSurfaceView.DEBUG_LOG_GL_CALLS);

            myRender2 = new GPUImageRender(this);

            glSView.setRenderer(myRender2);
            glSView.setRenderMode(RENDERMODE_CONTINUOUSLY);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            glSView.queueEvent(new Runnable() {
                // 这个方法会在渲染线程里被调用
                public void run() {
                    Log.i(TAG, "run: KEYCODE_BACK  curThread="+Thread.currentThread());
                }});
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isSupportEs3) {
            glSView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isSupportEs3) {
            glSView.onPause();
        }
    }

}
