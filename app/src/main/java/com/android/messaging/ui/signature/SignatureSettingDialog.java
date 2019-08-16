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

    public static final String PREF_KEY_SIGNATURE_CONTENT = "pref_key_signature_content";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BugleAnalytics.logEvent("SMS_Signature_Show", true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSave(String text) {

        Preferences.getDefault().putString(PREF_KEY_SIGNATURE_CONTENT, text);
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
        return Preferences.getDefault().getString(PREF_KEY_SIGNATURE_CONTENT, null);
    }

    @Override
    public String getTitle() {
        return getActivity().getResources().getString(R.string.signature);
    }
}
