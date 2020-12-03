/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.av.mediajourney.opengl.texture.programs;

import android.content.Context;
import android.util.Log;

import com.av.mediajourney.MyApplication;
import com.av.mediajourney.opengl.ShaderHelper;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

public class TextureShaderProgram  extends ShaderProgram{
    // Uniform locations
//    private final int uMatrixLocation;
    private final int uTextureUnitLocation;
    
    // Attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;
    private final int aColorLocation;
    private final int uTypeIndex;

    public TextureShaderProgram(Context context) {

        String vertexCode = ShaderHelper.loadAsset(MyApplication.getContext().getResources(), "texture_vertex_shader.glsl");
        String fragmentCode = ShaderHelper.loadAsset(MyApplication.getContext().getResources(), "texture_fragment_shader.glsl");
        //创建着色器程序
        program = ShaderHelper.loadProgram(vertexCode, fragmentCode);

        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, A_TEXTURE_COORDINATES);

        aColorLocation = glGetAttribLocation(program, A_COLOR);

        uTypeIndex = glGetUniformLocation(program,U_TYPE_INDEX);

    }

    public TextureShaderProgram(Context context,String vertexCode,String fragmentCode) {

           //创建着色器程序
        program = ShaderHelper.loadProgram(vertexCode, fragmentCode);

        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, A_TEXTURE_COORDINATES);

        aColorLocation = glGetAttribLocation(program, A_COLOR);

        uTypeIndex = glGetUniformLocation(program,U_TYPE_INDEX);


        Log.i("TextureShaderProgram", "TextureShaderProgram: aTypeIndex="+ uTypeIndex +" aColorLocation="+aColorLocation
        +" aTextureCoordinatesLocation="+aTextureCoordinatesLocation+" aPositionLocation="+aPositionLocation);


    }


    public void setUniforms( int textureId) {
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        glUniform1i(uTextureUnitLocation, 0);
    }


    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }

    public int getColorAttributeLocation() {
        return aColorLocation;
    }

    public int getFilterIndexUniformLocation(){
        return uTypeIndex;
    }

}
