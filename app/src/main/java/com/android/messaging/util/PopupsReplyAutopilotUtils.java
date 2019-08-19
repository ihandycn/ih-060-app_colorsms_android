package com.android.messaging.util;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class PopupsReplyAutopilotUtils {

    private static final String POP_UPS_REPLY_TOPIC_ID = "topic-783nstv2k";


    public static boolean getIsNewPopups() {
        return AutopilotConfig.getBooleanToTestNow(POP_UPS_REPLY_TOPIC_ID, "type", false);
    }

    public static void logNotificationPushed() {
        AutopilotEvent.logTopicEvent(POP_UPS_REPLY_TOPIC_ID, "notification_pushed");
    }

    public static void logNotificationClicked() {
        AutopilotEvent.logTopicEvent(POP_UPS_REPLY_TOPIC_ID, "notification_clicked");
    }

    public static void logNotificationReplied() {
        AutopilotEvent.logTopicEvent(POP_UPS_REPLY_TOPIC_ID, "notification_replied");
    }

    public static void logDefaultAlertShow() {
        AutopilotEvent.logTopicEvent(POP_UPS_REPLY_TOPIC_ID, "defaultalert_show");
    }

    public static void logDefaultAlertClick() {
        AutopilotEvent.logTopicEvent(POP_UPS_REPLY_TOPIC_ID, "defaultalert_click");
    }

    public static void logDefaultAlertSuccess() {
        AutopilotEvent.logTopicEvent(POP_UPS_REPLY_TOPIC_ID, "defaultalert_success");
    }

    public static void logPopupShow() {
        AutopilotEvent.logTopicEvent(POP_UPS_REPLY_TOPIC_ID, "popup_show");
    }

    public static void logPopupReply() {
        AutopilotEvent.logTopicEvent(POP_UPS_REPLY_TOPIC_ID, "popup_reply");
    }

    public static void logPopupOpenClick() {
        AutopilotEvent.logTopicEvent(POP_UPS_REPLY_TOPIC_ID, "popup_btnopen");
    }

    public static void logPopupClickContent() {
        AutopilotEvent.logTopicEvent(POP_UPS_REPLY_TOPIC_ID, "popup_clickcontent");
    }
}
