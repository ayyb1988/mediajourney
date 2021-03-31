package com.av.mediajourney.opengl.filter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.av.mediajourney.R;
import com.av.mediajourney.opengl.filter.ImageFilter.ImageFilterActivity;
import com.av.mediajourney.opengl.filter.camerapreview.CameraPreViewFilterActivity;
import com.av.mediajourney.opengl.filter.colorFilter.ColorFilterActivity;

public class FilterMainActivity extends FragmentActivity implements View.OnClickListener {

    private TextView tvColorfilter;
    private TextView tvCameaFilter;
    private TextView tvImageFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_main);

        initView();
    }


    private void initView() {
        tvColorfilter = (TextView) findViewById(R.id.tv_colorfilter);
        tvCameaFilter = (TextView) findViewById(R.id.tv_cameaFilter);
        tvImageFilter = (TextView) findViewById(R.id.tv_imageFilter);
        tvColorfilter.setOnClickListener(this);
        tvCameaFilter.setOnClickListener(this);
        tvImageFilter.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Class  tagertClass = null;
        switch (v.getId()){
            case R.id.tv_colorfilter:
                tagertClass = ColorFilterActivity.class;
                break;
            case R.id.tv_imageFilter:
                tagertClass = ImageFilterActivity.class;
                break;

            case R.id.tv_cameaFilter:
//                tagertClass = CameraActivity.class;
                tagertClass = CameraPreViewFilterActivity.class;
                    break;
        }
        if (tagertClass != null) {
            Intent intent = new Intent();
            intent.setClass(FilterMainActivity.this, tagertClass);
            startActivity(intent);
        }




    }
}
