package com.thundersoft.autosignintool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thundersoft.autosignintool.services.AlarmIntentService;

/**
 * 接收闹钟广播
 */
public class AlarmReceiver extends BroadcastReceiver {
    private Context mContext;
    public static final String ACTION_ALARM = "com.thundersoft.autosignintool.alarmbroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        if (intent != null){
            if (intent.getAction().equals(ACTION_ALARM)){
                Utils.log("AlarmReceiver start service");
                Intent intent1 = new Intent(context, AlarmIntentService.class);
                intent1.setAction(AlarmIntentService.ACTION_START);
                AlarmIntentService.enqueueWork(context, intent1);
            }
        }
    }
}