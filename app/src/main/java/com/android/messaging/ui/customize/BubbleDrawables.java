package com.android.messaging.ui.customize;

import android.support.annotation.DrawableRes;

import com.android.messaging.R;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.util.BuglePrefs;

import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_BUBBLE_DRAWABLE_ID;

public class BubbleDrawables {
    private static final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();

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


    public static int getSelectedIndex() {
        return prefs.getInt(PREFS_KEY_BUBBLE_DRAWABLE_ID, 0);
    }

    public static void setSelectedIndex(int position) {
        prefs.putInt(PREFS_KEY_BUBBLE_DRAWABLE_ID, position);
        ConversationDrawables.get().updateBubbleDrawable(position);
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
    public static int getSelectedDrawable(int index, boolean incoming) {
        if (incoming) {
            return BUBBLES_INCOMING[index];
        } else {
            return BUBBLES_OUTGOING[index];
        }
    }
}


