package com.thundersoft.autosignintool;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.OutputStream;
import java.util.List;

public class AutoService extends Service {

    private boolean start = true;
    private OutputStream os;

    private static String ADB_SHELL = "input tap 550 2150 \n";

    private static String PACKAGE_NAME = "com.ss.android.lark";

    public AutoService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent intent1 = new Intent("android.intent.action.MAIN");
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName componentName = new ComponentName("com.ss.android.lark", "com.ss.android.lark.main.app.MainActivity");
        intent1.setComponent(componentName);
        startActivity(intent1);

        autoClick();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        start = false;
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
                Log.e(SettingsActivity.TAG, ra.processName);
                return ra.processName;
            } else {
                Log.e(SettingsActivity.TAG, "找不到");
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
            Log.e(SettingsActivity.TAG, e.getMessage());
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}