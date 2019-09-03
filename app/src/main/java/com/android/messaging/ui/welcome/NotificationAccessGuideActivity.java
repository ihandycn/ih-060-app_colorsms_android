package com.android.messaging.ui.welcome;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.NotificationCleanerTest;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.dialog.NotificationAccessDialogActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Permissions;
import com.superapps.util.Threads;

public class NotificationAccessGuideActivity extends AppCompatActivity {
    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    public static final int MSG_WHAT_NOTIFICATION_LISTENING_CHECK = 100;
    public static final int MSG_WHAT_NOTIFICATION_LISTENING_CANCEL = 101;
    public static final int INTERVAL_PERMISSION_CHECK = 1000;
    public static final int DELAY_START_TO_PERMISSION_CHECK = 1000;
    public static final int DURATION_PERMISSION_CHECK_CONTINUED = 120000;
    private boolean mIsBackPressedFromNotificationAccessSettings;
    protected final Handler mHandler = new Handler();

    @SuppressLint("HandlerLeak") // This mCheckPermissionHandler holds activity reference for no longer than 120s
    private Handler mCheckPermissionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_NOTIFICATION_LISTENING_CHECK:
                    if (!Permissions.isNotificationAccessGranted()) {
                        sendEmptyMessageDelayed(MSG_WHAT_NOTIFICATION_LISTENING_CHECK, INTERVAL_PERMISSION_CHECK);
                        break;
                    }
                    BugleAnalytics.logEvent("Start_NA_SetSuccess", true);
                    BugleFirebaseAnalytics.logEvent("SMS_NotificationAccess", "NA_setsuccess", "true");
                    NotificationCleanerTest.logNotificationAccessGrant();
                    Intent intentSelf = new Intent(HSApplication.getContext(), NotificationAccessGuideActivity.class);
                    intentSelf.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    HSApplication.getContext().startActivity(intentSelf);
                    break;
                case MSG_WHAT_NOTIFICATION_LISTENING_CANCEL:
                    removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CHECK);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_guide);
        TextView skipButton = findViewById(R.id.skip_button);
        skipButton.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE,
                getResources().getColor(R.color.notification_guide_skip_button_color),
                Dimensions.pxFromDp(14), false, true));

        TextView confirmButton = findViewById(R.id.notification_guide_confirm_button);
        confirmButton.setBackground(BackgroundDrawables.
                createBackgroundDrawable(getResources().getColor(R.color.notification_guide_confirm_button_color),
                        Dimensions.pxFromDp(6.7f), true));

        TextView guideTitle = findViewById(R.id.notification_guide_title);
        TextView guideContent = findViewById(R.id.notification_guide_content);
        ImageView guideBackground = findViewById(R.id.notification_guide_background);

        Interpolator interpolator = PathInterpolatorCompat.create(0.17f, 0.17f, 0.6f, 1);

        ObjectAnimator titleAlphaAnimator = ObjectAnimator.ofFloat(guideTitle, "alpha", 0, 1);
        titleAlphaAnimator.setDuration(320);
        titleAlphaAnimator.setStartDelay(160);

        ObjectAnimator titleTranslationAnimator = ObjectAnimator.ofFloat(guideTitle, "translationY", Dimensions.pxFromDp(-47), 0);
        titleTranslationAnimator.setDuration(400);
        titleTranslationAnimator.setStartDelay(160);
        titleTranslationAnimator.setInterpolator(interpolator);

        ObjectAnimator contentAlphaAnimator = ObjectAnimator.ofFloat(guideContent, "alpha", 0, 1);
        contentAlphaAnimator.setDuration(320);
        contentAlphaAnimator.setStartDelay(120);

        ObjectAnimator contentTranslationAnimator = ObjectAnimator.ofFloat(guideContent, "translationY", Dimensions.pxFromDp(-47), 0);
        contentTranslationAnimator.setDuration(400);
        contentTranslationAnimator.setStartDelay(120);
        contentTranslationAnimator.setInterpolator(interpolator);

        titleAlphaAnimator.start();
        titleTranslationAnimator.start();
        contentAlphaAnimator.start();
        contentTranslationAnimator.start();
        Threads.postOnMainThreadDelayed(() -> {
            guideBackground.setVisibility(View.VISIBLE);
            skipButton.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.VISIBLE);
        }, 160);

        skipButton.setOnClickListener(v -> {
            BugleAnalytics.logEvent("Start_NApage_SKIP", true, "Type", "skip");
            UIIntents.get().launchConversationListActivity(NotificationAccessGuideActivity.this);
            finish();
        });

        confirmButton.setOnClickListener(v -> {
            BugleAnalytics.logEvent("Start_NApage_Click", true);
            BugleFirebaseAnalytics.logEvent("SMS_NotificationAccess", "NApage_click", "true");
            Intent intent = new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
            mHandler.postDelayed(() -> Navigations.startActivity(this, NotificationAccessDialogActivity.class), 800);
            BugleAnalytics.logEvent("Start_NAguide_Show", true);
            BugleFirebaseAnalytics.logEvent("SMS_NotificationAccess", "NAguide_show", "true");
            mIsBackPressedFromNotificationAccessSettings = true;
            mCheckPermissionHandler.removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CHECK);
            mCheckPermissionHandler.sendEmptyMessageDelayed(MSG_WHAT_NOTIFICATION_LISTENING_CHECK, DELAY_START_TO_PERMISSION_CHECK);
            mCheckPermissionHandler.sendEmptyMessageDelayed(MSG_WHAT_NOTIFICATION_LISTENING_CANCEL, DURATION_PERMISSION_CHECK_CONTINUED);
        });
        BugleAnalytics.logEvent("Start_NApage_Show", true);
        BugleFirebaseAnalytics.logEvent("SMS_NotificationAccess", "NApage_show", "true");
    }

    @Override
    public void onBackPressed() {
        BugleAnalytics.logEvent("Start_NApage_SKIP", true, "Type", "back");
        UIIntents.get().launchConversationListActivity(NotificationAccessGuideActivity.this);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Permissions.isNotificationAccessGranted() && !mIsBackPressedFromNotificationAccessSettings) {
            return;
        }
        Intent intent = new Intent(NotificationAccessGuideActivity.this, ConversationListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCheckPermissionHandler.removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CANCEL);
        mCheckPermissionHandler.removeMessages(MSG_WHAT_NOTIFICATION_LISTENING_CHECK);
    }
}
