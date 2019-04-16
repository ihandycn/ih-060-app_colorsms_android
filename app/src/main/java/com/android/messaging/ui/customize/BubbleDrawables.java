package com.android.messaging.ui.customize;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;

import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_BUBBLE_DRAWABLE_IDENTIFIER;

public class BubbleDrawables {
    private static final BuglePrefs prefs = Factory.get().getCustomizePrefs();

    public static final int CUSTOM_BUBBLES_COUNT = 8;

    // R.drawable.style_01,
    private static final int THEME_DRAWABLE_IDENTIFIER = -1;

    public static final int[] BUBBLES_INCOMING = new int[]{
            R.drawable.style_01,
            R.drawable.style_02,
            R.drawable.style_03,
            R.drawable.style_04,
            R.drawable.style_05,
            R.drawable.style_06,
            R.drawable.style_07,
            R.drawable.style_08,
            R.drawable.bubble_diamond_incoming,
            R.drawable.bubble_neon_incoming,
            R.drawable.bubble_waterdrop_incoming
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
            R.drawable.bubble_diamond_outgoing,
            R.drawable.bubble_neon_outgoing,
            R.drawable.bubble_waterdrop_outging
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
            8,
            9,
            10,
            11
    };

    public static int getSelectedIdentifier() {
        return prefs.getInt(PREFS_KEY_BUBBLE_DRAWABLE_IDENTIFIER, THEME_DRAWABLE_IDENTIFIER);
    }

    public static void setSelectedIdentifier(int identifier) {
        prefs.putInt(PREFS_KEY_BUBBLE_DRAWABLE_IDENTIFIER, identifier);
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

    public static void setSelectedIndex(int index) {
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
    public static int getSelectedDrawable(boolean incoming, String conversationId) {
        int selectedIdentifier =
                prefs.getInt(PREFS_KEY_BUBBLE_DRAWABLE_IDENTIFIER + "_" + conversationId, getSelectedIdentifier());
        int selectedIndex = -1;
        for (int i = 0; i < IDENTIFIER.length; i++) {
            if (IDENTIFIER[i] == selectedIdentifier) {
                selectedIndex = i;
            }
        }
        if (selectedIndex != -1) {
            return getSelectedDrawable(selectedIndex, incoming);
        } else {
            return getSelectedDrawable(0, incoming);
        }
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


