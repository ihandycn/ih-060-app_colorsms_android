package com.android.messaging.backup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;

public class CloudBackUpProcessDialog extends BaseDialogFragment {
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
        return createBodyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View createBodyView() {
        View mContentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.cloud_backup_process_dialog, null);
        return mContentView;
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }
}
