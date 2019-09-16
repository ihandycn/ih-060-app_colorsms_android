package com.android.messaging.util;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class ActivePushAutopilotUtils {

    private static final String ACTIVE_PUSH_TOPIC_ID = "topic-77jtl9kou";


    public static boolean getEnablePush() {
        return AutopilotConfig.getBooleanToTestNow(ACTIVE_PUSH_TOPIC_ID, "enable_push", false);
    }

    public static String getPushDescription() {
        return AutopilotConfig.getStringToTestNow(ACTIVE_PUSH_TOPIC_ID, "push_description", "cycle");
    }

    public static void logPushShow() {
        AutopilotEvent.logTopicEvent(ACTIVE_PUSH_TOPIC_ID, "push_show");
    }

    public static void logPushClick() {
        AutopilotEvent.logTopicEvent(ACTIVE_PUSH_TOPIC_ID, "push_click");
    }

    public static void logPushSuccess() {
        AutopilotEvent.logTopicEvent(ACTIVE_PUSH_TOPIC_ID, "push_success");
    }
}
