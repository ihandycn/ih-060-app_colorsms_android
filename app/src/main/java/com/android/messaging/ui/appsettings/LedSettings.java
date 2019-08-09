package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;

public class LedSettings {

    private static final String PREF_KEY_LED_COLOR = "pref_key_led_mode";

    public static final int NONE = 0;
    public static final int COLOR_WHITE = 1;
    public static final int COLOR_RED = 2;
    public static final int COLOR_BLUE = 3;
    public static final int COLOR_YELLOW = 4;
    public static final int COLOR_GREEN = 5;
    public static final int COLOR_PURPLE = 6;
    public static final int COLOR_CYAN = 7;

    @IntDef({NONE, COLOR_WHITE, COLOR_RED, COLOR_BLUE, COLOR_YELLOW, COLOR_GREEN, COLOR_PURPLE, COLOR_CYAN})
    @interface LedColor {
    }

    private static BuglePrefs sPrefs = Factory.get().getCustomizePrefs();

    @LedColor
    public static int getLedColor() {
        return getLedColor("");
    }

    @LedColor
    public static int getLedColor(String conversationId) {
        if (TextUtils.isEmpty(conversationId)) {
            return sPrefs.getInt(PREF_KEY_LED_COLOR, COLOR_WHITE);
        }
        return sPrefs.getInt(PREF_KEY_LED_COLOR + conversationId, getLedColor());
    }

    public static void setLedColor(String conversationId, @LedColor int color) {
        if (TextUtils.isEmpty(conversationId)) {
            sPrefs.putInt(PREF_KEY_LED_COLOR, color);
        } else {
            sPrefs.putInt(PREF_KEY_LED_COLOR + conversationId, color);
        }
    }

    public static String getLedDescription(String conversationId) {
        Context context = Factory.get().getApplicationContext();
        switch (getLedColor(conversationId)) {
            case LedSettings.NONE:
                return context.getString(R.string.settings_led_none);
            case LedSettings.COLOR_WHITE:
                return context.getString(R.string.settings_led_white);
            case LedSettings.COLOR_RED:
                return context.getString(R.string.settings_led_red);
            case LedSettings.COLOR_BLUE:
                return context.getString(R.string.settings_led_blue);
            case LedSettings.COLOR_YELLOW:
                return context.getString(R.string.settings_led_yellow);
            case LedSettings.COLOR_GREEN:
                return context.getString(R.string.settings_led_green);
            case LedSettings.COLOR_PURPLE:
                return context.getString(R.string.settings_led_purple);
            case LedSettings.COLOR_CYAN:
                return context.getString(R.string.settings_led_cyan);
        }
        return context.getString(R.string.settings_led_white);
    }

    public static int getLedHex(@LedColor int mode) {
        switch (mode) {
            case LedSettings.NONE:
                return 0xffffffff;
            case COLOR_RED:
                return 0xffff0000;
            case COLOR_YELLOW:
                return 0xffffff00;
            case COLOR_GREEN:
                return 0xff00ff00;
            case COLOR_CYAN:
                return 0xff00ffff;
            case COLOR_BLUE:
                return 0xff0000ff;
            case COLOR_PURPLE:
                return 0xffff00ff;
            case COLOR_WHITE:
            default:
                return 0xffffffff;
        }
    }
}


