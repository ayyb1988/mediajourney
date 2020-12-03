package com.av.mediajourney.opengl;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderHelper {

    private static final String TAG = "ShaderHelper";


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
        GLES20.glAttachShader(programId, loadShader(GLES20.GL_VERTEX_SHADER, verCode));
        GLES20.glAttachShader(programId, loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode));
        //3. 链接
        GLES20.glLinkProgram(programId);
        //4. 使用
        GLES20.glUseProgram(programId);
        return programId;
    }


}
