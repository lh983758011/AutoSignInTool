package com.thundersoft.autosignintool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.thundersoft.autosignintool.socket.SocketClient;

import java.util.Calendar;
import java.util.List;

public class Utils {

    public static final double COMPANY_LATITUDE = 30.586166;
    public static final double COMPANY_LONGTIUDE = 104.058851;
    public static final int MIN_DISTANCE = 450; // 单位：m

    public static Location getCurrentLocation(Context context) {

        //获取当前位置信息
        //获取定位服务
        String provider = null;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //获取当前可用的位置控制器
        List<String> list = locationManager.getProviders(true);

        if (list.contains(locationManager.GPS_PROVIDER)) {
//            GPS位置控制器
            provider = locationManager.GPS_PROVIDER;//GPS定位
        } else if (list.contains(locationManager.NETWORK_PROVIDER)) {
//            网络位置控制器
            provider = locationManager.NETWORK_PROVIDER;//网络定位
        }

        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "位置服务授权失败", Toast.LENGTH_LONG).show();
                return null;
            }
            Location lastKnownLocation = getLastKnownLocation(locationManager);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    log("location:" + location);
                }
            }, context.getMainLooper());
            return lastKnownLocation;
        } else {
            Toast.makeText(context, "请检查网络或GPS是否打开", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private static Location getLastKnownLocation(LocationManager locationManager) {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission")
            Location location = locationManager.getLastKnownLocation(provider);
            if (location == null) {
                continue;
            }
            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = location;
            }
        }
        return bestLocation;
    }


    public static String getCurrentLocationStr(Context context) {
        Location location = getCurrentLocation(context);
        if (location == null)
            return "";
        return "" + location.getLatitude() + "," + location.getLongitude();
    }

    /**
     * 判断是否进入可打卡范围
     */
    public static boolean isEnterRange(Context context) {
        Location location = getCurrentLocation(context);
        if (location != null) {
            return isEnterRange(location.getLatitude(), location.getLongitude());
        }
        return false;
    }

    /**
     * 判断是否进入可打卡范围
     *
     * @param lat
     * @param lng
     * @return true 进入范围内
     */
    public static boolean isEnterRange(double lat, double lng) {
        int distance = Integer.parseInt(getDistance(lat, lng, COMPANY_LATITUDE, COMPANY_LONGTIUDE));
        if (distance <= MIN_DISTANCE) {
            log("进入范围内");
            return true;
        }
        log("未进入范围");
        return false;
    }

    public static String getDistance(double lat, double lng) {
        return getDistance(lat, lng, COMPANY_LATITUDE, COMPANY_LONGTIUDE);
    }

    private static final Double PI = Math.PI;

    private static final Double PK = 180 / PI;

    /**
     * @param lat_a a的经度
     * @param lng_a a的维度
     * @param lat_b b的经度
     * @param lng_b b的维度
     * @return 距离
     * @Description: 根据经纬度计算两点之间的距离
     */
    public static String getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        double t1 =
                Math.cos(lat_a / PK) * Math.cos(lng_a / PK) * Math.cos(lat_b / PK) * Math.cos(lng_b / PK);
        double t2 =
                Math.cos(lat_a / PK) * Math.sin(lng_a / PK) * Math.cos(lat_b / PK) * Math.sin(lng_b / PK);
        double t3 = Math.sin(lat_a / PK) * Math.sin(lat_b / PK);

        double tt = Math.acos(t1 + t2 + t3);
        String res = (6366000 * tt) + "";
        log("distance = " + res);
        return res.substring(0, res.indexOf("."));
    }


    public static void exeOrderOnTouchEvent(float X, float Y) {
        try {
            log(orderStr(X, Y));
            Process ps = Runtime.getRuntime().exec(orderStr(X, Y));
            Thread.sleep(2 * 1000);
        } catch (Exception e) {
            log("" + e);
            e.printStackTrace();
        }
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private static String orderStr(float X, float Y) {
        return String.format("input tap %.2f %.2f", X, Y);
    }

    // 打开飞书APP
    public static void startLarkApp(Context context) {
        log("starting lark app");
        Intent intent1 = new Intent("android.intent.action.MAIN");
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName componentName = new ComponentName("com.ss.android.lark", "com.ss.android.lark.main.app.MainActivity");
        intent1.setComponent(componentName);
        context.startActivity(intent1);
    }

    // 播放铃声
    public static void playRing(Context context) throws InterruptedException {
        AudioManager am = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = volumnCurrent / audioMaxVolumn;
        SoundPool soundPool = new SoundPool.Builder().setMaxStreams(1)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC).build())
                .build();
        int sourceId = soundPool.load(context, R.raw.notification_at, 1);
        Thread.sleep(1000);

        soundPool.play(sourceId, volumnRatio, volumnRatio, 1, 2, 1);
    }

    // 日志
    public static void log(String message) {
        Log.e(SettingsActivity.TAG, message);
    }


    private static final int[] days = new int[]{
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
    };

    public static void setAlarm(Context context, int hour, int minute) {
        for (int i : days) {
            Calendar targetTime = Calendar.getInstance();
            targetTime.set(Calendar.DAY_OF_WEEK, i);
            targetTime.set(Calendar.HOUR_OF_DAY, hour);
            targetTime.set(Calendar.MINUTE, minute);
            targetTime.set(Calendar.SECOND, 0);
            targetTime.set(Calendar.MILLISECOND, 0);
            setAlarm(context, i, targetTime);
        }
    }

    private static void setAlarm(Context context, int requestCode, Calendar targetTime) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTime.getTimeInMillis(), pi);
    }

    public static void cancelAlarm(Context context) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (int i : days) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.setAction(AlarmReceiver.ACTION_ALARM);
            PendingIntent pi = PendingIntent.getBroadcast(context, i, intent, 0);
            mAlarmManager.cancel(pi);
            pi.cancel();
        }
    }

    /**
     * 执行shell命令
     *
     * adb push classes.dex /data/local/tmp
     * cd /data/local/tmp
     * app_process -Djava.class.path=/data/local/tmp/classes.dex /system/bin shellService.Main
     *
     * @param cmd
     */
    public static void runShell(final String cmd){
        if (TextUtils.isEmpty(cmd)) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                new SocketClient(cmd, new SocketClient.onServiceSend() {
                    @Override
                    public void getSend(String result) {
                        log("runShell return :" + result);
                    }
                });
            }
        }).start();
    }

    public interface ShellCallback{
        void onCallback(String result);
    }

    public static void runShell(final String cmd, ShellCallback callback){
        if (TextUtils.isEmpty(cmd)) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                new SocketClient(cmd, new SocketClient.onServiceSend() {
                    @Override
                    public void getSend(String result) {
                        log("runShell return :" + result);
                        callback.onCallback(result);
                    }
                });
            }
        }).start();
    }

}
