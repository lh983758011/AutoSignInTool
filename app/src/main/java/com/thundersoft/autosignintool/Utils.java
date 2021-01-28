package com.thundersoft.autosignintool;

import android.Manifest;
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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

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
            Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    log("location:" + location);
                }
            });
            return lastKnownLocation;
        } else {
            Toast.makeText(context, "请检查网络或GPS是否打开", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public static String getCurrentLocationStr(Context context){
        Location location = getCurrentLocation(context);
        if (location == null)
            return "";
        return "" + location.getLatitude() + "," + location.getLongitude();
    }

    /**
     * 判断是否进入可打卡范围
     */
    public static boolean isEnterRange(Context context){
        Location location = getCurrentLocation(context);
        if (location != null){
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
    public static boolean isEnterRange(double lat, double lng){
        int distance = Integer.parseInt(getDistance(lat, lng, COMPANY_LATITUDE, COMPANY_LONGTIUDE));
        if (distance <= MIN_DISTANCE)
            return true;
        log("未进入范围");
        return false;
    }

    public static String getDistance(double lat, double lng){
        return getDistance(lat, lng, COMPANY_LATITUDE, COMPANY_LONGTIUDE);
    }

    private static final Double PI = Math.PI;

    private static final Double PK = 180 / PI;

    /**
     * @Description: 根据经纬度计算两点之间的距离
     *
     * @param lat_a a的经度
     * @param lng_a a的维度
     * @param lat_b b的经度
     * @param lng_b b的维度
     * @return 距离
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

    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private static String orderStr(float X, float Y) {
        return String.format("input tap %.2f %.2f", X, Y);
    }

    // 打开飞书APP
    public static void startLarkApp(Context context){
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

    public static void log(String message){
        Log.e(SettingsActivity.TAG, message);
    }
}
