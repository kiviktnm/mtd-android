package com.github.windore.mtd;

import android.util.Log;

public class Mtd {

    private static final String TAG = "Mtd";

    static {
        System.loadLibrary("mtda");
    }

    private static native String hello(String input);

    public static void test() {
        Log.d(TAG, hello("World"));
    }
}
