package com.android.messaging.ui.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PhoneUtils;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Calendars;
import com.superapps.util.Dimensions;
import com.superapps.util.Toasts;

public class WelcomeSetAsDefaultActivity extends AppCompatActivity {

    private static final int REQUEST_SET_DEFAULT_SMS_APP = 3;
    private boolean mAllowBackKey = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_set_as_default);

        findViewById(R.id.welcome_set_default_button).setBackgroundDrawable(
                BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.welcome_button_dark_green), Dimensions.pxFromDp(6.7f), true));
        findViewById(R.id.welcome_set_default_button).setOnClickListener(v -> {
            final Intent intent = UIIntents.get().getChangeDefaultSmsAppIntent(WelcomeSetAsDefaultActivity.this);
            startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
            BugleAnalytics.logEvent("SMS_Start_SetDefaultPage_Btnclick", true);
            if (Calendars.isSameDay(System.currentTimeMillis(), CommonUtils.getAppInstallTimeMillis())) {
                BugleAnalytics.logEvent("SMS_Start_SetDefaultPage_Btnclick_NewUser", true);
            }
        });

        mAllowBackKey = HSConfig.optBoolean(true, "Application", "StartPageAllowBack");
        BugleAnalytics.logEvent("SMS_ActiveUsers", true);
    }

    @Override
    public void onStart() {
        super.onStart();
        BugleAnalytics.logEvent("SMS_Start_SetDefaultPage_Show", true);
        if (Calendars.isSameDay(System.currentTimeMillis(), CommonUtils.getAppInstallTimeMillis())) {
            BugleAnalytics.logEvent("SMS_Start_SetDefaultPage_Show_NewUser", true);
        }
    }

    @Override
    public void onBackPressed() {
        if (mAllowBackKey) {
            super.onBackPressed();
            BugleAnalytics.logEvent("SMS_Start_SetDefaultPage_Back", true);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            if (PhoneUtils.getDefault().isDefaultSmsApp()) {
                if (OsUtil.hasRequiredPermissions()) {
                    Factory.get().onDefaultSmsSetAndPermissionsGranted();
                    UIIntents.get().launchConversationListActivity(this);
                } else {
                    UIIntents.get().launchWelcomePermissionActivity(this);
                }
                BugleAnalytics.logEvent("SMS_Start_SetDefault_Success", true);
                if (Calendars.isSameDay(System.currentTimeMillis(), CommonUtils.getAppInstallTimeMillis())) {
                    BugleAnalytics.logEvent("SMS_Start_SetDefault_Success_NewUser", true);
                }
                finish();
            } else {
                Toasts.showToast(R.string.welcome_set_default_failed_toast, Toast.LENGTH_LONG);
            }
        }
    }
}
