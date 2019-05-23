package com.android.messaging.ui.appsettings;

import android.content.Context;
import android.support.annotation.IntDef;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;

public class SendDelaySettings {
    private static final String PREF_KEY_SEND_DELAY = "pref_key_send_delay";

    public static final int NO_DELAY = 0;
    public static final int ONE_SECOND = 1;
    public static final int TWO_SECONDS = 2;
    public static final int THREE_SECONDS = 3;
    public static final int FOUR_SECONDS = 4;
    public static final int FIVE_SECONDS = 5;
    public static final int SIX_SECONDS = 6;
    public static final int SEVEN_SECONDS = 7;
    public static final int EIGHT_SECONDS = 8;
    public static final int NINE_SECONDS = 9;


    @IntDef({NO_DELAY, ONE_SECOND, TWO_SECONDS, THREE_SECONDS, FOUR_SECONDS,
            FIVE_SECONDS, SIX_SECONDS, SEVEN_SECONDS, EIGHT_SECONDS, NINE_SECONDS})
    @interface SendDelay {
    }

    private static BuglePrefs sPrefs = Factory.get().getCustomizePrefs();

    @SendDelay
    public static int getSendDelay() {
        return sPrefs.getInt(PREF_KEY_SEND_DELAY, NO_DELAY);
    }

    public static void setSendDelay(@SendDelay int mode) {
            sPrefs.putInt(PREF_KEY_SEND_DELAY, mode);
    }

    public static String getSendDelayDescription() {
        Context context = Factory.get().getApplicationContext();
        switch (getSendDelay()) {
            case SendDelaySettings.NO_DELAY:
                return context.getString(R.string.send_delay_no_delay);
            case SendDelaySettings.ONE_SECOND:
                return context.getResources().getQuantityString(R.plurals.send_delay_seconds, SendDelaySettings.ONE_SECOND, SendDelaySettings.ONE_SECOND);
            case SendDelaySettings.TWO_SECONDS:
                return context.getResources().getQuantityString(R.plurals.send_delay_seconds, SendDelaySettings.TWO_SECONDS, SendDelaySettings.TWO_SECONDS);
            case SendDelaySettings.THREE_SECONDS:
                return context.getResources().getQuantityString(R.plurals.send_delay_seconds, SendDelaySettings.THREE_SECONDS, SendDelaySettings.THREE_SECONDS);
            case SendDelaySettings.FOUR_SECONDS:
                return context.getResources().getQuantityString(R.plurals.send_delay_seconds, SendDelaySettings.FOUR_SECONDS, SendDelaySettings.FOUR_SECONDS);
            case SendDelaySettings.FIVE_SECONDS:
                return context.getResources().getQuantityString(R.plurals.send_delay_seconds, SendDelaySettings.FIVE_SECONDS, SendDelaySettings.FIVE_SECONDS);
            case SendDelaySettings.SIX_SECONDS:
                return context.getResources().getQuantityString(R.plurals.send_delay_seconds, SendDelaySettings.SIX_SECONDS, SendDelaySettings.SIX_SECONDS);
            case SendDelaySettings.SEVEN_SECONDS:
                return context.getResources().getQuantityString(R.plurals.send_delay_seconds, SendDelaySettings.SEVEN_SECONDS, SendDelaySettings.SEVEN_SECONDS);
            case SendDelaySettings.EIGHT_SECONDS:
                return context.getResources().getQuantityString(R.plurals.send_delay_seconds, SendDelaySettings.EIGHT_SECONDS, SendDelaySettings.EIGHT_SECONDS);
            case SendDelaySettings.NINE_SECONDS:
                return context.getResources().getQuantityString(R.plurals.send_delay_seconds, SendDelaySettings.NINE_SECONDS, SendDelaySettings.NINE_SECONDS);
        }
        return context.getString(R.string.send_delay_no_delay);
    }
}
