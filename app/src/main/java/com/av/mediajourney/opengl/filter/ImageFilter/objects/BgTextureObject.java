/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.av.mediajourney.opengl.filter.ImageFilter.objects;
import com.av.mediajourney.opengl.texture.data.VertexArray;
import com.av.mediajourney.opengl.texture.programs.TextureShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;

public class BgTextureObject {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private final static int BYTES_PER_FLOAT = 4;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT
        + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    

    private final VertexArray vertexArray;
    
    public BgTextureObject(float[] vertexData) {
        vertexArray = new VertexArray(vertexData);
    }
    
    public void bindData(TextureShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(
            0, 
            textureProgram.getPositionAttributeLocation(), 
            POSITION_COMPONENT_COUNT,
            STRIDE);


        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            textureProgram.getTextureCoordinatesAttributeLocation(),
            TEXTURE_COORDINATES_COMPONENT_COUNT, 
            STRIDE);
    }
    
    public void draw() {                                
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }
}
