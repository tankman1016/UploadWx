package com.pcjh.uploadwx.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.atomic.AtomicInteger;

public class DbManager {

    private AtomicInteger openCounter = new AtomicInteger();

    private static DbManager instance;

    private static SQLiteDatabase database;

    private DbHelper dbHelper;

    private DbManager(Context context) {
        dbHelper = new DbHelper(context);
    }

    /**
     * 初始化数据库单例
     */
    public static synchronized void initializeInstance(Context context) {
        if (instance == null) {
            instance = new DbManager(context);
        }
    }

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static synchronized DbManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DbManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    /**
     * 打开数据库
     *
     * @return 数据库
     */
    public synchronized SQLiteDatabase openDatabase() {
        if (openCounter.incrementAndGet() == 1) {
            // Opening new database
            database = dbHelper.getWritableDatabase();
        }
        return database;
    }

    /**
     * 关闭数据库
     */
    public synchronized void closeDatabase() {
        if (openCounter.decrementAndGet() == 0) {
            // Closing database
            database.close();
        }
    }

}
