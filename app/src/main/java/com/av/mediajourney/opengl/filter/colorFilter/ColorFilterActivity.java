package com.av.mediajourney.opengl.filter.colorFilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.av.mediajourney.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ColorFilterActivity extends FragmentActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "FilterMainActivity";
    @BindView(R.id.bt_origin)
    Button btOrigin;
    @BindView(R.id.bt_gray)
    Button btGray;
    @BindView(R.id.bt_warm)
    Button btWarm;
    @BindView(R.id.bt_cool)
    Button btCool;
    @BindView(R.id.sb_hue)
    SeekBar sbHue;//调节色调
    @BindView(R.id.sb_saturation)
    SeekBar sbSaturation;//调节饱和度
    @BindView(R.id.sb_lum)
    SeekBar sbLum;//调节亮度
    @BindView(R.id.iv_image)
    ImageView ivImage;

    private Bitmap mBitmap;
    private float midValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_filter);
        ButterKnife.bind(this);

        initSeekBarChangeListener();
        midValue = sbHue.getMax() * 1.0F / 2;

        mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bg);
        ivImage.setImageBitmap(mBitmap);

    }

    // 4*5矩阵
    float[] mOriginMatrix = {
            1,0,0,0,0,
            0,1,0,0,0,
            0,0,1,0,0,
            0,0,0,1,0};

    //Gray=R*0.3+G*0.59+B*0.11
    float[] mBWMatrix = {
            0.3f,0.59f,0.11f,0,0,
            0.3f,0.59f,0.11f,0,0,
            0.3f,0.59f,0.11f,0,0,
            0,0,0,1,0};

    //暖色调的处理可以增加红绿通道的值
    float[] mWarmMatrix = {
            2,0,0,0,0,
            0,2,0,0,0,
            0,0,1,0,0,
            0,0,0,1,0};

    //冷色调的处理可以通过单一增加蓝色通道的值
    float[] mCoolMatrix = {
            1,0,0,0,0,
            0,1,0,0,0,
            0,0,2,0,0,
            0,0,0,1,0};



    @OnClick({R.id.bt_origin, R.id.bt_gray, R.id.bt_warm, R.id.bt_cool})
    public void onViewClicked(View view) {
        float[] colorMatrix = mOriginMatrix;
        switch (view.getId()) {
            case R.id.bt_origin:
                colorMatrix = mOriginMatrix;
                break;
            case R.id.bt_gray:
                colorMatrix = mBWMatrix;
                break;
            case R.id.bt_warm:
                colorMatrix = mWarmMatrix;
                break;
            case R.id.bt_cool:
                colorMatrix = mCoolMatrix;
                break;
        }
        setImageMatrix(colorMatrix);

    }

    public void setImageMatrix(float[] mColorMatrix) {
        Bitmap bmp = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(),Bitmap.Config.ARGB_8888);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(mColorMatrix);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(mBitmap,0,0,paint);
        ivImage.setImageBitmap(bmp);
    }


    private void initSeekBarChangeListener() {
        sbHue.setOnSeekBarChangeListener(this);
        sbSaturation.setOnSeekBarChangeListener(this);
        sbLum.setOnSeekBarChangeListener(this);
    }

    private float mSaturaion =1;
    private float mLum=1;
    private float mHue=0;
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        switch (id){
            case R.id.sb_hue:
                mHue = (progress - midValue) * 1.0F / midValue * 180;
                break;
            case R.id.sb_saturation:
                mSaturaion = progress*1.0f/midValue;
                break;
            case R.id.sb_lum:
                mLum = progress*1.0f/midValue;
                break;
        }
        Log.d(TAG, "onProgressChanged: midValue="+midValue+" mhue="+mHue+" msaturation="+mSaturaion+" mlum="+mLum+" progress="+progress);

        ivImage.setImageBitmap(handleImageEffect(mBitmap,mHue,mSaturaion,mLum));

    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 色调、饱和度、亮度 通过ColorMatrix的PostConcat相乘，对原始图片进行变换处理
     * @param oriBmp
     * @param hue 色调调节范围 -180度至180度
     * @param saturation
     * @param lum
     * @return
     */
    public static Bitmap handleImageEffect(Bitmap oriBmp,  float hue, float saturation, float lum) {
        Bitmap bmp = Bitmap.createBitmap(oriBmp.getWidth(), oriBmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();

        //调节色调
        Log.i(TAG, "handleImageEffect: 色调 rotate="+hue);
        ColorMatrix hueMatrix = new ColorMatrix();
        hueMatrix.setRotate(0, hue);//围绕red旋转 hue角度
        hueMatrix.setRotate(1, hue);//围绕green旋转 hue角度
        hueMatrix.setRotate(2, hue);//围绕blue旋转 hue角度

        //调节饱和度
        Log.i(TAG, "handleImageEffect: 饱和度saturation="+saturation);
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(saturation);

        //调节亮度
        Log.i(TAG, "handleImageEffect: 亮度lum="+lum);
        ColorMatrix lumMatrix = new ColorMatrix();
        lumMatrix.setScale(lum, lum, lum, 1);

        ColorMatrix imageMatrix = new ColorMatrix();
        imageMatrix.postConcat(hueMatrix);
        imageMatrix.postConcat(saturationMatrix);
        imageMatrix.postConcat(lumMatrix);

        paint.setColorFilter(new ColorMatrixColorFilter(imageMatrix));
        canvas.drawBitmap(oriBmp, 0, 0, paint);

        return bmp;
    }

}
