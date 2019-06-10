package com.android.messaging.backup.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.BaseDialogFragment;
import com.ihs.app.framework.HSApplication;

public class BackupTipsDialog extends BaseDialogFragment {

    @Override
    protected CharSequence getTitle() {
        SpannableString ss = new SpannableString(HSApplication.getContext().getString(R.string.tips));
        Drawable drawable = getResources().getDrawable(R.drawable.ic_tips);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        ss.setSpan(new ImageSpan(drawable,ImageSpan.ALIGN_BASELINE),
                ss.length() - 1, ss.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return ss;
    }

    @Override
    protected CharSequence getMessages() {
        return HSApplication.getContext().getString(R.string.backup_tip_dialog_content);
    }

    @Override
    protected CharSequence getNegativeButtonText() {
        return getString(R.string.restore_tab).toUpperCase();
    }

    @Override
    protected CharSequence getPositiveButtonText() {
        return getString(R.string.backup_tab).toUpperCase();
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

    @Override
    protected void onContentViewAdded() {
        super.onContentViewAdded();
        removeDialogContentHorizontalMargin();
    }
}
