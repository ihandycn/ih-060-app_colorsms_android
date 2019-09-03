package com.android.messaging.notificationcleaner.data;

import android.app.Notification;
import android.app.PendingIntent;

public class BlockedNotificationInfo {

    public long idInDB;
    public int notificationId;
    public String tag;
    public String packageName;
    public Notification notification;
    public PendingIntent contentIntent;
    public String title;
    public String text;
    public String key;
    public long postTime;

    public BlockedNotificationInfo() {
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof BlockedNotificationInfo
            && this.idInDB == ((BlockedNotificationInfo) object).idInDB;
    }
}
