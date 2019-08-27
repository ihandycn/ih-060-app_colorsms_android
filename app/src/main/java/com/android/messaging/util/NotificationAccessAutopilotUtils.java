package com.android.messaging.util;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class NotificationAccessAutopilotUtils {

    private static final String NOTIFICATION_ACCESS_TOPIC_ID = "topic-77gqqjobm";

    public static boolean getIsNotificationAccessSwitchOn() {
        return AutopilotConfig.getBooleanToTestNow(NOTIFICATION_ACCESS_TOPIC_ID, "na_switch", false);
    }

    public static void logNotificationPushed() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "notification_pushed");
    }

    public static void logNotificationClicked() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "notification_clicked");
    }

    public static void logNotificationReplied() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "notification_replied");
    }

}
