package com.android.messaging.notificationcleaner;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.activity.NotificationBlockedActivity;
import com.android.messaging.notificationcleaner.data.NotificationCleanerProvider;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Notifications;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

public class NotificationPushGuideUtils {
    private static final String NOTIFICATION_CLEANER_PUSH_GUIDE_SHOWN = "notification_cleaner_push_guide_shown";

    private static final int[] ICON_CONTAINER_RES_ID = {
            R.id.recentest_notification_icon_0,
            R.id.recentest_notification_icon_1,
            R.id.recentest_notification_icon_2,
            R.id.recentest_notification_icon_3,
            R.id.recentest_notification_icon_4
    };

    public static void pushNotificationCleanerGuideIfNeed() {
        if (Preferences.getDefault().getBoolean(NOTIFICATION_CLEANER_PUSH_GUIDE_SHOWN, false)
                || NotificationCleanerProvider.isNotificationOrganizerSwitchOn()
                || !NotificationCleanerTest.getSwitch()) {
            return;
        }


        Runnable runnable = () -> {
            if (Preferences.getDefault().getBoolean(NOTIFICATION_CLEANER_PUSH_GUIDE_SHOWN, false)
                    || NotificationCleanerProvider.isNotificationOrganizerSwitchOn()
                    || !NotificationCleanerTest.getSwitch()) {
                return;
            }
            sendNotificationCleanerNotification(HSApplication.getContext());
            Preferences.getDefault().putBoolean(NOTIFICATION_CLEANER_PUSH_GUIDE_SHOWN, true);
        };
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Threads.removeOnMainThread(runnable);
                HSApplication.getContext().unregisterReceiver(this);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        HSApplication.getContext().registerReceiver(receiver, filter);

        Threads.postOnMainThreadDelayed(runnable, 10 * DateUtils.SECOND_IN_MILLIS);

        Threads.postOnMainThreadDelayed(
                () -> HSApplication.getContext().unregisterReceiver(receiver),
                10 * DateUtils.SECOND_IN_MILLIS);
    }

    public static void sendNotificationCleanerNotification(Context context) {
        int notificationId = 10009;

        RemoteViews notification = new RemoteViews(context.getPackageName(), R.layout.notification_cleaner);
        notification.setImageViewResource(R.id.protect_image, R.drawable.notification_cleaner_push_icon);
        notification.setTextViewText(R.id.notification_btn_text,
                context.getResources().getString(R.string.clean));
        notification.setTextViewText(R.id.block_title_text,
                context.getResources().getString(R.string.notification_cleaner_push_title));

        notification.setImageViewResource(R.id.notification_btn_bg, R.drawable.notification_cleaner_push_button_bg);

        for (int id : ICON_CONTAINER_RES_ID) {
            notification.setViewVisibility(id, View.GONE);
        }
        BoostAnimationManager boostAnimationManager = new BoostAnimationManager();
        Bitmap[] bitmaps = boostAnimationManager.getBoostAppIconBitmaps(HSApplication.getContext());
        for (int i = 0; i < bitmaps.length && i < 4; i++) {
            notification.setViewVisibility(ICON_CONTAINER_RES_ID[i], View.VISIBLE);
            notification.setImageViewBitmap(ICON_CONTAINER_RES_ID[i], bitmaps[i]);
        }
        notification.setViewVisibility(ICON_CONTAINER_RES_ID[ICON_CONTAINER_RES_ID.length - 1], View.VISIBLE);

        PendingIntent pendingIntent = getPendingIntent();

        android.support.v4.app.NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, Notifications.NC_CHANNEL_ID)
                        .setSmallIcon(R.drawable.notification_clean_small_icon)
                        .setContent(notification)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

//        boolean isHeadsUp = HSConfig.optBoolean(false, "Application", "Notification", "HeadsUp", "NotificationCleaner");
//        if (isHeadsUp) {
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND
                | NotificationCompat.DEFAULT_VIBRATE
                | NotificationCompat.DEFAULT_LIGHTS);

        // 测试中存在高版本出现 crash, notified from MAX team
        try {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        } catch (Exception e) {
            HSLog.i("builder.setPriority(NotificationCompat.PRIORITY_MAX) EXCEPTION");
        }
//        } else {
//            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
//        }
        BugleAnalytics.logEvent("NotificationCleaner_GuidePush_Show", true);
        Notifications.notifySafely(notificationId, buildNotificationSafely(builder),
                Notifications.getChannel(Notifications.NC_CHANNEL_ID,
                        HSApplication.getContext().getResources().getString(R.string.nc_notification_channel),
                        HSApplication.getContext().getResources().getString(R.string.nc_notification_channel_description)));
    }

    private static Notification buildNotificationSafely(NotificationCompat.Builder builder) {
        try {
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    private static PendingIntent getPendingIntent() {
        Context context = HSApplication.getContext();
        int requestCode = (int) System.currentTimeMillis();
        Intent notificationCleanerIntent = new Intent(context, NotificationBlockedActivity.class);
        notificationCleanerIntent.putExtra(NotificationBlockedActivity.START_FROM,
                NotificationBlockedActivity.START_FROM_GUIDE_BAR);
        notificationCleanerIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return PendingIntent.getActivity(context, requestCode, notificationCleanerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
