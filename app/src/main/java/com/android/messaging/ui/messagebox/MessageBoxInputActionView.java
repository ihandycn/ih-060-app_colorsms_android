package com.android.messaging.ui.messagebox;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.messaging.R;
import com.android.messaging.ui.PlainTextEditText;
import com.android.messaging.ui.conversation.SimIconView;
import com.android.messaging.ui.customize.PrimaryColors;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

class MessageBoxInputActionView extends LinearLayout {

    private ImageView mSelfSendIcon;
    private MessageBoxActivity mHost;
    private PlainTextEditText mComposeEditText;
    private ImageView mEmojiIcon;

    public MessageBoxInputActionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);

        mHost = (MessageBoxActivity) context;
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.message_box_input_action_layout, this, true);

        mSelfSendIcon = findViewById(R.id.self_send_icon);
        mSelfSendIcon.setImageResource(R.drawable.input_send_message_icon);
        mSelfSendIcon.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                PrimaryColors.getPrimaryColorDark(),
                Dimensions.pxFromDp(20), false, true));
        mSelfSendIcon.setOnClickListener(mHost);

        mComposeEditText = findViewById(R.id.compose_message_text);
        mComposeEditText.requestFocus();

        mEmojiIcon = findViewById(R.id.emoji_btn);
    }

    String getMessage() {
        Editable editable = mComposeEditText.getText();
        return editable != null ? editable.toString() : null;
    }

    public PlainTextEditText getComposeEditText() {
        return mComposeEditText;
    }

    public ImageView getEmojiIcon() {
        return mEmojiIcon;
    }
}
