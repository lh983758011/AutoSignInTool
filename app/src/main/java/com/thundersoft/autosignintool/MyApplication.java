package com.thundersoft.autosignintool;


import android.app.Application;

public class MyApplication extends Application {

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    // 是否开启自动打卡
    private boolean isOpen = false;
    // 是否进入打卡界面
    private boolean isEnterSignInScreen = false;
    // 是否进入打卡范围
    private boolean isEnterSignInRange = false;
    // 正在打卡状态
    private boolean isSigning = false;
    // 打开红包助手开关
    private boolean isRedPacketOpen = false;


    public boolean isRedPacketOpen() {
        return isRedPacketOpen;
    }

    public void setRedPacketOpen(boolean redPacketOpen) {
        isRedPacketOpen = redPacketOpen;
    }

    public boolean isSigning() {
        return isSigning;
    }

    public void setSigning(boolean signing) {
        isSigning = signing;
    }

    public boolean isEnterSignInScreen() {
        return isEnterSignInScreen;
    }

    public void setEnterSignInScreen(boolean enterSignInScreen) {
        isEnterSignInScreen = enterSignInScreen;
    }

    public boolean isEnterSignInRange() {
        return isEnterSignInRange;
    }

    public void setEnterSignInRange(boolean enterSignInRange) {
        isEnterSignInRange = enterSignInRange;
    }



    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        isOpen = false;
        isEnterSignInRange = false;
        isEnterSignInScreen = false;
        isSigning = false;
    }
}
