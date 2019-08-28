package com.android.messaging.notificationcleaner.floatwindow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.ui.dialog.AnimationPermissionGuideDialog;

public class NotificationCleanerAccessGuideAnimationDialog extends AnimationPermissionGuideDialog {

    public NotificationCleanerAccessGuideAnimationDialog(final Context context) {
        super(context);
    }

    public NotificationCleanerAccessGuideAnimationDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected String getTitle() {
        return getResources().getString(R.string.app_name);
    }

    @Override
    protected String getContent() {
        return getResources().getString(R.string.off);
    }

    @Override
    protected String getDescription() {
        return String.format(getResources().getString(R.string.notification_cleaner_permission_guide_title),
                getResources().getString(R.string.app_name));
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected boolean shouldDismissCompletelyOnTouch() {
        return false;
    }

    @Override
    protected boolean isShowConfirmDialog() {
        return true;
    }
}
