package com.android.messaging.upgrader;

import android.content.Context;

import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.util.BuglePrefs;
import com.superapps.font.FontStyleManager;
import com.superapps.util.Preferences;

public class Upgrader extends BaseUpgrader {

    private static Upgrader sUpgrader;

    public static Upgrader getUpgrader(Context context) {
        if (sUpgrader == null) {
            sUpgrader = new Upgrader(context);
        }
        return sUpgrader;
    }

    public Upgrader(Context context) {
        super(context);
    }

    @Override
    protected void onAppUpgrade(int oldVersion, int newVersion) {
        if (oldVersion <= 12 && newVersion > 12) {
            Preferences.getDefault().putBoolean(ConversationListActivity.PREF_KEY_MAIN_DRAWER_OPENED, false);
        }

        if (oldVersion == 13 && newVersion > 13) {
            int fontLevel = BuglePrefs.getApplicationPrefs().getInt("message_font_scale", 2);
            FontStyleManager.setFontScaleLevel(fontLevel);
        }
    }

    private void migrateLong(Preferences from, Preferences to, String key) {
        to.putLong(key, from.getLong(key, 0L));
    }
}
