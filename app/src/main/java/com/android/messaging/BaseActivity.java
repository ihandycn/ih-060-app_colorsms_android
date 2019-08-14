package com.android.messaging;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.welcome.WelcomeSetAsDefaultActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.DefaultSMSUtils;
import com.android.messaging.util.NotificationAccessAutopilotUtils;
import com.android.messaging.util.TransitionUtils;
import com.ihs.commons.utils.HSLog;

import static com.android.messaging.datamodel.NotificationServiceV18.EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION;
import static com.android.messaging.datamodel.NotificationServiceV18.EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION_ACTION;
import static com.android.messaging.ui.UIIntents.UI_INTENT_EXTRA_NOTIFICATION_TO_CONVERSATION;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private boolean mJustCreated = true;
    protected boolean mShouldFinishThisTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!DefaultSMSUtils.isDefaultSmsApp()) {
            if (getIntent() != null && getIntent().getBooleanExtra(UI_INTENT_EXTRA_NOTIFICATION_TO_CONVERSATION, false)) {
                HSLog.d("NotificationListener", "UI_INTENT_EXTRA_NOTIFICATION_TO_CONVERSATION true");
                NotificationAccessAutopilotUtils.logNotificationClicked();
                BugleAnalytics.logEvent("Notification_Clicked_NA", true);
                final Intent intent = new Intent(this, WelcomeSetAsDefaultActivity.class);
                intent.putExtra(UI_INTENT_EXTRA_NOTIFICATION_TO_CONVERSATION, true);
                startActivity(intent, TransitionUtils.getTransitionInBundle(this));
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
            } else if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION, false)){
                HSLog.d("NotificationListener", "EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION true");
                NotificationAccessAutopilotUtils.logNotificationClicked();
                BugleAnalytics.logEvent("Notification_Clicked_NA", true);
                final Intent intent = new Intent(this, WelcomeSetAsDefaultActivity.class);
                intent.putExtra(EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION, true);
                startActivity(intent, TransitionUtils.getTransitionInBundle(this));
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                finish();
            } else {
                HSLog.d("NotificationListener", "UI_INTENT_EXTRA_NOTIFICATION_TO_CONVERSATION false");
                UIIntents.get().launchWelcomeSetAsDefaultActivity(this);
                finish();
            }

            if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION_ACTION, false)) {
                NotificationAccessAutopilotUtils.logNotificationReplied();
                BugleAnalytics.logEvent("Notification_Replied_NA", true);
            }
            mShouldFinishThisTime = true;
            HSLog.d(TAG, "Show welcome set as default");
        }
    }

    @Override protected void onResume() {
        super.onResume();

        if (!mJustCreated) {
            if (!DefaultSMSUtils.isDefaultSmsApp()) {
                if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_FROM_OVERRIDE_SYSTEM_SMS_NOTIFICATION, false)) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                }
                UIIntents.get().launchWelcomeSetAsDefaultActivity(this);
                finish();
                HSLog.d(TAG, "Show welcome set as default");
            }
        }

        mJustCreated = false;
    }

    @Override public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_null, R.anim.slide_out_to_right_and_fade);
    }

    public void finishWithoutOverridePendingTransition() {
        super.finish();
    }
}
