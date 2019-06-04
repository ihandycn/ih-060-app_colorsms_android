package com.android.messaging.ui.invitefriends;

import android.content.res.Resources;

import com.android.messaging.R;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Toasts;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;
import net.appcloudbox.autopilot.BuildConfig;

public class InviteFriendsTest {

    private static final String SMS_TOPIC_ID = "topic-74rv6bpwy";

    private static final String DIALOG_TOPIC_ID = "topic-74oxuasmo";

    public static String getSendDescription() {
        String type = AutopilotConfig.getStringToTestNow(SMS_TOPIC_ID, "send_description", "default");
        if (BuildConfig.DEBUG) {
            Toasts.showToast("in test : " + type);
        }

        Resources resources = HSApplication.getContext().getResources();
        if ("default".equals(type)) {
            return resources.getString(R.string.invite_friends_invite_default_content);
        } else if ("high_rating".equals(type)) {
            return resources.getString(R.string.invite_friends_high_rating_content);
        } else if ("better_than_system".equals(type)) {
            return resources.getString(R.string.invite_friends_better_than_system_content);
        } else if ("help_get_reward".equals(type)) {
            return resources.getString(R.string.invite_friends_help_get_reward_content);
        }
        return resources.getString(R.string.invite_friends_invite_default_content);
    }

    public static void logInviteSmsSent() {
        if (BuildConfig.DEBUG) {
            getSendDescription();
        }
        AutopilotEvent.logTopicEvent(SMS_TOPIC_ID, "invite_sms_send");
    }

    public static String getAlertType() {
        String type =  AutopilotConfig.getStringToTestNow(DIALOG_TOPIC_ID, "alert_description", "freesms");
        if (BuildConfig.DEBUG) {
            Toasts.showToast("in test : " + type);
        }
        return type;
    }

    public static void logGuideAlertShow() {
        AutopilotEvent.logTopicEvent(DIALOG_TOPIC_ID, "guide_alert_show");
    }

    public static void logGuideAlertClick() {
        AutopilotEvent.logTopicEvent(DIALOG_TOPIC_ID, "guide_alert_click");
    }

    public static void logInviteFriendsClick() {
        if (BuildConfig.DEBUG) {
            getAlertType();
        }
        AutopilotEvent.logTopicEvent(DIALOG_TOPIC_ID, "invite_send_click");
    }

}
