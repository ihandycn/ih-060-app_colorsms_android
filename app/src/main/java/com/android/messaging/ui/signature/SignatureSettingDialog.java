package com.android.messaging.ui.signature;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.Preferences;

public class SignatureSettingDialog extends TextSettingDialog {

    public static final String PREF_KEY_SIGNATURE_CONTENT = "pref_key_signature_content";

    @Override
    public void onSave(String text) {
        Preferences.getDefault().putString(PREF_KEY_SIGNATURE_CONTENT, text);

        boolean hasEmoji = false;
        for (String s : super.mInputEmojiSet) {
            if (text.contains(s)) {
                hasEmoji = true;
                break;
            }
        }
        BugleAnalytics.logEvent("SMS_Signature_Change", true, "with_emoji", String.valueOf(hasEmoji));
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
