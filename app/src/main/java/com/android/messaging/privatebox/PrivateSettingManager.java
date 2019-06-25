package com.android.messaging.privatebox;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;

public class PrivateSettingManager {

    private static final String PREF_KEY_PRIVATE_SETTING_HIDE_ICON = "pref_key_private_setting_hide_icon";
    private static final String PREF_KEY_PRIVATE_SETTING_ENABLE_NOTIFICATIONS = "pref_key_private_setting_enable_notification";

    public static boolean isNotificationEnable() {
        return Preferences.getDefault().getBoolean(PREF_KEY_PRIVATE_SETTING_ENABLE_NOTIFICATIONS, true);
    }

    public static void setNotificationEnable(boolean enable) {
        Preferences.getDefault().putBoolean(PREF_KEY_PRIVATE_SETTING_ENABLE_NOTIFICATIONS, enable);
    }

    public static void setIconHidden(boolean hidden) {
        Preferences.getDefault().putBoolean(PREF_KEY_PRIVATE_SETTING_HIDE_ICON, hidden);
    }

    public static boolean isPrivateBoxIconHidden() {
        return !PrivateBoxSettings.getIsPrivateBoxEnabled() ||
                Preferences.getDefault().getBoolean(PREF_KEY_PRIVATE_SETTING_HIDE_ICON, false);
    }
}
