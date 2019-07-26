package com.android.messaging.datamodel;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.RemoteViews;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationServiceV18 extends NotificationListenerService {

    public NotificationServiceV18() {
        super();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        HSLog.d("NotificationListener","Notification posted");
        HSLog.d("NotificationListener", "onNotificationPosted(), statusBarNotification.getPackageName = "
                + statusBarNotification.getPackageName());
        final BlockedNotificationInfo notificationInfo = loadNotificationInfo(statusBarNotification);
        if (TextUtils.isEmpty(notificationInfo.title) && TextUtils.isEmpty(notificationInfo.text)) {
            HSLog.d("NotificationListener", "onNotificationPosted(), not block, title or text is empty");

            return;
        }
        HSLog.d("NotificationListener", "onNotificationPosted(), block, title = " + notificationInfo.title + ", text = " + notificationInfo.text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !TextUtils.isEmpty(notificationInfo.key)) {
            try {
                cancelNotification(notificationInfo.key);
            } catch (SecurityException ignored) {
            }
        } else {
            cancelNotification(notificationInfo.packageId, notificationInfo.tag, notificationInfo.notificationId);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        HSLog.d("NotificationListener", "Removed notification: " + sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) {
        super.onNotificationRemoved(sbn, rankingMap, reason);
        HSLog.d("NotificationListener", "Removed notification2: " + sbn);
    }

    public static void toggleNotificationListenerService() {
        Context context = HSApplication.getContext();
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, NotificationServiceV18.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(context, NotificationServiceV18.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static boolean isNewsAccessSettingsOn() {
        Context context = HSApplication.getContext();
        Set<String> enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context);
        return enabledPackages.contains(context.getPackageName());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static BlockedNotificationInfo loadNotificationInfo(StatusBarNotification statusBarNotification) {

        BlockedNotificationInfo notificationInfo = new BlockedNotificationInfo(statusBarNotification.getPackageName(),
                statusBarNotification.getPostTime(), statusBarNotification.getNotification());
        notificationInfo.contentIntent = statusBarNotification.getNotification().contentIntent;
        notificationInfo.notificationId = statusBarNotification.getId();
        notificationInfo.tag = statusBarNotification.getTag();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationInfo.key = statusBarNotification.getKey();
        }

        Bundle extras = getExtras(notificationInfo.notification);
        if (null != extras) {
            notificationInfo.title = getExtrasTitle(extras);
            notificationInfo.text = getExtrasText(extras);
        }
        return notificationInfo;

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Nullable
    private static Bundle getExtras(@NonNull final Notification notification) {
        try {
            Field extrasField = ReflectionHelper.getField(notification.getClass(), "extras");
            return (Bundle) extrasField.get(notification);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getExtrasTitle(@NonNull final Bundle extras) {
        String result;

        CharSequence title = extras.getCharSequence("android.title");
        CharSequence bigTitle = extras.getCharSequence("android.title.big");

        if (!TextUtils.isEmpty(title)) {
            result = !TextUtils.isEmpty(bigTitle) ? String.valueOf(bigTitle) : String.valueOf(title);
        } else {
            result = !TextUtils.isEmpty(bigTitle) ? String.valueOf(bigTitle) : "";
        }

        return result;
    }

    private static String getExtrasText(@NonNull final Bundle extras) {
        CharSequence text = extras.getCharSequence("android.text");
        if (!TextUtils.isEmpty(text)) {
            assert text != null;
            return text.toString();
        }

        CharSequence textLines = extras.getCharSequence("android.textLines");
        if (!TextUtils.isEmpty(textLines)) {
            assert textLines != null;
            return textLines.toString();
        }

        CharSequence subText = extras.getCharSequence("android.subText");
        if (!TextUtils.isEmpty(subText)) {
            assert subText != null;
            return subText.toString();
        }

        return null;
    }

    private static void loadTitleOnJellyBean(BlockedNotificationInfo notificationInfo) {
        RemoteViews views = notificationInfo.notification.contentView;
        if (null == views) {
            views = notificationInfo.notification.bigContentView;
        }
        if (null == views) {
            return;
        }
        Class secretClass = views.getClass();

        try {
            SparseArray<String> text = new SparseArray<>();

            Field outerField = secretClass.getDeclaredField("mActions");
            outerField.setAccessible(true);
            List<Object> actions = (List<Object>) outerField.get(views);

            for (Object action : actions) {
                Field innerFields[] = action.getClass().getDeclaredFields();
                Field innerFieldsSuper[] = action.getClass().getSuperclass().getDeclaredFields();

                Object value = null;
                Integer type = null;
                Integer viewId = null;

                for (Field field : innerFields) {
                    field.setAccessible(true);
                    if (field.getName().equals("value")) {
                        value = field.get(action);
                    } else if (field.getName().equals("type")) {
                        type = field.getInt(action);
                    }
                }

                for (Field field : innerFieldsSuper) {
                    field.setAccessible(true);
                    if (field.getName().equals("viewId")) {
                        viewId = field.getInt(action);
                    }
                }

                if (value != null && type != null && viewId != null && (type == 9 || type == 10)) {
                    text.put(viewId, value.toString());
                }
            }

            notificationInfo.title = text.get(16908310);
            notificationInfo.text = text.get(16908358);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
