package com.android.messaging.backup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;

public class MessageFreeUpDialog extends BaseDialogFragment {
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
        return getString(R.string.message_free_up_button);
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
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private View createBodyView() {
        View mContentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.message_free_up_tip_dialog, null);
        ImageView freeUpCloseButton = mContentView.findViewById(R.id.message_free_up_close);
        freeUpCloseButton.setOnClickListener(v -> {
            dismissAllowingStateLoss();
        });
        return mContentView;
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }
}
