package com.android.messaging.datamodel;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.media.BugleNotificationChannelUtil;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.DefaultSMSUtils;
import com.android.messaging.util.PendingIntentConstants;
import com.android.messaging.util.RingtoneUtil;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.ReflectionHelper;
import com.superapps.util.Threads;

import java.lang.reflect.Field;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationServiceV18 extends NotificationListenerService {

    public NotificationServiceV18() {
        super();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        HSLog.d("NotificationListener", "Notification posted");
        HSLog.d("NotificationListener", "onNotificationPosted(), statusBarNotification.getPackageName = "
                + statusBarNotification.getPackageName());
        if (DefaultSMSUtils.isDefaultSmsApp()) {
            return;
        }
        if (!statusBarNotification.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(HSApplication.getContext()))) {
            return;
        }
        final BlockedNotificationInfo notificationInfo = loadNotificationInfo(statusBarNotification);
        if (TextUtils.isEmpty(notificationInfo.title) && TextUtils.isEmpty(notificationInfo.text)) {
            HSLog.d("NotificationListener", "onNotificationPosted(), not block, title or text is empty");

            return;
        }
        HSLog.d("NotificationListener", "onNotificationPosted(), block, title = " + notificationInfo.title + ", text = " + notificationInfo.text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !TextUtils.isEmpty(notificationInfo.key)) {
            try {
                cancelNotification(notificationInfo.key);
                HSLog.d("NotificationListener", "onNotificationPosted(), cancelNotification(notificationInfo.key);");
            } catch (SecurityException ignored) {
                HSLog.d("NotificationListener", "catch (SecurityException ignored) ");
            }
        } else {
            cancelNotification(notificationInfo.packageId, notificationInfo.tag, notificationInfo.notificationId);
            HSLog.d("NotificationListener", "onNotificationPosted(), " +
                    "cancelNotification(notificationInfo.packageId, notificationInfo.tag, notificationInfo.notificationId);");
        }

        sendNotification(notificationInfo.title, notificationInfo.text);
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

    private void sendNotification(String messageTitle, String messageText) {
        Runnable notifyRunnable = () -> {
            NotificationManager notifyMgr = (NotificationManager)
                    HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifyMgr != null) {
                try {
                    final Uri ringtoneUri = RingtoneUtil.getNotificationRingtoneUri(null);
                    String channelId = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        int priority = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel notificationChannel = BugleNotificationChannelUtil.getSmsNotificationChannel(ringtoneUri, shouldVibrate(), priority);
                        notificationChannel.setShowBadge(true);
                        channelId = notificationChannel.getId();
                        notificationChannel.setImportance(priority);
                    }
                    Notification notification = createNotification(channelId, messageTitle, messageText);
                    notifyMgr.notify(PendingIntentConstants.SMS_NOTIFICATION_ID, notification);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Threads.postOnSingleThreadExecutor(notifyRunnable); // Keep notifications in original order
    }

    private Notification createNotification(String channelId, String messageTitle, String messageText) {
        Intent intent = new Intent(this, ConversationListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        int defaults = Notification.DEFAULT_LIGHTS;
        if (shouldVibrate()) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle(messageTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageText))
                .setContentText(messageText)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_sms_light)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setColor(PrimaryColors.getPrimaryColor())
                .setContentIntent(pendingIntent)
                .setDefaults(defaults)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();
    }

    private boolean shouldVibrate() {
        final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
        final Context context = Factory.get().getApplicationContext();
        final String prefKey = context.getString(R.string.notification_vibration_pref_key);
        final boolean defaultValue = context.getResources().getBoolean(
                R.bool.notification_vibration_pref_default);
        return prefs.getBoolean(prefKey, defaultValue);
    }
}
