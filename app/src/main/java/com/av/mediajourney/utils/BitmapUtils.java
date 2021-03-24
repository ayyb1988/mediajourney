package com.av.mediajourney.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapUtils {
    //水平镜像翻转
    public static Bitmap mirror(Bitmap rawBitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1f, 1f);
        return Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.getWidth(), rawBitmap.getHeight(), matrix, true);
    }

    //旋转
    public static Bitmap rotate(Bitmap rawBitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.getWidth(), rawBitmap.getHeight(), matrix, true);
    }

    /**
     * 缩放图片
     *
     * @param bitmap 源图片
     * @param w      新图片宽
     * @param h      新图片高
     * @return 新图片
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, int w, int h) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidht = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidht, scaleHeight);
        return createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m,
                                      boolean filter) {
        Bitmap bitmap = null;
        if (source != null && !source.isRecycled()) {
            try {
                bitmap = Bitmap.createBitmap(source, 0, 0, width, height, m, true);
            }  catch (Exception e) {
               e.printStackTrace();
            }
        }
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
        }
        return bitmap;
    }
}
