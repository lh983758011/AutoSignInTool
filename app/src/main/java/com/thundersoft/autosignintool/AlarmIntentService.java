package com.thundersoft.autosignintool;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class AlarmIntentService extends JobIntentService {

    private static final String ACTION_START = "com.thundersoft.autosignintool.action.start";
    private boolean isContinue = true;

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
        while(isContinue){
            boolean isEnterSignInRange = Utils.isEnterRange(getApplicationContext());
            if (isEnterSignInRange){
                Utils.startLarkApp(getApplicationContext());
                isContinue = false;
                stopSelf();
            }else{
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void startActionStart(Context context) {
        Intent intent = new Intent(context, AlarmIntentService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }
}