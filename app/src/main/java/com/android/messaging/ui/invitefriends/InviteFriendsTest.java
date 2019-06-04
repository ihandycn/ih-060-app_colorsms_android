package com.android.messaging.ui.invitefriends;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;
import net.appcloudbox.autopilot.BuildConfig;

public class InviteFriendsTest {

    private static final String SMS_TOPIC_ID = "topic-74rv6bpwy";
    private static final String DIALOG_TOPIC_ID = "topic-74oxuasmo";

    public static String getSendDescription() {
        return AutopilotConfig.getStringToTestNow(SMS_TOPIC_ID, "send_description", "");
    }

    public static void logInviteSmsSent() {
        AutopilotEvent.logTopicEvent(SMS_TOPIC_ID, "invite_sms_send");
    }

    public static String getAlertType() {
        return AutopilotConfig.getStringToTestNow(DIALOG_TOPIC_ID, "alert_description", "freesms");
    }

    public static void logGuideAlertShow() {
        AutopilotEvent.logTopicEvent(DIALOG_TOPIC_ID, "guide_alert_show");
    }

    public static void logGuideAlertClick() {
        if (BuildConfig.DEBUG) {
            getSendDescription();

        }
//        AutopilotEvent.logTopicEvent(DIALOG_TOPIC_ID, "guide_alert_click");
    }

    public static void logInviteFriendsClick() {
        if (BuildConfig.DEBUG) {
            getAlertType();
        }
//        AutopilotEvent.logTopicEvent(DIALOG_TOPIC_ID, "invite_send_click");
    }

}
