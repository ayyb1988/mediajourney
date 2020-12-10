package com.av.mediajourney.particles;

import android.app.Activity;
import android.drm.DrmStore;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.av.mediajourney.R;

public class ParticleActivity extends Activity{

    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particle_layout);
        glSurfaceView = findViewById(R.id.glSurfaceView);

        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new ParticlesRender(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
