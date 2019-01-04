package com.android.messaging.ui.welcome;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.view.TypefacedTextView;

public class WelcomeStartActivity extends AppCompatActivity implements View.OnClickListener {

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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.welcome_start_button:
                if (!UiUtils.redirectToWelcomeIfNeeded(this)) {
                    UIIntents.get().launchConversationListActivity(this);
                    finish();
                }
                break;

            case R.id.welcome_start_service:
//                Intent serviceIntent = new Intent();
//                serviceIntent.setData(Uri.parse(HSConfig.optString(
//                        "", "Application", "TermsOfServiceURL")));
//                HSLog.d("WelcomeStartActivity", HSConfig.optString(
//                        "", "Application", "TermsOfServiceURL"));
//                serviceIntent.setAction(Intent.ACTION_VIEW);
//                startActivity(serviceIntent);
                break;

            case R.id.welcome_start_policy:
//                Intent privacyIntent = new Intent();
//                privacyIntent.setData(Uri.parse(HSConfig.optString(
//                        "", "Application", "PrivacyPolicyURL")));
//                HSLog.d("WelcomeStartActivity", HSConfig.optString(
//                        "", "Application", "TermsOfServiceURL"));
//                privacyIntent.setAction(Intent.ACTION_VIEW);
//                startActivity(privacyIntent);
                break;
        }
    }
}
