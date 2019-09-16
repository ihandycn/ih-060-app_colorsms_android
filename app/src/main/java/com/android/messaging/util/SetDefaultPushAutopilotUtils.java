package com.android.messaging.util;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class SetDefaultPushAutopilotUtils {

    private static final String SET_DEFAULT_PUSH_TOPIC_ID = "topic-77jtf18mt";


    public static boolean getEnablePush() {
        return AutopilotConfig.getBooleanToTestNow(SET_DEFAULT_PUSH_TOPIC_ID, "push_afteralert", false);
    }

    public static void logPushSetDefaultShow() {
        AutopilotEvent.logTopicEvent(SET_DEFAULT_PUSH_TOPIC_ID, "push_setdefault_show");
    }

    public static void logPushSetDefaultClick() {
        AutopilotEvent.logTopicEvent(SET_DEFAULT_PUSH_TOPIC_ID, "push_setdefault_click");
    }

    public static void logPushSetDefaultSuccess() {
        AutopilotEvent.logTopicEvent(SET_DEFAULT_PUSH_TOPIC_ID, "push_setdefault_success");
    }

    public static void logAlertSetDefaultShow(){
        AutopilotEvent.logTopicEvent(SET_DEFAULT_PUSH_TOPIC_ID, "alert_setdefault_show");
    }

    public static void logAlertSetDefaultClick(){
        AutopilotEvent.logTopicEvent(SET_DEFAULT_PUSH_TOPIC_ID, "alert_setdefault_click");
    }

    public static void logAlertSetDefaultSuccess(){
        AutopilotEvent.logTopicEvent(SET_DEFAULT_PUSH_TOPIC_ID, "alert_setdefault_success");
    }

}
