package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;

public class VibrateSettings {

    private static final String PREF_KEY_VIBRATE_MODE = "pref_key_vibrate_mode";

    public static final int OFF = 0;
    public static final int VIBRATE_NORMAL = 1;
    public static final int VIBRATE_SHORT = 2;
    public static final int VIBRATE_LONG = 3;
    public static final int VIBRATE_MULTIPLE_SHORT = 4;
    public static final int VIBRATE_MULTIPLE_LONG = 5;

    @IntDef({OFF, VIBRATE_NORMAL, VIBRATE_SHORT, VIBRATE_LONG, VIBRATE_MULTIPLE_LONG, VIBRATE_MULTIPLE_SHORT})
    @interface VibrateMode {
    }

    private static BuglePrefs sPrefs = Factory.get().getCustomizePrefs();

    @VibrateMode
    public static int getPrivacyMode() {
        return getVibrateMode("");
    }

    @VibrateMode
    public static int getVibrateMode(String conversationId) {
        if (TextUtils.isEmpty(conversationId)) {
            return sPrefs.getInt(PREF_KEY_VIBRATE_MODE, VIBRATE_NORMAL);
        }
        return sPrefs.getInt(PREF_KEY_VIBRATE_MODE + conversationId, getPrivacyMode());
    }

    public static void setVibrateMode(String conversationId, @VibrateMode int mode) {
        if (TextUtils.isEmpty(conversationId)) {
            sPrefs.putInt(PREF_KEY_VIBRATE_MODE, mode);
        } else {
            sPrefs.putInt(PREF_KEY_VIBRATE_MODE + conversationId
                    , mode);
        }
    }

    public static void setVibrateMode(@VibrateMode int mode) {
        sPrefs.putInt(PREF_KEY_VIBRATE_MODE, mode);
    }

    public static String getVibrateDescription(String conversationId) {
        Context context = Factory.get().getApplicationContext();
        switch (getVibrateMode(conversationId)) {
            case VibrateSettings.OFF:
                return context.getString(R.string.settings_vibrate_off);
            case VibrateSettings.VIBRATE_NORMAL:
                return context.getString(R.string.settings_vibrate_normal);
            case VibrateSettings.VIBRATE_SHORT:
                return context.getString(R.string.settings_vibrate_short);
            case VibrateSettings.VIBRATE_LONG:
                return context.getString(R.string.settings_vibrate_long);
            case VibrateSettings.VIBRATE_MULTIPLE_SHORT:
                return context.getString(R.string.settings_vibrate_multiple_short);
            case VibrateSettings.VIBRATE_MULTIPLE_LONG:
                return context.getString(R.string.settings_vibrate_multiple_long);
        }
        return context.getString(R.string.settings_vibrate_normal);
    }

    public static long[] getViratePattern(@VibrateMode int mode) {
        switch (mode) {
            case VibrateSettings.OFF:
                return new long[]{0};
            case VIBRATE_SHORT:
                return new long[]{0, 300};
            case VIBRATE_LONG:
                return new long[]{0, 2500};
            case VIBRATE_MULTIPLE_SHORT:
                return new long[]{0, 300, 100, 300, 100, 300, 100, 300, 100, 300};
            case VIBRATE_MULTIPLE_LONG:
                return new long[]{0, 800, 100, 800, 100, 800};
            case VIBRATE_NORMAL:
            default:
                return new long[]{0, 1000};

        }
    }
}


