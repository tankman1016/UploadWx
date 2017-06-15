package com.pcjh.uploadwx.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class UploadAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, UploadService.class));
    }
}
