package com.android.messaging.ui.customize;

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
}
