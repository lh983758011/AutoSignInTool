package com.thundersoft.autosignintool;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    public static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setDisplayHomeAsUpEnabled(true);
        }

        requestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAccessibilityOn();
    }

    private void checkAccessibilityOn(){
        if (!isAccessibilitySettingsOn(this,
                AutoSigninService.class.getName())) {// 判断服务是否开启
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Utils.toast(this, "服务已开启");
            // 打开飞书应用
            Intent intent1 = new Intent("android.intent.action.MAIN");
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName componentName = new ComponentName("com.ss.android.lark", "com.ss.android.lark.main.app.MainActivity");
            intent1.setComponent(componentName);
            startActivity(intent1);
        }
    }

    //判断自定义辅助功能服务是否开启
    private boolean isAccessibilitySettingsOn(Context context, String className) {
        if (context == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningServiceInfo> runningServices =
                    activityManager.getRunningServices(100);// 获取正在运行的服务列表
            if (runningServices.size() < 0) {
                return false;
            }
            for (int i = 0; i < runningServices.size(); i++) {
                ComponentName service = runningServices.get(i).service;
                if (service.getClassName().equals(className)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference locationPreference = findPreference("location");
            locationPreference.setOnPreferenceClickListener(preference -> {
                String locationStr = Utils.getCurrentLocationStr(getContext());
                Log.e(TAG, "location:" + locationStr);
                preference.setSummary(locationStr);
                return false;
            });

            Preference startPre = findPreference("start");
            startPre.setOnPreferenceClickListener(preference -> {
                // 前往开启辅助服务界面
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                return false;
            });

            Preference launchPre = findPreference("launch");
            launchPre.setOnPreferenceClickListener(preference -> {
                // 打开飞书应用
                Intent intent1 = new Intent("android.intent.action.MAIN");
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName componentName = new ComponentName("com.ss.android.lark", "com.ss.android.lark.main.app.MainActivity");
                intent1.setComponent(componentName);
                startActivity(intent1);
                return false;
            });

            ListPreference listPreference = findPreference("choose_date");
            SwitchPreferenceCompat switchPreference = findPreference("switch");


            switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean switchState = (boolean) newValue;


                return true;
            });

            listPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (preference instanceof ListPreference) {
                    ListPreference listPreference1 = (ListPreference) preference;
                    CharSequence[] entries = listPreference1.getEntries();
                    int index = listPreference1.findIndexOfValue((String) newValue);
                    //listPreference1.setSummary(entries[index]);
                }
                return true;
            });
        }
    }

    private void requestPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0){
            Log.e(TAG, "0 result = " + grantResults);
        }else if(requestCode == 1) {
            Log.e(TAG, "1 result = " + grantResults);
        }
    }
}