package com.thundersoft.autosignintool;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class AutoSigninService extends AccessibilityService {

    private AutoSigninService mService = null;
    private boolean isSigning = false; // 正在打卡状态
    // 是否进入打卡界面
    private boolean isEnterSignInScreen = false;
    // 是否进入打卡范围
    private boolean isEnterSignInRange = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mService = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.toast(getApplicationContext(), "自动打开辅助功能退出了");
        mService = null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        try {
            //拿到根节点
            AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
            if (rootInfo == null)
                return;
            //开始遍历，这里拎出来细讲，直接往下看正文
            if (rootInfo.getChildCount() != 0) {
                DFS(rootInfo);
            }
        } catch (Exception e) {
            Log.e(SettingsActivity.TAG, "" + e);
        }
    }

    private void DFS(AccessibilityNodeInfo rootInfo) {
        if (!((MyApplication)getApplication()).isOpen())
            return;
        if (isSigning)
            return;
        if (rootInfo == null || TextUtils.isEmpty(rootInfo.getClassName())) {
            return;
        }
        if (!isEnterSignInScreen) {
            if (!"android.widget.TextView".equals(rootInfo.getClassName())) {
                Log.e(SettingsActivity.TAG, rootInfo.getClassName().toString());
                for (int i = 0; i < rootInfo.getChildCount(); i++) {
                    DFS(rootInfo.getChild(i));
                }
            } else {
                Log.e(SettingsActivity.TAG, "==find TextView==");
                if (rootInfo != null && !TextUtils.isEmpty(rootInfo.getText())) {
                    String text = rootInfo.getText().toString();
                    Log.e(SettingsActivity.TAG, "==text ==" + text);
                    if (text.equals("工作台")) {
                        performClick(rootInfo.getParent());
                    } else if (text.equals("集团工作平台")) {
                        performClick(rootInfo.getParent());
                        isEnterSignInScreen = true;
                    }
                }
            }
        } else {
            if (!"android.view.View".equals(rootInfo.getClassName())) {
                Log.e(SettingsActivity.TAG, rootInfo.getClassName().toString());
                for (int i = 0; i < rootInfo.getChildCount(); i++) {
                    DFS(rootInfo.getChild(i));
                }
            } else {
                if (rootInfo != null && !TextUtils.isEmpty(rootInfo.getText())) {
                    String text = rootInfo.getText().toString();
                    if (text.equals("已进入打卡范围重新定位")) {
                        Log.e(SettingsActivity.TAG, "==text ==" + text);
                        isEnterSignInRange = true;
                    }
                    if (text.equals("打卡") && isEnterSignInRange) {
                        Log.e(SettingsActivity.TAG, "==text ==" + text);
                        Path path = new Path();
                        path.moveTo(300, 957);
                        final GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(path, 2000, 50);
                        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new AccessibilityService.GestureResultCallback(){
                            @Override
                            public void onCompleted(GestureDescription gestureDescription) {
                                super.onCompleted(gestureDescription);
                                Log.e(SettingsActivity.TAG, "打卡成功");
                                Vibrator mVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                mVibrator.vibrate(VibrationEffect.createWaveform(new long[] { 800, 800, 800 }, 2));
                                Utils.toast(getApplicationContext(), "打卡成功");
                                isEnterSignInScreen = false;
                                isEnterSignInRange = false;
                                isSigning = false;
                                mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                            }
                        }, null);
                        isSigning = true;
                    }

                }
            }
        }
    }

    // 获取Node通过Text
    public List<AccessibilityNodeInfo> findNodesByText(AccessibilityNodeInfo rootInfo, String text) {
        AccessibilityNodeInfo nodeInfo = rootInfo;
        if (nodeInfo != null) {
            Log.i("accessibility", "getClassName：" + nodeInfo.getClassName());
            Log.i("accessibility", "getText：" + nodeInfo.getText());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //需要在xml文件中声明权限android:accessibilityFlags="flagReportViewIds"
                // 并且版本大于4.3 才能获取到view 的 ID
                Log.i("accessibility", "getClassName：" + nodeInfo.getViewIdResourceName());
            }
            return nodeInfo.findAccessibilityNodeInfosByText(text);
        }
        return null;
    }

    // 获取Node通过ViewId
    public List<AccessibilityNodeInfo> findNodesById(AccessibilityNodeInfo rootInfo, String viewId) {
        AccessibilityNodeInfo nodeInfo = rootInfo;
        if (nodeInfo != null) {
            if (Build.VERSION.SDK_INT >= 18) {
                return nodeInfo.findAccessibilityNodeInfosByViewId(viewId);
            }
        }
        return null;
    }

    private void performClick(AccessibilityNodeInfo targetInfo) {
        targetInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        try {
            Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {

    }
}