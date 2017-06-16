package com.pcjh.uploadwx;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.pcjh.uploadwx.data.AppHolder;
import com.pcjh.uploadwx.data.BaseSubscriber;
import com.pcjh.uploadwx.data.api.ApiManager;
import com.pcjh.uploadwx.data.db.DbManager;
import com.pcjh.uploadwx.data.entity.Token;
import com.pcjh.uploadwx.data.entity.User;
import com.pcjh.uploadwx.ui.aty.HomeAty;
import com.pcjh.uploadwx.utils.ChmodUtil;
import com.pcjh.uploadwx.utils.Root;
import com.pcjh.uploadwx.utils.XmlPaser;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class IndexActivity extends AppCompatActivity {
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置显示全屏;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //Root
        Root.getInstance().getRoot(new Root.IGotRootListener() {
            @Override
            public void onGotRootResult(boolean hasRoot) {
                if (hasRoot) {
                    init();
                } else {
                    Toast.makeText(IndexActivity.this, "未获得root权限", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("Lin", "onPause");
        if (subscription != null && subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    private void init() {

        final String wxUin = XmlPaser.getUinFromFile();
        if (TextUtils.isEmpty(wxUin) || wxUin.equals("0")) {
            alertMessageAndExit("请登录微信号！");
        } else {
            subscription = Observable.just(wxUin)
                    .map(new Func1<String, User>() {
                        @Override
                        public User call(String wxUin) {
                            return queryUser(wxUin);
                        }
                    })
                    .map(new Func1<User, User>() {
                        @Override
                        public User call(User user) {
                            if (TextUtils.isEmpty(user.getUsername())) {
                                user = getUserFromWxDb(user);
                                addUser(user);
                            }
                            return user;
                        }
                    })
                    .flatMap(new Func1<User, Observable<Token>>() {
                        @Override
                        public Observable<Token> call(User user) {
                            if (!TextUtils.isEmpty(user.getAlias())) {
                                AppHolder.getInstance().setUser(user);
                                return ApiManager.getClientNoCache().getTokenRxJava(user.getAlias());
                            }
                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseSubscriber<Token>(this) {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            alertMessageAndExit(e.toString());
                        }

                        @Override
                        public void onNext(Token token) {
                            if (token == null) {
                                alertMessageAndExit("微信号未设置！");
                            } else if (token.getStatus() == 0) {
                                AppHolder.getInstance().getUser().setToken(token.getToken());
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(IndexActivity.this,HomeAty.class));
                                        finish();
                                    }
                                },1500);
                            } else {
                                alertMessageAndExit(token.getMsg());
                            }
                        }
                    });
        }

    }

    //从本地数据库查询User
    private User queryUser(String wxUin) {
        User user = new User();
        user.setUin(wxUin);
        android.database.sqlite.SQLiteDatabase database = DbManager.getInstance().openDatabase();
        try {
            android.database.Cursor cursor = database.rawQuery("select * from user where wxuin=" + wxUin, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    user.setAlias(cursor.getString(cursor.getColumnIndex("alias")));
                    user.setUsername(cursor.getString(cursor.getColumnIndex("username")));
                    user.setFilename(cursor.getString(cursor.getColumnIndex("filename")));
                    user.setWxdbpwd(cursor.getString(cursor.getColumnIndex("wxdbpwd")));
                    user.setWxdbpath(cursor.getString(cursor.getColumnIndex("wxdbpath")));
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("Lin", "查询失败:" + e.toString());
            return user;
        } finally {
            DbManager.getInstance().closeDatabase();
        }
        return user;
    }

    //添加用户
    private void addUser(User user) {

        long execSQLResult;
        android.database.sqlite.SQLiteDatabase database = DbManager.getInstance().openDatabase();

        ContentValues values = new ContentValues();
        values.put("alias", user.getAlias());
        values.put("username", user.getUsername());
        values.put("wxdbpwd", user.getWxdbpwd());
        values.put("wxdbpath", user.getWxdbpath());
        values.put("filename", user.getFilename());
        values.put("wxuin", user.getUin());
        //返回行数 -1 代表失败
        execSQLResult = database.insert("user", null, values);
        Log.v("addUserNumber:", execSQLResult + "");
        values.clear();
        DbManager.getInstance().closeDatabase();
    }

    //获得user从微信数据库
    private User getUserFromWxDb(User user) {

        //真机
        // String phoneIMEI = PhoneUtils.getPhoneIMEI(this);
        // String pwdWxDb = MD5Utils.getMd5Value(phoneIMEI + wxUin).substring(0, 7);
        //虚拟机
        String pwdWxDb = "4bdb2cc";
        user.setWxdbpwd(pwdWxDb);
        SQLiteDatabase.loadLibs(this);

        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;"); //兼容2.0的数据库
            }
        };

        File microMsgFile = new File("/data/data/com.tencent.mm/MicroMsg/");
        //设置可读取
        if (!microMsgFile.canRead()) {
            ChmodUtil.setFileCanRead(microMsgFile);
        }

        /*符合微信EnMicrloMsg.db的文件格式的文件*/
        ArrayList<File> dbDatas = new ArrayList<>();

        //得到名字32位长度的文件夹
        for (File md5File : microMsgFile.listFiles(new WxFileFilter())) {

            //获得指定的文件夹下的目录；

            if (!md5File.canRead()) {
                ChmodUtil.setFileCanRead(md5File);
            }
            for (File enMicroMsgDb : md5File.listFiles(new DbFileFilter())) {
                //获得下一层的数据 ；
                if (!enMicroMsgDb.canRead()) {
                    ChmodUtil.setFileCanRead(enMicroMsgDb);
                }
                //添加所有的EnMicroMsg.db到集合中 ；
                dbDatas.add(enMicroMsgDb);
            }
        }
        // 解密数据库文件得到信息，一个密码这里只能解决一个账户,有密码找寻对应数据库
        if (!dbDatas.isEmpty()) {
            Log.v("微信数据库个数：", dbDatas.size() + "");
            for (File enMicroMsgDb : dbDatas) {
                try {
                    SQLiteDatabase db = SQLiteDatabase.openDatabase(enMicroMsgDb.getAbsolutePath(),
                            pwdWxDb, null, SQLiteDatabase.OPEN_READWRITE, hook);
                    Cursor cursor = db.rawQuery("select * from userinfo", null);
                    if (cursor != null) {
                        user.setFilename(enMicroMsgDb.getParentFile().getName());
                        user.setWxdbpath(enMicroMsgDb.getAbsolutePath());
                        while (cursor.moveToNext()) {
                            String id = cursor.getString(cursor.getColumnIndex("id"));
                            String value = cursor.getString(cursor.getColumnIndex("value"));
                            switch (id) {
                                case "2":
                                    user.setUsername(value);
                                    break;
                                case "42":
                                    user.setAlias(value);
                                    break;
                            }
                        }
                        cursor.close();
                    }
                    db.close();
                    //能打开就break当前循环，不能打开就catch然后继续循环进行下次解密！
                    break;
                } catch (Exception e) {
                    //这里默认为密码错误！0_0
                    Log.v("Lin", "获取微信数据库userinfo表错误!:" + e);
                }
            }
        }
        return user;
    }

    /*将MicroMsg 文件夹下的子目录过滤出长度32位的文件*/
    private class WxFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().length() == 32;
        }
    }

    //将文件夹下的名字为 EnMicroMsg.db 过滤出来
    private class DbFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().equals("EnMicroMsg.db");
        }
    }

    //提示信息并退出！(延迟2s)
    private void alertMessageAndExit(String alertMessage) {
        Toast.makeText(IndexActivity.this, alertMessage,
                Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }

}
