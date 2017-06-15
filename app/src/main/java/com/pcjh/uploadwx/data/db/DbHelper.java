package com.pcjh.uploadwx.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pcjh.uploadwx.data.AppConstants;


public class DbHelper extends SQLiteOpenHelper {

    private DbHelper(Context context, String name,
                     SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

    }

    public DbHelper(Context context) {
        this(context, AppConstants.Database_Name, null, AppConstants.Database_Version);
    }

    /**
     * 用户表
     * username 微信用户名
     * wxdbpwd 微信数据库密码
     * wxuin  微信uin
     * alias  微信别名
     * filename 32位的文件名字
     * wxdbpath 微信数据库路径
     */

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL("create table user(username TEXT PRIMARY KEY,wxdbpwd TEXT,wxuin TEXT,alias TEXT,filename TEXT,wxdbpath TEXT)");
        } catch (Exception e) {
            Log.v("Lin", "创建表失败!");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
