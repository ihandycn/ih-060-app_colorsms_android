package com.android.messaging.backup.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.backup.BackupManager;
import com.android.messaging.ui.BaseDialogFragment;

public class MessageFreeUpProcessDialog extends BaseDialogFragment {
    @Override
    protected CharSequence getTitle() {
        return null;
    }

    @Override
    protected CharSequence getMessages() {
        return null;
    }

    @Override
    protected CharSequence getNegativeButtonText() {
        return null;
    }

    @Override
    protected CharSequence getPositiveButtonText() {
        return null;
    }

    @Override
    protected View getContentView() {
        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.message_free_up_process_dialog, null);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        BackupManager.getInstance().deleteLocalMessages(30, new BackupManager.MessageDeleteListener() {
            @Override
            public void onDeleteStart() {

            }

            @Override
            public void onScanFinished(int deleteCount) {

            }

            @Override
            public void onDeleteUpdate(int deletedCount) {

            }

            @Override
            public void onDeleteFinished() {

            }
        });
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }
}
