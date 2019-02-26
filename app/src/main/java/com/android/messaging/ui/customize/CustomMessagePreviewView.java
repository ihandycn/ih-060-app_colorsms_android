package com.android.messaging.ui.customize;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.ConversationDrawables;

public class CustomMessagePreviewView extends ConstraintLayout {

    private int mIncomingBackgroundPreviewColor;
    private int mOutgoingBackgroundPreviewColor;
    private int mIncomingTextPreviewColor;
    private int mOutgoingTextPreviewColor;

    private int mPreviewBubbleDrawableId;

    private TextView mIncomingMessage;
    private TextView mOutgoingMessage;

    public CustomMessagePreviewView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.customize_preview_view, this, true);
        mIncomingMessage = findViewById(R.id.incoming_message_preview_item);
        mOutgoingMessage = findViewById(R.id.outgoing_message_preview_item);
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

        mPreviewBubbleDrawableId = BubbleDrawables.getSelectedIndex();
    }

    public void previewCustomBubbleDrawables(int id) {
        mIncomingMessage.setBackgroundResource(BubbleDrawables.getSelectedDrawable(id, true));
        mOutgoingMessage.setBackgroundResource(BubbleDrawables.getSelectedDrawable(id, false));

        mIncomingMessage.getBackground().setColorFilter(mIncomingBackgroundPreviewColor, PorterDuff.Mode.SRC_ATOP);
        mOutgoingMessage.getBackground().setColorFilter(mOutgoingBackgroundPreviewColor, PorterDuff.Mode.SRC_ATOP);

        mPreviewBubbleDrawableId = id;
    }

    public void previewCustomBubbleBackgroundColor(boolean incoming, @ColorInt int color) {
        if (incoming) {
            mIncomingMessage.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } else {
            mOutgoingMessage.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
        if (incoming) {
            mIncomingBackgroundPreviewColor = color;
        } else {
            mOutgoingBackgroundPreviewColor = color;
        }
    }

    public void previewCustomTextColor(boolean incoming, @ColorInt int color) {
        if (incoming) {
            mIncomingMessage.setTextColor(color);
        } else {
            mOutgoingMessage.setTextColor(color);
        }
    }

    public void save(String conversationId) {
        ConversationColors.get().setBubbleBackgroundColor(true, mIncomingBackgroundPreviewColor);
        ConversationColors.get().setBubbleBackgroundColor(false, mOutgoingBackgroundPreviewColor);

        ConversationColors.get().setMessageTextColor(true, mIncomingTextPreviewColor);
        ConversationColors.get().setMessageTextColor(false, mOutgoingTextPreviewColor);

        BubbleDrawables.setSelectedIndex(mPreviewBubbleDrawableId);
    }
}
