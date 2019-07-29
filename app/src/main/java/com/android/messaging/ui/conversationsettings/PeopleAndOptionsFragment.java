/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.ui.conversationsettings;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.action.PinConversationAction;
import com.android.messaging.datamodel.binding.Binding;
import com.android.messaging.datamodel.binding.BindingBase;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.datamodel.data.ParticipantListItemData;
import com.android.messaging.datamodel.data.PeopleAndOptionsData;
import com.android.messaging.datamodel.data.PeopleAndOptionsData.PeopleAndOptionsDataListener;
import com.android.messaging.datamodel.data.PeopleOptionsItemData;
import com.android.messaging.ui.BaseAlertDialog;
import com.android.messaging.ui.BaseDialogFragment;
import com.android.messaging.ui.PersonItemView;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.appsettings.BaseItemView;
import com.android.messaging.ui.appsettings.GeneralSettingItemView;
import com.android.messaging.ui.appsettings.SelectPrivacyModeDialog;
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.ui.wallpaper.WallpaperPreviewActivity;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BugleFirebaseAnalytics;
import com.android.messaging.util.UiUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Toasts;

import java.util.List;

import static com.android.messaging.datamodel.data.PeopleOptionsItemData.SETTINGS_PIN;
import static com.android.messaging.datamodel.data.PeopleOptionsItemData.SETTING_ADD_CONTACT;
import static com.android.messaging.datamodel.data.PeopleOptionsItemData.SETTING_DELETE;
import static com.android.messaging.datamodel.data.PeopleOptionsItemData.SETTING_NOTIFICATION_ENABLED;
import static com.android.messaging.datamodel.data.PeopleOptionsItemData.SETTING_NOTIFICATION_SOUND_URI;
import static com.android.messaging.datamodel.data.PeopleOptionsItemData.SETTING_NOTIFICATION_VIBRATION;
import static com.android.messaging.datamodel.data.PeopleOptionsItemData.SETTING_PRIVACY_MODE;
import static com.android.messaging.ui.conversation.ConversationActivity.DELETE_CONVERSATION_RESULT_CODE;
import static com.android.messaging.ui.conversation.ConversationActivity.FINISH_RESULT_CODE;
import static com.android.messaging.ui.conversation.ConversationFragment.EVENT_UPDATE_BUBBLE_DRAWABLE;

/**
 * Shows a list of participants of a conversation and displays options.
 */
public class PeopleAndOptionsFragment extends Fragment
        implements PeopleAndOptionsDataListener, PeopleOptionsItemView.HostInterface, WallpaperManager.WallpaperChangeListener {

    private final Binding<PeopleAndOptionsData> mBinding = BindingBase.createBinding(this);
    private String mConversationId;
    private String mRingtone;

    private static final int REQUEST_CODE_RINGTONE_PICKER = 1000;
    private ParticipantData mOtherParticipantData;  // TODO: 2019-07-26

    private GeneralSettingItemView mBubbleItemView;
    private GeneralSettingItemView mChatBgItemView;
    private GeneralSettingItemView mResetItemView;
    private PeopleOptionsItemView mNotificationItemView;
    private PeopleOptionsItemView mSoundItemView;
    private PeopleOptionsItemView mVibrateItemView;
    private PeopleOptionsItemView mPrivacyModeItemView;
    private PeopleOptionsItemView mPinItemView;
    private PeopleOptionsItemView mBlockItemView;
    private PeopleOptionsItemView mDeleteItemView;
    private ViewGroup mParticipantContainer;
    private Cursor mCursor;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.getData().init(getLoaderManager(), mBinding);
        WallpaperManager.addWallpaperChangeListener(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.people_and_options_fragment, container, false);
        mBubbleItemView = view.findViewById(R.id.setting_item_chat_bubble);
        mChatBgItemView = view.findViewById(R.id.setting_item_chat_background);
        mResetItemView = view.findViewById(R.id.setting_item_reset_customization);
        mNotificationItemView = view.findViewById(R.id.setting_item_notifications);
        mSoundItemView = view.findViewById(R.id.setting_item_sound);
        mVibrateItemView = view.findViewById(R.id.setting_item_vibrate);
        mPrivacyModeItemView = view.findViewById(R.id.setting_item_privacy_mode);
        mPinItemView = view.findViewById(R.id.setting_item_pin_to_top);
        mBlockItemView = view.findViewById(R.id.setting_item_block);
        mDeleteItemView = view.findViewById(R.id.setting_item_delete);
        mParticipantContainer = view.findViewById(R.id.participant_list_container);

        Activity activity = getActivity();
        mChatBgItemView.setOnItemClickListener(new BaseItemView.OnSettingItemClickListener() {
            @Override
            public void onClick() {
                WallpaperPreviewActivity.startWallpaperPreviewByConversationId(activity, mConversationId);
            }
        });
        mResetItemView.setOnItemClickListener(new BaseItemView.OnSettingItemClickListener() {
            @Override
            public void onClick() {
                new BaseAlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.setting_reset_customization_title))
                        .setPositiveButton(R.string.reset_customization_confirmation_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ConversationColors.get().resetConversationCustomization(mConversationId);
                                BubbleDrawables.resetConversationCustomization(mConversationId);
                                WallpaperManager.resetConversationCustomization(mConversationId);
                                BugleAnalytics.logEvent("Customize_Chat_Reset", true);

                                HSGlobalNotificationCenter.sendNotification(EVENT_UPDATE_BUBBLE_DRAWABLE);
                            }
                        })
                        .setNegativeButton(R.string.reset_customization_decline_button, null)
                        .show();
            }
        });

        mBubbleItemView.setOnItemClickListener(new BaseItemView.OnSettingItemClickListener() {
            @Override
            public void onClick() {
                UIIntents.get().launchCustomBubblesActivity(activity, mConversationId);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_RINGTONE_PICKER) {
            final Parcelable pick = data.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            final String pickedUri = pick == null ? "" : pick.toString();
            mBinding.getData().setConversationNotificationSound(mBinding, pickedUri);
            if (pickedUri != null && !pickedUri.equals(mRingtone)) {
                BugleAnalytics.logEvent("Customize_Notification_Sound_Change", true, "from", "chat");
                BugleFirebaseAnalytics.logEvent("Customize_Notification_Sound_Change", "from", "chat");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
        WallpaperManager.removeWallpaperChangeListener(this);
    }

    public void setConversationId(final String conversationId) {
        Assert.isTrue(getView() == null);
        Assert.notNull(conversationId);
        mBinding.bind(DataModel.get().createPeopleAndOptionsData(conversationId, getActivity(), this));
        mConversationId = conversationId;
    }

    @Override
    public void onOptionsCursorUpdated(final PeopleAndOptionsData data, final Cursor cursor) {
        mBinding.ensureBound(data);
        HSLog.d("conversation_setting_test", "onOptionCursorUpdated:  ");

        Cursor oldCursor = mCursor;
        if (cursor == oldCursor) {
            return;
        }
        mCursor = cursor;
        if (mCursor == null) {
            return;
        }
        cursor.moveToFirst();
        boolean isGroup = isGroup();
        boolean addContactVisble = addContactVisble();
        mPinItemView.bind(cursor, SETTINGS_PIN, mOtherParticipantData, PeopleAndOptionsFragment.this,
                isGroup, addContactVisble, mConversationId);
        mNotificationItemView.bind(cursor, SETTING_NOTIFICATION_ENABLED, mOtherParticipantData, PeopleAndOptionsFragment.this,
                isGroup, addContactVisble, mConversationId);
        mPrivacyModeItemView.bind(cursor, SETTING_PRIVACY_MODE, mOtherParticipantData, PeopleAndOptionsFragment.this,
                isGroup, addContactVisble, mConversationId);
        mSoundItemView.bind(cursor, SETTING_NOTIFICATION_SOUND_URI, mOtherParticipantData, PeopleAndOptionsFragment.this,
                isGroup, addContactVisble, mConversationId);
        mVibrateItemView.bind(cursor, SETTING_NOTIFICATION_VIBRATION, mOtherParticipantData, PeopleAndOptionsFragment.this,
                isGroup, addContactVisble, mConversationId);
        mDeleteItemView.bind(cursor, SETTING_DELETE, mOtherParticipantData, PeopleAndOptionsFragment.this,
                isGroup, addContactVisble, mConversationId);
    }

    @Override
    public void onParticipantsListLoaded(final PeopleAndOptionsData data,
                                         final List<ParticipantData> participants) {
        mBinding.ensureBound(data);
        HSLog.d("conversation_setting_test", "onParticipantsListLoaded:  " + participants.size());
        Activity activity = getActivity();
        for (ParticipantData item : participants) {
            PersonItemView itemView = (PersonItemView) LayoutInflater.from(activity).inflate(R.layout.people_list_item_view, mParticipantContainer, false);
            ParticipantListItemData itemData = DataModel.get().createParticipantListItemData(item);
            itemView.bind(itemData);
            mParticipantContainer.addView(itemView);
        }
        // TODO: 2019-07-29 bind mBlockItemView
        mOtherParticipantData = participants.size() == 1 ?
                participants.get(0) : null;
    }

    @Override
    public void onOptionsItemViewClicked(final PeopleOptionsItemData item,
                                         final boolean isChecked) {
        switch (item.getItemId()) {
            case SETTINGS_PIN:
                if (isChecked) {
                    BugleAnalytics.logEvent("SMS_Detailspage_Settings_PinToTop_Click", "Switch", "Pin");
                    PinConversationAction.pinConversation(mConversationId);
                } else {
                    BugleAnalytics.logEvent("SMS_Detailspage_Settings_PinToTop_Click", "Switch", "UnPin");
                    PinConversationAction.unpinConversation(mConversationId);
                }
                break;

            case PeopleOptionsItemData.SETTING_NOTIFICATION_ENABLED:
                mBinding.getData().enableConversationNotifications(mBinding, isChecked);
                break;

            case PeopleOptionsItemData.SETTING_PRIVACY_MODE:
                SelectPrivacyModeDialog selectPrivacyModeDialog = SelectPrivacyModeDialog.newInstance(mConversationId);
                selectPrivacyModeDialog.setOnDismissOrCancelListener(new BaseDialogFragment.OnDismissOrCancelListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // TODO: 2019-07-26
                    }

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });
                UiUtils.showDialogFragment(getActivity(), selectPrivacyModeDialog);
                break;

            case SETTING_NOTIFICATION_SOUND_URI:
                mRingtone = item.getRingtoneUri() == null ? "" : item.getRingtoneUri().toString();
                final Intent ringtonePickerIntent = UIIntents.get().getRingtonePickerIntent(
                        getString(R.string.notification_sound_pref_title),
                        item.getRingtoneUri(), Settings.System.DEFAULT_NOTIFICATION_URI,
                        RingtoneManager.TYPE_NOTIFICATION);

                try {
                    startActivityForResult(ringtonePickerIntent, REQUEST_CODE_RINGTONE_PICKER);
                } catch (ActivityNotFoundException | SecurityException e) {
                    Toasts.showToast(com.superapps.R.string.setting_device_not_support_message);
                }
                break;

            case SETTING_NOTIFICATION_VIBRATION:
                mBinding.getData().enableConversationNotificationVibration(mBinding,
                        isChecked);
                break;

            case SETTING_ADD_CONTACT:
                BugleAnalytics.logEvent("SMS_Detailspage_Settings_AddContact_Click");
                UIIntents.get().launchAddContactActivity(getActivity(), mOtherParticipantData.getDisplayDestination());
                break;

            case PeopleOptionsItemData.SETTING_BLOCKED:
                if (item.getOtherParticipant().isBlocked()) {
                    mBinding.getData().setDestinationBlocked(mBinding, false);
                    break;
                }
                final Resources res = getResources();
                final Activity activity = getActivity();

                new BaseAlertDialog.Builder(activity)
                        .setTitle(res.getString(R.string.block_confirmation_title,
                                item.getOtherParticipant().getDisplayDestination()))
                        .setMessage(res.getString(R.string.block_confirmation_message))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                (arg0, arg1) -> {
                                    mBinding.getData().setDestinationBlocked(mBinding, true);
                                    activity.setResult(FINISH_RESULT_CODE);
                                    activity.finish();
                                })
                        .show();
                break;
            case SETTING_DELETE:
                BugleAnalytics.logEvent("SMS_Detailspage_Settings_Delete_Click");
                new BaseAlertDialog.Builder(getActivity())
                        .setTitle(getResources().getQuantityString(
                                R.plurals.delete_conversations_confirmation_dialog_title, 1))
                        .setPositiveButton(R.string.delete_conversation_confirmation_button,
                                (dialog, button) -> deleteConversation())
                        .setNegativeButton(R.string.delete_conversation_decline_button, null)
                        .show();
                break;
        }
    }

    private void deleteConversation() {
        if (getActivity() != null) {
            getActivity().setResult(DELETE_CONVERSATION_RESULT_CODE);
            getActivity().finish();
        }
    }

    @Override
    public void onWallpaperChanged() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onOnlineWallpaperChanged() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private boolean addContactVisble() {
        return (mOtherParticipantData != null
                && TextUtils.isEmpty(mOtherParticipantData.getLookupKey()));
    }

    private boolean isGroup() {
        return mOtherParticipantData == null;
    }

}
