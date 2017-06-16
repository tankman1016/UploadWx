package com.pcjh.uploadwx.data.api;

import com.pcjh.uploadwx.data.AppConstants;
import com.pcjh.uploadwx.data.entity.ReturnResult;
import com.pcjh.uploadwx.data.entity.Token;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;


/**
 * 接口管理
 */

public class ApiManager {
    private static AppApi appApi;

    public static AppApi getClientNoCache() {

        if (appApi == null) {
            Retrofit client = new Retrofit.Builder()
                    .baseUrl(AppConstants.Base_Url_Test)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            appApi = client.create(AppApi.class);
        }
        return appApi;
    }

    public interface AppApi {
        //获得token
        //kefu_alias 客服微信号
        @FormUrlEncoded
        @POST("get_token")
        Observable<Token> getTokenRxJava(@Field("kefu_alias") String username);

        // token
        // 上传粉丝列表
        @FormUrlEncoded
        @POST("append_fans")
        Observable<ReturnResult> uploadFansRxJava(
                @Field("token") String token,
                @Field("kefu_alias") String kefu_alias,
                @Field("fans_list") String encryptJson);

        // token
        // 上传信息列表
        @FormUrlEncoded
        @POST("append_messages")
        Observable<ReturnResult> uploadMessagesRxJava(
                @Field("token") String token,
                @Field("kefu_alias") String kefu_alias,
                @Field("chat_log") String encryptJson);

        //上传文件
        // token
        // zip文件zip文件
        @Multipart
        @POST("upload_zip")
        Observable<ReturnResult> uploadZipRxJava(@Part("token") RequestBody token ,@Part() MultipartBody.Part body);

    }

}
