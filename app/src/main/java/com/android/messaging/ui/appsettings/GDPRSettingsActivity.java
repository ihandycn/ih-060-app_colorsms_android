package com.android.messaging.ui.appsettings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.HSGdprConsent;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class GDPRSettingsActivity extends HSAppCompatActivity {
    private SettingItemView mSMSDeliveryReports;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gdpr_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.setting_gdpr));
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mSMSDeliveryReports = findViewById(R.id.setting_gdpr);
        mSMSDeliveryReports.setOnItemClickListener(() -> {
            boolean status = !(HSGdprConsent.getConsentState() == HSGdprConsent.ConsentState.ACCEPTED);
            if (!status) {
                mSMSDeliveryReports.setChecked(true);
                new BaseAlertDialog.Builder(GDPRSettingsActivity.this)
                        .setTitle(getResources().getString(R.string.gdpr_dialog_title))
                        .setMessage(getResources().getString(R.string.gdpr_dialog_content))
                        .setNegativeButton(R.string.message_box_negative_button,
                                (arg0, arg1) -> {
                                    HSGdprConsent.setGranted(false);
                                    mSMSDeliveryReports.setChecked(false);
                                })
                        .setPositiveButton(R.string.message_box_positive_action, null)
                        .show();
            } else {
                mSMSDeliveryReports.setChecked(true);
                HSGdprConsent.setGranted(true);
            }
        });

        refresh();
    }

    @Override protected void onResume() {
        super.onResume();

        refresh();
    }

    private void refresh() {
        mSMSDeliveryReports.setChecked(HSGdprConsent.getConsentState() == HSGdprConsent.ConsentState.ACCEPTED);
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
