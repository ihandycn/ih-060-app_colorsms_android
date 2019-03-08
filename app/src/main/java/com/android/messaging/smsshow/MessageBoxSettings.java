package com.android.messaging.smsshow;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSPreferenceHelper;

public class MessageBoxSettings {

    private static final String PREFS_SMS_MESSAGE_ASSISTANT = "prefs_sms_message_assistant";


    /**
     * @param isOpened set SMS assistant module to the {@param isOpened} state
     */
    public static void setSMSAssistantModuleEnabled(boolean isOpened) {
        HSPreferenceHelper.getDefault().putBoolean(PREFS_SMS_MESSAGE_ASSISTANT, isOpened);
    }

    /**
     * the state of SMS assistant module
     *
     * @return
     */
    public static boolean isSMSAssistantModuleEnabled() {
        return HSPreferenceHelper.getDefault().getBoolean(PREFS_SMS_MESSAGE_ASSISTANT,
                isOpenDefault());
    }

    public static boolean isOpenDefault() {
        return HSConfig.optBoolean(false, "Application", "SMSPopUps", "DefaultSwitch");
    }
}
