package com.av.mediajourney.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import com.av.mediajourney.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static void saveBitmapToFile(Bitmap bitmap, String fileName) {
        File file = new File(MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
