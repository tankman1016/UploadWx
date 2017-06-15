package com.pcjh.uploadwx.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Lin 2016/3/11
 * 偏好设置类，用于缓存用户的数据 ；
 */
public class SharedPrefsUtil {
    public static final String SYSTEM_CONFIG = "system_config";

    public static void putValue(Context context, String key, int value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(SYSTEM_CONFIG, Context.MODE_PRIVATE).edit();
        sp.putInt(key, value);
        sp.commit();
    }

    public static void putValue(Context context, String key, boolean value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(SYSTEM_CONFIG, Context.MODE_PRIVATE).edit();
        sp.putBoolean(key, value);
        sp.commit();
    }

    public static void putValue(Context context, String key, String value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(SYSTEM_CONFIG, Context.MODE_PRIVATE).edit();
        sp.putString(key, value);
        sp.commit();
    }

    public static int getValue(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(SYSTEM_CONFIG, Context.MODE_PRIVATE);
        int value = sp.getInt(key, defValue);
        return value;
    }

    public static void putValue(Context context, String key, long value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(SYSTEM_CONFIG, Context.MODE_PRIVATE).edit();
        sp.putLong(key, value);
        sp.commit();
    }

    public static long getValue(Context context, String key, long defValue) {
        SharedPreferences sp = context.getSharedPreferences(SYSTEM_CONFIG, Context.MODE_PRIVATE);
        long value = sp.getLong(key, defValue);
        return value;
    }



    public static boolean getValue(Context context, String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(SYSTEM_CONFIG, Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(key, defValue);
        return value;
    }

    public static String getValue(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(SYSTEM_CONFIG, Context.MODE_PRIVATE);
        String value = sp.getString(key, defValue);
        return value;
    }
}
