package com.thundersoft.autosignintool;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.List;

class Utils {

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
                    Log.e(SettingsActivity.TAG, "location:" + location);
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


    public static void exeOrderOnTouchEvent(float X, float Y) {
        try {
            Log.e(SettingsActivity.TAG, orderStr(X, Y));
            Process ps = Runtime.getRuntime().exec(orderStr(X, Y));
            Thread.sleep(2 * 1000);
        } catch (Exception e) {
            Log.e(SettingsActivity.TAG, "" + e);
            e.printStackTrace();
        }
    }

    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private static String orderStr(float X, float Y) {
        return String.format("input tap %.2f %.2f", X, Y);
    }

    public static boolean isInScope(){

        return false;
    }
}
