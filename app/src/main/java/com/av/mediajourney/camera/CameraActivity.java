package com.av.mediajourney.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.av.mediajourney.R;
import com.av.mediajourney.utils.BitmapUtils;
import com.av.mediajourney.utils.CameraUtil;
import com.av.mediajourney.utils.FileUtils;
import com.av.mediajourney.utils.SystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 通过{@link Camera }
 * 实现Camera的预览和视频采集功能
 * <p>
 * 1. Camera的打开 关闭 开始预览 停止预览
 * 2. Camera设置Surface 开始预览
 * 3. Camera的坐标
 * 4。 Camera的录制录制回调
 */
public class CameraActivity extends FragmentActivity {

    private static final String TAG = "CameraActivity";
    @BindView(R.id.surfaceview)
    SurfaceView surfaceview;
    @BindView(R.id.bt_record)
    ImageView btRecord;
    @BindView(R.id.bt_camrea_switch)
    ImageView btCamreaSwitch;

    private Camera mCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_layout);
        ButterKnife.bind(this);
        initSurfaceView();
    }

    private int curCameraId = 0;
    private boolean hadPrinted = false;

    private void initCamera(int cameraId) {
        curCameraId = cameraId;
        mCamera = Camera.open(curCameraId);
        Log.d(TAG, "initCamera: Camera Open ");

        setCamerDisplayOrientation(this, curCameraId, mCamera);

        if (!hadPrinted) {
            printCameraInfo();
            hadPrinted = true;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size closelyPreSize = CameraUtil.getCloselyPreSize(true, SystemUtils.getDisplayWidth(), SystemUtils.getDisplayHeight(), parameters.getSupportedPreviewSizes());
        Log.i(TAG, "initCamera: closelyPreSizeW="+closelyPreSize.width+" closelyPreSizeH="+closelyPreSize.height);
        parameters.setPreviewSize(closelyPreSize.width, closelyPreSize.height);
        mCamera.setParameters(parameters);
    }

    private void printCameraInfo() {
        //1. 调用getParameters获取Parameters
        Camera.Parameters parameters = mCamera.getParameters();

        //2. 获取Camera预览支持的图片格式(常见的是NV21和YUV420sp)
        int previewFormat = parameters.getPreviewFormat();
        Log.d(TAG, "initCamera: previewFormat=" + previewFormat); // NV21

        //3. 获取Camera预览支持的W和H的大小，
        // 手动设置Camera的W和H时，要检测camera是否支持，如果设置了Camera不支持的预览大小，会出现黑屏。
        // 那么这里有一个问，由于Camera不同厂商支持的预览大小不同，如果做到兼容呐？
        // 需要使用方采用一定策略进行选择（比如：选择和预设置的最接近的支持的WH）
        //通过输出信息，我们可以看到Camera是横向的即 W>H
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size item : supportedPreviewSizes
        ) {
            Log.d(TAG, "initCamera: supportedPreviewSizes w= " + item.width + " h=" + item.height);
        }

        //可以看到Camera的宽高是屏幕的宽高是不一致的，手机屏幕是竖屏的H>W，而Camera的宽高是横向的W>H
        Camera.Size previewSize = parameters.getPreviewSize();
        int[] physicalSS = SystemUtils.getPhysicalSS(this);
        Log.i(TAG, "initCamera: w=" + previewSize.width + " h=" + previewSize.height
                + " screenW=" + SystemUtils.getDisplayWidth() + " screenH=" + SystemUtils.getDisplayHeight()
                + " physicalW=" + physicalSS[0] + " physicalH=" + physicalSS[1]);

        //4. 获取Camera支持的帧率 一般是10～30
        List<Integer> supportedPreviewFrameRates = parameters.getSupportedPreviewFrameRates();
        for (Integer item : supportedPreviewFrameRates
        ) {
            Log.i(TAG, "initCamera: supportedPreviewFrameRates frameRate=" + item);
        }

        //5. 获取Camera的个数信息，以及每一个Camera的orientation，这个很关键，如果根据Camera的orientation正确的设置Camera的DisplayOrientation可能会导致预览倒止或者出现镜像的情况
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            Log.i(TAG, "initCamera: facing=" + cameraInfo.facing
                    + " orientation=" + cameraInfo.orientation);
        }
    }

    /**
     * 设置Camera展示的方向，如果不设置就会导致前摄像方向不对
     *
     * @param activity
     * @param cameraId
     * @param camera
     */
    public static void setCamerDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Log.i(TAG, "setCamerDisplayOrientation: rotation=" + rotation + " cameraId=" + cameraId);
        int degress = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degress = 0;
                break;
            case Surface.ROTATION_90:
                degress = 90;
                break;
            case Surface.ROTATION_180:
                degress = 180;
                break;
            case Surface.ROTATION_270:
                degress = 270;
                break;
        }
        int result = 0;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degress) % 360;
            //镜像
            //问题4：前摄像头出现倒立的情况
            result = (360 - result) % 360;

        } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = (cameraInfo.orientation - degress + 360) % 360;
        }
        Log.i(TAG, "setCamerDisplayOrientation: result=" + result + " cameraId=" + cameraId + " facing=" + cameraInfo.facing + " cameraInfo.orientation=" + cameraInfo.orientation);

        camera.setDisplayOrientation(result);
    }

    /**
     * 页面重新打开后SurfaceView部分黑屏
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        //问题5：如果在onReusme中设置Camera的参数、打开Camera以及开始预览，第一次会出现黑屏，不断的锁屏然后再打开屏幕也会概率性的出现黑屏
        //而如果在SurfeHodler的回调中处理则不会出现上述问题
        //通过Log查看 有一个信息引起注意：Camera: app passed NULL surface 只有出现黑屏的时候才会出现
//        handleSurfaceCreated();

    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause: ");
//        handleSurfaceDestroyed();
        super.onPause();

    }

    private SurfaceHolder mSurfaceHolder;

    private void initSurfaceView() {
        mSurfaceHolder = surfaceview.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated: ");
                handleSurfaceCreated();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed: ");
                handleSurfaceDestroyed();
            }
        });
    }

    private void handleSurfaceDestroyed() {
        releaseCamera();
        mSurfaceHolder = null;
        Log.i(TAG, "handleSurfaceDestroyed: ");
    }

    private void handleSurfaceCreated() {
        Log.i(TAG, "handleSurfaceCreated: start");
        if (mSurfaceHolder == null) {
            mSurfaceHolder = surfaceview.getHolder();
        }
        if (mCamera == null) {
            initCamera(curCameraId);
        }
        try {
            //问题2：页面重新打开后SurfaceView的内容黑屏
            //Camera is being used after Camera.release() was called
            //在surfaceDestroyed时调用了Camera的release 但是没有设置为null,
            //--》如何解耦合，把生命周期相关的方法和Camera的生命周期绑定而不时在回调中处理，方便业务实现
            //onResume--》surfaceCreated
            //onPause--》surfaceDestroyed
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "handleSurfaceCreated: " + e.getMessage());
        }
        startPreview();
        Log.i(TAG, "handleSurfaceCreated: end");
    }

    private void startPreview() {
//        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                Log.i(TAG, "onPreviewFrame: setPreviewCallback");
//            }
//        });
        //问题六：很多时候，不仅仅要预览，在预览视频的时候，希望能做一些检测，比如人脸检测等。这就需要获得预览帧视频，该如何做呐？
        mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Log.i(TAG, "onPreviewFrame: setOneShotPreviewCallback");
                Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
                if(!yuvImage.compressToJpeg(new Rect(0,0,previewSize.width,previewSize.height),100,os)){
                    Log.e(TAG, "onPreviewFrame: compressToJpeg error" );
                    return;
                }
                byte[] bytes = os.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                //这里的处理方式是简单的把预览的一帧图保存下。如果需要做人脸设别或者其他操作，可以拿到这个bitmap进行分析处理
                //我们可以通过找出这张图片发现预览保存的图片的方向是不对的，还是Camera的原始方向，需要旋转一定角度才可以。
                //问题7：那么该如何处理呐？
                if(curCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    bitmap = BitmapUtils.rotate(bitmap,90);
                }else {
                    bitmap = BitmapUtils.mirror(BitmapUtils.rotate(bitmap,270));
                }
                FileUtils.saveBitmapToFile(bitmap,"oneShot.jpg");
            }
        });

//        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                Log.i(TAG, "onPreviewFrame: setPreviewCallbackWithBuffer");
//            }
//        });
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
            case R.id.bt_record:
                break;
            case R.id.bt_camrea_switch:
                switchCamera();
                break;
        }
    }

    private void switchCamera() {
        if (mCamera != null) {
            releaseCamera();

            //问题1. 切换摄像头后画面卡住
            //关闭Camera释放资源，重新打开切换后的Camera，重新设置PreviewDisplay 然后开始预览
            initCamera((curCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT);
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            startPreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.release();
        }
    }

}

///视频会被拉长，就是比如说拍人脸的时候，人脸会被拉长  previewSize的设置

//为什么前后摄像头最终设置的setDisplayOrientation都是90，而前后摄像头本身的orientation前者是270，后者是90。坐标系。


