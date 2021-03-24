package com.av.mediajourney.opengl.gpuimage;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.av.mediajourney.R;
import com.av.mediajourney.opengl.gpuimage.util.Rotation;
import com.av.mediajourney.opengl.texture.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GPUImageRender implements GLSurfaceView.Renderer {

    private Context context;
    private int inputTextureId;
    private GPUImageGaussianBlurFilter blurFilter;
    private  FloatBuffer glCubeBuffer;
    private  FloatBuffer glTextureBuffer;
    private int imageWidth;
    private int imageHeight;

    public static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };


    public GPUImageRender(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        blurFilter = new GPUImageGaussianBlurFilter();
        blurFilter.ifNeedInit();

        inputTextureId = TextureHelper.loadTexture(context, R.drawable.bg);

        glCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        glCubeBuffer.put(CUBE).position(0);

        glTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        glTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0, 0, width, height);
        blurFilter.onOutputSizeChanged(width,height);
//        adjustImageScaling(width,height);

    }

    private void adjustImageScaling(int outputWidth, int outputHeight) {

        float ratio1 = outputWidth / imageWidth;
        float ratio2 = outputHeight / imageHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(imageWidth * ratioMax);
        int imageHeightNew = Math.round(imageHeight * ratioMax);

        float ratioWidth = imageWidthNew / outputWidth;
        float ratioHeight = imageHeightNew / outputHeight;

        float[] cube = CUBE;
        cube = new float[]{
                CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
        };

        glCubeBuffer.clear();
        glCubeBuffer.put(cube).position(0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0f,0f,0f,1f);
        blurFilter.onDraw(inputTextureId,glCubeBuffer,glTextureBuffer);
    }
}
