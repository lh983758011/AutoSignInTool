package com.thundersoft.autosignintool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 暂时没用
 */
public class AlarmReceiver extends BroadcastReceiver {
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        if (intent != null){
            if (intent.getAction().equals("com.thundersoft.autosignintool.alarmbroadcast")){
                if(Utils.isEnterRange(context)){
                    //进入打卡范围
                    Utils.startLarkApp(context);
                }
            }
        }
    }
}