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

    private int mIncomingPreviewColor;
    private int mOutgoingPreviewColor;

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

        mIncomingPreviewColor = ConversationColors.get().getBubbleBackgroundColor(true);
        mOutgoingPreviewColor = ConversationColors.get().getBubbleBackgroundColor(false);
    }

    public void previewCustomBubbleDrawables(int id) {
        int drawableResId = BubbleDrawables.getSelectedDrawable(id);
        mIncomingMessage.setBackgroundResource(drawableResId);
        mOutgoingMessage.setBackgroundResource(drawableResId);

        mIncomingMessage.getBackground().setColorFilter(mIncomingPreviewColor, PorterDuff.Mode.SRC_ATOP);
        mOutgoingMessage.getBackground().setColorFilter(mOutgoingPreviewColor, PorterDuff.Mode.SRC_ATOP);
    }

    public void previewCustomBubbleBackgroundColor(boolean incoming, @ColorInt int color) {
        if (incoming) {
            mIncomingMessage.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } else {
            mOutgoingMessage.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
        if (incoming) {
            mIncomingPreviewColor = color;
        } else {
            mOutgoingPreviewColor = color;
        }
    }

    public void previewCustomTextColor(boolean incoming, @ColorInt int color) {
        if (incoming) {
            mIncomingMessage.setTextColor(color);
        } else {
            mOutgoingMessage.setTextColor(color);
        }
    }
}
