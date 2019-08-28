package com.android.messaging.notificationcleaner.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.NotificationBarUtil;
import com.android.messaging.notificationcleaner.NotificationCleanerConstants;
import com.android.messaging.notificationcleaner.NotificationCleanerUtil;
import com.android.messaging.notificationcleaner.data.NotificationCleanerProvider;
import com.android.messaging.notificationcleaner.views.AnimatedNotificationView;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Permissions;

public class NotificationGuideActivity extends HSAppCompatActivity {
    public static final String START_FROM = "start_from";
    public static final String START_FROM_FULL_GUIDE = "full_guide";
    public static final String START_FROM_BAR_GUIDE = "bar_guide";
    public static final String START_FROM_NO_PERMISSION = "no_permission";

    private String mFrom;
    private boolean mIsFromGuide = false;
    private boolean mIsMainPageGuideJustCreated = false;

    private AnimatedNotificationView mAnimatedNotificationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_cleaner_guide);

        mFrom =  getIntent().getStringExtra(START_FROM);
        mIsFromGuide = START_FROM_FULL_GUIDE.equals(mFrom) || START_FROM_BAR_GUIDE.equals(mFrom);
        if (mIsFromGuide) {
            mIsMainPageGuideJustCreated = true;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        ((FrameLayout.LayoutParams) toolbar.getLayoutParams()).topMargin
                = Dimensions.getStatusBarHeight(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!mIsFromGuide);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(!mIsFromGuide);

        View exitContainer = findViewById(R.id.exit);
        exitContainer.setVisibility(mIsFromGuide ? View.VISIBLE : View.GONE);
        if (mIsFromGuide) {
            exitContainer.setBackground(BackgroundDrawables.createTransparentBackgroundDrawable(
                    getResources().getColor(R.color.ripples_ripple_color), Dimensions.pxFromDp(24)));
            exitContainer.setOnClickListener(v -> finish());
        }

        mAnimatedNotificationView = findViewById(R.id.guide_container_view);
        mAnimatedNotificationView.setIsMainPageGuide(mIsFromGuide);
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
                || (mIsFromGuide && mIsMainPageGuideJustCreated)) {
            mIsMainPageGuideJustCreated = false;
            return;
        }

        NotificationBarUtil.checkToUpdateBlockedNotification();
        Intent intentBlocked = new Intent(NotificationGuideActivity.this, NotificationBlockedActivity.class);
        intentBlocked.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentBlocked.putExtra(NotificationBlockedActivity.START_FROM, mFrom);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAnimatedNotificationView != null) {
            mAnimatedNotificationView.stopButtonFlash();
        }
    }
}
