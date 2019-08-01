package com.android.messaging.ui.dialog;

import android.content.Context;
import android.view.View;

import com.android.messaging.R;

public class NotificationAccessGuideAnimationDialog extends AnimationPermissionGuideDialog {
    @Override
    protected String getTitle() {
        return getResources().getString(R.string.app_name);
    }

    @Override
    protected String getContent() {
        return "";
    }

    @Override
    protected String getDescription() {
        return getResources().getString(R.string.permission_guide_description);
    }

    @Override
    protected void onActionButtonClick(View v) {

    }

    @Override
    protected void onBackClick() {

    }

    @Override
    protected AnimationPermissionGuideDialog.AnimationType getAnimationType() {
        return AnimationType.SingleLayer;
    }

    public NotificationAccessGuideAnimationDialog(final Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected boolean shouldDismissCompletelyOnTouch() {
        return false;
    }

    @Override protected boolean isShowConfirmDialog() {
        return true;
    }
}
