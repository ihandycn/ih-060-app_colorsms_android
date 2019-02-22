package com.android.messaging.ui.customize;

import android.support.annotation.DrawableRes;

import com.android.messaging.R;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.util.BuglePrefs;

import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_BUBBLE_DRWABLE_ID;

public class BubbleDrawables {
    private static final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();

    public static final int[] BUBBLES = new int[]{
            R.drawable.style_01,
            R.drawable.style_02,
            R.drawable.style_03,
            R.drawable.style_04,
            R.drawable.style_05,
            R.drawable.style_06,
            R.drawable.style_07,
            R.drawable.style_08,
    };

    public static int getSelectedIndex() {
        return prefs.getInt(PREFS_KEY_BUBBLE_DRWABLE_ID, 0);
    }

    public static void setSelectedIndex(int position) {
        prefs.putInt(PREFS_KEY_BUBBLE_DRWABLE_ID, position);
        ConversationDrawables.get().updateBubbleDrawable(position);
    }

    @DrawableRes
    public static int getSelectedDrawable() {
        return BUBBLES[getSelectedIndex()];
    }

    @DrawableRes
    public static int getSelectedDrawable(int index) {
        return BUBBLES[index];
    }
}


