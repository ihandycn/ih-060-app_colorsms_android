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
package com.android.messaging.datamodel.data;

import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.android.messaging.R;
import com.android.messaging.datamodel.data.ConversationListItemData.ConversationListViewColumns;
import com.android.messaging.ui.appsettings.GeneralSettingItemView;
import com.android.messaging.ui.appsettings.PrivacyModeSettings;
import com.android.messaging.ui.appsettings.VibrateSettings;
import com.android.messaging.util.Assert;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.RingtoneUtil;
import com.ihs.app.framework.HSApplication;

public class PeopleOptionsItemData {
    public static final String[] PROJECTION = {
            ConversationListViewColumns.NOTIFICATION_ENABLED,
            ConversationListViewColumns.NOTIFICATION_SOUND_URI,
            ConversationListViewColumns.NOTIFICATION_VIBRATION,
            ConversationListViewColumns.PIN_TIMESTAMP,
            ConversationListViewColumns.NAME
    };

    // Column index for query projection.
    private static final int INDEX_NOTIFICATION_ENABLED = 0;
    private static final int INDEX_NOTIFICATION_SOUND_URI = 1;
    private static final int INDEX_NOTIFICATION_VIBRATION = 2;
    private static final int INDEX_PIN_TIMESTAMP = 3;
    private static final int INDEX_GROUP_NAME = 4;

    // Identification for each setting that's surfaced to the UI layer.
    public static final int SETTINGS_PIN = 0;
    public static final int SETTING_NOTIFICATION_ENABLED = 1;
    public static final int SETTING_PRIVACY_MODE = 2;
    public static final int SETTING_NOTIFICATION_SOUND_URI = 3;
    public static final int SETTING_NOTIFICATION_VIBRATION = 4;
    public static final int SETTING_ADD_CONTACT = 5;
    public static final int SETTING_BLOCKED = 6;
    public static final int SETTING_DELETE = 7;
    public static final int SETTING_RENAME_GROUP = 8;
    public static final int SETTINGS_COUNT = 9;


    // Type of UI switch to show for the toggle button.
    public static final int TOGGLE_TYPE_CHECKBOX = 0;
    public static final int TOGGLE_TYPE_SWITCH = 1;

    private String mTitle;
    private String mSubtitle;
    private Uri mRingtoneUri;
    private boolean mCheckable;
    private boolean mChecked;
    private boolean mEnabled;
    private int mType;
    private int mItemId;
    private ParticipantData mOtherParticipant;

    private final Context mContext;

    public PeopleOptionsItemData(final Context context) {
        mContext = context;
    }

    /**
     * Bind to a specific setting column on conversation metadata cursor. (Note
     * that it binds to columns because it treats individual columns of the cursor as
     * separate options to display for the conversation, e.g. notification settings).
     */
    public void bind(
            final Cursor cursor, final ParticipantData otherParticipant, final int settingType, String conversationId) {
        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        mSubtitle = null;
        mRingtoneUri = null;
        mCheckable = true;
        mEnabled = true;
        mItemId = settingType;
        mOtherParticipant = otherParticipant;

        final boolean notificationEnabled = cursor.getInt(INDEX_NOTIFICATION_ENABLED) == 1;
        switch (settingType) {
            case SETTING_NOTIFICATION_ENABLED:
                mTitle = mContext.getString(R.string.notifications_enabled_conversation_pref_title);
                mChecked = notificationEnabled;
                mType = GeneralSettingItemView.SWITCH;
                break;

            case SETTING_PRIVACY_MODE:
                mTitle = mContext.getString(R.string.privacy_mode_settings);
                mSubtitle = PrivacyModeSettings.getPrivacyModeDescription(conversationId);
                mEnabled = notificationEnabled;
                mCheckable = false;
                mType = GeneralSettingItemView.NORMAL;
                break;

            case SETTING_NOTIFICATION_SOUND_URI:
                mTitle = mContext.getString(R.string.notification_sound_pref_title);
                final String ringtoneString = cursor.getString(INDEX_NOTIFICATION_SOUND_URI);
                Uri ringtoneUri = RingtoneUtil.getNotificationRingtoneUri(ringtoneString);
                mSubtitle = mContext.getString(R.string.silent_ringtone);
                try {
                    if (ringtoneUri != null) {
                        final Ringtone ringtone = RingtoneManager.getRingtone(mContext, ringtoneUri);
                        if (ringtone != null) {
                            mSubtitle = ringtone.getTitle(mContext);
                        }
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                mCheckable = false;
                mRingtoneUri = ringtoneUri;
                mEnabled = notificationEnabled;
                mType = GeneralSettingItemView.NORMAL;
                break;

            case SETTING_NOTIFICATION_VIBRATION:
                mTitle = mContext.getString(R.string.notification_vibrate_pref_title);
                mSubtitle = VibrateSettings.getVibrateDescription(conversationId);
                mCheckable = false;
                mEnabled = notificationEnabled;
                mType = GeneralSettingItemView.NORMAL;
                break;

            case SETTING_BLOCKED:
                Assert.notNull(otherParticipant);
                final int resourceId = otherParticipant.isBlocked() ?
                        R.string.tap_to_unblock_message : R.string.action_block;
                mTitle = mContext.getString(resourceId);
                mCheckable = false;
                mType = GeneralSettingItemView.NORMAL;
                break;
            case SETTINGS_PIN:
                mTitle = mContext.getString(R.string.action_pin);
                mChecked = cursor.getInt(INDEX_PIN_TIMESTAMP) != 0;
                mType = GeneralSettingItemView.SWITCH;
                break;

            case SETTING_ADD_CONTACT:
                mTitle = mContext.getString(R.string.action_add_contact);
                mCheckable = false;
                mType = GeneralSettingItemView.NORMAL;
                break;

            case SETTING_DELETE:
                mTitle = mContext.getString(R.string.action_delete);
                mCheckable = false;
                mType = GeneralSettingItemView.NORMAL;
                break;

            case SETTING_RENAME_GROUP:
                mTitle = mContext.getString(R.string.action_rename_group_chat);
                mSubtitle = cursor.getString(INDEX_GROUP_NAME);
                mCheckable = false;
                mType = GeneralSettingItemView.NORMAL;
                break;

            default:
                Assert.fail("Unsupported conversation option type!");
                break;
        }
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public boolean getCheckable() {
        return mCheckable;
    }

    public boolean getChecked() {
        return mChecked;
    }

    public boolean getEnabled() {
        return mEnabled;
    }

    public int getItemId() {
        return mItemId;
    }

    public int getType(){
        return mType;
    }

    public Uri getRingtoneUri() {
        return mRingtoneUri;
    }

    public ParticipantData getOtherParticipant() {
        return mOtherParticipant;
    }
}
