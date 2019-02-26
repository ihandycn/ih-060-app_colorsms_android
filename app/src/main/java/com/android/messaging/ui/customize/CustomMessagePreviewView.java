package com.android.messaging.ui.customize;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.ConversationDrawables;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class CustomMessagePreviewView extends ConstraintLayout {

    private int mIncomingBackgroundPreviewColor;
    private int mOutgoingBackgroundPreviewColor;
    private int mIncomingTextPreviewColor;
    private int mOutgoingTextPreviewColor;

    private int mPreviewBubbleDrawableIdentifier;

    private TextView mIncomingMessage;
    private TextView mOutgoingMessage;

    public CustomMessagePreviewView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.customize_preview_view, this, true);
        mIncomingMessage = findViewById(R.id.incoming_message_preview_item);
        mOutgoingMessage = findViewById(R.id.outgoing_message_preview_item);
        findViewById(R.id.message_preview_timestamp_1).setBackground(BackgroundDrawables.createBackgroundDrawable(
                getResources().getColor(R.color.white_40_transparent), Dimensions.pxFromDp(16), false
        ));
        findViewById(R.id.message_preview_timestamp_2).setBackground(BackgroundDrawables.createBackgroundDrawable(
                getResources().getColor(R.color.white_40_transparent), Dimensions.pxFromDp(16), false
        ));
        initBubbleDrawables();
    }

    private void initBubbleDrawables() {
        mIncomingMessage.setBackground(
                ConversationDrawables.get().getBubbleDrawable(false, true, true, false));
        mOutgoingMessage.setBackground(
                ConversationDrawables.get().getBubbleDrawable(false, false, true, false));

        mIncomingBackgroundPreviewColor = ConversationColors.get().getBubbleBackgroundColor(true);
        mOutgoingBackgroundPreviewColor = ConversationColors.get().getBubbleBackgroundColor(false);

        mIncomingTextPreviewColor = ConversationColors.get().getMessageTextColor(true);
        mOutgoingTextPreviewColor = ConversationColors.get().getMessageTextColor(false);

        mIncomingMessage.setTextColor(mIncomingTextPreviewColor);
        mOutgoingMessage.setTextColor(mOutgoingTextPreviewColor);

        mPreviewBubbleDrawableIdentifier = BubbleDrawables.getSelectedIndex();
    }

    public void previewCustomBubbleDrawables(int id) {
        mIncomingMessage.setBackgroundResource(BubbleDrawables.getSelectedDrawable(id, true));
        mOutgoingMessage.setBackgroundResource(BubbleDrawables.getSelectedDrawable(id, false));

        mIncomingMessage.getBackground().setColorFilter(mIncomingBackgroundPreviewColor, PorterDuff.Mode.SRC_ATOP);
        mOutgoingMessage.getBackground().setColorFilter(mOutgoingBackgroundPreviewColor, PorterDuff.Mode.SRC_ATOP);

        mPreviewBubbleDrawableIdentifier = id;
    }

    public void previewCustomBubbleBackgroundColor(boolean incoming, @ColorInt int color) {
        if (incoming) {
            mIncomingMessage.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            mIncomingBackgroundPreviewColor = color;
        } else {
            mOutgoingMessage.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            mOutgoingBackgroundPreviewColor = color;
        }
    }

    public void previewCustomTextColor(boolean incoming, @ColorInt int color) {
        if (incoming) {
            mIncomingMessage.setTextColor(color);
            mIncomingTextPreviewColor = color;
        } else {
            mOutgoingMessage.setTextColor(color);
            mOutgoingTextPreviewColor = color;
        }
    }

    public void save(String conversationId) {
        boolean bubbleDrawableChanged = false;
        boolean bubbleBackgroundColorChanged = false;
        boolean bubbleTextColorChanged = false;

        if (mPreviewBubbleDrawableIdentifier != BubbleDrawables.getSelectedIndex()) {
            BubbleDrawables.setSelectedIndex(mPreviewBubbleDrawableIdentifier);
            bubbleDrawableChanged = true;
            BugleAnalytics.logEvent("Customize_Bubble_Style_Change", "style",
                    String.valueOf(mPreviewBubbleDrawableIdentifier));
        }

        if (mIncomingBackgroundPreviewColor != ConversationColors.get().getBubbleBackgroundColor(true)) {
            ConversationColors.get().setBubbleBackgroundColor(true, mIncomingBackgroundPreviewColor);
            bubbleBackgroundColorChanged = true;
        }

        if (mOutgoingBackgroundPreviewColor != ConversationColors.get().getBubbleBackgroundColor(false)) {
            ConversationColors.get().setBubbleBackgroundColor(false, mOutgoingBackgroundPreviewColor);
            bubbleBackgroundColorChanged = true;
        }

        if (mIncomingTextPreviewColor != ConversationColors.get().getMessageTextColor(true)) {
            ConversationColors.get().setMessageTextColor(true, mIncomingTextPreviewColor);
            bubbleTextColorChanged = true;
        }

        if (mOutgoingTextPreviewColor != ConversationColors.get().getMessageTextColor(false)) {
            ConversationColors.get().setMessageTextColor(false, mOutgoingTextPreviewColor);
            bubbleTextColorChanged = true;
        }

        String from = TextUtils.isEmpty(conversationId) ? "settings" : "chat";

        BugleAnalytics.logEvent("Customize_Bubble_Change", "from", from, "type",
                getBubbleChangeString(bubbleDrawableChanged, bubbleBackgroundColorChanged || bubbleTextColorChanged));

        if (bubbleBackgroundColorChanged || bubbleTextColorChanged) {
            BugleAnalytics.logEvent("Customize_Bubble_Color_Change", "type",
                    getBubbleColorChangeString(bubbleBackgroundColorChanged, bubbleTextColorChanged));
        }
    }

    private String getBubbleChangeString(boolean drawableChanged, boolean colorChanged) {
        if (drawableChanged && colorChanged ) {
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

}
