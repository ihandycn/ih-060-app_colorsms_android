package com.android.messaging.ui.appsettings;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.android.messaging.ui.SettingEmojiStyleItemView;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.ui.invitefriends.InviteFriendsActivity;
import com.android.messaging.ui.messagebox.MessageBoxSettings;
import com.android.messaging.ui.ringtone.RingtoneInfo;
import com.android.messaging.ui.ringtone.RingtoneInfoManager;
import com.android.messaging.ui.ringtone.RingtoneSettingActivity;
import com.android.messaging.ui.signature.SignatureSettingDialog;
import com.android.messaging.ui.signature.TextSettingDialog;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.DefaultSMSUtils;
import com.android.messaging.util.PhoneUtils;
import com.android.messaging.util.TransitionUtils;
import com.android.messaging.util.UiUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

import java.util.Collections;

import static android.view.View.GONE;

public class SettingActivity extends BaseActivity implements TextSettingDialog.TextSettingDialogCallback {
    private static final int REQUEST_CODE_RINGTONE_PICKER = 1;

    private static final int RC_SIGN_IN = 12;
    private static final int EMOJI_STYLE_SET = 13;

    private GeneralSettingItemView mSmsShowView;
    private GeneralSettingItemView mNotificationView;
    private GeneralSettingItemView mPopUpsView;
    private SignatureItemView mSignature;
    private GeneralSettingItemView mSoundView;
    private GeneralSettingItemView mVibrateView;
    private GeneralSettingItemView mLedColorView;
    private GeneralSettingItemView mPrivacyModeView;
    private GeneralSettingItemView mSyncSettingsView;
    private GeneralSettingItemView mSendDelayView;
    private GeneralSettingItemView mSMSDeliveryReports;
    private SettingEmojiSkinItemView mSettingEmojiSkinItemView;
    private SettingEmojiStyleItemView mSettingEmojiStyleItemView;
    private GeneralSettingItemView mOutgoingSoundView;

    private View mNotificationChildrenGroup;

    final BuglePrefs prefs = BuglePrefs.getApplicationPrefs();

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

        mVibrateView = findViewById(R.id.setting_item_vibrate);
        updateVibrateSummary();
        mVibrateView.setOnItemClickListener(() -> {
            SelectVibrateModeDialog dialog = new SelectVibrateModeDialog();
            dialog.setOnDismissOrCancelListener(new BaseDialogFragment.OnDismissOrCancelListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    updateVibrateSummary();
                }

                @Override
                public void onCancel(DialogInterface dialog) {

                }
            });
            UiUtils.showDialogFragment(SettingActivity.this, dialog);
            BugleAnalytics.logEvent("SMS_Settings_Vibrate_Click", true);
        });

        mLedColorView = findViewById(R.id.setting_item_led_color);
        updateLedSummary();
        mLedColorView.setOnItemClickListener(() -> {
            SelectLedColorDialog dialog = new SelectLedColorDialog();
            dialog.setOnDismissOrCancelListener(new BaseDialogFragment.OnDismissOrCancelListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    updateLedSummary();
                }

                @Override
                public void onCancel(DialogInterface dialog) {

                }
            });
            UiUtils.showDialogFragment(SettingActivity.this, dialog);
            BugleAnalytics.logEvent("SMS_Settings_LEDColor_Click", true);
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
        mSignature.setOnItemClickListener(() -> {
            TextSettingDialog signatureSettingDialog = new SignatureSettingDialog();
            signatureSettingDialog.setHost(this);
            UiUtils.showDialogFragment(this, signatureSettingDialog);
        });

        //sounds
        mSoundView = findViewById(R.id.setting_item_sound);
        updateSoundSummary();
        mSoundView.setOnItemClickListener(() -> {
            onSoundItemClick();
            BugleAnalytics.logEvent("SMS_Settings_Sound_Click", true);
        });

        //notification
        setUpNotificationView();

        //emoji
        mSettingEmojiStyleItemView = findViewById(R.id.setting_item_emoji_style);
        mSettingEmojiStyleItemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                BugleAnalytics.logEvent("Settings_EmojiStyle_Click", true);
                Intent intent = new Intent(SettingActivity.this, EmojiStyleSetActivity.class);
                startActivityForResult(intent, EMOJI_STYLE_SET, TransitionUtils.getTransitionInBundle(SettingActivity.this));
            }
        });

        mSettingEmojiSkinItemView = findViewById(R.id.setting_item_emoji_skin);
        updateEmojiSkinItemView(EmojiManager.isSystemEmojiStyle());

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
                Intent intent = UIIntents.get().getAdvancedSettingsIntent(SettingActivity.this);
                SettingActivity.this.startActivity(intent);
                SettingActivity.this.overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
            } else {
                UIIntents.get().launchSettingsSimSelectActivity(SettingActivity.this);
            }
        });

        GeneralSettingItemView inviteFriends = findViewById(R.id.setting_item_invite_friends);
        inviteFriends.setOnItemClickListener(() -> {
            Intent inviteFriendsIntent = new Intent(SettingActivity.this, InviteFriendsActivity.class);
            startActivity(inviteFriendsIntent, TransitionUtils.getTransitionInBundle(SettingActivity.this));
        });

        GeneralSettingItemView fiveStarRating = findViewById(R.id.setting_item_five_star_rating);
        fiveStarRating.setOnItemClickListener(() -> {
            FiveStarRateDialog.showFiveStarFromSetting(SettingActivity.this);
        });

        //sms delivery reports
        mSMSDeliveryReports = findViewById(R.id.setting_advanced_delivery_reports);
        final String deliveryReportsKey = getString(R.string.delivery_reports_pref_key);
        final Preferences prefs = Preferences.getDefault();
        mSMSDeliveryReports.setChecked(prefs.getBoolean(deliveryReportsKey,
                HSConfig.optBoolean(true, "Application", "DeliveryReportDefaultSwitch")));
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

    private void updateEmojiSkinItemView(boolean isSystemStyle) {
        if (Build.VERSION.SDK_INT < 24 && isSystemStyle) {
            mSettingEmojiSkinItemView.setVisibility(GONE);
        } else {
            mSettingEmojiSkinItemView.setVisibility(View.VISIBLE);

            mSettingEmojiSkinItemView.setDefault(EmojiManager.getSkinDefault());
            mSettingEmojiSkinItemView.setOnItemClickListener(() -> {
                ChooseEmojiSkinDialog dialog = new ChooseEmojiSkinDialog();
                dialog.setOnDismissOrCancelListener(new BaseDialogFragment.OnDismissOrCancelListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mSettingEmojiSkinItemView.updateSkin(EmojiManager.getSkinDefault());
                    }

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });

                UiUtils.showDialogFragment(this, dialog);
                BugleAnalytics.logEvent("Settings_EmojiSkintone_Click");
            });
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
        String ringtoneName = "";
        try {
            RingtoneInfo info = RingtoneInfoManager.getCurSound();
            ringtoneName = info.name;
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

    private void updateVibrateSummary() {
        mVibrateView.setSummary(VibrateSettings.getVibrateDescription(null));
    }

    private void updateLedSummary() {
        mLedColorView.setSummary(LedSettings.getLedDescription(null));
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
        Intent ringtonePickerIntent = new Intent(this, RingtoneSettingActivity.class);
        ringtonePickerIntent.putExtra(RingtoneSettingActivity.EXTRA_CUR_RINGTONE_INFO, RingtoneInfoManager.getCurSound());
        ringtonePickerIntent.putExtra(RingtoneSettingActivity.EXTRA_FROM_PAGE, RingtoneSettingActivity.FROM_SETTING);

        Navigations.startActivityForResultSafely(SettingActivity.this,
                ringtonePickerIntent, REQUEST_CODE_RINGTONE_PICKER);
        overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);

        RingtoneEntranceAutopilotUtils.logSettingsRingtoneClick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RINGTONE_PICKER) {
            if (resultCode != RESULT_OK) {
                return;
            }
            if (data == null) {
                return;
            }
            RingtoneInfo info = data.getParcelableExtra(RingtoneSettingActivity.EXTRA_CUR_RINGTONE_INFO);
            RingtoneInfoManager.setCurSound(info);
            updateSoundSummary();
            HSLog.i("test_test", "onActivityResult: ");

        } else if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GeneralSettingSyncManager.overrideLocalData(() -> {
                    setUpNotificationView();
                    setUpPopUpsView();
                });
                Toasts.showToast(R.string.firebase_login_succeed);
                BugleAnalytics.logEvent("SyncSettings_LogIn_Success");
                mSyncSettingsView.setSummary(getString(R.string.firebase_sync_desktop_settings_description_logged_in));
            } else {
                Toasts.showToast(R.string.firebase_login_failed);
            }
        } else if (requestCode == EMOJI_STYLE_SET) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("name");
                String url = data.getStringExtra("url");
                mSettingEmojiStyleItemView.update(name, url);

                updateEmojiSkinItemView(name.equals(EmojiManager.EMOJI_STYLE_SYSTEM));
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

    @Override
    public void onTextSaved(String text) {
        refreshSignature();
    }
}


