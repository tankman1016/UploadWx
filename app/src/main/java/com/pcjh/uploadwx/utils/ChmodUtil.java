package com.pcjh.uploadwx.utils;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 修改读写权限工具；
 */
public class ChmodUtil {
    public static void setFileCanRead(File file) {
        try {
            String command = "chmod 777 " + file.getAbsolutePath();
            Log.i("Lin", "command = " + command);
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("su");
            DataOutputStream os = new DataOutputStream(proc.getOutputStream());
            os.writeBytes(command+"\n");
            os.writeBytes("exit\n");
            os.flush();
            int status =proc.waitFor();
            if(status==0){
                Log.i("Lin","file chmod ok");
            }else{
                Log.i("Lin","file chmod failed");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
