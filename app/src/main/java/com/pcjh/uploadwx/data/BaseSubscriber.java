package com.pcjh.uploadwx.data;

import android.content.Context;
import android.widget.Toast;

import com.pcjh.uploadwx.utils.NetWorkUtils;

import rx.Subscriber;

/**
 * 基础订阅
 */

public abstract class BaseSubscriber<T> extends Subscriber<T> {

    private Context context;

    public BaseSubscriber(Context context){
       this.context=context;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!NetWorkUtils.isAvailable(context)){
            Toast.makeText(context,"网络连接不能用，请检查网络情况",Toast.LENGTH_SHORT).show();
        }
    }

}
