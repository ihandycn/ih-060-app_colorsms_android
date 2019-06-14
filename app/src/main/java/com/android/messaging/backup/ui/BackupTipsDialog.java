package com.android.messaging.backup.ui;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;
import com.ihs.app.framework.HSApplication;

public class BackupTipsDialog extends BaseDialogFragment {

    @Override
    protected CharSequence getTitle() {
        return HSApplication.getContext().getString(R.string.tips);
    }

    @Override
    protected CharSequence getMessages() {
        return HSApplication.getContext().getString(R.string.backup_tip_dialog_content);
    }

    @Override
    protected CharSequence getNegativeButtonText() {
        return getString(R.string.restore_verb).toUpperCase();
    }

    @Override
    protected CharSequence getPositiveButtonText() {
        return getString(R.string.backup_verb).toUpperCase();
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }
}
