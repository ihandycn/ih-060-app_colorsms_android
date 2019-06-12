package com.android.messaging.backup.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

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

    private View createBodyView() {
        View mContentView = LayoutInflater.from(getActivity()).inflate(R.layout.message_free_up_tip_dialog, null);
        ImageView freeUpCloseButton = mContentView.findViewById(R.id.message_free_up_close);
        freeUpCloseButton.setBackground(BackgroundDrawables.createTransparentBackgroundDrawable(
                0x33000000, Dimensions.pxFromDp(14)));
        freeUpCloseButton.setOnClickListener(v -> {
            dismissAllowingStateLoss();
            BugleAnalytics.logEvent("Backup_Freeupmsg_Alert_Close");
        });
        return mContentView;
    }

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
        removeDialogContentVerticalMargin();
    }
}
