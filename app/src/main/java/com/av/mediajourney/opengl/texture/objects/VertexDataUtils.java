package com.av.mediajourney.opengl.texture.objects;

public class VertexDataUtils {

    public static final float[] VERTEX_DATA = {
            // Order of coordinates: X, Y, R, G, B, S, T
            // Triangle Fan
            0f,    0f, 1.0f,0.0f,0.0f,  0.5f, 0.5f,
            -1f, -1f,   1.0f,1.0f,0.0f,  0f, 1f,
            1f, -1f,   0.0f,0.0f,1.0f,  1f, 1f,
            1f,  1f,   0.0f,0.0f,0.0f,  1f, 0.0f,
            -1f,  1f,   1.0f,0.0f,0.0f,  0f, 0.0f,
            -1f, -1f,  1.0f,1.0f,0.0f,   0f, 1f };

    public static final float[] SPLIT_SCREEN_2_VERTEX_DATA = {
            // Order of coordinates: X, Y, R, G, B, S, T
            // Triangle Fan
            0f,    0f, 1.0f,0.0f,0.0f,  0.5f, 1f,
            -1f, -1f,   1.0f,1.0f,0.0f,  0f, 2f,
            1f, -1f,   0.0f,0.0f,1.0f,  1f, 2f,
            1f,  1f,   0.0f,0.0f,0.0f,  1f, 0.0f,
            -1f,  1f,   1.0f,0.0f,0.0f,  0f, 0.0f,
            -1f, -1f,  1.0f,1.0f,0.0f,   0f, 2f };


    public static final float[] SPLIT_SCREEN_3_VERTEX_DATA = {
            // Order of coordinates: X, Y, R, G, B, S, T
            // Triangle Fan
            0f,    0f, 1.0f,0.0f,0.0f,  0.5f, 1.5f,
            -1f, -1f,   1.0f,1.0f,0.0f,  0f, 3f,
            1f, -1f,   0.0f,0.0f,1.0f,  1f, 3f,
            1f,  1f,   0.0f,0.0f,0.0f,  1f, 0.0f,
            -1f,  1f,   1.0f,0.0f,0.0f,  0f, 0.0f,
            -1f, -1f,  1.0f,1.0f,0.0f,   0f, 3f };


    public static final float[] SPLIT_SCREEN_8_VERTEX_DATA = {
            // Order of coordinates: X, Y, R, G, B, S, T
            // Triangle Fan
            0f,    0f, 1.0f,0.0f,0.0f,  1f, 2f,
            -1f, -1f,   1.0f,1.0f,0.0f,  0f, 4f,
            1f, -1f,   0.0f,0.0f,1.0f,  2f, 4f,
            1f,  1f,   0.0f,0.0f,0.0f,  2f, 0.0f,
            -1f,  1f,   1.0f,0.0f,0.0f,  0f, 0.0f,
            -1f, -1f,  1.0f,1.0f,0.0f,   0f, 4f };

}
