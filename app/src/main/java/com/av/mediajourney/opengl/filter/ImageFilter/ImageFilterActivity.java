package com.av.mediajourney.opengl.filter.ImageFilter;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.av.mediajourney.R;

public class ImageFilterActivity extends FragmentActivity implements View.OnClickListener {

    private GLSurfaceView mGlSurfaceView;
    private ImageFilterRender imageFilterRender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_filter);
        mGlSurfaceView = findViewById(R.id.glSurfaceView);
        findViewById(R.id.bt_origin).setOnClickListener(this);
        findViewById(R.id.bt_gray).setOnClickListener(this);
        findViewById(R.id.bt_warm).setOnClickListener(this);
        findViewById(R.id.bt_cool).setOnClickListener(this);

        mGlSurfaceView.setEGLContextClientVersion(2);
        imageFilterRender = new ImageFilterRender(this);
        mGlSurfaceView.setRenderer(imageFilterRender);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGlSurfaceView != null) {
            mGlSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGlSurfaceView != null) {
            mGlSurfaceView.onPause();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_origin:
                imageFilterRender.setFilter(0);
                break;
            case R.id.bt_gray:
                imageFilterRender.setFilter(1);
                break;
            case R.id.bt_warm:
                imageFilterRender.setFilter(2);
                break;
            case R.id.bt_cool:
                imageFilterRender.setFilter(3);
                break;
        }

    }
}
