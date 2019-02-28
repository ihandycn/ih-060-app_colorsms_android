package com.android.messaging.util;

import android.content.Context;

public class BugleCustomizePrefs extends BuglePrefsImpl {

    /**
     * Shared preferences name for customize.
     */
    public static final String CUSTOMIZE_SHARED_PREFERENCES_NAME = "customize";

    public BugleCustomizePrefs(Context context) {
        super(context);
    }

    @Override
    public String getSharedPreferencesName() {
        return CUSTOMIZE_SHARED_PREFERENCES_NAME;
    }

    @Override
    public void onUpgrade(int oldVersion, int newVersion) {
    }
}
