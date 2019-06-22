package com.android.messaging.ui.appsettings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.WebViewActivity;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.HSGdprConsent;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Navigations;

public class SettingAboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.about));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //privacy policy
        GeneralSettingItemView mPrivacyPolicy = findViewById(R.id.setting_item_privacy_policy);
        mPrivacyPolicy.setOnItemClickListener(() -> {
            Intent privacyIntent = WebViewActivity.newIntent(
                    HSConfig.optString("", "Application", "PrivacyPolicyUrl"),
                    false, false);
            startActivity(privacyIntent);
        });

        //terms of service
        GeneralSettingItemView mTermsOfService = findViewById(R.id.setting_item_terms_of_service);
        mTermsOfService.setOnItemClickListener(() -> {
            Intent termsOfServiceIntent = WebViewActivity.newIntent(
                    HSConfig.optString("", "Application", "TermsOfServiceUrl"),
                    false, false);
            startActivity(termsOfServiceIntent);
        });

        // gdpr
        GeneralSettingItemView mGdpr = findViewById(R.id.setting_item_analytics_advertising);
        mGdpr.setOnItemClickListener(() -> {
            Navigations.startActivitySafely(SettingAboutActivity.this,
                    new Intent(SettingAboutActivity.this, GDPRSettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        });
        if (HSGdprConsent.isGdprUser()) {
            mGdpr.setVisibility(View.VISIBLE);
        } else {
            mGdpr.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


