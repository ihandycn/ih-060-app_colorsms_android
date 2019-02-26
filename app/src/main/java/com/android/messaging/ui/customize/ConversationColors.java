package com.android.messaging.ui.customize;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.util.BugleApplicationPrefs;
import com.android.messaging.util.BuglePrefs;

import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_BUBBLE_BACKGROUND_COLOR_INCOMING;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_BUBBLE_BACKGROUND_COLOR_OUTGOING;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_MESSAGE_TEXT_COLOR_INCOMING;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_MESSAGE_TEXT_COLOR_OUTGOING;

public class ConversationColors {
    private static ConversationColors sInstance;

    static int[] COLORS = new int[]{
            PrimaryColors.getPrimaryColor(),
            0xff0083fe,
            0xff16c7d3,
            0xffff7e2a,
            0xff7646ff,
            0xfff846c0,
            0xffd619ff,
            0xfff14d4d,
            0xfff5d20d,
            0xff000000,
            0xff81de09,
            0xfff6bd01
    };

    private final BuglePrefs mPrefs = BugleApplicationPrefs.getApplicationPrefs();
    private int mIncomingBubbleBackgroundColor;
    private int mOutgoingBubbleBackgroundColor;
    private int mIncomingTextColor;
    private int mOutgoingTextColor;

    public static ConversationColors get() {
        if (sInstance == null) {
            sInstance = new ConversationColors();
        }
        return sInstance;
    }

    private ConversationColors() {
        // Pre-create all the drawables.
        updateColors();
    }

    public void updateColors() {
        Resources res = Factory.get().getApplicationContext().getResources();

        mIncomingBubbleBackgroundColor = mPrefs.getInt(PREFS_KEY_BUBBLE_BACKGROUND_COLOR_INCOMING,
                res.getColor(R.color.message_bubble_color_incoming));

        mOutgoingBubbleBackgroundColor = mPrefs.getInt(PREFS_KEY_BUBBLE_BACKGROUND_COLOR_OUTGOING,
                PrimaryColors.getPrimaryColor());

        mIncomingTextColor = mPrefs.getInt(PREFS_KEY_MESSAGE_TEXT_COLOR_INCOMING,
                res.getColor(R.color.message_text_color_incoming));

        mOutgoingTextColor = mPrefs.getInt(PREFS_KEY_MESSAGE_TEXT_COLOR_OUTGOING,
                res.getColor(R.color.message_text_color_outgoing));
    }

    @ColorInt
    public int getBubbleBackgroundColor(boolean incoming) {
        if (incoming) {
            return mIncomingBubbleBackgroundColor;
        } else {
            return mOutgoingBubbleBackgroundColor;
        }
    }

    @ColorInt
    public int getMessageTextColor(boolean incoming) {
        if (incoming) {
            return mIncomingTextColor;
        } else {
            return mOutgoingTextColor;
        }
    }

    public void setBubbleBackgroundColor(boolean incoming, @ColorInt int color) {
        if (incoming) {
            if (mIncomingBubbleBackgroundColor != color) {
                mPrefs.putInt(PREFS_KEY_BUBBLE_BACKGROUND_COLOR_INCOMING, color);
                mIncomingBubbleBackgroundColor = color;
            }
        } else {
            if (mOutgoingBubbleBackgroundColor != color) {
                mPrefs.putInt(PREFS_KEY_BUBBLE_BACKGROUND_COLOR_OUTGOING, color);
                mOutgoingBubbleBackgroundColor = color;
            }
        }
    }

    public void setMessageTextColor(boolean incoming, @ColorInt int color) {
        if (incoming) {
            if (mIncomingTextColor != color) {
                mPrefs.putInt(PREFS_KEY_MESSAGE_TEXT_COLOR_INCOMING, color);
                mIncomingTextColor = color;
            }
        } else {
            if (mOutgoingTextColor != color) {
                mPrefs.putInt(PREFS_KEY_MESSAGE_TEXT_COLOR_OUTGOING, color);
                mOutgoingTextColor = color;
            }
        }
    }

    @ColorInt
    public int getBubbleBackgroundColorDark(boolean incoming) {
        return getColorDark(getBubbleBackgroundColor(incoming));
    }

    private int getColorDark(@ColorInt int color) {
        final int blendedRed = (int) Math.floor(0.8 * Color.red(color));
        final int blendedGreen = (int) Math.floor(0.8 * Color.green(color));
        final int blendedBlue = (int) Math.floor(0.8 * Color.blue(color));
        return Color.rgb(blendedRed, blendedGreen, blendedBlue);
    }
}
