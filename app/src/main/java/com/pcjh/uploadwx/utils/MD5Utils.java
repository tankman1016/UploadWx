package com.pcjh.uploadwx.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * Created by Lin on 2017/3/23.
 * 32位MD5加密方法
 */

public class MD5Utils {

    public static String getMd5Value(String sSecret) {
        try {
            MessageDigest bmd5 = MessageDigest.getInstance("MD5");
            bmd5.update(sSecret.getBytes());
            int i;
            StringBuilder stringBuilder = new StringBuilder();
            byte[] b = bmd5.digest();// 加密
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    stringBuilder.append("0");
                stringBuilder.append(Integer.toHexString(i));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
