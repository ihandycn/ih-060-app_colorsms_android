package com.android.messaging.ui.customize;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.ConversationDrawables;

import org.qcode.fontchange.impl.FontManagerImpl;

public class CustomizePreviewView extends ConstraintLayout {

    private TextView mIncomingMessage;
    private TextView mOutgoingMessage;

    public CustomizePreviewView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.customize_preview_view, this, true);
        mIncomingMessage = findViewById(R.id.incoming_message_preview_item);
        mOutgoingMessage = findViewById(R.id.outgoing_message_preview_item);
        updateBubbleDrawables();
    }

    public void updateBubbleDrawables() {
        mIncomingMessage.setBackground(
                ConversationDrawables.get().getBubbleDrawable(false, true, true, false));
        mOutgoingMessage.setBackground(
                ConversationDrawables.get().getBubbleDrawable(false, false, true, false));
    }
}
