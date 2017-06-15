package com.pcjh.uploadwx.utils;

import android.os.Environment;

import java.io.File;

public class SDCardUtils {

    /**
     * 判断SD卡是否可用
     * @return true : 可用<br>false : 不可用
     */
    public static boolean isSDCardEnable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取SD卡路径
     * <p>一般是/storage/emulated/0/</p>
     * @return SD卡路径
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath() + File.separator;
    }

    /**
     * 获取SD卡Data路径
     * @return Data路径
     */
    public static String getDataPath() {
        return Environment.getDataDirectory().getPath();

    }


}
