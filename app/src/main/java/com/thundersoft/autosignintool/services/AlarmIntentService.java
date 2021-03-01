package com.thundersoft.autosignintool.services;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.Context;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.thundersoft.autosignintool.MyApplication;
import com.thundersoft.autosignintool.Utils;

public class AlarmIntentService extends JobIntentService {

    public static final String ACTION_START = "com.thundersoft.autosignintool.action.start";
    private static Context mContext;
    private boolean isContinue = true;
    private PowerManager.WakeLock sCpuWakeLock = null;

    private static final int JOB_ID = 1000;

    public static void enqueueWork(Context context, Intent work) {
        mContext = context;
        enqueueWork(context, AlarmIntentService.class, JOB_ID, work);
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

    @SuppressLint("InvalidWakeLockTag")
    private void handleActionStart() {
        if (sCpuWakeLock == null) {
            //PowerManager power = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
            //sCpuWakeLock = power.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
            //        PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "WakeLock");
        }
        while(isContinue && ((MyApplication)getApplication()).isOpen()){
            boolean isEnterSignInRange = Utils.isEnterRange(mContext);
            /*try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            //isEnterSignInRange = false;
            if (isEnterSignInRange){
                Utils.log("进入范围内，自动打开飞书");
                dismissKeyguard();
                //Utils.toast(getApplicationContext(), "进入范围内，自动打开飞书");
                Utils.startLarkApp(getApplicationContext());
                isContinue = false;
                //releaseWakeLock();
                stopSelf();
            }else{
                try {
                    Utils.log("未进入范围内");
                    Utils.toast(getApplicationContext(), "未进入范围内");
                    //wakeUpScreen();
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void dismissKeyguard(){
        try {
            Utils.log("唤醒屏幕");
            Utils.runShell("input keyevent KEYCODE_WAKEUP");
            Thread.sleep(500);
            Utils.runShell("wm dismiss-keyguard");
            Thread.sleep(500);
            Utils.runShell("input text 0925");
            Thread.sleep(1000);
            Utils.log("解锁完毕");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void wakeUpScreen(){
        if (sCpuWakeLock != null) {
            Utils.log("唤醒屏幕");
            sCpuWakeLock.acquire();
        }
    }

    private void releaseWakeLock(){
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}