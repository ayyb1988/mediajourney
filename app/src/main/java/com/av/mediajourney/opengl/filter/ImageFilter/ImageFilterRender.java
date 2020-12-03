package com.av.mediajourney.opengl.filter.ImageFilter;

import android.content.Context;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.av.mediajourney.MyApplication;
import com.av.mediajourney.R;
import com.av.mediajourney.opengl.ShaderHelper;
import com.av.mediajourney.opengl.filter.ImageFilter.data.ImageBgData;
import com.av.mediajourney.opengl.filter.ImageFilter.objects.BgTextureObject;
import com.av.mediajourney.opengl.texture.programs.TextureShaderProgram;
import com.av.mediajourney.opengl.texture.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ImageFilterRender implements GLSurfaceView.Renderer {

    private Context context;
    private int textureId;
    private TextureShaderProgram textureProgram;
    private BgTextureObject textureObject;

    public ImageFilterRender(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES10.glClearColor(0f, 0f, 0f, 0f);

        textureObject = new BgTextureObject(ImageBgData.VERTEX_DATA);

        String fragmentCode = ShaderHelper.loadAsset(MyApplication.getContext().getResources(), "image_filter_texture_fragment_shader.glsl");

        String vertexCode = ShaderHelper.loadAsset(MyApplication.getContext().getResources(), "texture_vertex_shader.glsl");

        textureProgram = new TextureShaderProgram(context, vertexCode, fragmentCode);

        textureId = TextureHelper.loadTexture(context, R.drawable.bg);

    }

    private void refreshTexureProgram(String fragmentCode) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        textureProgram.useProgram();


//
        GLES20.glUniform1i(textureProgram.getFilterIndexUniformLocation(), mIndex);

        switch (mIndex){
            case 0:
                //原图效果，不同处理
                break;
            case 1:
                //黑白滤镜
                GLES20.glVertexAttrib3fv(textureProgram.getProgram(),ImageBgData.GRAY_FILTER_COLOR_DATA,0);
                break;
            case 2:
                //暖色滤镜
                GLES20.glVertexAttrib3fv(textureProgram.getProgram(), ImageBgData.WARM_FILTER_COLOR_DATA, 0);
                break;
            case 3:
                //冷色滤镜
                GLES20.glVertexAttrib3fv(textureProgram.getProgram(), ImageBgData.COOL_FILTER_COLOR_DATA, 0);
                break;
        }

        textureProgram.setUniforms(textureId);

        textureObject.bindData(textureProgram);

        textureObject.draw();


    }

    private int mIndex;

    public void setFilter(int index) {

        mIndex = index;

    }
}
