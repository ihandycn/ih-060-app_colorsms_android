package com.android.messaging.ui.signature;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.Preferences;

public class SignatureSettingDialog extends TextSettingDialog {
    public static final String BUNDLE_KEY_CONVERSATION_ID = "conversation_id";

    private String mConversationId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(BUNDLE_KEY_CONVERSATION_ID)) {
            mConversationId = getArguments().getString(BUNDLE_KEY_CONVERSATION_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BugleAnalytics.logEvent("SMS_Signature_Show", true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSave(String text) {
        SignatureManager.setSignature(text, mConversationId);
        if (mHost != null) {
            mHost.onTextSaved(text);
        }
        dismiss();

        boolean hasEmoji = false;
        if (!TextUtils.isEmpty(text)) {
            for (String s : super.mInputEmojiSet) {
                if (text.contains(s)) {
                    hasEmoji = true;
                    break;
                }
            }
        }
        BugleAnalytics.logEvent("SMS_Signature_Change", true, "with_emoji", String.valueOf(hasEmoji));
    }

    @Override
    public void onCancel() {

    }

    @Override
    public String getDefaultText() {
        return SignatureManager.getSignatureString(mConversationId);
    }

    @Override
    public String getTitle() {
        return getActivity().getResources().getString(R.string.signature);
    }
}
