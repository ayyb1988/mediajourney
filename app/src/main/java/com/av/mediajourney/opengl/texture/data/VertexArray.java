/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.av.mediajourney.opengl.texture.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

public class VertexArray {    
    private final FloatBuffer floatBuffer;

    private final static int BYTES_PER_FLOAT = 4;

    public VertexArray(float[] vertexData) {
        floatBuffer = ByteBuffer
            .allocateDirect(vertexData.length * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData);
    }
        
    public void setVertexAttribPointer(int dataOffset, int attributeLocation,
        int componentCount, int stride) {        
        floatBuffer.position(dataOffset);        
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, 
            false, stride, floatBuffer);
        glEnableVertexAttribArray(attributeLocation);
        
        floatBuffer.position(0);
    }
}
