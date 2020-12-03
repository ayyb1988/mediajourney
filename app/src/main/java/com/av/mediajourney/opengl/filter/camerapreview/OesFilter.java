package com.av.mediajourney.opengl.filter.camerapreview;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;


import com.av.mediajourney.opengl.filter.ImageFilter.data.ImageBgData;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * 接受来自相机的oes纹理
 * <p>
 * 一个Fliter需要的东西：
 * 1. textureId
 * 2. program to use
 * 3. VBO
 * 4. matrix
 */
public class OesFilter {

    private final static boolean DEBUG = true;
    private final static String TAG = "OesFilter";

    private Resources mRes;

    private int mProgram;

    private int mAPosition;
    private int mACoord;

    private int mUMatrix;

    private int mUFilterIndex;
    private int uColor;

    //pos vertex
    protected FloatBuffer mVerBuffer;
    //这里用两个FloatBuffer来表示，也可以将两个FloatBuffer合并到一起
    private FloatBuffer mTextureCoordinate;

    //texture unit
    private int mUTexture;
    private int textureType = 0;      //默认使用Texture2D0
    private int textureId = 0;

    //顶点坐标
    private float pos[] = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,
    };

    //纹理坐标
    private float[] coord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };
    public static final float[] OM = getOriginalMatrix();


    public static float[] getOriginalMatrix() {
        float[] identityMatrix = new float[16];
        Matrix.setIdentityM(identityMatrix, 0);
        return identityMatrix;
    }

    private int mUCoordMatrix;
    private float[] mCoordMatrix = Arrays.copyOf(OM,16);
    private float[] matrix = Arrays.copyOf(OM,16);

    public OesFilter(Resources resources) {
        this.mRes = resources;
        initBuffer();
    }

    public void setCoordMatrix(float[] matrix) {
        this.mCoordMatrix = matrix;
    }

    public void create() {
        onCreate();
    }

    //load shader and link program . get uniform and varying
    public void onCreate() {
        createProgramByAssetsFile("oes_base_vertex.glsl", "oes_base_fragment.glsl");
        mUCoordMatrix = GLES20.glGetUniformLocation(mProgram, "uCoordinateMatrix");
    }

    //draw step
    public void draw() {
        //step0 clear
        onClear();
        //step1 use program
        onUseProgram();

        onBindFilter();
        //step2 active and bind custom data
        onSetExpandData();
        //step3 bind texture
        onBindTexture();
        //step4 normal draw
        onDraw();
    }

    private void onBindFilter() {
        GLES20.glUniform1i(mUFilterIndex, mIndex);

        switch (mIndex){
            case 0:
                //原图效果，不同处理
                break;
            case 1:
                //黑白滤镜
//                GLES20.glVertexAttrib3fv(mProgram, ImageBgData.GRAY_FILTER_COLOR_DATA,0);
                GLES20.glUniform3fv(uColor,1,ImageBgData.GRAY_FILTER_COLOR_DATA,0);
                break;
            case 2:
                //暖色滤镜
                GLES20.glUniform3fv(uColor,1,ImageBgData.WARM_FILTER_COLOR_DATA,0);
//                GLES20.glVertexAttrib3fv(mProgram, ImageBgData.WARM_FILTER_COLOR_DATA, 0);
                break;
            case 3:
                //冷色滤镜
                GLES20.glUniform3fv(uColor,1,ImageBgData.COOL_FILTER_COLOR_DATA,0);

//                GLES20.glVertexAttrib3fv(mProgram, ImageBgData.COOL_FILTER_COLOR_DATA, 0);
                break;
        }
    }


    public int getTextureId() {
        return textureId;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public int getOutputTexture() {
        return -1;
    }

    private void createProgram(String vertex, String fragment) {
        mProgram = uCreateGlProgram(vertex, fragment);
        //两个变量varying vPosition和vCoord
        mAPosition = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mACoord = GLES20.glGetAttribLocation(mProgram, "aCoordinate");

        //两个uniform texture 和 matrix
        mUMatrix = GLES20.glGetUniformLocation(mProgram, "uMatrix");
        mUTexture = GLES20.glGetUniformLocation(mProgram, "uTexture");

        uColor = GLES20.glGetUniformLocation(mProgram,"u_Color");
        mUFilterIndex = GLES20.glGetUniformLocation(mProgram,"u_TypeIndex");
    }

    private void createProgramByAssetsFile(String vertex, String fragment) {
        createProgram(uRes(mRes, vertex), uRes(mRes, fragment));
    }

    /**
     * Buffer初始化
     */
    private void initBuffer() {
        //每个float占用4个字节
        mVerBuffer = ByteBuffer
                .allocateDirect(pos.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(pos);
        mVerBuffer.position(0);


        mTextureCoordinate = ByteBuffer
                .allocateDirect(coord.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(coord);
        mTextureCoordinate.position(0);
    }


    private static void glError(int code, Object index) {
        if (DEBUG && code != 0) {
            Log.e(TAG, "glError:" + code + "---" + index);
        }
    }

    /********************************************************
     *          加载shader
     * *********************************************************/

    //通过路径加载Assets中的文本内容
    private static String uRes(Resources mRes, String path) {
        StringBuilder result = new StringBuilder();
        try {
            InputStream is = mRes.getAssets().open(path);
            int ch;
            byte[] buffer = new byte[1024];
            while (-1 != (ch = is.read(buffer))) {
                result.append(new String(buffer, 0, ch));
            }
        } catch (Exception e) {
            return null;
        }
        return result.toString().replaceAll("\\r\\n", "\n");
    }

    //加载shader
    private static int uLoadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (0 != shader) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                glError(1, "Could not compile shader:" + shaderType);
                glError(1, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    //创建GL程序
    private static int uCreateGlProgram(String vertexSource, String fragmentSource) {
        int vertex = uLoadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertex == 0) return 0;
        int fragment = uLoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragment == 0) return 0;
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertex);
            GLES20.glAttachShader(program, fragment);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                glError(1, "Could not link program:" + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }


    /**
     * Draw step0 :清除画布
     */
    private void onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * step1
     */
    private void onUseProgram() {
        GLES20.glUseProgram(mProgram);
    }

    /**
     * step2
     */
    private void onSetExpandData() {
        GLES20.glUniformMatrix4fv(mUMatrix, 1, false, matrix, 0);
        GLES20.glUniformMatrix4fv(mUCoordMatrix, 1, false, mCoordMatrix, 0);
    }

    /**
     * step3
     */
    private void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + getTextureType());
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getTextureId());
        GLES20.glUniform1i(mUTexture, getTextureType());
    }

    private int getTextureType() {
        return 0;
    }

    /**
     * step 4
     * 启用顶点坐标和纹理坐标进行绘制。
     * <p>
     * 主要就是激活和使用定义的varying和uniform 变量
     */
    private void onDraw() {
        //设置定点数据
        GLES20.glEnableVertexAttribArray(mAPosition);
        GLES20.glVertexAttribPointer(
                mAPosition,
                2,
                GLES20.GL_FLOAT,
                false,
                0,
                mVerBuffer);
        //
        GLES20.glEnableVertexAttribArray(mACoord);
        GLES20.glVertexAttribPointer(
                mACoord,
                2,
                GLES20.GL_FLOAT,
                false,
                0,
                mTextureCoordinate);
        //绘制三角形带
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mAPosition);
        GLES20.glDisableVertexAttribArray(mACoord);
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

    private int mIndex;
    public void setFilter(int index) {
        this.mIndex = index;
    }
}
