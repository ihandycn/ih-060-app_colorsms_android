package com.android.messaging.privatebox;

import com.android.messaging.BugleFiles;
import com.android.messaging.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;


/**
 * Created by lizhe on 2019/3/28.
 */

public class PrivateBoxSettings {

    public enum PasswordStyle {
        PATTERN(0),
        PIN(1);

        private int value = 0;

        PasswordStyle(int value) {
            this.value = value;
        }

        public static PasswordStyle valueOf(int value) {
            switch (value) {
                case 0:
                    return PATTERN;
                case 1:
                    return PIN;
                default:
                    return null;
            }
        }

        public int value() {
            return this.value;
        }

        public String toString() {
            switch (this) {
                case PATTERN:
                    return "Pattern";
                case PIN:
                    return "Pin";
                default:
                    return "";
            }
        }
    }

    public static final String PASSWORD_PLACEHOLDER = "PASSWORD_PLACEHOLDER";
    public static final String PREF_KEY_PENDING_STATE_TIME = "PREF_KEY_PENDING_STATE_TIME";
    public static final String PREF_KEY_LOCK_ON_SCREEN_LOCK = "PREF_KEY_LOCK_ON_SCREEN_LOCK";
    public static final String PREF_KEY_INTRUDER_ATTEMPTS = "PREF_KEY_INTRUDER_ATTEMPTS";
    public static final String PREF_KEY_LAST_APP_LOCK_TIME = "PREF_KEY_LAST_APP_LOCK_TIME";
    public static final String PREF_KEY_USAGE_ACCESS_ALERT_POP_TIME = "PREF_KEY_USAGE_ACCESS_ALERT_POP_TIME";
    public static final String PREF_KEY_ENTRY_CLICKED = "PREF_KEY_ENTRY_CLICKED";
    public static final String PREF_KEY_PASSWORD_STYLE = "PREF_KEY_PASSWORD_STYLE";
    public static final String PREF_KEY_ENABLE_GESTURE_HIDING = "PREF_KEY_ENABLE_GESTURE_HIDING";
    public static final String PREF_KEY_PIN_PASSWORD = "PREF_KEY_PIN_PASSWORD";
    public static final String PREF_KEY_GESTURE_PASSWORD = "PREF_KEY_GESTURE_PASSWORD";
    public static final String INTRUDER_VIEW_BACK = "intruder_view_back";
    public static final String ENABLE_INTRUDER_SHOT = "enable_intruder_shot";
    public static final String PREF_KEY_IS_SECURITY_QUESTION_SET = "PREF_KEY_IS_SECURITY_QUESTION_SET";


    public static PasswordStyle getLockStyle() {
        return PasswordStyle.valueOf(Preferences.get(BugleFiles.PRIVATE_BOX_PREFS).getInt(PREF_KEY_PASSWORD_STYLE, getLockDefaultStyle()));
    }

    public static boolean isAnyPasswordSet() {
        return !PASSWORD_PLACEHOLDER.equals(getUnlockGesture()) || !PASSWORD_PLACEHOLDER.equals(getUnlockPIN());
    }

    public static int getLockDefaultStyle() {
        return PasswordStyle.PATTERN.value();
    }

    public static void setLockStyle(PasswordStyle passwordStyle) {
        Preferences.get(BugleFiles.PRIVATE_BOX_PREFS).putInt(PREF_KEY_PASSWORD_STYLE, passwordStyle.value());
    }

    public static String getUnlockGesture() {
        return Preferences.get(BugleFiles.PRIVATE_BOX_PREFS).getString(PREF_KEY_GESTURE_PASSWORD, PASSWORD_PLACEHOLDER);
    }

    public static void setUnlockGesture(String key) {
        Preferences.get(BugleFiles.PRIVATE_BOX_PREFS).putString(PREF_KEY_GESTURE_PASSWORD, key);
    }

    public static String getUnlockPIN() {
        return Preferences.get(BugleFiles.PRIVATE_BOX_PREFS).getString(PREF_KEY_PIN_PASSWORD, PASSWORD_PLACEHOLDER);
    }

    public static void setUnlockPIN(String pin) {
        Preferences.get(BugleFiles.PRIVATE_BOX_PREFS).putString(PREF_KEY_PIN_PASSWORD, pin);
    }

    public static boolean isSecurityQuestionSet() {
        return Preferences.get(BugleFiles.COMMON_PREFS).getBoolean(PrivateBoxSettings.PREF_KEY_IS_SECURITY_QUESTION_SET, false);
    }

    public static void setSecurityQuestionSet(boolean isSet) {
        Preferences.get(BugleFiles.COMMON_PREFS).putBoolean(PrivateBoxSettings.PREF_KEY_IS_SECURITY_QUESTION_SET, isSet);
    }

    public static boolean getIsPrivateBoxEnabled() {
        if (HSApplication.getFirstLaunchInfo().appVersionCode >= 59) {
            return isAnyPasswordSet() || HSConfig.optBoolean(false, "Application", "PrivateBox");
        }
        return true;
    }
}
