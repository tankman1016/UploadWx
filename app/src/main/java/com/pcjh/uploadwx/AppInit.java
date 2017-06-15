package com.pcjh.uploadwx;


import android.app.Application;

import com.pcjh.uploadwx.data.db.DbManager;


public class AppInit extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DbManager.initializeInstance(getApplicationContext());
    }
}
