package com.android.messaging.ui.conversationsettings;

import android.text.TextUtils;

import com.android.messaging.R;
import com.android.messaging.datamodel.action.RenameGroupAction;
import com.android.messaging.ui.signature.TextSettingDialog;

public class RenameGroupDialog extends TextSettingDialog {
    private String mDefaultText = "";
    private String mConversationId = null;

    @Override
    public void onSave(String text) {
        if (TextUtils.isEmpty(mConversationId)) {
            throw new RuntimeException();
        }
        RenameGroupAction.renameGroup(mConversationId, text);
    }

    @Override
    public String getDefaultText() {
        return mDefaultText;
    }

    @Override
    public String getTitle() {
        return getActivity().getResources().getString(R.string.action_rename_group_chat);
    }

    public void setDefaultText(String text) {
        this.mDefaultText = text;
    }

    public void setConversationId(String conversationId) {
        this.mConversationId = conversationId;
    }
}
