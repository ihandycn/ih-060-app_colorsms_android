package com.android.messaging.ui.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

public class WelcomeSetAsDefaultActivity extends AppCompatActivity {

    public static final String EXTRA_FROM_WELCOME_START = "extra_from_welcome_start";
    private static final int REQUEST_SET_DEFAULT_SMS_APP = 3;
    private boolean mAllowBackKey = true;
    private boolean mIsFromWelcomeStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_set_as_default);

        if (getIntent() != null) {
            mIsFromWelcomeStart = getIntent().getBooleanExtra(EXTRA_FROM_WELCOME_START, false);
        }

        findViewById(R.id.welcome_set_default_button).setBackgroundDrawable(
                BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.welcome_button_dark_green), Dimensions.pxFromDp(6.7f), true));
        findViewById(R.id.welcome_set_default_button).setOnClickListener(v -> {
            final Intent intent = UIIntents.get().getChangeDefaultSmsAppIntent(WelcomeSetAsDefaultActivity.this);
            startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
            if (mIsFromWelcomeStart) {
                BugleAnalytics.logEvent("Start_SetAsDefault_Click", true, true);
            } else {
                BugleAnalytics.logEvent("SetAsDefault_GuidePage_Click", true, true);
            }
        });

        mAllowBackKey = HSConfig.optBoolean(true, "Application", "StartPageAllowBack");
        BugleAnalytics.logEvent("SMS_ActiveUsers", true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsFromWelcomeStart) {
            BugleAnalytics.logEvent("Start_SetAsDefault_Show", true, true);
        } else {
            BugleAnalytics.logEvent("SetAsDefault_GuidePage_Show", true, true);
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
            if (PhoneUtils.getDefault().isDefaultSmsApp()) {
                if (OsUtil.hasRequiredPermissions()) {
                    Factory.get().onDefaultSmsSetAndPermissionsGranted();
                    if (!Preferences.getDefault().contains(WelcomeChooseThemeActivity.PREF_KEY_WELCOME_CHOOSE_THEME_SHOWN)) {
                        Navigations.startActivitySafely(this, new Intent(this, WelcomeChooseThemeActivity.class));
                    } else {
                        UIIntents.get().launchConversationListActivity(this);
                    }
                } else {
                    UIIntents.get().launchWelcomePermissionActivity(this);
                }
                if (mIsFromWelcomeStart) {
                    BugleAnalytics.logEvent("Start_SetAsDefault_Success", true, true, "step", "setasdefault page");
                } else {
                    BugleAnalytics.logEvent("SetAsDefault_GuidePage_Success", true, true);
                }
                finish();
            } else {
                Toasts.showToast(R.string.welcome_set_default_failed_toast, Toast.LENGTH_LONG);
            }
        }
    }
}
