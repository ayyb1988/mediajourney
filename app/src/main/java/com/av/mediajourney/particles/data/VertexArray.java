package com.av.mediajourney.particles.data;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VertexArray {
    public static final int BYTES_PER_FLOAT = 4;

    private final FloatBuffer floatBuffer;

    /**
     * 构造VertexArray
     * @param vertexData 顶点数据
     */
    public VertexArray(float[] vertexData) {
        floatBuffer = ByteBuffer.allocateDirect(vertexData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }


    /**
     * 更新floatBuffer数据
     * @param vertexData
     * @param start
     * @param count
     */
    public void updateBuffer(float[] vertexData, int start, int count) {
        //FloatBuffer 游标到start
        floatBuffer.position(start);
        //put数据
        floatBuffer.put(vertexData,start,count);
        //重置position
        floatBuffer.position(0);
    }

    /**
     * 给顶点着色器的Attribute属性的变量赋值
     * @param dataOffset
     * @param location
     * @param count
     * @param stride
     */
    public void setVertexAttributePointer(int dataOffset, int location, int count, int stride) {
        floatBuffer.position(dataOffset);
        GLES20.glVertexAttribPointer(location,count,GLES20.GL_FLOAT,false,stride,floatBuffer);
        GLES20.glEnableVertexAttribArray(location);
        floatBuffer.position(0);
    }
}
