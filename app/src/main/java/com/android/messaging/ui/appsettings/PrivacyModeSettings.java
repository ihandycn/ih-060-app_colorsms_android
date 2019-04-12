package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.util.BugleCustomizePrefs;
import com.android.messaging.util.BuglePrefs;
import com.superapps.util.Preferences;

public class PrivacyModeSettings {
    private static final String PREF_KEY_PRIVACY_MODE = "pref_key_privacy_mode";

    public static final int NONE = 0;
    public static final int HIDE_MESSAGE_ONLY = 1;
    public static final int HIDE_CONTACT_AND_MESSAGE = 2;

    @IntDef({NONE, HIDE_MESSAGE_ONLY, HIDE_CONTACT_AND_MESSAGE})
    @interface PrivacyMode {
    }

    private static BuglePrefs sPrefs = Factory.get().getCustomizePrefs();

    @PrivacyMode
    public static int getPrivacyMode() {
        return getPrivacyMode("");
    }

    @PrivacyMode
    public static int getPrivacyMode(String conversationId) {
        if (TextUtils.isEmpty(conversationId)) {
            return sPrefs.getInt(PREF_KEY_PRIVACY_MODE, NONE);
        }
        return sPrefs.getInt(PREF_KEY_PRIVACY_MODE + conversationId, getPrivacyMode());
    }

    public static void setPrivacyMode(String conversationId, @PrivacyMode int mode) {
        if (TextUtils.isEmpty(conversationId)) {
            sPrefs.putInt(PREF_KEY_PRIVACY_MODE, mode);
        } else {
            sPrefs.putInt(PREF_KEY_PRIVACY_MODE + conversationId, mode);
        }
    }

    public static void setPrivacyMode(@PrivacyMode int mode) {
        sPrefs.putInt(PREF_KEY_PRIVACY_MODE, mode);
    }

    public static String getPrivacyModeDescription(String conversationId) {
        Context context = Factory.get().getApplicationContext();
        switch (getPrivacyMode(conversationId)) {
            case PrivacyModeSettings.NONE:
                return context.getString(R.string.privacy_mode_disable);
            case PrivacyModeSettings.HIDE_MESSAGE_ONLY:
                return context.getString(R.string.privacy_mode_hide_message_only);
            case PrivacyModeSettings.HIDE_CONTACT_AND_MESSAGE:
                return context.getString(R.string.privacy_mode_hide_contact_and_message);
        }
        return context.getString(R.string.privacy_mode_disable);
    }
}


