/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.av.mediajourney.opengl.texture;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.translateM;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;

import com.av.mediajourney.R;
import com.av.mediajourney.opengl.texture.objects.TextureObject;
import com.av.mediajourney.opengl.texture.objects.VertexDataUtils;
import com.av.mediajourney.opengl.texture.programs.TextureShaderProgram;
import com.av.mediajourney.opengl.texture.util.TextureHelper;


public class GuangZhouTaRenderer implements Renderer {
    private final Context context;


    private TextureObject guangzhouta;

    private TextureShaderProgram textureProgram;

    private int textureId;

    public GuangZhouTaRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        guangzhouta = new TextureObject(VertexDataUtils.SPLIT_SCREEN_2_VERTEX_DATA);

        textureProgram = new TextureShaderProgram(context);

        textureId = TextureHelper.loadTexture(context, R.drawable.guangzhou);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);

    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        // Draw the table.
        textureProgram.useProgram();
        textureProgram.setUniforms(textureId);
        guangzhouta.bindData(textureProgram);
        guangzhouta.draw();

    }
}