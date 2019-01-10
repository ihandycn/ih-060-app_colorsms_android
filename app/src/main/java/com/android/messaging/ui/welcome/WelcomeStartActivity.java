package com.android.messaging.ui.welcome;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.WebViewActivity;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.OsUtil;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Dimensions;
import com.superapps.view.TypefacedTextView;

public class WelcomeStartActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 1;
    private boolean mAllowBackKey = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_start);
        configNavigationBar();

        findViewById(R.id.welcome_start_button).setOnClickListener(this);

        TypefacedTextView serviceText = findViewById(R.id.welcome_start_service);
        serviceText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        serviceText.setOnClickListener(this);

        TypefacedTextView policyText = findViewById(R.id.welcome_start_policy);
        policyText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        policyText.setOnClickListener(this);

        mAllowBackKey = HSConfig.optBoolean(true, "Application", "StartPageAllowBack");
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
                String[] permissions = OsUtil.getMissingRequiredPermissions();
                if (OsUtil.isAtLeastM()) {
                    BugleAnalytics.logEvent("SMS_Start_WelcomePage_BtnClick_Above23", true);
                } else {
                    BugleAnalytics.logEvent("SMS_Start_WelcomePage_BtnClick_Below23", true);
                }
                if (permissions.length != 0) {
                    requestPermissions(permissions, REQUIRED_PERMISSIONS_REQUEST_CODE);
                } else {
                    UIIntents.get().launchConversationListActivity(this);
                    finish();
                }
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

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String permissions[], final int[] grantResults) {
        if (requestCode == REQUIRED_PERMISSIONS_REQUEST_CODE) {
            if (OsUtil.hasRequiredPermissions()) {
                BugleAnalytics.logEvent("SMS_Start_WelcomePage_Permission_Success", true);
            }
            UIIntents.get().launchConversationListActivity(this);
            finish();
        }
    }

    @SuppressLint("NewApi")
    private void configNavigationBar() {
        if (OsUtil.isAtLeastL()) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);

            int navigationHeight = Dimensions.getNavigationBarHeight(this);

            View button = findViewById(R.id.welcome_start_button);
            FrameLayout.LayoutParams buttonParams = (FrameLayout.LayoutParams) button.getLayoutParams();
            buttonParams.bottomMargin += navigationHeight;

            View content = findViewById(R.id.welcome_start_content);
            FrameLayout.LayoutParams contentParams = (FrameLayout.LayoutParams) content.getLayoutParams();
            contentParams.bottomMargin += navigationHeight;
        }
    }
}
