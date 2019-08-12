package com.android.messaging.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.android.messaging.R;

public class NotificationAccessGuideAnimationDialog extends AnimationPermissionGuideDialog {

    private Context mContext;
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
        if (mContext != null) {
            if (mContext instanceof NotificationAccessDialogActivity){
                ((NotificationAccessDialogActivity) mContext).finish();
            }
        }
    }

    @Override
    protected void onBackClick() {

    }

    @Override
    protected AnimationPermissionGuideDialog.AnimationType getAnimationType() {
        return AnimationType.SingleLayer;
    }

    public NotificationAccessGuideAnimationDialog(final Context context) {
        this(context, null);
    }

    public NotificationAccessGuideAnimationDialog(final Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public NotificationAccessGuideAnimationDialog(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setHost (Context context) {
        mContext = context;
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
