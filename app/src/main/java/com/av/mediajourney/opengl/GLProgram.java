package com.av.mediajourney.opengl;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

public class GLProgram {

    //每一个顶点坐标的个数
    private final static int COORDS_PER_VERTEX = 2;

    //每个个顶点颜色的个数
    private final static int COLOR_PER_VERTEX = 3;

    private final static int BYTES_PER_FLOAT = 4;

    //-个float占用4个字节，STRIDE是一个点的字节偏移
    private static final int STRIDE = (COORDS_PER_VERTEX+ COLOR_PER_VERTEX )* BYTES_PER_FLOAT;
    private static final String TAG = "TexuteProgram";

    //顶点坐标数组
    private float[] TRIANGLE_COORDS = {
            0.5f, 0.5f,0.5f, 0.5f,0.5f
            -0.5f, -0.5f,0.5f, 0.5f,0.5f,
            0.5f, -0.5f,0.5f, 0.5f,0.5f

    };
    private FloatBuffer mVertexData = ByteBuffer
            .allocateDirect(TRIANGLE_COORDS.length * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(TRIANGLE_COORDS);


    public void drawFrame(int programId) {
//        GLES20.glUseProgram(programId);



        glClear(GL_COLOR_BUFFER_BIT);

//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,3);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,3);

//        GLES20.glDisableVertexAttribArray(aPosition);


    }

    public void create(int programId) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        mVertexData.position(0);

        int aPosition = GLES20.glGetAttribLocation(programId, "a_Position");
        Log.i(TAG, "drawFrame: aposition="+aPosition);

        GLES20.glVertexAttribPointer(aPosition,
                COORDS_PER_VERTEX,//用几个偏移描述一个顶点
                GLES20.GL_FLOAT,//顶点数据类型
                false,
                STRIDE,//一个顶点需要多少个字节偏移
                mVertexData//分配的buffer
        );

        //开启顶点着色器的attribute
        GLES20.glEnableVertexAttribArray(aPosition);

        int aColor = GLES20.glGetAttribLocation(programId, "a_Color");
        mVertexData.position(COORDS_PER_VERTEX);
        GLES20.glVertexAttribPointer(aColor,COLOR_PER_VERTEX,GL_FLOAT,false,STRIDE,mVertexData);
        GLES20.glEnableVertexAttribArray(aColor);

    }
}
