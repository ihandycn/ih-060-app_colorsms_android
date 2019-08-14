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
import android.database.Cursor;
import android.graphics.Bitmap;
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
import com.android.messaging.datamodel.media.AvatarRequestDescriptor;
import com.android.messaging.datamodel.media.ImageResource;
import com.android.messaging.datamodel.media.MediaRequest;
import com.android.messaging.datamodel.media.MediaResourceManager;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.AvatarUriUtil;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.DefaultSMSUtils;
import com.android.messaging.util.NotificationAccessAutopilotUtils;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PendingIntentConstants;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.RingtoneUtil;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Notifications;
import com.superapps.util.ReflectionHelper;
import com.superapps.util.Threads;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.NotificationCompat.isGroupSummary;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationServiceV18 extends NotificationListenerService {

    public static final String EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION = "override_system_sms_notification";
    public static final String EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION_ACTION = "override_system_sms_notification_action";

    private static String sDefaultSmsApp;

    public static void updateDefaultSmsPackage(String defaultSmsApp) {
        sDefaultSmsApp = defaultSmsApp;
    }

    public NotificationServiceV18() {
        super();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        HSLog.d("NotificationListener", "onNotificationPosted(), statusBarNotification.getPackageName = "
                + statusBarNotification.getPackageName());
        if (DefaultSMSUtils.isDefaultSmsApp()) {
            return;
        }

        if (sDefaultSmsApp == null) {
            sDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(HSApplication.getContext());
        }

        if (!statusBarNotification.getPackageName().equals(sDefaultSmsApp)) {
            return;
        }

        final BlockedNotificationInfo notificationInfo = loadNotificationInfo(statusBarNotification);
        if (TextUtils.isEmpty(notificationInfo.title) && TextUtils.isEmpty(notificationInfo.text)) {
            HSLog.d("NotificationListener", "onNotificationPosted(), not block, title or text is empty");
            return;
        }
        HSLog.d("NotificationListener", "onNotificationPosted(), block, title = " + notificationInfo.title + ", text = " + notificationInfo.text +
                ", notificationId = " + notificationInfo.notificationId);

        if ((notificationInfo.notification.flags & Notification.FLAG_NO_CLEAR) != 0
                || (notificationInfo.notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) {
            HSLog.d("NotificationListener", "Resident Notification");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !TextUtils.isEmpty(notificationInfo.key)) {
            try {
                cancelNotification(notificationInfo.key);
            } catch (SecurityException ignored) {
            }
        } else {
            cancelNotification(notificationInfo.packageId, notificationInfo.tag, notificationInfo.notificationId);
        }

        if (!isGroupSummary(notificationInfo.notification)) {
            HSLog.d("NotificationListener", "is not group summary");
            sendNotification(notificationInfo.tag, notificationInfo.notificationId, notificationInfo.title, notificationInfo.text);
        } else {
            HSLog.d("NotificationListener", "is group summary");
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

    @Override
    public StatusBarNotification[] getActiveNotifications() {
        StatusBarNotification[] notifications = null;
        try {
            notifications = super.getActiveNotifications();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (notifications == null) {
            notifications = new StatusBarNotification[0];
        }
        return notifications;
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

    private void sendNotification(String tag, int id, String messageTitle, String messageText) {
        Runnable notifyRunnable = () -> {
            NotificationManager notifyMgr = (NotificationManager)
                    HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifyMgr != null) {
                // create channel
                final Uri ringtoneUri = RingtoneUtil.getNotificationRingtoneUri(null);
                String channelId = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    int priority = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = Notifications.getChannel(
                            PendingIntentConstants.SMS_NOTIFICATION_CHANNEL_ID + "_set_default_app",
                            HSApplication.getContext().getResources().getString(R.string.sms_notification_channel),
                            HSApplication.getContext().getResources().getString(R.string.sms_notification_channel_description), priority);
                    notificationChannel.setSound(ringtoneUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                    notificationChannel.enableVibration(shouldVibrate());
                    if (shouldVibrate()) {
                        notificationChannel.setVibrationPattern(new long[]{100, 200, 300});
                    }
                    notificationChannel.setShowBadge(true);
                    channelId = notificationChannel.getId();
                    notificationChannel.setImportance(priority);
                    notifyMgr.createNotificationChannel(notificationChannel);
                }

                StatusBarNotification[] allNotifications = getActiveNotifications();
                List<String> summaryNotificationMessageTitle = new ArrayList<>();
                List<String> summaryNotificationMessageText = new ArrayList<>();
                BlockedNotificationInfo notificationInfo;

                String existBigText = null;
                // find the existed notification
                int num = 0;
                StatusBarNotification lastNotification = null;
                for (StatusBarNotification statusBarNotification : allNotifications) {
                    String notificationPackageName = statusBarNotification.getPackageName();
                    HSLog.d("NotificationListener", "notificationPackageName = " + notificationPackageName);
                    if (notificationPackageName.equals(getPackageName())) {
                        notificationInfo = loadNotificationInfo(statusBarNotification);
                        summaryNotificationMessageTitle.add(notificationInfo.title);
                        summaryNotificationMessageText.add(notificationInfo.text);
                        HSLog.d("NotificationListener", "notificationInfo.title = " + notificationInfo.title);
                        HSLog.d("NotificationListener", "messageTitle = " + messageTitle);
                        if (notificationInfo.title.equals(messageTitle)) {
                            Bundle extras = statusBarNotification.getNotification().extras;
                            Object bigText = extras.get(NotificationCompat.EXTRA_BIG_TEXT);
                            existBigText = (bigText == null ? null : bigText.toString());
                        }
                        lastNotification = statusBarNotification;
                        num++;
                    }
                }

                HSLog.d("NotificationListener", "num =" + num);
                if (OsUtil.isAtLeastN() &&
                        lastNotification != null &&
                        (lastNotification.getId() != id || !TextUtils.equals(tag, lastNotification.getTag()))) {
                    notifyMgr.notify(id, createSummaryNotification(channelId, summaryNotificationMessageTitle, summaryNotificationMessageText));
                }

                Notification notification = createNotification(channelId, messageTitle, messageText, existBigText);
                NotificationAccessAutopilotUtils.logNotificationPushed();
                BugleAnalytics.logEvent("Notification_Pushed_NA", true);
                notifyMgr.notify(tag, id, notification);
            }
        };
        Threads.postOnSingleThreadExecutor(notifyRunnable); // Keep notifications in original order
    }

    private Notification createSummaryNotification(String channelId, List<String> messageTitle, List<String> messageText) {
        String groupKey = "groupkey";
        Intent intent = new Intent(this, ConversationListActivity.class);
        intent.putExtra(EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (int i = 0; i < messageTitle.size(); i++) {
            inboxStyle.addLine(messageTitle.get(i) + " " + messageText.get(i));
        }
        return new NotificationCompat.Builder(this, channelId)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_sms_light)
                .setColor(PrimaryColors.getPrimaryColor())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setGroup(groupKey)
                .setGroupSummary(true)
                .build();
    }

    private Notification createNotification(String channelId, String messageTitle, String messageText, String existBigText) {
        String conversationId = null;
        String normalizedMessageTitle = PhoneUtils.getDefault().getCanonicalBySimLocale(messageTitle);
        String displayMessageTitle = PhoneUtils.getDefault().formatForDisplay(normalizedMessageTitle);
        DatabaseWrapper db = DataModel.get().getDatabase();
        Cursor cursor = db.query(DatabaseHelper.CONVERSATIONS_TABLE, new String[]{DatabaseHelper.ConversationColumns._ID},
                DatabaseHelper.ConversationColumns.NAME + "=? OR " + DatabaseHelper.ConversationColumns.NAME + "=?",
                new String[]{messageTitle, displayMessageTitle}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                conversationId = cursor.getString(0);
                HSLog.d("NotificationListener", "conversationId = " + conversationId);
            }
            cursor.close();
        }

        HSLog.d("NotificationListener", "messageTitle = " + messageTitle);
        HSLog.d("NotificationListener", "normalizedMessageTitle = " + normalizedMessageTitle);
        HSLog.d("NotificationListener", "displayMessageTitle = " + displayMessageTitle);

        //group
        String groupKey = "groupkey";

        //pending destinationIntent
        Intent destinationIntent;
        PendingIntent destinationPendingIntent;


        //pending replyActionIntent
        Intent replyActionIntent;
        PendingIntent replyActionPendingIntent;
        if (TextUtils.isEmpty(conversationId)) {
            HSLog.d("NotificationListener", "conversationId is empty");
            destinationIntent = new Intent(this, ConversationListActivity.class);
            destinationIntent.putExtra(EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION, true);
            destinationPendingIntent = PendingIntent.getActivity(this, 0, destinationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            replyActionIntent = new Intent(this, ConversationListActivity.class);
            replyActionIntent.putExtra(EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION, true);
            replyActionIntent.putExtra(EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION_ACTION, true);
            replyActionPendingIntent = PendingIntent.getActivity(this, 0, replyActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            HSLog.d("NotificationListener", "conversationId is not empty");
            destinationPendingIntent = UIIntents.get()
                    .getPendingIntentForConversationActivityFromFakeDefaultSmsNotification(HSApplication.getContext(), conversationId, false);
            replyActionPendingIntent = UIIntents.get()
                    .getPendingIntentForConversationActivityFromFakeDefaultSmsNotification(HSApplication.getContext(), conversationId, true);

        }

        //defaults
        int defaults = Notification.DEFAULT_LIGHTS;
        if (shouldVibrate()) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }

        //action
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_wear_reply,
                        HSApplication.getContext().getString(R.string.notification_reply_via_sms), replyActionPendingIntent)
                        .build();

        //large icon
        final Uri avatarUri = AvatarUriUtil.createAvatarUri(
                null, null, null, null);
        AvatarRequestDescriptor descriptor = new AvatarRequestDescriptor(avatarUri,
                (int) Factory.get().getApplicationContext().getResources().getDimension(android.R.dimen.notification_large_icon_width),
                (int) Factory.get().getApplicationContext().getResources().getDimension(android.R.dimen.notification_large_icon_height),
                HSApplication.getContext().getResources().getColor(R.color.notification_avatar_background_color));
        MediaRequest<ImageResource> imageRequest = descriptor.buildSyncMediaRequest(HSApplication.getContext());
        ImageResource avatarImage = MediaResourceManager.get().requestMediaResourceSync(imageRequest);
        Bitmap avatarBitmap = Bitmap.createBitmap(avatarImage.getBitmap());
        avatarImage.release();

        //style
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(messageTitle);
        if (existBigText != null) {
            bigTextStyle.bigText(existBigText + "\n" + messageText);
        } else {
            bigTextStyle.bigText(messageText);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle(messageTitle)
                .setContentText(messageText)
                .setStyle(bigTextStyle)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_sms_light)
                .setLargeIcon(avatarBitmap)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setColor(PrimaryColors.getPrimaryColor())
                .setContentIntent(destinationPendingIntent)
                .setDefaults(defaults)
                .setSound(RingtoneUtil.getNotificationRingtoneUri(null))
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setGroup(groupKey)
                .addAction(action)
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
