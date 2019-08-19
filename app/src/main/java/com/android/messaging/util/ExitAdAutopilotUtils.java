package com.android.messaging.util;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class ExitAdAutopilotUtils {


    private static final String EXIT_AD_TOPIC_ID = "topic-76k40hett";


    public static boolean getIsExitAdSwitchOn() {
        return AutopilotConfig.getBooleanToTestNow(EXIT_AD_TOPIC_ID, "switch", false);
    }

    public static double getExitAdShowMaxTimes() {
        return AutopilotConfig.getDoubleToTestNow(EXIT_AD_TOPIC_ID, "maxtimes", 2);
    }

    public static void logSmsExitApp() {
        AutopilotEvent.logTopicEvent(EXIT_AD_TOPIC_ID, "sms_exitapp");
    }

    public static void logExitAdClick() {
        AutopilotEvent.logTopicEvent(EXIT_AD_TOPIC_ID, "exitad_click");
    }
}
