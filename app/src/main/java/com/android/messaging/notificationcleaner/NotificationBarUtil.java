package com.android.messaging.notificationcleaner;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.RemoteViews;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.activity.NotificationBlockedActivity;
import com.android.messaging.notificationcleaner.data.BlockedNotificationInfo;
import com.android.messaging.notificationcleaner.data.NotificationCleanerProvider;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Notifications;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;

public class NotificationBarUtil {

    private static final String TAG = "BLOCKED_APP_NOTIFICATION";

    private static final int[] ICON_CONTAINER_RES_ID = {
            R.id.recentest_notification_icon_0,
            R.id.recentest_notification_icon_1,
            R.id.recentest_notification_icon_2,
            R.id.recentest_notification_icon_3,
            R.id.recentest_notification_icon_4
    };

    public static void checkToUpdateBlockedNotification() {
        boolean isNotificationOrganizerEnabled = NotificationCleanerUtil.isNotificationOrganizerEnabled();
        HSLog.d(NotificationCleanerConstants.TAG, "checkToUpdateBlockedNotification isNotificationOrganizerEnabled = " + isNotificationOrganizerEnabled);
        if (!isNotificationOrganizerEnabled) {
            Notifications.cancelSafely(NotificationCleanerConstants.NOTIFICATION_ID_BLOCK_NOTIFICATION);
            return;
        }

        Threads.postOnThreadPoolExecutor(() -> {
            NotificationCompat.Builder builder = createBlockNotificationBuilder(createBlockNotificationRemoteViews());
            Notifications.buildAndNotifySafely(NotificationCleanerConstants.NOTIFICATION_ID_BLOCK_NOTIFICATION, builder,
                    Notifications.getChannel(Notifications.NC_CHANNEL_ID,
                            HSApplication.getContext().getResources().getString(R.string.nc_notification_channel),
                            HSApplication.getContext().getResources().getString(R.string.nc_notification_channel_description)));
        });
    }

    private static NotificationCompat.Builder createBlockNotificationBuilder(@Nullable RemoteViews remoteViews) {
        if (null == remoteViews) {
            return null;
        }

        Context context = HSApplication.getContext();
        int requestCode = (int) System.currentTimeMillis();
        Intent intent = new Intent(HSApplication.getContext(), NotificationBlockedActivity.class);
        intent.putExtra(NotificationBlockedActivity.START_FROM,
                NotificationBlockedActivity.START_FROM_NOTIFICATION_BAR);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(HSApplication.getContext(), Notifications.NC_CHANNEL_ID);
        builder.setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSmallIcon(R.drawable.notification_cleaner_small_icon)
                .setContent(remoteViews)
                .setWhen(0);

        try {
            builder.setPriority(Notification.PRIORITY_MAX);
        } catch (Exception e) {
            e.printStackTrace();
            HSLog.d(TAG, "builder.setPriority(NotificationCompat.PRIORITY_MAX) EXCEPTION");
        }

        return builder;
    }

    @WorkerThread
    private static RemoteViews createBlockNotificationRemoteViews() {
        List<BlockedNotificationInfo> notificationList = NotificationCleanerProvider.fetchBlockedAndTimeValidNotificationDataList(true);
        List<BlockedNotificationInfo> fixedNotificationList = new ArrayList<>();
        List<ApplicationInfo> applicationInfoList = BuglePackageManager.getInstance().getInstalledApplications();

        for (BlockedNotificationInfo notificationData : notificationList) {
            boolean isContain = false;
            if (null == applicationInfoList || applicationInfoList.size() == 0) {
                isContain = true;
            } else {
                for (ApplicationInfo applicationInfo : applicationInfoList) {
                    if (null != applicationInfo && TextUtils.equals(notificationData.packageName, applicationInfo.packageName)) {
                        isContain = true;
                        break;
                    }
                }
            }

            if (NotificationCleanerUtil.FAKE_NOTIFICATION_PACKAGE_NAME_1.equals(notificationData.packageName)
                    || NotificationCleanerUtil.FAKE_NOTIFICATION_PACKAGE_NAME_2.equals(notificationData.packageName)) {
                isContain = true;
            }
            if (!isContain) {
                continue;
            }

            fixedNotificationList.add(notificationData);
        }

        final int blockedNotificationCount = fixedNotificationList.size();
        //  final int blockedNotificationCount = NotificationCleanerProvider.fetchBlockedAndTimeValidNotificationCount(false);
        HSLog.d(NotificationCleanerConstants.TAG, "createBlockNotificationRemoteViews blockedNotificationCount = " + blockedNotificationCount);
        if (blockedNotificationCount <= 0) {
            Notifications.cancelSafely(NotificationCleanerConstants.NOTIFICATION_ID_BLOCK_NOTIFICATION);
            return null;
        }

        // List<String> recentBlockedApps = NotificationCleanerProvider.fetchRecentBlockedAppPackageNameList(false);
        List<String> recentBlockedApps = new ArrayList<>();
        for (int i = 0; i < blockedNotificationCount; i++) {
            if (!recentBlockedApps.contains(fixedNotificationList.get(i).packageName)) {
                recentBlockedApps.add(fixedNotificationList.get(i).packageName);
            }
        }
        List<Drawable> drawableList = new ArrayList<>();
        for (int index = 0; index < recentBlockedApps.size() && index < ICON_CONTAINER_RES_ID.length; index++) {
            Drawable drawable = BuglePackageManager.getInstance().getApplicationIcon(recentBlockedApps.get(index));
            if (drawable == null) {
                continue;
            }
            drawableList.add(drawable);
        }
        return createNotificationRemoteViews2(blockedNotificationCount, drawableList);
    }

    private static RemoteViews createNotificationRemoteViews2(int blockedNotificationCount, List<Drawable> drawableList) {
        RemoteViews remoteViews = new RemoteViews(HSApplication.getContext().getPackageName(), R.layout.notification_cleaner_bar_block_view_2);

        SpannableString spannableString = new SpannableString(HSApplication.getContext().getString(R.string.notification_cleaner_intercepted, String.valueOf(blockedNotificationCount)));
        ForegroundColorSpan fcsNumber = new ForegroundColorSpan(ContextCompat.getColor(HSApplication.getContext(), R.color.notification_red));
        final int start = HSApplication.getContext().getString(R.string.notification_cleaner_intercepted, String.valueOf(blockedNotificationCount)).indexOf(String.valueOf(blockedNotificationCount));
        final int end = start + String.valueOf(blockedNotificationCount).length();
        spannableString.setSpan(fcsNumber, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        remoteViews.setTextViewText(R.id.block_title_text, spannableString);

        for (int id : ICON_CONTAINER_RES_ID) {
            remoteViews.setViewVisibility(id, View.GONE);
        }

        int iconSize = ICON_CONTAINER_RES_ID.length;
        for (int i = 0; i < drawableList.size(); i++) {
            if (i == iconSize - 1) {
                remoteViews.setViewVisibility(ICON_CONTAINER_RES_ID[i], View.VISIBLE);
                break;
            }

            if (i > iconSize - 1) {
                break;
            }
            remoteViews.setViewVisibility(ICON_CONTAINER_RES_ID[i], View.VISIBLE);
            remoteViews.setImageViewBitmap(ICON_CONTAINER_RES_ID[i], NotificationCleanerUtil.drawableToBitmap(drawableList.get(i)));
        }
        return remoteViews;
    }

}
