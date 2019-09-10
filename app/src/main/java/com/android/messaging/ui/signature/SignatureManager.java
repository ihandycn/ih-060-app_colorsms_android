package com.android.messaging.ui.signature;

import android.text.TextUtils;

import com.superapps.util.Preferences;

public class SignatureManager {
    public static final String PREF_KEY_SIGNATURE_CONTENT = "pref_key_signature_content";

    public static String getSignatureString(String conversationId) {
        if (!TextUtils.isEmpty(conversationId)) {
            if (Preferences.getDefault().contains(PREF_KEY_SIGNATURE_CONTENT + "_" + conversationId)) {
                return Preferences.getDefault().getString(PREF_KEY_SIGNATURE_CONTENT + "_" + conversationId, null);
            }
        }
        return Preferences.getDefault().getString(PREF_KEY_SIGNATURE_CONTENT , null);
    }

    public static void setSignature(String signature, String conversationId) {
        Preferences.getDefault().putString(PREF_KEY_SIGNATURE_CONTENT
                + (TextUtils.isEmpty(conversationId) ? "" : "_" + conversationId), signature);
    }

    public static String getConversationSignature(String conversationId) {
        if (!TextUtils.isEmpty(conversationId)) {
            if (Preferences.getDefault().contains(PREF_KEY_SIGNATURE_CONTENT + "_" + conversationId)) {
                return Preferences.getDefault().getString(PREF_KEY_SIGNATURE_CONTENT + "_" + conversationId, null);
            }
        }
        return null;
    }
}
