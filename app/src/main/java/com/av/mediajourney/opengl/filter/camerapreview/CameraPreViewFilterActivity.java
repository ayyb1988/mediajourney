package com.av.mediajourney.opengl.filter.camerapreview;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.av.mediajourney.R;
import com.av.mediajourney.utils.CameraUtil;
import com.av.mediajourney.utils.SystemUtils;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraPreViewFilterActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = "CameraPreViewActivity";
    @BindView(R.id.surfaceview)
    GLSurfaceView glSurfaceview;
    @BindView(R.id.bt_record)
    ImageView btRecord;
    @BindView(R.id.bt_camrea_switch)
    ImageView btCamreaSwitch;

//    @BindView(R.id.bt_origin)
//    ImageView btOrigin;
//
//    @BindView(R.id.bt_gray)
//    ImageView btGray;
//
//    @BindView(R.id.bt_warm)
//    ImageView btWarm;
//
//    @BindView(R.id.bt_cool)
//    ImageView btCool;

    private Camera mCamera;
    private int curCameraId = 0;
    private boolean hadPrinted = false;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    //视图矩阵。控制旋转和变化
    private float[] mModelMatrix = new float[16];
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private OesFilter mOesFilter;
    private Button btOrigin;
    private Button btGray;
    private Button btWarm;
    private Button btCool;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_camera_preview_layout);
        ButterKnife.bind(this);


        mOesFilter = new OesFilter(getResources());
        initSurfaceView();
        initView();
    }

    private void initCamera(int cameraId) {
        curCameraId = cameraId;
        mCamera = Camera.open(curCameraId);
        Log.d(TAG, "initCamera: Camera Open ");


        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size closelyPreSize = CameraUtil.getCloselyPreSize(true, SystemUtils.getDisplayWidth(), SystemUtils.getDisplayHeight(), parameters.getSupportedPreviewSizes());
        Log.i(TAG, "initCamera: closelyPreSizeW=" + closelyPreSize.width + " closelyPreSizeH=" + closelyPreSize.height);
        parameters.setPreviewSize(closelyPreSize.width, closelyPreSize.height);
        //这里把Camera的宽高做下反转
        mPreviewWidth = closelyPreSize.height;
        mPreviewHeight = closelyPreSize.width;

        mCamera.setParameters(parameters);
    }

    private int genOesTextureId() {
        int[] textureObjectId = new int[1];
        GLES20.glGenTextures(1, textureObjectId, 0);
        //绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureObjectId[0]);
        //设置放大缩小。设置边缘测量
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST_MIPMAP_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return textureObjectId[0];
    }


    //计算需要变化的矩阵
    private void calculateMatrix() {
        //得到通用的显示的matrix
        Gl2Utils.getShowMatrix(mModelMatrix, mPreviewWidth, mPreviewHeight, this.mSurfaceWidth, this.mSurfaceHeight);
        if (curCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {  //前置摄像头
            Gl2Utils.flip(mModelMatrix, true, false);
            Gl2Utils.rotate(mModelMatrix, 90);
        } else {  //后置摄像头
            int rotateAngle = 270;
            Gl2Utils.rotate(mModelMatrix, rotateAngle);
        }
        mOesFilter.setMatrix(mModelMatrix);
    }

    private void initSurfaceView() {
        glSurfaceview.setEGLContextClientVersion(2);
        glSurfaceview.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                //生成纹理
                mTextureId = genOesTextureId();
                //创建内部的surfaceView
                mSurfaceTexture = new SurfaceTexture(mTextureId);

                //创建滤镜.同时绑定滤镜上
                mOesFilter.create();
                mOesFilter.setTextureId(mTextureId);

                handleSurfaceCreated();
                Log.i(TAG, "onSurfaceCreated: ");
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                //在这里监听到尺寸的改变。做出对应的变化

                mSurfaceWidth = width;
                mSurfaceHeight = height;
                calculateMatrix();

                GLES20.glViewport(0, 0, width, height);
                Log.i(TAG, "onSurfaceChanged: ");
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                //每次绘制后，都通知texture刷新
                if (mSurfaceTexture != null) {
                    //It will implicitly bind its texture to the GL_TEXTURE_EXTERNAL_OES texture target.
                    mSurfaceTexture.updateTexImage();
                }
                mOesFilter.draw();
            }
        });
    }


    private void handleSurfaceCreated() {
        if (mCamera == null) {
            initCamera(curCameraId);
        }
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
//            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "handleSurfaceCreated: " + e.getMessage());
        }
        startPreview();
    }

    private void startPreview() {
        mCamera.startPreview();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @OnClick({R.id.bt_record, R.id.bt_camrea_switch})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_camrea_switch:
                switchCamera();
                break;

        }
    }

    private void initView() {
        btOrigin = (Button) findViewById(R.id.bt_origin);
        btGray = (Button) findViewById(R.id.bt_gray);
        btWarm = (Button) findViewById(R.id.bt_warm);
        btCool = (Button) findViewById(R.id.bt_cool);

        btOrigin.setOnClickListener(this);
        btGray.setOnClickListener(this);
        btWarm.setOnClickListener(this);
        btCool.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_camrea_switch:
                switchCamera();
                break;
            case R.id.bt_origin:
                mOesFilter.setFilter(0);
                break;
            case R.id.bt_gray:
                mOesFilter.setFilter(1);
                break;
            case R.id.bt_warm:
                mOesFilter.setFilter(2);
                break;
            case R.id.bt_cool:
                mOesFilter.setFilter(3);
                break;
        }
    }

    private void switchCamera() {
        if (mCamera != null) {
            releaseCamera();
            initCamera((curCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT);

            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
//                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            startPreview();
            //TODO：这里调用调整Camera的矩阵，在切换前后摄像头时会有画面残留，需要分析处理
            calculateMatrix();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }


}

