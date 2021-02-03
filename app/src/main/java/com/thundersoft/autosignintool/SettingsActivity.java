package com.thundersoft.autosignintool;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.thundersoft.autosignintool.services.AlarmIntentService;
import com.thundersoft.autosignintool.services.AutoService;
import com.thundersoft.autosignintool.services.AutoSigninService;
import com.thundersoft.autosignintool.services.RedPacketService;

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //保持屏幕常亮
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
            //openAccessibilitySettings();
            Utils.toast(this, "自动签卡服务没有开启");
        }
        else if (!isAccessibilitySettingsOn(this,
                RedPacketService.class.getName())) {
            //openAccessibilitySettings();
            Utils.toast(this, "红包助手服务没有开启");
        } else if (!isNotificationEnabled()){
            Utils.toast(this, "红包通知服务没有开启");
        } else{
            Utils.toast(this, "准备就绪");
        }
    }

    // 检查通知服务是否打开
    private boolean isNotificationEnabled() {
        ContentResolver contentResolver = getContentResolver();
        String enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");

        if (!TextUtils.isEmpty(enabledListeners)) {
            return enabledListeners.contains(getPackageName() + "/" + getPackageName() + ".services.NotificationService");
        } else {
            return false;
        }
    }


    // 打开飞书
    private void startLarkApp(){
        Intent intent1 = new Intent("android.intent.action.MAIN");
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName componentName = new ComponentName("com.ss.android.lark", "com.ss.android.lark.main.app.MainActivity");
        intent1.setComponent(componentName);
        startActivity(intent1);
    }

    // 打开微信
    private void startWechatApp(){
        Intent intent1 = new Intent("android.intent.action.MAIN");
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
        intent1.setComponent(componentName);
        startActivity(intent1);
    }

    private void startNotificationListenerSettings(){
        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    // 打开无障碍设置
    private void openAccessibilitySettings(){
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // 判断自定义辅助功能服务是否开启
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

        SwitchPreferenceCompat mSwitchPreference = null;
        SwitchPreferenceCompat mRedPacketSwitchPreference = null;
        Preference mStatePreference = null;
        Preference mDistancePreference = null;


        @Override
        public void onStart() {
            super.onStart();
            if (mSwitchPreference != null){
                mSwitchPreference.setChecked(((MyApplication)getActivity().getApplication()).isOpen());
            }
            if (mRedPacketSwitchPreference != null){
                mRedPacketSwitchPreference.setChecked(((MyApplication)getActivity().getApplication()).isRedPacketOpen());
            }
            if (mStatePreference != null){
                mStatePreference.setSummary(
                        "自动签卡服务 : " + (((SettingsActivity)getActivity()).isAccessibilitySettingsOn(getContext(), AutoSigninService.class.getName())? "开启": "关闭")
                        + "\n红包通知服务 : " + (((SettingsActivity)getActivity()).isNotificationEnabled()? "开启": "关闭")
                        + "\n红包助手服务 : " + (((SettingsActivity)getActivity()).isAccessibilitySettingsOn(getContext(), RedPacketService.class.getName())? "开启": "关闭")
                );
            }
            if (mDistancePreference != null){
                Location location = Utils.getCurrentLocation(getContext());
                if (location != null)
                    mDistancePreference.setSummary("进入打卡范围：" + Utils.isEnterRange(location.getLatitude(), location.getLongitude())
                            + " 距离： " + Utils.getDistance(location.getLatitude(), location.getLongitude()) + "米");
                else
                    mDistancePreference.setSummary("定位失败");
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // 状态
            mStatePreference = findPreference("state");

            // 定位
            Preference locationPreference = findPreference("location");
            locationPreference.setOnPreferenceClickListener(preference -> {
                String locationStr = Utils.getCurrentLocationStr(getContext());
                Utils.log("location:" + locationStr);
                preference.setSummary(locationStr);
                return false;
            });

            // 距离
            mDistancePreference = findPreference("distance");
            mDistancePreference.setOnPreferenceClickListener(preference -> {
                Location location = Utils.getCurrentLocation(getContext());
                if (location != null)
                    preference.setSummary("进入打卡范围：" + Utils.isEnterRange(location.getLatitude(), location.getLongitude())
                            + " 距离： " + Utils.getDistance(location.getLatitude(), location.getLongitude()) + "米");
                else
                    preference.setSummary("定位失败");
                return false;
            });

            // 重置
            Preference resetPreference = findPreference("reset");
            resetPreference.setOnPreferenceClickListener(preference -> {
                reset();
                return false;
            });

            // 打开无障碍设置
            Preference startPre = findPreference("start");
            startPre.setOnPreferenceClickListener(preference -> {
                // 前往开启辅助服务界面
                ((SettingsActivity)getActivity()).openAccessibilitySettings();
                return false;
            });

            // 打开通知设置
            Preference startNotificationPre = findPreference("start_notification");
            startNotificationPre.setOnPreferenceClickListener(preference -> {
                // 前往开启辅助服务界面
                ((SettingsActivity)getActivity()).startNotificationListenerSettings();
                return false;
            });

            // 打开飞书
            Preference launchPre = findPreference("launch");
            launchPre.setOnPreferenceClickListener(preference -> {
                // 打开飞书应用
                ((SettingsActivity)getActivity()).startLarkApp();
                return false;
            });

            // 打开微信
            Preference launchWechatPre = findPreference("launch_wechat");
            launchWechatPre.setOnPreferenceClickListener(preference -> {
                ((SettingsActivity)getActivity()).startWechatApp();
                return false;
            });


            // 自动签卡开关
            mSwitchPreference = findPreference("switch");
            mSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean switchState = (boolean) newValue;
                if (switchState){
                    if(((SettingsActivity) getActivity()).isAccessibilitySettingsOn(getContext(), AutoSigninService.class.getName())) {
                        ((MyApplication)getActivity().getApplication()).setOpen(true);
                        //((SettingsActivity) getActivity()).startLarkApp();
                        //Intent intent = new Intent(getContext(), AlarmIntentService.class);
                        //intent.setAction(AlarmIntentService.ACTION_START);
                        //AlarmIntentService.enqueueWork(getContext(), intent);
                    }else{
                        ((SettingsActivity) getActivity()).openAccessibilitySettings();
                    }
                }else{
                    ((MyApplication)getActivity().getApplication()).setOpen(false);
                }

                return true;
            });

            // 红包助手开关
            mRedPacketSwitchPreference = findPreference("switch_red_packet");
            mRedPacketSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean switchState = (boolean) newValue;
                if (switchState){
                    if(((SettingsActivity) getActivity()).isAccessibilitySettingsOn(getContext(), RedPacketService.class.getName())) {
                        ((MyApplication)getActivity().getApplication()).setRedPacketOpen(true);
                        ((SettingsActivity) getActivity()).startWechatApp();
                    }else{
                        ((SettingsActivity) getActivity()).openAccessibilitySettings();
                    }
                }else{
                    ((MyApplication)getActivity().getApplication()).setRedPacketOpen(false);
                }

                return true;
            });

            // 选择定时
            ListPreference listPreference = findPreference("choose_date");
            listPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (preference instanceof ListPreference) {
                    ListPreference listPreference1 = (ListPreference) preference;
                    CharSequence[] entries = listPreference1.getEntries();
                    int index = listPreference1.findIndexOfValue((String) newValue);
                    if(index == 0){
                        // 打开定时
                        Utils.log("打开定时");
                        //getActivity().startService(new Intent(getActivity(), AutoService.class));
                        // 设置早上9:20的闹钟
                        Utils.setAlarm(getContext(), 9, 20);
                    }else if (index == 1){
                        // 关闭定时
                        Utils.log("关闭定时");
                        //getActivity().stopService(new Intent(getActivity(), AutoService.class));
                        Utils.cancelAlarm(getContext());
                    }
                }
                return true;
            });

            // 播放铃声
            Preference playPreference = findPreference("play");
            playPreference.setOnPreferenceClickListener(preference -> {
                try {
                    Utils.playRing(getContext());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            });
        }

        private void reset() {
            ((MyApplication)getActivity().getApplication()).setEnterSignInRange(false);
            ((MyApplication)getActivity().getApplication()).setEnterSignInScreen(false);
            ((MyApplication)getActivity().getApplication()).setSigning(false);

        }
    }

    private void requestPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0){
            Utils.log("0 result = " + grantResults);
        }else if(requestCode == 1) {
            Utils.log("1 result = " + grantResults);
        }else if(requestCode == 2) {
            Utils.log("2 result = " + grantResults);
        }
    }
}