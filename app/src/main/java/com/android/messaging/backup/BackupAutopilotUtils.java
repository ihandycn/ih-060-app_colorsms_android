package com.android.messaging.backup;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class BackupAutopilotUtils {

    public static final String BACK_UP_TOPIC_ID = "topic-768lyi3sp";


    public static boolean getIsBackupSwitchOn() {
        return AutopilotConfig.getBooleanToTestNow(BACK_UP_TOPIC_ID, "switch", true);
    }

    public static boolean getIsBackupFullGuideSwitchOn() {
        return AutopilotConfig.getBooleanToTestNow(BACK_UP_TOPIC_ID, "fullguide", false);
    }

    public static boolean getIsBackupCleanSwitchOn() {
        return AutopilotConfig.getBooleanToTestNow(BACK_UP_TOPIC_ID, "cleanswitch", false);
    }

    public static void logBackupOnce() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "backup_once");
    }

    public static void logMenuShow() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "menu_show");
    }

    public static void logMenuBackupClick() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "menu_backup_click");
    }

    public static void logFullGuideShow() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "fullguide_show");
    }

    public static void logFullGuideClick() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "fullguide_click");
    }

    public static void logBackupPageShow() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "backuppage_show");
    }

    public static void logBackupPageClick() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "backuppage_click");
    }

    public static void logBackupPageSuccess() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "backuppage_success");
    }

    public static void logRestorePageShow() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "restorepage_show");
    }

    public static void logRestorePageClick() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "restorepage_click");
    }

    public static void logRestorePageSuccess() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "restorepage_success");
    }

    public static void logFreeUpMsgAlertShow() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "freeupmsg_alert_show");
    }

    public static void logFreeUpMsgAlertClick() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "freeupmsg_alert_click");
    }

    public static void logFreeUpMsgSuccess() {
        AutopilotEvent.logTopicEvent(BACK_UP_TOPIC_ID, "freeupmsg_success");
    }

}
