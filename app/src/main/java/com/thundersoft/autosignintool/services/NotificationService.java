package com.thundersoft.autosignintool.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.thundersoft.autosignintool.MyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听状态栏消息
 */
public class NotificationService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if(!((MyApplication)getApplication()).isRedPacketOpen())
            return;
        Notification notification = sbn.getNotification();
        if (null == notification) return;

        Bundle extras = notification.extras;
        if (null == extras) return;

        List<String> textList = new ArrayList<>();
        String title = extras.getString("android.title");
        if (!TextUtils.isEmpty(title)) textList.add(title);

        String detailText = extras.getString("android.text");
        if (!TextUtils.isEmpty(detailText)) textList.add(detailText);

        if (textList.size() == 0) return;
        for (String text : textList) {
            if (!TextUtils.isEmpty(text) && text.contains("[微信红包]")) {
                final PendingIntent pendingIntent = notification.contentIntent;
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                }
                break;
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }
}