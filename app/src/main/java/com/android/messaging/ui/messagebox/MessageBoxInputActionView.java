package com.android.messaging.ui.messagebox;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.PlainTextEditText;
import com.android.messaging.ui.conversation.SimIconView;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.signature.SignatureSettingDialog;
import com.superapps.font.FontUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

class MessageBoxInputActionView extends LinearLayout {

    private ImageView mSelfSendIcon;
    private MessageBoxActivity mHost;
    private PlainTextEditText mComposeEditText;
    private ImageView mEmojiIcon;
    private ProgressBar mProgressBar;

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
        mComposeEditText.setTypeface(FontUtils.getTypeface());

        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(PrimaryColors.getPrimaryColor(), PorterDuff.Mode.SRC_IN);
        mEmojiIcon = findViewById(R.id.emoji_btn);
        mComposeEditText.requestFocus();

        ForegroundColorSpan signatureSpan = new ForegroundColorSpan(0xb3222327);
        String signature = Preferences.getDefault().getString(SignatureSettingDialog.PREF_KEY_SIGNATURE_CONTENT, null);
        if (!TextUtils.isEmpty(signature)) {
            SpannableString sb = new SpannableString("\n" + signature);
            sb.setSpan(signatureSpan, 1, sb.length(), 0);
            sb.setSpan(new AbsoluteSizeSpan(13, true), 1, sb.length(), 0);
            mComposeEditText.setText(sb, TextView.BufferType.SPANNABLE);
            mComposeEditText.setSelection(0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mComposeEditText.setShowSoftInputOnFocus(false);
        }

    }

    void performReply() {
        mProgressBar.setVisibility(VISIBLE);
        mSelfSendIcon.setVisibility(GONE);
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