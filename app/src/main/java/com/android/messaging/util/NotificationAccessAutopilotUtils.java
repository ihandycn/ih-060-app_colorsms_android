package com.android.messaging.util;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class NotificationAccessAutopilotUtils {

    private static final String NOTIFICATION_ACCESS_TOPIC_ID = "topic-77gqqjobm";

    public static boolean getIsNotificationAccessSwitchOn() {
        return AutopilotConfig.getBooleanToTestNow(NOTIFICATION_ACCESS_TOPIC_ID, "na_switch", false);
    }

    public static void logHomePageShow() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "homepage_show");
    }

    public static void logDetailsPageShow() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "detailspage_show");
    }

    public static void logBannerAdShouldShow() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "bannerad_chance");
    }

    public static void logBannerAdShow() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "bannerad_show");
    }

    public static void logTopAdShouldShow() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "topad_chance");
    }

    public static void logTopAdShow() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "topad_show");
    }

    public static void logFullAdShouldShow() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "fullad_chance");
    }

    public static void logFullAdShow() {
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "fullad_show");
    }

    public static void logExitAdShouldShow(){
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "exitad_chance");
    }

    public static void logExitAdShow(){
        AutopilotEvent.logTopicEvent(NOTIFICATION_ACCESS_TOPIC_ID, "exitad_show");
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
