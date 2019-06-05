package com.android.messaging.backup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;

public class BackUpTipsDialog extends BaseDialogFragment {

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
        return getString(R.string.restore_button_text);
    }

    @Override
    protected CharSequence getPositiveButtonText() {
        return getString(R.string.backup_button_text);
    }
    @Override
    protected View getContentView() {
        return createBodyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        setOnPositiveButtonClickListener(v -> {
            dismissAllowingStateLoss();
        });
        setOnNegativeButtonClickListener(v -> {
            dismissAllowingStateLoss();
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private View createBodyView() {
        View mContentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.backup_tip_dialog, null);
        return mContentView;
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }
}
