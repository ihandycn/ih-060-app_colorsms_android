package com.android.messaging.ui.welcome;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.WebViewActivity;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.commons.config.HSConfig;
import com.superapps.view.TypefacedTextView;

public class WelcomeStartActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 1;
    private boolean mAllowBackKey = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_start);

        findViewById(R.id.welcome_start_button).setOnClickListener(this);

        TypefacedTextView serviceText = findViewById(R.id.welcome_start_service);
        serviceText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        serviceText.setOnClickListener(this);

        TypefacedTextView policyText = findViewById(R.id.welcome_start_policy);
        policyText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        policyText.setOnClickListener(this);

        mAllowBackKey = HSConfig.optBoolean(true, "Application", "StartPageAllowBack");
        BugleAnalytics.logEvent("SMS_ActiveUsers", true);
    }

    @Override
    public void onStart() {
        super.onStart();
        BugleAnalytics.logEvent("SMS_Start_WelcomePage_Show", true);
    }

    @Override
    public void onBackPressed() {
        if (mAllowBackKey) {
            super.onBackPressed();
            BugleAnalytics.logEvent("SMS_Start_WelcomePage_Back", true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.welcome_start_button:
                BugleAnalytics.logEvent("SMS_Start_WelcomePage_BtnClick", true);
                UIIntents.get().launchConversationListActivity(this);
                finish();
                break;

            case R.id.welcome_start_service:
                Intent termsOfServiceIntent = WebViewActivity.newIntent(
                        HSConfig.optString("", "Application", "TermsOfServiceUrl"),
                        false, false);
                startActivity(termsOfServiceIntent);
                break;

            case R.id.welcome_start_policy:
                Intent privacyIntent = WebViewActivity.newIntent(
                        HSConfig.optString("", "Application", "PrivacyPolicyUrl"),
                        false, false);
                startActivity(privacyIntent);
                break;
        }
    }

}
