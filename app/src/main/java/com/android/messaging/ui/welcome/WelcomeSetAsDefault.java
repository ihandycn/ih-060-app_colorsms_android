package com.android.messaging.ui.welcome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.PhoneUtils;
import com.ihs.commons.config.HSConfig;

public class WelcomeSetAsDefault extends AppCompatActivity {
    private static final int REQUEST_SET_DEFAULT_SMS_APP = 3;
    private boolean mShieldBackKey = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_set_as_default);

        findViewById(R.id.welcome_set_default_button).setOnClickListener(v -> {
            final Intent intent = UIIntents.get().getChangeDefaultSmsAppIntent(WelcomeSetAsDefault.this);
            startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
            BugleAnalytics.logEvent("SMS_Start_SetDefaultPage_Btnclick", true);
        });

        mShieldBackKey = HSConfig.optBoolean(false, "Application", "StartPageAllowBack");
    }

    @Override
    public void onStart() {
        super.onStart();
        BugleAnalytics.logEvent("SMS_Start_SetDefaultPage_Show", true);
    }

    @Override
    public void onBackPressed() {
        if (!mShieldBackKey) {
            super.onBackPressed();
            BugleAnalytics.logEvent("SMS_Start_SetDefaultPage_Back", true);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            if (PhoneUtils.getDefault().isDefaultSmsApp()) {
                UIIntents.get().launchConversationListActivity(this);
                BugleAnalytics.logEvent("SMS_Start_SetDefault_Success", true);
                finish();
            } else {
                Toast.makeText(this, R.string.welcome_set_default_failed_toast,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
