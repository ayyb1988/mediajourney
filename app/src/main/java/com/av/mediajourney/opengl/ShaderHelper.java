package com.av.mediajourney.opengl;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.IntBuffer;

public class ShaderHelper {

    private static final String TAG = "ShaderHelper";
    public static final int NO_TEXTURE = -1;

    public static String loadAsset(Resources res, String path) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream is = res.getAssets().open(path);

//            InputStreamReader inputStreamReader = new InputStreamReader(is);
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//            String nextLine;
//            while ((nextLine = bufferedReader.readLine())!=null){
//                stringBuilder.append(nextLine);
//                stringBuilder.append("\n");
//            }

            byte[] buffer = new byte[1024];
            int count;
            while (-1 != (count = is.read(buffer))) {
                stringBuilder.append(new String(buffer, 0, count));
            }
            Log.i(TAG, "loadAsset: " + stringBuilder);
            String result = stringBuilder.toString().replaceAll("\\r\\n", "\n");
            Log.i(TAG, "loadAsset: 22=" + result);
            return result;
//            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * https://upload-images.jianshu.io/upload_images/1791669-8f5d5ad196aaa27c.jpeg?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp
     *
     * @param type
     * @param codeStr
     * @return
     */
    private static int loadShader(int type, String codeStr) {
        //1. 根据类型（顶点着色器、片元着色器）创建着色器，拿到着色器句柄
        int shader = GLES20.glCreateShader(type);
        Log.i(TAG, "compileShaderCode: type=" + type + " shaderId=" + shader);

        if (shader > 0) {
            //2. 设置着色器代码 ，shader句柄和code进行绑定
            GLES20.glShaderSource(shader, codeStr);
            //3. 编译着色器，
            GLES20.glCompileShader(shader);

            //4. 查询编译状态
            int[] status = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
            Log.i(TAG, "loadShader: status[0]=" + status[0]);
            //如果失败，释放资源
            if (status[0] == 0) {
                GLES20.glDeleteShader(shader);
                return 0;
            }
        }
        return shader;
    }


    public static int loadProgram(String verCode, String fragmentCode) {
        //1. 创建Shader程序，获取到program句柄
        int programId = GLES20.glCreateProgram();

        if(programId == 0){
            int errorCode = GLES20.glGetError();

            Log.e(TAG, "loadProgram: glCreateProgram error errorCode="+errorCode );
            return 0;
        }
        //2. 根据着色器语言类型和代码，attach着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, verCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode);
        GLES20.glAttachShader(programId, vertexShader);
        GLES20.glAttachShader(programId, fragmentShader);
        //3. 链接
        GLES20.glLinkProgram(programId);
        //4. 使用
        GLES20.glUseProgram(programId);

        int[] status = new int[1];
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] <= 0) {
            Log.d("Load Program", "Linking Failed");
            return 0;
        }
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);

        return programId;
    }

    public static int loadTexture(final Bitmap img, final int usedTexId) {
        return loadTexture(img, usedTexId, true);
    }

    public static int loadTexture(final Bitmap img, final int usedTexId, final boolean recycle) {
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, img);
            textures[0] = usedTexId;
        }
        if (recycle) {
            img.recycle();
        }
        return textures[0];
    }

    public static int loadTexture(final IntBuffer data, final int width, final int height, final int usedTexId) {
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width,
                    height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data);
            textures[0] = usedTexId;
        }
        return textures[0];
    }

    public static int loadTextureAsBitmap(final IntBuffer data, final Camera.Size size, final int usedTexId) {
        Bitmap bitmap = Bitmap
                .createBitmap(data.array(), size.width, size.height, Bitmap.Config.ARGB_8888);
        return loadTexture(bitmap, usedTexId);
    }


}
