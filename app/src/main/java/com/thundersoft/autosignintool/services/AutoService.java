package com.thundersoft.autosignintool.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.thundersoft.autosignintool.AlarmReceiver;
import com.thundersoft.autosignintool.Utils;
import com.thundersoft.autosignintool.services.AlarmIntentService;

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
            setAlarm(getApplicationContext(), i, targetTime);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void setAlarm(Context context, int requestCode, Calendar targetTime) {
        if (mAlarmManager == null)
            mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, 0);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, targetTime.getTimeInMillis(), pi);
    }

    @Override
    public void onDestroy() {
        start = false;
        if (mAlarmManager != null) {
            for (int i : days) {
                Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                intent.setAction(AlarmReceiver.ACTION_ALARM);
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), i, intent, 0);
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