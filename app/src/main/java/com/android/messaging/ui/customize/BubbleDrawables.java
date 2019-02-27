package com.android.messaging.ui.customize;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;

import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_BUBBLE_DRAWABLE_IDENTIFIER;

public class BubbleDrawables {
    private static final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();

    // R.drawable.style_01,
    private static final int DEFAULT_DRAWABLE_IDENTIFIER = 1;

    public static final int[] BUBBLES_INCOMING = new int[]{
            R.drawable.style_01,
            R.drawable.style_02,
            R.drawable.style_03,
            R.drawable.style_04,
            R.drawable.style_05,
            R.drawable.style_06,
            R.drawable.style_07,
            R.drawable.style_08,
    };

    public static final int[] BUBBLES_OUTGOING = new int[]{
            R.drawable.style_01_outgoing,
            R.drawable.style_02_outgoing,
            R.drawable.style_03_outgoing,
            R.drawable.style_04_outgoing,
            R.drawable.style_05_outgoing,
            R.drawable.style_06_outgoing,
            R.drawable.style_07_outgoing,
            R.drawable.style_08_outgoing,
    };

    // each identifier matches one style
    public static final int[] IDENTIFIER = new int[]{
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8
    };

    public static int getSelectedIdentifier() {
        return prefs.getInt(PREFS_KEY_BUBBLE_DRAWABLE_IDENTIFIER, DEFAULT_DRAWABLE_IDENTIFIER);
    }

    static int getSelectedIndex() {
        int selectedIdentifier = getSelectedIdentifier();
        for (int i = 0; i < IDENTIFIER.length; i++) {
            if (IDENTIFIER[i] == selectedIdentifier) {
                return i;
            }
        }
        return -1;
    }

    static int getSelectedIndex(String conversationId) {
        int selectedIdentifier =
                prefs.getInt(PREFS_KEY_BUBBLE_DRAWABLE_IDENTIFIER + "_" + conversationId,
                        getSelectedIdentifier());
        for (int i = 0; i < IDENTIFIER.length; i++) {
            if (IDENTIFIER[i] == selectedIdentifier) {
                return i;
            }
        }
        return -1;
    }

    static void setSelectedIndex(int index) {
        prefs.putInt(PREFS_KEY_BUBBLE_DRAWABLE_IDENTIFIER, IDENTIFIER[index]);
    }

    static void setSelectedIndex(int index, String conversationId) {
        if (TextUtils.isEmpty(conversationId)) {
            setSelectedIndex(index);
        } else {
            prefs.putInt(PREFS_KEY_BUBBLE_DRAWABLE_IDENTIFIER + "_" + conversationId, IDENTIFIER[index]);
        }
    }

    @DrawableRes
    public static int getSelectedDrawable(boolean incoming) {
        if (incoming) {
            return BUBBLES_INCOMING[getSelectedIndex()];
        } else {
            return BUBBLES_OUTGOING[getSelectedIndex()];
        }
    }

    @DrawableRes
    public static int getSelectedDrawable(boolean incoming, String conversationId) {
        return getSelectedDrawable(getSelectedIndex(conversationId), incoming);
    }

    @DrawableRes
    public static int getSelectedDrawable(int index, boolean incoming) {
        if (incoming) {
            return BUBBLES_INCOMING[index];
        } else {
            return BUBBLES_OUTGOING[index];
        }
    }
}


