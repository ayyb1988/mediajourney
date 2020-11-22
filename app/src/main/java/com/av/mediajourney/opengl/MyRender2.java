package com.av.mediajourney.opengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.av.mediajourney.MyApplication;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glClearColor;

public class MyRender2 implements GLSurfaceView.Renderer {
    private static final String TAG = "MyRender";
    private int programId;
//    private TexuteProgram glProgram;


    //每个顶点坐标的个数
    private final static int COORDS_PER_VERTEX = 2;

    //每个顶点颜色的个数
    private final static int COLOR_PER_VERTEX = 3;

    private final static int BYTES_PER_FLOAT = 4;

    private static final String A_POSITION = "a_Position";
    private static final String A_COLOR = "a_Color";
    private static final String U_MATRIX = "u_Matrix";

    //-个float占用4个字节，STRIDE是一个点的字节偏移
    private final int STRIDE = (COORDS_PER_VERTEX+ COLOR_PER_VERTEX )* BYTES_PER_FLOAT;

    private FloatBuffer mVertexData;

    private int uMatrixLocation;//glsl中的uniform mat4变量的location
    private final float[] projectionMatrix = new float[16]; //4*4的投影矩阵
    private final float[] modelMatrix = new float[16]; //4*4的模型矩阵
    private final float[] viewMatrix = new float[16];//4*4d的视图矩阵
    private final float[] mvpMatrix = new float[16];//4*4d的MVP矩阵


    public MyRender2() {
        //顶点坐标数组
        float[] TRIANGLE_COORDS = {

                0.5f, 0.5f,1f, 0.5f,0.5f,
                -0.5f, -0.5f,0.5f, 1f,0.5f,
                0.5f, -0.5f,0.5f, 0.5f,1f,
                0.5f, 0.5f,1f, 0.5f,0.5f,
                -0.5f, 0.5f,0.5f, 0.5f,1f,
                -0.5f, -0.5f,0.5f, 1f,0.5f,

        };
        mVertexData = ByteBuffer
                .allocateDirect(TRIANGLE_COORDS.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TRIANGLE_COORDS);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated: curThread= " + Thread.currentThread());
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        String vertexCode = ShaderHelper.loadAsset(MyApplication.getContext().getResources(), "vertex_shader.glsl");
        String fragmentCode = ShaderHelper.loadAsset(MyApplication.getContext().getResources(), "fragment_shader.glsl");
        //创建着色器程序
        programId = ShaderHelper.loadProgram(vertexCode, fragmentCode);

        uMatrixLocation = GLES20.glGetUniformLocation(programId, U_MATRIX);

        int aPosition = GLES20.glGetAttribLocation(programId, A_POSITION);
        Log.i(TAG, "drawFrame: aposition="+aPosition);
        mVertexData.position(0);
        GLES20.glVertexAttribPointer(aPosition,
                COORDS_PER_VERTEX,//用几个偏移描述一个顶点
                GLES20.GL_FLOAT,//顶点数据类型
                false,
                STRIDE,//一个顶点需要多少个字节偏移
                mVertexData//分配的buffer
        );

        //开启顶点着色器的attribute
        GLES20.glEnableVertexAttribArray(aPosition);

        int aColor = GLES20.glGetAttribLocation(programId, A_COLOR);
        mVertexData.position(COORDS_PER_VERTEX);
        GLES20.glVertexAttribPointer(aColor,COLOR_PER_VERTEX,GL_FLOAT,false,STRIDE,mVertexData);
        GLES20.glEnableVertexAttribArray(aColor);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged: width=" + width + " h=" + height + "curThread= " + Thread.currentThread());
        GLES20.glViewport(0, 0, width, height);

        //设置模型矩阵 即M
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.translateM(modelMatrix,0,0.3f,0.3f,0);
//        Matrix.scaleM(modelMatrix,0,0.5f,0.5f,0);
//        Matrix.translateM(modelMatrix,0,2f,0,0f);
//        Matrix.rotateM(modelMatrix,0,-60,1,0,0);

        //设置视图矩阵 即V
        Matrix.setLookAtM(viewMatrix,0,0,0,1,0,0,0,1,0,0);

        //设置投影矩阵 即P
        Matrix.perspectiveM(projectionMatrix,0,60,(float) width/ height,1f,200f);


        //为什么先投影矩阵*模型矩阵，再乘视图矩阵？
        Matrix.multiplyMM(projectionMatrix,0,projectionMatrix,0,modelMatrix,0);
        Matrix.multiplyMM(mvpMatrix,0,projectionMatrix,0,viewMatrix,0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.i(TAG, "onDrawFrame: " + "curThread= " + Thread.currentThread());
        GLES20.glClear(GL_COLOR_BUFFER_BIT);

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, modelMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);



    }
}
