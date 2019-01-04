package com.android.messaging.smsshow;

import com.android.messaging.util.BuglePrefs;
import com.superapps.util.Preferences;

public class SmsShowUtils {
    private static final String PREFS_KEY_SMS_SHOW_ENABLED = "PREFS_KEY_SMS_SHOW_ENABLED";
    private static final String PREFS_KEY_SMS_SHOW_APPLIED_ID = "PREFS_KEY_SMS_SHOW_APPLIED_ID";

    public void setSmsShowUserEnabled(boolean enabled) {
        if (isSmsShowEnabledByUser() != enabled) {
            Preferences.get(BuglePrefs.SMS_SHOW_SHARED_PREFERENCES_NAME).putBoolean(PREFS_KEY_SMS_SHOW_ENABLED, enabled);
        }
    }

    public boolean isSmsShowEnabledByUser() {
        return Preferences.get(BuglePrefs.SMS_SHOW_SHARED_PREFERENCES_NAME).getBoolean(PREFS_KEY_SMS_SHOW_ENABLED, false);
    }

    public void setSmsShowAppliedId(int id) {
        Preferences.get(BuglePrefs.SMS_SHOW_SHARED_PREFERENCES_NAME).putInt(PREFS_KEY_SMS_SHOW_APPLIED_ID, id);
        setSmsShowUserEnabled(true);
    }

    public int getSmsShowAppliedId() {
        return Preferences.get(BuglePrefs.SMS_SHOW_SHARED_PREFERENCES_NAME).getInt(PREFS_KEY_SMS_SHOW_APPLIED_ID, 0);
    }
}
