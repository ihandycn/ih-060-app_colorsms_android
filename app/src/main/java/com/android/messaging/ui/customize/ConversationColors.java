package com.android.messaging.ui.customize;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;

import static com.android.messaging.ui.appsettings.ChooseThemeColorRecommendViewHolder.COLORS;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_BUBBLE_BACKGROUND_COLOR_INCOMING;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_BUBBLE_BACKGROUND_COLOR_OUTGOING;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_CONVERSATION_AD_ACTION_COLOR;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_CONVERSATION_LIST_SUBTITLE_COLOR;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_CONVERSATION_LIST_TIME_COLOR;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_CONVERSATION_LIST_TITLE_COLOR;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_MESSAGE_TEXT_COLOR_INCOMING;
import static com.android.messaging.util.BuglePrefsKeys.PREFS_KEY_MESSAGE_TEXT_COLOR_OUTGOING;

public class ConversationColors {

    private static ConversationColors sInstance;
    private final BuglePrefs mPrefs = Factory.get().getCustomizePrefs();

    private int mIncomingBubbleBackgroundColor;
    private int mOutgoingBubbleBackgroundColor;
    private int mIncomingTextColor;
    private int mOutgoingTextColor;

    private int mListTitleColor;
    private int mListSubtitleColor;
    private int mListTimeColor;
    private int mAdActionColor;

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

        mListTitleColor = mPrefs.getInt(PREFS_KEY_CONVERSATION_LIST_TITLE_COLOR,
                res.getColor(R.color.conversation_list_item_conversation));

        mListSubtitleColor = mPrefs.getInt(PREFS_KEY_CONVERSATION_LIST_SUBTITLE_COLOR,
                res.getColor(R.color.conversation_list_item_snippet));

        mListTimeColor = mPrefs.getInt(PREFS_KEY_CONVERSATION_LIST_TIME_COLOR,
                res.getColor(R.color.conversation_list_timestamp));

        mAdActionColor = mPrefs.getInt(PREFS_KEY_CONVERSATION_AD_ACTION_COLOR,
                res.getColor(R.color.conversation_ad_action));
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
    public int getBubbleBackgroundColor(boolean incoming, String conversationId) {
        if (TextUtils.isEmpty(conversationId)) {
            return getBubbleBackgroundColor(incoming);
        } else if (incoming) {
            return mPrefs.getInt(PREFS_KEY_BUBBLE_BACKGROUND_COLOR_INCOMING + "_" + conversationId, mIncomingBubbleBackgroundColor);
        } else {
            return mPrefs.getInt(PREFS_KEY_BUBBLE_BACKGROUND_COLOR_OUTGOING + "_" + conversationId, mOutgoingBubbleBackgroundColor);
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

    @ColorInt
    public int getMessageTextColor(boolean incoming, String conversationId) {
        if (TextUtils.isEmpty(conversationId)) {
            return getMessageTextColor(incoming);
        } else if (incoming) {
            return mPrefs.getInt(PREFS_KEY_MESSAGE_TEXT_COLOR_INCOMING + "_" + conversationId, mIncomingTextColor);
        } else {
            return mPrefs.getInt(PREFS_KEY_MESSAGE_TEXT_COLOR_OUTGOING + "_" + conversationId, mOutgoingTextColor);
        }
    }

    @ColorInt public int getListTitleColor() {
        return mListTitleColor;
    }

    @ColorInt public int getListSubtitleColor() {
        return mListSubtitleColor;
    }

    @ColorInt public int getListTimeColor() {
        return mListTimeColor;
    }

    @ColorInt public int getAdActionColor() {
        return mAdActionColor;
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

    public void setBubbleBackgroundColor(boolean incoming, @ColorInt int color, String conversationId) {
        if (TextUtils.isEmpty(conversationId)) {
            setBubbleBackgroundColor(incoming, color);
        } else if (incoming) {
            mPrefs.putInt(PREFS_KEY_BUBBLE_BACKGROUND_COLOR_INCOMING + "_" + conversationId, color);
        } else {
            mPrefs.putInt(PREFS_KEY_BUBBLE_BACKGROUND_COLOR_OUTGOING + "_" + conversationId, color);
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

    public void setMessageTextColor(boolean incoming, @ColorInt int color, String conversationId) {
        if (TextUtils.isEmpty(conversationId)) {
            setMessageTextColor(incoming, color);
        } else if (incoming) {
            mPrefs.putInt(PREFS_KEY_MESSAGE_TEXT_COLOR_INCOMING + "_" + conversationId, color);
        } else {
            mPrefs.putInt(PREFS_KEY_MESSAGE_TEXT_COLOR_OUTGOING + "_" + conversationId, color);
        }
    }

    public void setListTitleColor(@ColorInt int color) {
        mPrefs.putInt(PREFS_KEY_CONVERSATION_LIST_TITLE_COLOR, color);
        mListTitleColor = color;
    }

    public void setListSubTitleColor(@ColorInt int color) {
        mPrefs.putInt(PREFS_KEY_CONVERSATION_LIST_SUBTITLE_COLOR, color);
        mListSubtitleColor = color;
    }

    public void setListTimeColor(@ColorInt int color) {
        mPrefs.putInt(PREFS_KEY_CONVERSATION_LIST_TIME_COLOR, color);
        mListTimeColor = color;
    }

    public void setAdActionColor(@ColorInt int color) {
        mPrefs.putInt(PREFS_KEY_CONVERSATION_AD_ACTION_COLOR, color);
        mAdActionColor = color;
    }

    public void resetConversationCustomization(@NonNull String conversationId) {
        mPrefs.remove(PREFS_KEY_MESSAGE_TEXT_COLOR_INCOMING + "_" + conversationId);
        mPrefs.remove(PREFS_KEY_MESSAGE_TEXT_COLOR_OUTGOING + "_" + conversationId);
        mPrefs.remove(PREFS_KEY_BUBBLE_BACKGROUND_COLOR_INCOMING + "_" + conversationId);
        mPrefs.remove(PREFS_KEY_BUBBLE_BACKGROUND_COLOR_OUTGOING + "_" + conversationId);
    }

    public String getConversationColorEventType(boolean isBubble, boolean incoming) {
        if (isBubble && incoming) {
            return getIndexFromThemeColors(mIncomingBubbleBackgroundColor);
        } else if (isBubble) {
            return mOutgoingBubbleBackgroundColor == PrimaryColors.getPrimaryColor() ? "themeColor"
                    : getIndexFromThemeColors(mOutgoingBubbleBackgroundColor);
        } else if (incoming) {
            return getIndexFromThemeColors(mIncomingTextColor);
        } else {
            return getIndexFromThemeColors(mOutgoingTextColor);
        }
    }

    private String getIndexFromThemeColors(@ColorInt int color) {
        Resources res = Factory.get().getApplicationContext().getResources();
        if (color == res.getColor(R.color.message_bubble_color_incoming)
                || color == res.getColor(R.color.message_bubble_color_outgoing)
                || color == res.getColor(R.color.message_text_color_outgoing)
                || color == res.getColor(R.color.message_text_color_incoming)) {
            return "default";
        }

        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == color) {
                return String.valueOf(i);
            }
        }

        return "advance";
    }

    @ColorInt
    public int getBubbleBackgroundColorDark(boolean incoming, String conversationId) {
        return getColorDark(getBubbleBackgroundColor(incoming, conversationId));
    }

    private int getColorDark(@ColorInt int color) {
        int blenedAlpha = (int) Math.floor(Color.alpha(color));
        if (blenedAlpha == 0) {
            blenedAlpha = 128;
        }
        final int blendedRed = (int) Math.floor(0.8 * Color.red(color));
        final int blendedGreen = (int) Math.floor(0.8 * Color.green(color));
        final int blendedBlue = (int) Math.floor(0.8 * Color.blue(color));
        return argb(blenedAlpha, blendedRed, blendedGreen, blendedBlue);
    }

    @ColorInt
    private static int argb(
            @IntRange(from = 0, to = 255) int alpha,
            @IntRange(from = 0, to = 255) int red,
            @IntRange(from = 0, to = 255) int green,
            @IntRange(from = 0, to = 255) int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

}
