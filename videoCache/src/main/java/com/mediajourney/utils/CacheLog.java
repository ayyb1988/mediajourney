package com.mediajourney.utils;

import android.util.Log;

public class CacheLog {
    public final static boolean DEBUG = true;
    private static final String TAG = "CacheLog";


    public static void i(String tag, String s) {
        Log.i(TAG, "tag: "+s);

    }


    public static void d(String tag, String s) {
        Log.d(TAG, "tag: "+s);
    }


    public static void w(String tag, String s) {
        Log.w(TAG, "tag: "+s);
    }


    public static void e(String tag, String s) {
        Log.e(TAG, "tag: "+s);
    }

    public static void e(String s) {
        Log.e(TAG, ": "+s);
    }
}
