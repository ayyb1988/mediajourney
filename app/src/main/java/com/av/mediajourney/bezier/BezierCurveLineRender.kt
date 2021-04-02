package com.av.mediajourney.bezier

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.view.MotionEvent
import android.view.View
import com.av.mediajourney.common.IGLRender
import com.av.mediajourney.opengl.ShaderHelper
import java.nio.FloatBuffer
import java.nio.IntBuffer

class BezierCurveLineRender(private val context: Context) : IGLRender {

    val POINTS_NUM = 256
    val TRIANGLES_PER_POINT = 3

    var mProgram: Int = -1
    var tDataLocation = -1;
    var uOffsetLocation = -1;
    var uStartEndDataLocation = -1;
    var uControlDataLocation = -1;
    var uColorLocation = -1;
    var uMVPMatrixLocation = -1;


    lateinit var vaoBuffers: IntBuffer;

    var mMVPMatrix = FloatArray(16)

    //视图矩阵
    var mViewMatrix = FloatArray(16)

    //模型矩阵
    var mModelMatrix = FloatArray(16)

    //正交/投影矩阵
    var mPorjectMatrix = FloatArray(16)


    override fun onSurfaceCreated() {
        val vertexStr = ShaderHelper.loadAsset(context.resources, "vertex_beziercurve2.glsl")
        val fragStr = ShaderHelper.loadAsset(context.resources, "frag_beziercurve.glsl")
        mProgram = ShaderHelper.loadProgram(vertexStr, fragStr)

        //通过VAO批量传数据
        tDataLocation = GLES20.glGetAttribLocation(mProgram, "a_tData")

        uOffsetLocation = GLES20.glGetUniformLocation(mProgram, "u_offset")
        uStartEndDataLocation = GLES20.glGetUniformLocation(mProgram, "u_startEndData")
        uControlDataLocation = GLES20.glGetUniformLocation(mProgram, "u_ControlData")
        uMVPMatrixLocation = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix")
        uColorLocation = GLES20.glGetUniformLocation(mProgram, "u_Color")

        setVaoData();

        //设置视图矩阵
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f)

        //设置正交矩阵
        Matrix.orthoM(mPorjectMatrix, 0, -1f, 1f, -1f, 1f, 0.1f, 100f)
    }

    fun setVaoData() {
        val tDataSize = POINTS_NUM * TRIANGLES_PER_POINT;
        val floatBuffer: FloatBuffer = FloatBuffer.allocate(tDataSize)

        for (i in 0..tDataSize step TRIANGLES_PER_POINT) {
            //设置数据 0,1/3*256,2/3*256,3/3*356.... 1
//            if (i < tDataSize) {
//                floatBuffer.put(i, i * 1.0f / tDataSize)
//            }
//            if (i + 1 < tDataSize) {
//                floatBuffer.put(i + 1, (i + 1) * 1.0f / tDataSize)
//            }
//            if (i + 2 < tDataSize) {
//                floatBuffer.put(i + 2, (i + 2) * 1.0f / tDataSize)
//            }

            if (i < tDataSize) {
                floatBuffer.put(i, i * 1.0f / tDataSize)
            }
            if (i + 1 < tDataSize) {
                floatBuffer.put(i + 1, (i + 3) * 1.0f / tDataSize)
            }
            if (i + 2 < tDataSize) {
                floatBuffer.put(i + 2, -1f)
            }

        }

        //VBO
        val buffers: IntBuffer = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, buffers)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0])

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 4 * tDataSize, floatBuffer, GLES20.GL_STATIC_DRAW)

        //VAO
        vaoBuffers = IntBuffer.allocate(1)
        GLES30.glGenVertexArrays(1, vaoBuffers)
        GLES30.glBindVertexArray(vaoBuffers[0])

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0])
        GLES20.glEnableVertexAttribArray(tDataLocation)

        GLES30.glVertexAttribPointer(tDataLocation, 1, GLES20.GL_FLOAT, false, 4, 0)

        //delete
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(GLES30.GL_NONE)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    var mFrameIndex =0;

    override fun draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        GLES20.glUseProgram(mProgram)

        GLES30.glBindVertexArray(vaoBuffers[0])

        GLES20.glEnableVertexAttribArray(uStartEndDataLocation)
        GLES20.glUniform4f(uStartEndDataLocation, -1f, 0f, 1f, 0f)

        GLES20.glEnableVertexAttribArray(uControlDataLocation)
        GLES20.glUniform4f(uControlDataLocation, -0.04f, 0.99f, 0f, 0.99f)

        GLES20.glEnableVertexAttribArray(uColorLocation)
        GLES20.glUniform4f(uColorLocation, 1f, 0f, 0f, 1f)

        mFrameIndex++;
        var newIndex = mFrameIndex
        //这里的涉及有些巧妙,通过 frameIndex 归一化offset
        var offset = (newIndex % 100) * 1.0f / 100;
        //然后到达一定量之后取反 实现来回的闭环动画
        offset = if ((newIndex / 100) % 2 == 1) (1 - offset) else offset
//        Log.e("BezierRender", "draw: mFrameIndex="+mFrameIndex+" offset="+offset +" (newIndex / 100) % 2 == 1)="+((newIndex / 100) % 2 == 1))
        GLES20.glEnableVertexAttribArray(uOffsetLocation)
        GLES20.glUniform1f(uOffsetLocation, offset)

//        GLES20.glUniform1f(uOffsetLocation, 1f)

        drawOneBezier(1f)

    }

    private fun drawOneBezier(offset: Float) {
        var offset1=1f
        //设置模型矩阵
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.rotateM(mModelMatrix, 0, 0f, 1f*offset1, 0f, 0f)
//        Matrix.rotateM(mModelMatrix, 0, 180f, 0f, 1f*offset1, 0f)


        //矩阵相乘得到mvp矩阵变换
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mPorjectMatrix, 0, mMVPMatrix, 0)

        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mMVPMatrix, 0)

//        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, POINTS_NUM * TRIANGLES_PER_POINT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, POINTS_NUM * TRIANGLES_PER_POINT)

        //沿着x轴翻转,绘制另外半边
        //设置模型矩阵
        Matrix.setIdentityM(mModelMatrix, 0)

        Matrix.rotateM(mModelMatrix, 0, 180f, 1f*offset1, 0f, 0f)
//        Matrix.rotateM(mModelMatrix, 0, 180f, 0f, 1f*offset1, 0f)

        //矩阵相乘得到mvp矩阵变换
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mPorjectMatrix, 0, mMVPMatrix, 0)

        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mMVPMatrix, 0)

//        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, POINTS_NUM * TRIANGLES_PER_POINT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, POINTS_NUM * TRIANGLES_PER_POINT)
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return true
    }


}