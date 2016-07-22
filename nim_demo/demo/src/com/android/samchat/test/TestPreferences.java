package com.android.samchat.test;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.nim.demo.DemoCache;

public class TestPreferences {
    private static final String KEY_USER_TEST = "test";
    public static void saveUserTest(String test) {
        saveString(KEY_USER_TEST, test);
    }

    public static String getUserTest() {
        return getString(KEY_USER_TEST);
    }

    private static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getString(String key) {
        return getSharedPreferences().getString(key, null);
    }

    static SharedPreferences getSharedPreferences() {
        return DemoCache.getContext().getSharedPreferences("Demo", Context.MODE_PRIVATE);
    }
}

