package com.pcjh.uploadwx.utils;

import java.io.File;


public class FileUtils {
    //判断文件是不是存在
    public static boolean isHave(String path){
        File file=new File(path);
        if(file.exists()){
            return true;
        }else {
            return false;
        }
    }
    //改变文件的权限
    public static boolean isChangeModeFileSuccess(String fileName){
        ShellUtils.CommandResult commandResult=ShellUtils.execCmd("chmod -R 777 " + fileName, true, true);
        if (commandResult.result==0){
            return true;
        }else {
            return false;
        }
    }
    // 删除缓存
    public static boolean deleteCache(String fileName){

        ShellUtils.CommandResult commandResult=ShellUtils.execCmd("rm -r " + fileName, false, true);
        if (commandResult.result==0){
            return true;
        }else {
            return false;
        }
    }
    //复制文件
    public static boolean copyFile(String p1,String p2){
        StringBuilder shell=new StringBuilder("cp -f ");
        shell.append(p1);
        shell.append(" ");
        shell.append(p2);
        ShellUtils.CommandResult commandResult=ShellUtils.execCmd(shell.toString(),false,true);

        if (commandResult.result==0){
            return true;
        }else {
            return false;
        }
    }

}