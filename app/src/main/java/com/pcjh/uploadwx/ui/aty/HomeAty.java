package com.pcjh.uploadwx.ui.aty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pcjh.uploadwx.R;
import com.pcjh.uploadwx.data.AppHolder;
import com.pcjh.uploadwx.data.UploadService;
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
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeAty extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_home);
        //开启服务
        startService(new Intent(this, UploadService.class));
    }

}
