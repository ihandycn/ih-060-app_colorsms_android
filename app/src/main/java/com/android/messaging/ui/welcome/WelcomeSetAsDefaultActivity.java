package com.android.messaging.ui.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.ActiveNotification;
import com.android.messaging.ui.SetDefaultNotification;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.theme.ThemeSelectActivity;
import com.android.messaging.ui.emoji.EmojiStoreActivity;
import com.android.messaging.util.ActivePushAutopilotUtils;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.DefaultSMSUtils;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.SetDefaultPushAutopilotUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

public class WelcomeSetAsDefaultActivity extends AppCompatActivity {

    public static final String EXTRA_FROM_WELCOME_START = "extra_from_welcome_start";
    public static final String EXTRA_FROM_PUSH_START = "extra_from_push_start";
    public static final String EXTRA_FROM_PUSH_TYPE = "extra_from_push_type";

    private static final int REQUEST_SET_DEFAULT_SMS_APP = 3;
    private boolean mAllowBackKey = true;
    private boolean mIsFromWelcomeStart = false;
    private boolean mIsFromPush = false;
    private String mPushType;

    private static final int EVENT_RETRY_NAVIGATION = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (OsUtil.hasRequiredPermissions()) {
                Factory.get().onDefaultSmsSetAndPermissionsGranted();
                if (!Preferences.getDefault().contains(WelcomeChooseThemeActivity.PREF_KEY_WELCOME_CHOOSE_THEME_SHOWN)) {
                    Navigations.startActivitySafely(WelcomeSetAsDefaultActivity.this,
                            new Intent(WelcomeSetAsDefaultActivity.this, WelcomeChooseThemeActivity.class));
                    overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                } else {
                    UIIntents.get().launchConversationListActivity(WelcomeSetAsDefaultActivity.this);
                }
                if (mIsFromWelcomeStart) {
                    BugleAnalytics.logEvent("Start_SetAsDefault_Success", true, "step", "setasdefault page");
                    BugleFirebaseAnalytics.logEvent("Start_SetAsDefault_Success", "step", "setasdefault page");
                } else {
                    BugleAnalytics.logEvent("SetAsDefault_GuidePage_Success", true);
                    BugleFirebaseAnalytics.logEvent("SetAsDefault_GuidePage_Success");
                }
                finish();
            } else {
                sendEmptyMessageDelayed(EVENT_RETRY_NAVIGATION, 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_set_as_default);

        if (getIntent() != null) {
            mIsFromWelcomeStart = getIntent().getBooleanExtra(EXTRA_FROM_WELCOME_START, false);
            mIsFromPush = getIntent().getBooleanExtra(EXTRA_FROM_PUSH_START, false);
            if (mIsFromPush) {
                mPushType = getIntent().getStringExtra(EXTRA_FROM_PUSH_TYPE);
                if (mPushType != null) {
                    HSLog.i("test_test", "push_click: " + mPushType);
                    if (mPushType.equals(SetDefaultNotification.TYPE)) {
                        SetDefaultPushAutopilotUtils.logPushSetDefaultClick();
                    } else {
                        ActivePushAutopilotUtils.logPushClick();
                    }
                }
            }
        }

        if (mIsFromPush) {
            startSystemSetDefaultSms();
        }

        findViewById(R.id.welcome_set_default_button).setBackgroundDrawable(
                BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.primary_color), Dimensions.pxFromDp(6.7f), true));
        findViewById(R.id.welcome_set_default_button).setOnClickListener(v -> {
            startSystemSetDefaultSms();
        });

        mAllowBackKey = HSConfig.optBoolean(true, "Application", "StartPageAllowBack");
        BugleAnalytics.logEvent("SMS_ActiveUsers", true);
    }

    private void startSystemSetDefaultSms() {
        final Intent intent = UIIntents.get().getChangeDefaultSmsAppIntent(WelcomeSetAsDefaultActivity.this);
        startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
        if (mIsFromWelcomeStart) {
            BugleAnalytics.logEvent("Start_SetAsDefault_Click", true);
            BugleFirebaseAnalytics.logEvent("Start_SetAsDefault_Click");
        } else {
            BugleAnalytics.logEvent("SetAsDefault_GuidePage_Click", true);
            BugleFirebaseAnalytics.logEvent("SetAsDefault_GuidePage_Click");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsFromWelcomeStart) {
            BugleAnalytics.logEvent("Start_SetAsDefault_Show", true);
            BugleFirebaseAnalytics.logEvent("Start_SetAsDefault_Show");
        } else {
            BugleAnalytics.logEvent("SetAsDefault_GuidePage_Show", true);
            BugleFirebaseAnalytics.logEvent("SetAsDefault_GuidePage_Show");
        }
    }

    @Override
    public void onBackPressed() {
        if (mAllowBackKey) {
            super.onBackPressed();
        }
        if (mIsFromWelcomeStart) {
            BugleAnalytics.logEvent("Start_SetAsDefault_Back", true);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            if (DefaultSMSUtils.isDefaultSmsApp(true)) {
                if (mIsFromPush) {
                    if (mPushType.equals(SetDefaultNotification.TYPE)) {
                        SetDefaultPushAutopilotUtils.logPushSetDefaultSuccess();
                    } else {
                        ActivePushAutopilotUtils.logPushSuccess();
                    }
                    switch (mPushType) {
                        case ActiveNotification.TYPE_THEME: {
                            UIIntents.get().launchConversationListActivity(this);
                            Intent intent = new Intent(this, ThemeSelectActivity.class);
                            startActivity(intent);
                            break;
                        }
                        case ActiveNotification.TYPE_EMOJIS: {
                            UIIntents.get().launchConversationListActivity(this);
                            Intent intent = new Intent(this, EmojiStoreActivity.class);
                            startActivity(intent);
                            break;
                        }
                        case SetDefaultNotification.TYPE:
                        default:
                        case ActiveNotification.TYPE_SMS:
                            UIIntents.get().launchConversationListActivity(this);
                            break;
                    }
                    finish();
                } else {
                    mHandler.sendEmptyMessageDelayed(EVENT_RETRY_NAVIGATION, 100);
                }
            } else {
                Toasts.showToast(R.string.welcome_set_default_failed_toast, Toast.LENGTH_LONG);
            }
        }
    }
}
