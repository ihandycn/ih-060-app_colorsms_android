package com.android.messaging.ui.dialog;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

import com.android.messaging.R;

public class FiveStarRateGuideDialog extends BasePermissionGuideDialog {
    private View content;

    public FiveStarRateGuideDialog(Context context) {
        super(context);
        View.inflate(context, R.layout.five_star_rate_guide, this);
    }

    @Override
    protected boolean shouldDismissCompletelyOnTouch() {
        return false;
    }

    @Override
    public void onAddedToWindow() {
        setVisibility(VISIBLE);
        postDelayed(() -> {
            setVisibility(VISIBLE);
            content = findViewById(R.id.permission_guide_content);
            content.setAlpha(1);
        }, mIsShowImmediately ? 0 : ESTIMATED_ACTIVITY_SWITCH_TIME);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (content != null && content.getAlpha() > 0){
                PermissionGuideManager.getInstance().removePermissionGuide(true);
            }else {
                PermissionGuideManager.getInstance().removePermissionGuide(false);
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
