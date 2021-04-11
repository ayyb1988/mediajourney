package com.av.mediajourney.skybox

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.av.mediajourney.R
import com.av.mediajourney.opengl.ShaderHelper
import com.av.mediajourney.particles.android.util.TextureHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SkyBoxRender(var context: Context) : GLSurfaceView.Renderer {

    lateinit var skyBox: SkyBox;
    var mProgram = -1

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)

    private var aPositionLoc = -1;
    private var uMatrixLoc = -1;
    private var uTextureLoc = -1;
    private var skyBoxTexture = -1;


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        skyBox = SkyBox()
        val vertexStr = ShaderHelper.loadAsset(context.resources, "sky_box_vertex.glsl")
        val fragStr = ShaderHelper.loadAsset(context.resources, "sky_box_fragment.glsl")

        mProgram = ShaderHelper.loadProgram(vertexStr, fragStr)

        aPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition")

        uMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMatrix")

        uTextureLoc = GLES20.glGetUniformLocation(mProgram, "uTexture")

        skyBoxTexture = TextureHelper.loadCubeMap(context, intArrayOf(R.drawable.left2, R.drawable.right2,
                R.drawable.bottom2, R.drawable.top2,
                R.drawable.front2, R.drawable.back2))

    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val whRadio = width / (height * 1.0f)
        Matrix.setIdentityM(projectionMatrix, 0)
        Matrix.perspectiveM(projectionMatrix, 0, 105f, whRadio, 1f, 10f)
    }

    var frameIndex: Int = 0

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        //自动旋转的
        val xRotationAuto = frameIndex / 8f

        //整体旋转的值 = 自旋转+滑动触摸触发的旋转值
        val xRotationT = xRotationAuto +xRotation

        frameIndex++

        Matrix.setIdentityM(viewMatrix, 0)


        //采用移动的方式,可以看到立方体的6个面上的纹理图片
//        Matrix.translateM(viewMatrix,0, xRotation,0f,0f)

        //采用旋转的方式，只能采用旋转的方式，进行实现视角变换，达到移动的效果
        Matrix.rotateM(viewMatrix, 0, xRotationT, 0f, 1f, 0f)
//        Matrix.rotateM(viewMatrix, 0, yRotation, 1f, 0f, 0f)


        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        GLES20.glUseProgram(mProgram)

        //传mvp矩阵数据
        GLES20.glUniformMatrix4fv(uMatrixLoc, 1, false, viewProjectionMatrix, 0)
        //传纹理数据
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, skyBoxTexture)
        GLES20.glUniform1i(uTextureLoc, 0)


        GLES20.glEnableVertexAttribArray(aPositionLoc)
        skyBox.vertexArrayBuffer.position(0);
        GLES20.glVertexAttribPointer(aPositionLoc, SkyBox.POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT, false, 0, skyBox.vertexArrayBuffer)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_BYTE, skyBox.indexArrayBuffer)
    }


    private var xRotation = 0f
    private var yRotation = 0f

    fun handleTouchMove(deltaX: Float, deltaY: Float) {
        xRotation += deltaX / 16f
        yRotation += deltaY / 16f

        if (yRotation < -90f) {
            yRotation = -90f
        } else if (yRotation > 90) {
            yRotation = 90f
        }
    }

}