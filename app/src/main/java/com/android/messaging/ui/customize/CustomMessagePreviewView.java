package com.android.messaging.ui.customize;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class CustomMessagePreviewView extends ConstraintLayout
        implements WallpaperManager.WallpaperChangeListener {
    private String mConversationId;
    private boolean mHasCustomBackground;

    private static int sIncomingBackgroundPreviewColor;
    private static int sOutgoingBackgroundPreviewColor;
    private static int sIncomingTextPreviewColor;
    private static int sOutgoingTextPreviewColor;

    private int mPreviewBubbleDrawableIndex;

    private TextView mIncomingMessage;
    private TextView mOutgoingMessage;

    public static int getBubbleBackgroundPreviewColor(boolean incoming) {
        if (incoming) {
            return sIncomingBackgroundPreviewColor;
        } else {
            return sOutgoingBackgroundPreviewColor;
        }
    }

    public static int getMessageTextPreviewColor(boolean incoming) {
        if (incoming) {
            return sIncomingTextPreviewColor;
        } else {
            return sOutgoingTextPreviewColor;
        }
    }

    public CustomMessagePreviewView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.customize_preview_view, this, true);
        mIncomingMessage = findViewById(R.id.incoming_message_preview_item);
        mOutgoingMessage = findViewById(R.id.outgoing_message_preview_item);

        refreshTimestamp();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        WallpaperManager.addWallpaperChangeListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        WallpaperManager.removeWallpaperChangeListener(this);
    }

    private void refreshTimestamp() {
        boolean hasWallPaper = WallpaperManager.hasWallpaper("1");
        if (hasWallPaper) {
            int color = PrimaryColors.getPrimaryColor();
            findViewById(R.id.message_preview_timestamp_1).setBackground(BackgroundDrawables.createBackgroundDrawable(
                    Color.argb(51, Color.red(color), Color.green(color), Color.blue(color)), Dimensions.pxFromDp(16), false));
            findViewById(R.id.message_preview_timestamp_2).setBackground(BackgroundDrawables.createBackgroundDrawable(
                    Color.argb(51, Color.red(color), Color.green(color), Color.blue(color)), Dimensions.pxFromDp(16), false));
            ((TextView) findViewById(R.id.message_preview_timestamp_1)).setTextColor(getResources().getColor(android.R.color.white));
            ((TextView) findViewById(R.id.message_preview_timestamp_2)).setTextColor(getResources().getColor(android.R.color.white));
        } else {
            findViewById(R.id.message_preview_timestamp_1).setBackground(null);
            findViewById(R.id.message_preview_timestamp_2).setBackground(null);
            ((TextView) findViewById(R.id.message_preview_timestamp_1)).setTextColor(getResources().getColor(R.color.timestamp_text_incoming));
            ((TextView) findViewById(R.id.message_preview_timestamp_2)).setTextColor(getResources().getColor(R.color.timestamp_text_incoming));
        }
    }

    public void setIsFontPreview() {
        mIncomingMessage.setText(getResources().getString(R.string.bubble_customize_preview_incoming_message_font));
        mOutgoingMessage.setText(getResources().getString(R.string.bubble_customize_preview_outgoing_message_font));
    }

    public void updateAvatarDrawable(boolean hasCustomBackground) {
        TextView contactIcon = findViewById(R.id.contact_text);
        contactIcon.setTextColor(Color.parseColor(
                ThemeInfo.getThemeInfo(ThemeUtils.getCurrentThemeName()).avatarForegroundColor));
        ImageView contactBackground = findViewById(R.id.contact_background);
        Drawable avatar = AvatarBgDrawables.getAvatarBg(true, hasCustomBackground);
        if (avatar != null) {
            contactBackground.setBackground(avatar);
        } else {
            contactIcon.setBackgroundResource(R.drawable.bubble_customize_preview_contact_icon_background);
        }
    }

    public void updateBackgroundState(String conversationId, boolean hasCustomBackground) {
        if (mHasCustomBackground != hasCustomBackground) {
            updateAvatarDrawable(hasCustomBackground);
            mConversationId = conversationId;
            mIncomingMessage.setBackground(
                    ConversationDrawables.get().getBubbleDrawable(false, true,
                            true, false, mConversationId, hasCustomBackground));
            mOutgoingMessage.setBackground(
                    ConversationDrawables.get().getBubbleDrawable(false, false,
                            true, false, mConversationId, hasCustomBackground));
        }
        mHasCustomBackground = hasCustomBackground;
    }

    public void updateBubbleDrawables(final String conversationId) {
        updateBubbleDrawables(conversationId, false);
    }

    public void updateBubbleDrawables(final String conversationId, final boolean hasCustomBackground) {
        //update avatar drawable
        updateAvatarDrawable(hasCustomBackground);
        //update bubble drawables
        mConversationId = conversationId;
        mIncomingMessage.setBackground(
                ConversationDrawables.get().getBubbleDrawable(false, true, true, false, mConversationId, hasCustomBackground));
        mOutgoingMessage.setBackground(
                ConversationDrawables.get().getBubbleDrawable(false, false, true, false, mConversationId, hasCustomBackground));

        sIncomingBackgroundPreviewColor = ConversationColors.get().getBubbleBackgroundColor(true, conversationId);
        sOutgoingBackgroundPreviewColor = ConversationColors.get().getBubbleBackgroundColor(false, conversationId);

        sIncomingTextPreviewColor = ConversationColors.get().getMessageTextColor(true, conversationId);
        sOutgoingTextPreviewColor = ConversationColors.get().getMessageTextColor(false, conversationId);

        mIncomingMessage.setTextColor(sIncomingTextPreviewColor);
        mOutgoingMessage.setTextColor(sOutgoingTextPreviewColor);

        mPreviewBubbleDrawableIndex = BubbleDrawables.getSelectedIndex(conversationId);
    }

    public void previewCustomBubbleDrawables(int index) {
        mIncomingMessage.setBackgroundResource(BubbleDrawables.getSelectedDrawable(index, true));
        mOutgoingMessage.setBackgroundResource(BubbleDrawables.getSelectedDrawable(index, false));

        mIncomingMessage.getBackground().setColorFilter(sIncomingBackgroundPreviewColor, PorterDuff.Mode.SRC_ATOP);
        mOutgoingMessage.getBackground().setColorFilter(sOutgoingBackgroundPreviewColor, PorterDuff.Mode.SRC_ATOP);

        mPreviewBubbleDrawableIndex = index;
    }

    public void previewCustomBubbleBackgroundColor(boolean incoming, @ColorInt int color) {
        if (incoming) {
            mIncomingMessage.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            sIncomingBackgroundPreviewColor = color;
        } else {
            mOutgoingMessage.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            sOutgoingBackgroundPreviewColor = color;
        }
    }

    public void previewCustomTextColor(boolean incoming, @ColorInt int color) {
        if (incoming) {
            mIncomingMessage.setTextColor(color);
            sIncomingTextPreviewColor = color;
        } else {
            mOutgoingMessage.setTextColor(color);
            sOutgoingTextPreviewColor = color;
        }
    }

    public void save() {
        boolean bubbleDrawableChanged = false;
        boolean bubbleBackgroundColorChanged = false;
        boolean bubbleTextColorChanged = false;

        if (mPreviewBubbleDrawableIndex != BubbleDrawables.getSelectedIndex(mConversationId)) {
            BubbleDrawables.setSelectedIndex(mPreviewBubbleDrawableIndex, mConversationId);
            bubbleDrawableChanged = true;
            BugleAnalytics.logEvent("Customize_Bubble_Style_Change", "style",
                    String.valueOf(BubbleDrawables.getSelectedIdentifier()));
            BugleFirebaseAnalytics.logEvent("Customize_Bubble_Style_Change",  "style",
                    String.valueOf(BubbleDrawables.getSelectedIdentifier()));
        }

        if (sIncomingBackgroundPreviewColor != ConversationColors.get().getBubbleBackgroundColor(true, mConversationId)) {
            ConversationColors.get().setBubbleBackgroundColor(true, sIncomingBackgroundPreviewColor, mConversationId);
            bubbleBackgroundColorChanged = true;
        }

        if (sOutgoingBackgroundPreviewColor != ConversationColors.get().getBubbleBackgroundColor(false, mConversationId)) {
            ConversationColors.get().setBubbleBackgroundColor(false, sOutgoingBackgroundPreviewColor, mConversationId);
            bubbleBackgroundColorChanged = true;
        }

        if (sIncomingTextPreviewColor != ConversationColors.get().getMessageTextColor(true, mConversationId)) {
            ConversationColors.get().setMessageTextColor(true, sIncomingTextPreviewColor, mConversationId);
            bubbleTextColorChanged = true;
        }

        if (sOutgoingTextPreviewColor != ConversationColors.get().getMessageTextColor(false, mConversationId)) {
            ConversationColors.get().setMessageTextColor(false, sOutgoingTextPreviewColor, mConversationId);
            bubbleTextColorChanged = true;
        }

        String from = TextUtils.isEmpty(mConversationId) ? "settings" : "chat";

        BugleAnalytics.logEvent("Customize_Bubble_Change", true, "from", from, "type",
                getBubbleChangeString(bubbleDrawableChanged, bubbleBackgroundColorChanged || bubbleTextColorChanged));
        BugleFirebaseAnalytics.logEvent("Customize_Bubble_Change",  "from", from, "type",
                getBubbleChangeString(bubbleDrawableChanged, bubbleBackgroundColorChanged || bubbleTextColorChanged));

        if (bubbleBackgroundColorChanged || bubbleTextColorChanged) {
            BugleAnalytics.logEvent("Customize_Bubble_Color_Change", "type",
                    getBubbleColorChangeString(bubbleBackgroundColorChanged, bubbleTextColorChanged));
            BugleFirebaseAnalytics.logEvent("Customize_Bubble_Color_Change",  "type",
                    getBubbleColorChangeString(bubbleBackgroundColorChanged, bubbleTextColorChanged));
        }
    }

    private String getBubbleChangeString(boolean drawableChanged, boolean colorChanged) {
        if (drawableChanged && colorChanged) {
            return "both";
        } else if (drawableChanged) {
            return "style";
        } else if (colorChanged) {
            return "color";
        }
        return "noChange";
    }

    private String getBubbleColorChangeString(boolean bubbleColorChanged, boolean textColorChanged) {
        if (bubbleColorChanged && textColorChanged) {
            return "both";
        } else if (bubbleColorChanged) {
            return "bubble";
        } else if (textColorChanged) {
            return "text";
        }
        return "noChange";
    }

    @Override
    public void onWallpaperChanged() {
        refreshTimestamp();
    }

    @Override
    public void onOnlineWallpaperChanged() {
        refreshTimestamp();
    }
}
