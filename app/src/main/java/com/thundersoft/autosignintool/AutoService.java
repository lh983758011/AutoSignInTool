package com.thundersoft.autosignintool;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

public class AutoService extends Service {

    private boolean start = true;
    private OutputStream os;

    private AlarmManager mAlarmManager = null;

    private static final int[] days = new int[]{
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
    };

    private static String ADB_SHELL = "input tap 550 2150 \n";

    private static String PACKAGE_NAME = "com.ss.android.lark";

    public AutoService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        for (int i : days) {
            Calendar targetTime = Calendar.getInstance();
            targetTime.set(Calendar.DAY_OF_WEEK, i);
            targetTime.set(Calendar.HOUR_OF_DAY, 9);
            targetTime.set(Calendar.MINUTE, 20);
            targetTime.set(Calendar.SECOND, 0);
            targetTime.set(Calendar.MILLISECOND, 0);
            setAlarm(getApplicationContext(), i,targetTime);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void setAlarm(Context context, int requestCode, Calendar targetTime) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent("android.intent.action.MAIN");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        ComponentName componentName = new ComponentName("com.ss.android.lark", "com.ss.android.lark.main.app.MainActivity");
//        intent.setComponent(componentName);
//        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        // mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, targetTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, mPi);
        // mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.currentThreadTimeMillis() + 20 * 1000, 20 * 1000, mPi);
        Intent intent = new Intent(context, AlarmIntentService.class);
        intent.setAction(AlarmIntentService.ACTION_START);
        PendingIntent pi = PendingIntent.getService(context, requestCode, intent, 0);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, targetTime.getTimeInMillis(), pi);
    }

    @Override
    public void onDestroy() {
        start = false;
        if (mAlarmManager != null) {
//            Intent intent = new Intent("android.intent.action.MAIN");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            ComponentName componentName = new ComponentName("com.ss.android.lark", "com.ss.android.lark.main.app.MainActivity");
//            intent.setComponent(componentName);
//            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            for (int i : days) {
                Intent intent = new Intent(getApplicationContext(), AlarmIntentService.class);
                intent.setAction(AlarmIntentService.ACTION_START);
                PendingIntent pi = PendingIntent.getService(getApplicationContext(), i, intent, 0);
                mAlarmManager.cancel(pi);
            }
        }
        super.onDestroy();
    }

    private void autoClick() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //while (start) {
                if (isCurrentAppIsTarget()) {
                    exec(ADB_SHELL);
                }
//					 1.利用ProcessBuilder执行shell命令，不能后台
//					int x = 0, y = 0;
//					String[] order = { "input", "tap", " ", x + "", y + "" };
//					try {
//						new ProcessBuilder(order).start();
//					} catch (IOException e) {
//						Log.i("GK", e.getMessage());
//						e.printStackTrace();
//					}

                // 2.可以不用在 Activity 中增加任何处理，各 Activity 都可以响应，不能后台
//					try {
//						Instrumentation inst = new Instrumentation();
//						inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0));
//						inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0));
//						Log.i("GK", "模拟点击" + x + ", " + y);
//					} catch (Exception e) {
//						Log.e("Exception when sendPointerSync", e.toString());
//					}
                try {
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //}
            }
        }).start();
    }

    /**
     * 如果前台APP是目标apk
     */
    private boolean isCurrentAppIsTarget() {
        String name = getForegroundAppPackageName();
        if (!TextUtils.isEmpty(name) && PACKAGE_NAME.equalsIgnoreCase(name)) {
            return true;
        }
        return true;
    }

    /**
     * 获取前台程序包名，该方法在android L之前有效
     */
    public String getForegroundAppPackageName() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lr = am.getRunningAppProcesses();
        if (lr == null) {
            return null;
        }

        for (ActivityManager.RunningAppProcessInfo ra : lr) {
            if (ra.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE || ra.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                Utils.log(ra.processName);
                return ra.processName;
            } else {
                Utils.log("找不到");
            }
        }
        return "";
    }

    /**
     * 执行ADB命令： input tap 125 340
     */
    public final void exec(String cmd) {
        try {
            if (os == null) {
                os = Runtime.getRuntime().exec(ADB_SHELL).getOutputStream();
            }
            os.write(cmd.getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log(e.getMessage());
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}