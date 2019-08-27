package com.android.messaging.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.NoConfirmationSmsSendService;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.NotificationAccessAutopilotUtils;
import com.android.messaging.util.PopupsReplyAutopilotUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import net.appcloudbox.autopilot.AutopilotEvent;

import static com.android.messaging.ui.messagebox.MessageBoxActivity.NOTIFICATION_FINISH_MESSAGE_BOX;

public class RemoteInputReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            return;
        }

        // Perform some action depending on the intent
        String action = intent.getAction();
        if (Intent.ACTION_SENDTO.equals(action)) {
            // Build and send the intent
            final Intent sendIntent = new Intent(HSApplication.getContext(), NoConfirmationSmsSendService.class);
            sendIntent.setAction(TelephonyManager.ACTION_RESPOND_VIA_MESSAGE);
            sendIntent.putExtras(intent);
            // Wear apparently passes all of its extras via the clip data. Must pass it along.
            sendIntent.setClipData(intent.getClipData());

            BugleNotifications.cancelAllSmsNotifications();
            BugleNotifications.markMessagesAsRead(intent.getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID));

            HSApplication.getContext().startService(sendIntent);
            BugleAnalytics.logEvent("SMS_Notifications_Reply", true);
            BugleFirebaseAnalytics.logEvent("SMS_Notifications_Reply");
            AutopilotEvent.logTopicEvent("topic-768lyi3sp", "notification_replied");
            NotificationAccessAutopilotUtils.logNotificationReplied();
            PopupsReplyAutopilotUtils.logNotificationReplied();
            HSGlobalNotificationCenter.sendNotification(NOTIFICATION_FINISH_MESSAGE_BOX);
        }
    }
}
