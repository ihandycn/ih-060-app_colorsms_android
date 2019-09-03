package com.android.messaging.notificationcleaner.activity;

import android.os.Bundle;
import android.view.MotionEvent;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.floatwindow.NotificationCleanerAccessGuideAnimationDialog;
import com.ihs.app.framework.activity.HSActivity;

public class NCPermissionGuideActivity extends HSActivity {
    private NotificationCleanerAccessGuideAnimationDialog mPermissionGuide;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notification_cleaner_permission_guide_layout);
        mPermissionGuide = findViewById(R.id.notification_access_guide_animation_dialog);
        mPermissionGuide.setShowContentImmediately(false);
        mPermissionGuide.onAddedToWindow();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        finish();
        return false;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPermissionGuide != null) {
            mPermissionGuide.onRemovedFromWindow();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, R.anim.app_lock_fade_out_long);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }
}
