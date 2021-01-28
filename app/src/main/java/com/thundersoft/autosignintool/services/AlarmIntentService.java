package com.thundersoft.autosignintool.services;

import android.content.Intent;
import android.content.Context;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.thundersoft.autosignintool.Utils;

public class AlarmIntentService extends JobIntentService {

    public static final String ACTION_START = "com.thundersoft.autosignintool.action.start";
    private boolean isContinue = true;
    private PowerManager.WakeLock sCpuWakeLock = null;

    public AlarmIntentService() {

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                handleActionStart();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isContinue = false;
    }

    private void handleActionStart() {
        PowerManager power = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        sCpuWakeLock = power.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "WakeLock");
        while(isContinue){
            boolean isEnterSignInRange = Utils.isEnterRange(getApplicationContext());
            if (isEnterSignInRange){
                Utils.log("进入范围内，自动打开飞书");
                Utils.startLarkApp(getApplicationContext());
                isContinue = false;
                releaseWakeLock();
                stopSelf();
            }else{
                try {
                    wakeUpScreen();
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void wakeUpScreen(){
        if (sCpuWakeLock != null)
            sCpuWakeLock.acquire();
    }

    private void releaseWakeLock(){
        if (sCpuWakeLock != null)
            sCpuWakeLock.release();
    }


    public static void startActionStart(Context context) {
        Intent intent = new Intent(context, AlarmIntentService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }
}