package com.android.messaging.datamodel;

import android.app.Notification;
import android.app.PendingIntent;

public class BlockedNotificationInfo {

    /**
     * Identifier of item displayed as a blocked notification. It is:
     *
     * - App package name, if the item is an actual notification posted from an app.
     *
     * - One of the pre-defined constants used to represent special items.
     *
     */
    public String packageId;

    public long idInDB;
    public int notificationId;
    public String tag;
    public Notification notification;
    public PendingIntent contentIntent;
    public String title;
    public String text;
    public String key;
    public long postTime;

    public BlockedNotificationInfo() {
    }

    public BlockedNotificationInfo(String packageId, long postTime, Notification notification) {
        this.packageId = packageId;
        this.notification = notification;
        this.postTime = postTime;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof BlockedNotificationInfo
                && this.idInDB == ((BlockedNotificationInfo) object).idInDB;
    }
}
