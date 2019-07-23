package com.android.messaging.ui.appsettings;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.feedback.FeedbackActivity;
import com.android.messaging.smsshow.SmsShowUtils;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.messagebox.MessageBoxSettings;
import com.android.messaging.ui.signature.SignatureSettingDialog;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.DefaultSMSUtils;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.UiUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

import java.util.Collections;

import static android.view.View.GONE;

public class SettingActivity extends BaseActivity {
    private static final int REQUEST_CODE_START_RINGTONE_PICKER = 1;
    private static final int RC_SIGN_IN = 12;

    private GeneralSettingItemView mSmsShowView;
    private GeneralSettingItemView mNotificationView;
    private GeneralSettingItemView mPopUpsView;
    private GeneralSettingItemView mSignature;
    private GeneralSettingItemView mSoundView;
    private GeneralSettingItemView mVibrateView;
    private GeneralSettingItemView mPrivacyModeView;
    private GeneralSettingItemView mSyncSettingsView;
    private GeneralSettingItemView mSendDelayView;
    private GeneralSettingItemView mSMSDeliveryReports;
    private GeneralSettingItemView mOutgoingSoundView;

    private View mNotificationChildrenGroup;

    private BackPressedListener mBackListener;
    final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();

    public interface BackPressedListener {
        void onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.settings_activity_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //set title
        int color = PrimaryColors.getPrimaryColor();
        ((TextView) findViewById(R.id.setting_title_notifications)).setTextColor(color);
        ((TextView) findViewById(R.id.setting_title_general)).setTextColor(color);
        ((TextView) findViewById(R.id.setting_title_emoji)).setTextColor(color);
        ((TextView) findViewById(R.id.setting_title_others)).setTextColor(color);

        mNotificationChildrenGroup = findViewById(R.id.notification_children_group);

        //sms show
        mSmsShowView = findViewById(R.id.setting_item_sms_show);
        mSmsShowView.setChecked(SmsShowUtils.isSmsShowEnabledByUser());
        mSmsShowView.setOnClickListener(v -> {
            if (mSmsShowView.isChecked()) {
                new BaseAlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.setting_sms_show_close_dialog_title))
                        .setMessage(getResources().getString(R.string.setting_sms_show_close_dialog_content))
                        .setNegativeButton(R.string.setting_sms_show_close_dialog_ok,
                                (dialog, button) -> {
                                    SmsShowUtils.setSmsShowUserEnabled(false);
                                    mSmsShowView.setChecked(false);
                                })
                        .setPositiveButton(R.string.delete_conversation_decline_button, null)
                        .show();
            } else {
                SmsShowUtils.setSmsShowUserEnabled(true);
                mSmsShowView.setChecked(true);
            }
            BugleAnalytics.logEvent("SMS_Settings_SMSShow_Click", true);
        });

        //pop ups
        setUpPopUpsView();

        mPrivacyModeView = findViewById(R.id.setting_item_privacy_mode);
        updatePrivacyModeSummary();
        mPrivacyModeView.setOnItemClickListener(() -> {
            SelectPrivacyModeDialog dialog = new SelectPrivacyModeDialog();
            dialog.setOnDismissOrCancelListener(new BaseDialogFragment.OnDismissOrCancelListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    updatePrivacyModeSummary();
                }

                @Override
                public void onCancel(DialogInterface dialog) {

                }
            });
            UiUtils.showDialogFragment(SettingActivity.this, dialog);
        });

        //send delay
        mSendDelayView = findViewById(R.id.setting_item_send_delay);
        updateSendDelaySummary();
        mSendDelayView.setOnItemClickListener(() -> {
            SelectSendingMessageDelayTimeDialog dialog = new SelectSendingMessageDelayTimeDialog();
            dialog.setOnDismissOrCancelListener(new BaseDialogFragment.OnDismissOrCancelListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    updateSendDelaySummary();
                }

                @Override
                public void onCancel(DialogInterface dialog) {

                }
            });
            UiUtils.showDialogFragment(SettingActivity.this, dialog);
            BugleAnalytics.logEvent("Settings_SendDelay_Click");
        });


        //signature
        mSignature = findViewById(R.id.setting_item_signature);
        refreshSignature();
        mSignature.setOnItemClickListener(() -> UiUtils.showDialogFragment(this, new SignatureSettingDialog()));

        //sounds
        mSoundView = findViewById(R.id.setting_item_sound);
        updateSoundSummary();
        mSoundView.setOnItemClickListener(() -> {
            onSoundItemClick();
            BugleAnalytics.logEvent("SMS_Settings_Sound_Click", true);
        });

        //vibrate
        setUpVibrateView();

        //notification
        setUpNotificationView();

        //emoji
        SettingEmojiItemView settingEmojiItemView = findViewById(R.id.setting_item_emoji);
        if (Build.VERSION.SDK_INT >= 24) {
            settingEmojiItemView.setDefault(EmojiManager.EMOJI_SKINS[EmojiManager.getSkinDefault()]);
            settingEmojiItemView.setOnItemClickListener(() -> {
                ChooseEmojiSkinDialog dialog = new ChooseEmojiSkinDialog();
                dialog.setOnDismissOrCancelListener(new BaseDialogFragment.OnDismissOrCancelListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        settingEmojiItemView.updateSkin(EmojiManager.EMOJI_SKINS[EmojiManager.getSkinDefault()]);
                    }

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });


                UiUtils.showDialogFragment(this, dialog);
                BugleAnalytics.logEvent("Settings_EmojiSkintone_Click");
            });
        } else {
            settingEmojiItemView.setVisibility(GONE);
            findViewById(R.id.setting_title_emoji).setVisibility(GONE);
        }

        //blocked contacts
        GeneralSettingItemView mBlockedContactsView = findViewById(R.id.setting_item_blocked_contacts);
        mBlockedContactsView.setOnItemClickListener(() -> {
            UIIntents.get().launchBlockedParticipantsActivity(this);
            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
            BugleAnalytics.logEvent("SMS_Settings_BlockedContacts_Click", true);
        });

        //outgoing message sounds
        setUpOutgoingSoundView();

        //archived conversations
        GeneralSettingItemView archivedConversations = findViewById(R.id.setting_item_archive);
        archivedConversations.setOnItemClickListener(() -> {
            UIIntents.get().launchArchivedConversationsActivity(this);
            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        });

        //advances
        GeneralSettingItemView mAdvancedView = findViewById(R.id.setting_item_advanced);
        mAdvancedView.setOnItemClickListener(() -> {
            BugleAnalytics.logEvent("SMS_Settings_Advanced_Click", true);

            if (PhoneUtils.getDefault().getActiveSubscriptionCount() <= 1) {
                Intent intent = UIIntents.get().getAdvancedSettingsIntent(this);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
            } else {
                UIIntents.get().launchSettingsSimSelectActivity(this);
            }
        });

        //sms delivery reports
        mSMSDeliveryReports = findViewById(R.id.setting_advanced_delivery_reports);
        final String deliveryReportsKey = getString(R.string.delivery_reports_pref_key);
        final Preferences prefs = Preferences.getDefault();
        mSMSDeliveryReports.setChecked(prefs.getBoolean(deliveryReportsKey,
                getResources().getBoolean(R.bool.delivery_reports_pref_default)));
        mSMSDeliveryReports.setOnItemClickListener(() -> {
            prefs.putBoolean(deliveryReportsKey, mSMSDeliveryReports.isChecked());
            BugleAnalytics.logEvent("SMS_Settings_Advanced_DeliveryReports_Click", true);
        });

        //feedback
        ((GeneralSettingItemView) findViewById(R.id.setting_item_feedback)).setOnItemClickListener(
                () -> {
                    Intent intent = new Intent(this, FeedbackActivity.class);
                    intent.putExtra(FeedbackActivity.INTENT_KEY_LAUNCH_FROM, FeedbackActivity.LAUNCH_FROM_SETTING);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
                }
        );

        //about
        GeneralSettingItemView mPrivacyPolicy = findViewById(R.id.setting_item_about);
        mPrivacyPolicy.setOnItemClickListener(() -> {
            Navigations.startActivitySafely(this, SettingAboutActivity.class);
            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        });

        setUpSyncSettingsView();


        if (!DefaultSMSUtils.isDefaultSmsApp()) {
            mSMSDeliveryReports.setChecked(false);
        }
    }

    public void addBackPressListener(BackPressedListener listener) {
        mBackListener = listener;
    }

    public void clearBackPressedListener() {
        mBackListener = null;
    }

    @Override
    public void onBackPressed() {
        if (mBackListener != null) {
            mBackListener.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    public void refreshSignature() {
        String signature = Preferences.getDefault().getString(SignatureSettingDialog.PREF_KEY_SIGNATURE_CONTENT, null);
        mSignature.setSummary(signature);
    }

    private void setUpNotificationView() {
        mNotificationView = findViewById(R.id.setting_item_notifications);
        String notificationKey = getString(R.string.notifications_enabled_pref_key);
        boolean notificationEnable = prefs.getBoolean(
                notificationKey,
                getResources().getBoolean(R.bool.notifications_enabled_pref_default));
        if (!notificationEnable) {
            mNotificationChildrenGroup.setVisibility(View.GONE);
        } else {
            mNotificationChildrenGroup.setVisibility(View.VISIBLE);
        }

        LayoutTransition transition = new LayoutTransition();
        ObjectAnimator animator = ObjectAnimator.ofFloat(null, "alpha", 0, 0);
        transition.setAnimator(LayoutTransition.DISAPPEARING, animator);
        transition.setDuration(LayoutTransition.DISAPPEARING, 200);
        ((LinearLayout) findViewById(R.id.setting_root_container)).setLayoutTransition(transition);

        mNotificationView.setChecked(notificationEnable);
        mNotificationView.setOnItemClickListener(() -> {
                    boolean b = mNotificationView.isChecked();
                    prefs.putBoolean(notificationKey, b);

                    mNotificationView.hideDivideLine(!b);

                    if (b) {
                        mNotificationChildrenGroup.setVisibility(View.VISIBLE);
                    } else {
                        //mNotificationChildrenGroup.setAlpha(0);
                        mNotificationChildrenGroup.setVisibility(View.GONE);
                    }

                    GeneralSettingSyncManager.uploadNotificationSwitchToServer(b);
                    BugleAnalytics.logEvent("SMS_Settings_Notifications_Click", true);
                }
        );
    }

    private void setUpPopUpsView() {
        mPopUpsView = findViewById(R.id.setting_item_sms_pop_ups);
        boolean defaultV = MessageBoxSettings.isSMSAssistantModuleEnabled();
        mPopUpsView.setChecked(defaultV);
        if (!defaultV) {
            mSmsShowView.setEnable(false);
        }
        mPopUpsView.setOnItemClickListener(() -> {
            boolean b = mPopUpsView.isChecked();
            MessageBoxSettings.setSMSAssistantModuleEnabled(b);
            mSmsShowView.setEnable(b);
            GeneralSettingSyncManager.uploadMessageBoxSwitchToServer(b);
            BugleAnalytics.logEvent("SMS_Settings_Popups_Click", true);
        });

    }

    private void setUpVibrateView() {
        mVibrateView = findViewById(R.id.setting_item_vibrate);
        final String vibratePrefKey = getString(R.string.notification_vibration_pref_key);
        final boolean vibrateDefaultValue = getResources().getBoolean(R.bool.notification_vibration_pref_default);
        mVibrateView.setChecked(prefs.getBoolean(vibratePrefKey, vibrateDefaultValue));
        mVibrateView.setOnItemClickListener(() -> {
            prefs.putBoolean(vibratePrefKey, mVibrateView.isChecked());
            GeneralSettingSyncManager.uploadVibrateSwitchToServer(mVibrateView.isChecked());
            BugleAnalytics.logEvent("SMS_Settings_Vibrate_Click", true);
        });
    }

    private void setUpSyncSettingsView() {
        mSyncSettingsView = findViewById(R.id.sync_settings_item);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mSyncSettingsView.setSummary(getString(R.string.firebase_sync_desktop_settings_description_logged_in));
        }

        mSyncSettingsView.setOnItemClickListener(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(Collections.singletonList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                                .build(),
                        RC_SIGN_IN);
                BugleAnalytics.logEvent("SyncSettings_Icon_Click", "type", "loggedOut");
            } else {
                new BaseAlertDialog.Builder(SettingActivity.this)
                        .setTitle(R.string.firebase_login_out_title)
                        .setPositiveButton(R.string.firebase_login_out, (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            dialog.dismiss();
                            BugleAnalytics.logEvent("SyncSettings_LogOut");
                            Toasts.showToast(R.string.firebase_login_out_succeed);
                            mSyncSettingsView.setSummary(getString(R.string.firebase_sync_desktop_settings_description_logged_out));
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .show();
                BugleAnalytics.logEvent("SyncSettings_LogOut_PopUp_Show");
                BugleAnalytics.logEvent("SyncSettings_Icon_Click", "type", "loggedIn");
            }
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

        try {
            if (!TextUtils.isEmpty(ringtoneString)) {
                final Uri ringtoneUri = Uri.parse(ringtoneString);
                final Ringtone tone = RingtoneManager.getRingtone(this, ringtoneUri);

                if (tone != null) {
                    ringtoneName = tone.getTitle(this);
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            mSoundView.setVisibility(GONE);
            return;
        }
        mSoundView.setSummary(ringtoneName);
    }


    private void updatePrivacyModeSummary() {
        mPrivacyModeView.setSummary(PrivacyModeSettings.getPrivacyModeDescription(null));
    }

    private void updateSendDelaySummary() {
        mSendDelayView.setSummary(SendDelaySettings.getSendDelayDescription());
    }

    private void setUpOutgoingSoundView() {
        mOutgoingSoundView = findViewById(R.id.setting_advanced_outgoing_sounds);
        final String prefKey = getString(R.string.send_sound_pref_key);
        final boolean defaultValue = getResources().getBoolean(
                R.bool.send_sound_pref_default);
        mOutgoingSoundView.setChecked(prefs.getBoolean(prefKey, defaultValue));
        mOutgoingSoundView.setOnItemClickListener(() -> {
            prefs.putBoolean(prefKey, mOutgoingSoundView.isChecked());
            GeneralSettingSyncManager.uploadOutgoingMessageSoundsSwitchToServer(mOutgoingSoundView.isChecked());
            BugleAnalytics.logEvent("SMS_Settings_MessageSounds_Click", true);
        });
    }

    private void onSoundItemClick() {
        String prefKey = getString(R.string.notification_sound_pref_key);
        String ringtoneString = prefs.getString(prefKey, null);
        if (ringtoneString == null) {
            ringtoneString = Settings.System.DEFAULT_NOTIFICATION_URI.toString();
            prefs.putString(prefKey, ringtoneString);
        }

        Intent ringtonePickerIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(ringtoneString));
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        ringtonePickerIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getTitle()); //title
        Navigations.startActivityForResultSafely(SettingActivity.this,
                ringtonePickerIntent, REQUEST_CODE_START_RINGTONE_PICKER);
        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_START_RINGTONE_PICKER) {
            if (resultCode != RESULT_OK) {
                return;
            }
            if (data != null
                    && data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI) != null) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                String prefKey = getString(R.string.notification_sound_pref_key);
                String currentRingtone = prefs.getString(prefKey, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
                if (currentRingtone != null && !currentRingtone.equals(uri == null ? "" : uri.toString())) {
                    BugleAnalytics.logEvent("Customize_Notification_Sound_Change", true, true, "from", "settings");
                }
                prefs.putString(prefKey, uri == null ? "" : uri.toString());
                updateSoundSummary();
            }
        } else if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GeneralSettingSyncManager.overrideLocalData(() -> {
                    setUpNotificationView();
                    setUpPopUpsView();
                    setUpVibrateView();
                });
                Toasts.showToast(R.string.firebase_login_succeed);
                BugleAnalytics.logEvent("SyncSettings_LogIn_Success");
                mSyncSettingsView.setSummary(getString(R.string.firebase_sync_desktop_settings_description_logged_in));
            } else {
                Toasts.showToast(R.string.firebase_login_failed);
            }
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


