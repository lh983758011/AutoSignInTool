package com.thundersoft.autosignintool;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Rect;
import android.nfc.Tag;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class RedPacketService extends AccessibilityService {

    private String LAUCHER = "com.tencent.mm.ui.LauncherUI";
    private String LUCKEY_MONEY_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    private String LUCKEY_MONEY_RECEIVER = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    private String LUCKEY_MONEY_NOT_HOOK_RECEIVER = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI";

    // 自己发送红包  左 x 坐标
    private static final int SELF_X_LEFT_LOCATION = 305;

    /**
     * 用于判断是否点击过红包了
     */
    private boolean isOpenRP;
    // 内容变化，单次完成后再处理
    private boolean isContentChangeDoing = false;

    private RedPacketService mService = null;


    public RedPacketService() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mService = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!((MyApplication)getApplication()).isRedPacketOpen())
            return;
        int eventType = event.getEventType();
        Utils.log("event = " + event);
        switch (eventType) {
            //通知栏来信息，判断是否含有微信红包字样，是的话跳转
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                for (CharSequence text : texts) {
                    String content = text.toString();
                    if (!TextUtils.isEmpty(content)) {
                        //判断是否含有[微信红包]字样
                        if (content.contains("[微信红包]")) {
                            //如果有则打开微信红包页面
                            openWeChatPage(event);
                            isOpenRP=false;
                        }
                    }
                }
                break;
            //界面跳转的监听
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                //判断是否是微信聊天界面
                if (LAUCHER.equals(className)) {
                    //获取当前聊天页面的根布局
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    //开始找红包
                    findRedPacketWithoutDone(rootNode);
                }

                //判断是否是显示‘开’的那个红包界面
                if (LUCKEY_MONEY_RECEIVER.equals(className)) {
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    //开始抢红包
                    openRedPacket(rootNode);
                }

                //判断是否是显示‘开’的那个红包界面
                if (LUCKEY_MONEY_NOT_HOOK_RECEIVER.equals(className)) {
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    //开始抢红包
                    openRedPacket(rootNode);
                }

                //判断是否是红包领取后的详情界面
                if(LUCKEY_MONEY_DETAIL.equals(className)){
                    //返回桌面
                    back();
                }
                break;
            //内容变化的监听
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (!isContentChangeDoing) {
                    isContentChangeDoing = true;
                    //获取当前聊天页面的根布局
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    //开始找红包
                    findRedPacketWithoutDone(rootNode);
                    if (isOpenRP) {
                        try {
                            Thread.sleep(500);
                            openRedPacket(rootNode);
                            Thread.sleep(500);
                            back();
                            isContentChangeDoing = false;
                            isOpenRP = false;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    isContentChangeDoing = false;
                }
                break;
        }

    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 开始打开红包
     */
    private void openRedPacket(AccessibilityNodeInfo rootNode) {
        if (rootNode == null)
            return;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if(node == null)
                continue;
            if ("android.widget.Button".equals(node.getClassName())) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            openRedPacket(node);
        }
    }

    /**
     * 遍历查找红包
     */
    private void findRedPacket(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            //从最后一行开始找起
            for (int i = rootNode.getChildCount() - 1; i >= 0; i--) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                //如果node为空则跳过该节点
                if (node == null) {
                    continue;
                }
                CharSequence text = node.getText();
                if (text != null && (text.toString().equals("微信红包"))) {
                    AccessibilityNodeInfo parent = node.getParent();
                    //while循环,遍历"领取红包"的各个父布局，直至找到可点击的为止
                    while (parent != null) {
                        if (parent.isClickable()) {
                            //模拟点击
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            //isOpenRP用于判断该红包是否点击过
                            isOpenRP = true;
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
                //判断是否已经打开过那个最新的红包了，是的话就跳出for循环，不是的话继续遍历
                if (isOpenRP) {
                    break;
                } else {
                    findRedPacket(node);
                }

            }
        }
    }

    private void findRedPacketWithoutDone(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            //从最后一行开始找起
            out:
            for (int i = rootNode.getChildCount() - 1; i >= 0; i--) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                //如果node为空则跳过该节点
                if (node == null) {
                    continue;
                }
                CharSequence text = node.getText();
                if (text != null && (text.toString().equals("微信红包"))) {
                    // 1. 判断是否已经被领取
                    AccessibilityNodeInfo parent = node.getParent();
                    for (int n = parent.getChildCount() - 1; n >= 0; n--){
                        AccessibilityNodeInfo child = parent.getChild(n);
                        if (child != null){
                            if (child.getText() != null && (child.getText().toString().equals("已领取")
                                    || child.getText().toString().equals("已被领完"))){
                                Utils.log( "红包已领取");
                                break out;
                            }
                        }
                    }

                    // 2. 判断是自己或者被人发的红包，根据位置判断，左：222 别人，右:305 自己
                    Rect boundsInScreen = new Rect();
                    node.getBoundsInScreen(boundsInScreen);
                    if (boundsInScreen.left == SELF_X_LEFT_LOCATION){
                        break out;
                    }

                    //while循环,遍历"领取红包"的各个父布局，直至找到可点击的为止
                    while (parent != null) {
                        if (parent.isClickable()) {
                            //模拟点击
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            //isOpenRP用于判断该红包是否点击过
                            isOpenRP = true;
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
                //判断是否已经打开过那个最新的红包了，是的话就跳出for循环，不是的话继续遍历
                if (isOpenRP) {
                    break;
                } else {
                    findRedPacketWithoutDone(node);
                }

            }
        }
    }

    /**
     * 开启红包所在的聊天页面
     */
    private void openWeChatPage(AccessibilityEvent event) {
        Utils.log( "openWeChatPage event :" + event);
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            //打开对应的聊天界面
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回桌面
     */
    private void back() {
        mService.performGlobalAction(GLOBAL_ACTION_BACK);
    }

}