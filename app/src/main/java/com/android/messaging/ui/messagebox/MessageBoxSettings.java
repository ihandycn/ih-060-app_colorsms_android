package com.android.messaging.ui.messagebox;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.util.DefaultSMSUtils;
import com.android.messaging.util.OsUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;

public class MessageBoxSettings {

    private static final String PREFS_SMS_MESSAGE_ASSISTANT = "prefs_sms_message_assistant";


    public static boolean shouldPopUp() {
        return !Factory.get().getIsForeground()
                && (BugleNotifications.isNotificationSettingsSwitchOpenned() || OsUtil.isAtLeastO())
                && isSMSAssistantModuleEnabled()
                && DefaultSMSUtils.isDefaultSmsApp();
    }


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
