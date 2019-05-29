package com.android.messaging.ui.appsettings;

import android.content.Context;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;

public class SendDelaySettings {
    private static final String PREF_KEY_SEND_DELAY = "pref_key_send_delay";
    private static final int NO_DELAY = 0;

    private static BuglePrefs sPrefs = Factory.get().getCustomizePrefs();

    public static int getSendDelayInSecs() {
        return sPrefs.getInt(PREF_KEY_SEND_DELAY, NO_DELAY);
    }

    public static void setSendDelay(int timeInSecs) {
            sPrefs.putInt(PREF_KEY_SEND_DELAY, timeInSecs);
    }

    public static String getSendDelayDescription() {
        Context context = Factory.get().getApplicationContext();
        int delayTimeInSecs = getSendDelayInSecs();
        if (delayTimeInSecs == NO_DELAY) {
            return context.getString(R.string.send_delay_no_delay);
        } else {
            return context.getResources().getQuantityString(R.plurals.send_delay_seconds, delayTimeInSecs, delayTimeInSecs);
        }
    }
}
