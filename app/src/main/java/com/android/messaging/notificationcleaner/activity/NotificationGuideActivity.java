package com.android.messaging.notificationcleaner.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.NotificationBarUtil;
import com.android.messaging.notificationcleaner.NotificationCleanerConstants;
import com.android.messaging.notificationcleaner.NotificationCleanerUtil;
import com.android.messaging.notificationcleaner.data.NotificationCleanerProvider;
import com.android.messaging.notificationcleaner.views.AnimatedNotificationView;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.Navigations;
import com.superapps.util.Permissions;

public class NotificationGuideActivity extends HSAppCompatActivity {
    public static final String EXTRA_IS_MAIN_PAGE_GUIDE = "EXTRA_IS_VIEW_STYLE";

    private String from;
    private boolean mIsMainPageGuide = false;
    private boolean isMainPageGuideJustCreated = false;

    private AnimatedNotificationView mAnimatedNotificationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_cleaner_guide);

        mIsMainPageGuide = getIntent() != null && getIntent().getBooleanExtra(EXTRA_IS_MAIN_PAGE_GUIDE, false);
        if (mIsMainPageGuide) {
            isMainPageGuideJustCreated = true;
        }

        from = getIntent() != null ? getIntent().getStringExtra(NotificationCleanerConstants.EXTRA_START_FROM) : "";
        mAnimatedNotificationView = findViewById(R.id.guide_container_view);
        mAnimatedNotificationView.setIsMainPageGuide(mIsMainPageGuide);
        mAnimatedNotificationView.startAnimations();

        BugleAnalytics.logEvent("NotificationCleaner_Guide_Show", true);

        NotificationCleanerUtil.insertFakeNotificationsIfNeeded();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Permissions.isNotificationAccessGranted()
                || !NotificationCleanerProvider.isNotificationOrganizerSwitchOn()
                || (mIsMainPageGuide && isMainPageGuideJustCreated)) {
            isMainPageGuideJustCreated = false;
            return;
        }

        NotificationBarUtil.checkToUpdateBlockedNotification();
        Intent intentBlocked = new Intent(NotificationGuideActivity.this, NotificationBlockedActivity.class);
        intentBlocked.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentBlocked.putExtra(NotificationCleanerConstants.EXTRA_START_FROM, from);
        Navigations.startActivitySafely(this, intentBlocked);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (mAnimatedNotificationView != null) {
            mAnimatedNotificationView.stopButtonFlash();
        }
    }
}
