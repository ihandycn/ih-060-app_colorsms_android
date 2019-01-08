package com.android.messaging.ui.appsettings;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.feedback.FeedbackActivity;
import com.android.messaging.smsshow.SmsShowUtils;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.WebViewActivity;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.PhoneUtils;
import com.ihs.commons.config.HSConfig;
import com.messagecenter.customize.MessageCenterSettings;

public class SettingGeneralActivity extends AppCompatActivity {
    private static final int REQUEST_SET_DEFAULT_SMS_APP = 2;

    private SettingItemView mSetDefaultView;
    private SettingItemView mSMSShowView;
    private SettingItemView mOutgoingSoundView;
    private SettingItemView mNotificationView;
    private SettingItemView mPopUpsView;
    private SettingItemView mSoundView;
    private SettingItemView mVibrateView;
    final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.general_settings_activity_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final boolean topLevel = getIntent().getBooleanExtra(
                UIIntents.UI_INTENT_EXTRA_TOP_LEVEL_SETTINGS, false);

        //set default
        mSetDefaultView = findViewById(R.id.setting_item_default_sms);
        final String defaultSmsAppLabel = getString(R.string.default_sms_app,
                PhoneUtils.getDefault().getDefaultSmsAppLabel());
        mSetDefaultView.setSummary(defaultSmsAppLabel);
        mSetDefaultView.setOnItemClickListener(() -> {
            final Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            //intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
        });

        //outgoing message sounds
        mOutgoingSoundView = findViewById(R.id.setting_item_outgoing_message_sounds);
        final String prefKey = getString(R.string.send_sound_pref_key);
        final boolean defaultValue = getResources().getBoolean(
                R.bool.send_sound_pref_default);
        mOutgoingSoundView.setChecked(prefs.getBoolean(prefKey, defaultValue));
        mOutgoingSoundView.setOnItemClickListener(() -> prefs.putBoolean(prefKey, mOutgoingSoundView.isChecked()));

        //sms show --dong.guo
        mSMSShowView = findViewById(R.id.setting_item_sms_show);
        mSMSShowView.setChecked(SmsShowUtils.isSmsShowEnabledByUser());
        mSMSShowView.setOnItemClickListener(() -> SmsShowUtils.setSmsShowUserEnabled(mSMSShowView.isChecked()));

        //pop ups
        mPopUpsView = findViewById(R.id.setting_item_sms_pop_ups);
        boolean defaultV = MessageCenterSettings.isSMSAssistantModuleEnabled();
        mPopUpsView.setChecked(defaultV);
        if (!defaultV) {
            mSMSShowView.setEnable(false);
        }
        mPopUpsView.setOnItemClickListener(() -> {
            boolean b = mPopUpsView.isChecked();
            MessageCenterSettings.setSMSAssistantModuleEnabled(b);
            mSMSShowView.setEnable(b);
        });

        //sounds
        mSoundView = findViewById(R.id.setting_item_sound);
        updateSoundSummary();
        mSoundView.setOnItemClickListener(this::onSoundItemClick);

        //vibrate
        mVibrateView = findViewById(R.id.setting_item_vibrate);
        final String vibratePrefKey = getString(R.string.notification_vibration_pref_key);
        final boolean vibrateDefaultValue = getResources().getBoolean(R.bool.notification_vibration_pref_default);
        mVibrateView.setChecked(prefs.getBoolean(vibratePrefKey, vibrateDefaultValue));
        mVibrateView.setOnItemClickListener(() -> prefs.putBoolean(vibratePrefKey, mVibrateView.isChecked()));

        //notification
        mNotificationView = findViewById(R.id.setting_item_notifications);
        String notificationKey = getString(R.string.notifications_enabled_pref_key);
        boolean notificationDefaultValue = prefs.getBoolean(
                notificationKey,
                getResources().getBoolean(R.bool.notifications_enabled_pref_default));
        if (!notificationDefaultValue) {
            mSMSShowView.setEnable(false);
            mPopUpsView.setEnable(false);
            mSoundView.setEnable(false);
            mVibrateView.setEnable(false);
        }
        mNotificationView.setChecked(notificationDefaultValue);
        mNotificationView.setOnItemClickListener(() -> {
                    boolean b = mNotificationView.isChecked();
                    prefs.putBoolean(notificationKey, b);
                    mSMSShowView.setEnable(b);
                    mPopUpsView.setEnable(b);
                    mSoundView.setEnable(b);
                    mVibrateView.setEnable(b);
                }
        );

        //blocked contacts
        SettingItemView mBlockedContactsView = findViewById(R.id.setting_item_blocked_contacts);
        mBlockedContactsView.setOnItemClickListener(() ->
                UIIntents.get().launchBlockedParticipantsActivity(this));

        //advances
        SettingItemView mAdvancedView = findViewById(R.id.setting_item_advanced);
        if (topLevel) {
            mAdvancedView.setOnItemClickListener(() -> {
                Intent intent = UIIntents.get().getAdvancedSettingsIntent(this);
                startActivity(intent);
            });
        } else {
            mAdvancedView.setVisibility(View.GONE);
        }

        //5 star
        SettingItemView mFiveStarRatingView = findViewById(R.id.setting_item_five_star_rating);
        mFiveStarRatingView.setOnItemClickListener(() -> FiveStarRateDialog.showFiveStarFromSetting(this));

        //feedback
        ((SettingItemView) findViewById(R.id.setting_item_feedback)).setOnItemClickListener(
                () -> {
                    Intent intent = new Intent(this, FeedbackActivity.class);
                    intent.putExtra(FeedbackActivity.INTENT_KEY_LAUNCH_FROM, FeedbackActivity.LAUNCH_FROM_SETTING);
                    startActivity(intent);
                }
        );

        //privacy policy
        SettingItemView mPrivacyPolicy = findViewById(R.id.setting_item_privacy_policy);
        mPrivacyPolicy.setOnItemClickListener(() -> {
            Intent privacyIntent = WebViewActivity.newIntent(
                    HSConfig.optString("", "Application", "PrivacyPolicyURL"),
                    false, false);
            startActivity(privacyIntent);
        });

        //terms of service
        SettingItemView mTermsOfService = findViewById(R.id.setting_item_terms_of_service);
        mTermsOfService.setOnItemClickListener(() -> {
            Intent termsOfServiceIntent = WebViewActivity.newIntent(
                    HSConfig.optString("", "Application", "TermsOfServiceURL"),
                    false, false);
            startActivity(termsOfServiceIntent);
        });
    }

    private void updateSoundSummary() {
        // The silent ringtone just returns an empty string
        String ringtoneName = getString(R.string.silent_ringtone);
        String prefKey = getString(R.string.notification_sound_pref_key);

        String ringtoneString = prefs.getString(prefKey, null);

        if (ringtoneString == null) {
            ringtoneString = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
            prefs.putString(prefKey, ringtoneString);
        }

        if (!TextUtils.isEmpty(ringtoneString)) {
            final Uri ringtoneUri = Uri.parse(ringtoneString);
            final Ringtone tone = RingtoneManager.getRingtone(this, ringtoneUri);

            if (tone != null) {
                ringtoneName = tone.getTitle(this);
            }
        }

        mSoundView.setSummary(ringtoneName);
    }

    private void onSoundItemClick() {
        Intent ringtonePickerIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION));
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getTitle()); //title
        startActivityForResult(ringtonePickerIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 1) {
            if (data != null) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                String prefKey = getString(R.string.notification_sound_pref_key);
                prefs.putString(prefKey, uri == null ? "" : uri.toString());
                updateSoundSummary();
            }
        } else if (requestCode == REQUEST_SET_DEFAULT_SMS_APP) {
            final String defaultSmsAppLabel = getString(R.string.default_sms_app,
                    PhoneUtils.getDefault().getDefaultSmsAppLabel());
            mSetDefaultView.setSummary(defaultSmsAppLabel);
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
