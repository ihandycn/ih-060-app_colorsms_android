package com.android.messaging.ui.appsettings;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class RingtoneEntranceAutopilotUtils {
    private static final String RINGTONE_ENTRANCE_TOPIC_ID = "topic-786jg94fx";

    public static boolean getIsShowMenuEntrance(){
        return AutopilotConfig.getBooleanToTestNow(RINGTONE_ENTRANCE_TOPIC_ID, "menu_entrance", true);
    }

    public static void logMenuShow(){
        AutopilotEvent.logTopicEvent(RINGTONE_ENTRANCE_TOPIC_ID, "menu_show");
    }

    public static void logMenuRingtoneClick(){
        AutopilotEvent.logTopicEvent(RINGTONE_ENTRANCE_TOPIC_ID, "menu_ringtone_click");
    }

    public static void logRingtonePageShow(){
        AutopilotEvent.logTopicEvent(RINGTONE_ENTRANCE_TOPIC_ID, "ringtone_page_show");
    }

    public static void logRingtonePageSet(){
        AutopilotEvent.logTopicEvent(RINGTONE_ENTRANCE_TOPIC_ID, "ringtone_page_set");
    }

    public static void logSettingsRingtoneClick(){
        AutopilotEvent.logTopicEvent(RINGTONE_ENTRANCE_TOPIC_ID, "settings_ringtone_click");
    }

    public static void logDetailsRingtoneClick(){
        AutopilotEvent.logTopicEvent(RINGTONE_ENTRANCE_TOPIC_ID, "details_ringtone_click");
    }

    public static void logAppRingtoneSet(){
        AutopilotEvent.logTopicEvent(RINGTONE_ENTRANCE_TOPIC_ID, "appringtone_set");
    }
}
