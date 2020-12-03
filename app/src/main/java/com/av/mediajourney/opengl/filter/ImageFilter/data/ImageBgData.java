package com.av.mediajourney.opengl.filter.ImageFilter.data;

public class ImageBgData {
    public static final float[] VERTEX_DATA = {
            // Order of coordinates: X, Y,  S, T
            // Triangle Fan
            0f,   0f,   0.5f, 1f,
            -1f, -1f,   0f, 2f,
            1f, -1f,    1f, 2f,
            1f,  1f,    1f, 0.0f,
            -1f,  1f,   0f, 0.0f,
            -1f, -1f,   0f, 2f };


    public static final float[] GRAY_FILTER_COLOR_DATA = {0.3f,0.59f,0.11f};

    public static final float[] WARM_FILTER_COLOR_DATA = {0.3f, 0.3f, 0.0f};

    public static final float[] COOL_FILTER_COLOR_DATA = {0.0f, 0.0f, 0.2f};


}
