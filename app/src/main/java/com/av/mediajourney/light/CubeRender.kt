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

class CubeRender(var context: Context) : GLSurfaceView.Renderer {

    lateinit var cubeLight: CubeLight;
    var mProgram = -1

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modeMatrix = FloatArray(16)

    private val mvpMatrix = FloatArray(16)

    private var aPositionLoc = -1;
    private var aTextureCoorLoc = -1;
    private var uMatrixLoc = -1;
    private var uTextureLoc = -1;
    private var skyBoxTexture = -1;


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        cubeLight = CubeLight()
//        val vertexStr = ShaderHelper.loadAsset(context.resources, "cube_light_vertex.glsl")
        val vertexStr = ShaderHelper.loadAsset(context.resources, "cube_vertex.glsl")
//        val fragStr = ShaderHelper.loadAsset(context.resources, "cube_light_fragment.glsl")
        val fragStr = ShaderHelper.loadAsset(context.resources, "cube_fragment.glsl")

        mProgram = ShaderHelper.loadProgram(vertexStr, fragStr)

        aPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition")
        aTextureCoorLoc = GLES20.glGetAttribLocation(mProgram, "aTexCoord")

        uMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMatrix")

        uTextureLoc = GLES20.glGetUniformLocation(mProgram, "uTexture")

        skyBoxTexture = TextureHelper.loadTexture(context, R.drawable.guilin)

    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val whRadio = width / (height * 1.0f)
        Matrix.setIdentityM(projectionMatrix, 0)
        Matrix.perspectiveM(projectionMatrix, 0, 60f, whRadio, 1f, 100f)

        Matrix.setIdentityM(viewMatrix,0);
        Matrix.setLookAtM(viewMatrix,0,
        2f,0f,3f,
        0f,0f,0f,
        0f,1f,0f)
    }

    var frameIndex: Int = 0

    override fun onDrawFrame(gl: GL10?) {
        //这里由于用到深度测试需要清除GL_DEPTH_BUFFER_BIT
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glClearColor(0f, 0f, 0f, 1f)

        //必须要加深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)


        Matrix.setIdentityM(modeMatrix, 0)


        //采用旋转的方式，只能采用旋转的方式，进行实现视角变换，达到移动的效果
        Matrix.rotateM(modeMatrix, 0, xRotation, 1f, 0f, 0f)
        Matrix.rotateM(modeMatrix, 0, yRotation, 0f, 1f, 0f)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modeMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUseProgram(mProgram)

        //传mvp矩阵数据
        GLES20.glUniformMatrix4fv(uMatrixLoc, 1, false, mvpMatrix, 0)
        //传纹理数据
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, skyBoxTexture)
        GLES20.glUniform1i(uTextureLoc, 0)


        GLES20.glEnableVertexAttribArray(aPositionLoc)
        cubeLight.vertexArrayBuffer.position(0);
        GLES20.glVertexAttribPointer(aPositionLoc, CubeLight.POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT, false, CubeLight.STRIDE, cubeLight.vertexArrayBuffer)

        GLES20.glEnableVertexAttribArray(aTextureCoorLoc)
        cubeLight.vertexArrayBuffer.position(CubeLight.POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aTextureCoorLoc, CubeLight.POSITION_TEXTURE_COUNT, GLES20.GL_FLOAT, false, CubeLight.STRIDE, cubeLight.vertexArrayBuffer)

        cubeLight.vertexArrayBuffer.position(0);


//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_BYTE, cubeLight.indexArrayBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,36)
    }


    private var xRotationOffset = 0f
    private var yRotationOffset = 0f

    private var xRotation = 0f
    private var yRotation = 0f
    private val TOUCH_SCALE_FACTOR = 180.0f / 320
    private val MATH_PI = 3.1415926

    fun handleTouchMove(deltaX: Float, deltaY: Float) {
        xRotationOffset += deltaX * TOUCH_SCALE_FACTOR;
        yRotationOffset += deltaY * TOUCH_SCALE_FACTOR;

        xRotation = xRotationOffset % 360;
        yRotation = yRotationOffset % 360;

    }

}