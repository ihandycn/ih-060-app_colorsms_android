package com.android.messaging.ui.appsettings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.text.BidiFormatter;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.PhoneUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class SettingAdvancedActivity extends HSAppCompatActivity {

    private SettingItemView mAutoRetrieve;
    private SettingItemView mRoamingAutoRetrieve;
    private SettingItemView mSMSDeliveryReports;

    final BuglePrefs mPrefs = BuglePrefs.getApplicationPrefs();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_advanced);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.advanced_settings_activity_title));
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //phone number
        SettingItemView mPhoneNumberView = findViewById(R.id.setting_advanced_phone_number);
        Intent intent = getIntent();
        Assert.notNull(intent);
        int mSubId = (intent != null) ? intent.getIntExtra(UIIntents.UI_INTENT_EXTRA_SUB_ID,
                ParticipantData.DEFAULT_SELF_SUB_ID) : ParticipantData.DEFAULT_SELF_SUB_ID;

        String value = PhoneUtils.get(mSubId).getCanonicalForSelf(false/*allowOverride*/);
        final String displayValue = (!TextUtils.isEmpty(value))
                ? PhoneUtils.get(mSubId).formatForDisplay(value)
                : getString(R.string.unknown_phone_number_pref_display_value);
        final BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        final String phoneNumber = bidiFormatter.unicodeWrap
                (displayValue, TextDirectionHeuristicsCompat.LTR);
        mPhoneNumberView.setSummary(phoneNumber);

        //Auto Retrieve
        mAutoRetrieve = findViewById(R.id.setting_advanced_auto_retrieve);
        final String autoRetrieveKey = getString(R.string.auto_retrieve_mms_pref_key);
        mAutoRetrieve.setChecked(mPrefs.getBoolean(autoRetrieveKey,
                getResources().getBoolean(R.bool.auto_retrieve_mms_pref_default)));
        mAutoRetrieve.setOnItemClickListener(() -> {
            mPrefs.putBoolean(autoRetrieveKey, mAutoRetrieve.isChecked());
            BugleAnalytics.logEvent("SMS_Settings_Advanced_AutoRetrieve_Click", true);
        });

        //Roaming auto retrieve
        mRoamingAutoRetrieve = findViewById(R.id.setting_advanced_roaming_auto_retrieve);
        final String roamingAutoRetrieveKey = getString(R.string.auto_retrieve_mms_when_roaming_pref_key);
        mRoamingAutoRetrieve.setChecked(mPrefs.getBoolean(roamingAutoRetrieveKey,
                getResources().getBoolean(R.bool.auto_retrieve_mms_when_roaming_pref_default)));
        mRoamingAutoRetrieve.setOnItemClickListener(() -> mPrefs.putBoolean(roamingAutoRetrieveKey,
                mRoamingAutoRetrieve.isChecked()));

        //sms delivery reports
        mSMSDeliveryReports = findViewById(R.id.setting_advanced_delivery_reports);
        final String deliveryReportsKey = getString(R.string.delivery_reports_pref_key);
        final BuglePrefs prefs = BuglePrefs.getSubscriptionPrefs(mSubId);
        mSMSDeliveryReports.setChecked(prefs.getBoolean(deliveryReportsKey,
                getResources().getBoolean(R.bool.delivery_reports_pref_default)));
        mSMSDeliveryReports.setOnItemClickListener(() -> {
            prefs.putBoolean(deliveryReportsKey, mSMSDeliveryReports.isChecked());
            BugleAnalytics.logEvent("SMS_Settings_Advanced_DeliveryReports_Click", true);
        });

        if (!PhoneUtils.getDefault().isDefaultSmsApp()) {
            mAutoRetrieve.setChecked(false);
            mSMSDeliveryReports.setChecked(false);
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
