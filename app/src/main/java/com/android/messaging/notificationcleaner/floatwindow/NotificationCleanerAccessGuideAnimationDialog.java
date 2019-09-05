package com.android.messaging.notificationcleaner.floatwindow;

import android.app.Activity;
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
        findViewById(R.id.permission_guide_content_subtitle).setVisibility(VISIBLE);
    }

    @Override
    protected String getTitle() {
        return getResources().getString(R.string.app_name);
    }

    @Override
    protected String getContent() {
        return getResources().getString(R.string.function_off);
    }

    @Override
    protected String getDescription() {
        return String.format(getResources().getString(R.string.permission_guide_description_accessibility), getResources().getString(R.string.app_name));
    }

    @Override
    protected void onActionButtonClick(View v) {
        ((Activity)getContext()).finish();
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_OUTSIDE:
                ((Activity)getContext()).finish();
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
