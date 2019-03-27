package com.android.messaging.datamodel.media;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import com.android.messaging.R;
import com.android.messaging.util.PendingIntentConstants;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Notifications;
import com.superapps.util.Preferences;

import java.util.List;

public class BugleNotificationChannelUtil {

    public static final String PREF_KEY_NOTIFICATION_CHANNEL_INDEX = "pref_key_notification_channel_index";

    @TargetApi(Build.VERSION_CODES.O)
    public static NotificationChannel getSmsNotificationChannel(Uri soundPath, boolean enableVibration) {
        int channelIndex = Preferences.getDefault().getInt(PREF_KEY_NOTIFICATION_CHANNEL_INDEX, 0);
        NotificationManager notifyMgr = (NotificationManager)
                HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = null;
        if (notifyMgr != null) {
            channel = notifyMgr.getNotificationChannel(PendingIntentConstants.SMS_NOTIFICATION_CHANNEL_ID + "_" + channelIndex);
        }
        if (channel != null
                && channel.getSound().getPath().equals(soundPath.getPath())
                && channel.shouldVibrate() == enableVibration) {
            return channel;
        }

        deleteNotificationChannels();
        channelIndex++;
        Preferences.getDefault().putInt(PREF_KEY_NOTIFICATION_CHANNEL_INDEX, channelIndex);
        channel = Notifications.createChannel(
                PendingIntentConstants.SMS_NOTIFICATION_CHANNEL_ID + "_" + channelIndex,
                HSApplication.getContext().getResources().getString(R.string.sms_notification_channel),
                HSApplication.getContext().getResources().getString(R.string.sms_notification_channel_description));
        channel.setSound(soundPath, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        channel.enableVibration(enableVibration);
        if (enableVibration) {
            channel.setVibrationPattern(new long[]{100, 200, 300});
        }
        return channel;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void deleteNotificationChannels() {
        NotificationManager notifyMgr = (NotificationManager)
                HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifyMgr == null) {
            return;
        }
        List<NotificationChannel> channels = notifyMgr.getNotificationChannels();
        if (channels.size() > 0) {
            for (NotificationChannel c : channels) {
                notifyMgr.deleteNotificationChannel(c.getId());
            }
        }
    }
}
