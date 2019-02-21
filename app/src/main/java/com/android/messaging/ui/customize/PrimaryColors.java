package com.android.messaging.ui.customize;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.BuglePrefsKeys;

public class PrimaryColors {
    private static final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();
    private static final int DEFAULT_PRIMARY_COLOR = Factory.get().getApplicationContext().getResources().getColor(R.color.primary_color);

    public static void changePrimaryColor(@ColorInt int color) {
        prefs.putInt(BuglePrefsKeys.PREFS_KEY_PRIMARY_COLOR, color);
    }

    @ColorInt
    public static int getPrimaryColor() {
        return prefs.getInt(BuglePrefsKeys.PREFS_KEY_PRIMARY_COLOR, DEFAULT_PRIMARY_COLOR);
    }

    @ColorInt
    public static int getPrimaryColorDark() {
        int color = getPrimaryColor();
        final int blendedRed = (int) Math.floor(0.8 * Color.red(color));
        final int blendedGreen = (int) Math.floor(0.8 * Color.green(color));
        final int blendedBlue = (int) Math.floor(0.8 * Color.blue(color));
        return Color.rgb(blendedRed, blendedGreen, blendedBlue);
    }

    @ColorInt
    public static int getSoundLevelPrimaryColor() {
        return getPrimaryColor() & 0x33ffffff;
    }
}
