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

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.data.ParticipantData;
import com.android.messaging.datamodel.data.PeopleOptionsItemData;
import com.android.messaging.ui.appsettings.SettingItemView;
import com.android.messaging.util.Assert;
import com.android.messaging.util.OsUtil;

import static com.android.messaging.datamodel.data.PeopleOptionsItemData.SETTING_BLOCKED;

/**
 * The view for a single entry in the options section of people & options activity.
 */
public class PeopleOptionsItemView extends SettingItemView {
    /**
     * Implemented by the host of this view that handles options click event.
     */
    public interface HostInterface {
        void onOptionsItemViewClicked(PeopleOptionsItemData item, boolean isChecked);
    }

    private final PeopleOptionsItemData mData;
    private HostInterface mHostInterface;

    public PeopleOptionsItemView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mData = DataModel.get().createPeopleOptionsItemData(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnItemClickListener(() -> mHostInterface.onOptionsItemViewClicked(mData, !mData.getChecked()));
    }

    public void bind(final Cursor cursor, int columnIndex, ParticipantData otherParticipant,
                     final HostInterface hostInterface) {
        Assert.isTrue(columnIndex < PeopleOptionsItemData.SETTINGS_COUNT && columnIndex >= 0);
        if (OsUtil.isAtLeastO() && columnIndex == 1) {
            columnIndex = SETTING_BLOCKED;
        }


        mData.bind(cursor, otherParticipant, columnIndex);
        mHostInterface = hostInterface;

        setTitle(mData.getTitle());
        final String subtitle = mData.getSubtitle();
        if (!TextUtils.isEmpty(subtitle)) {
            setSummary(subtitle);
        }

        if (mData.getCheckable()) {
            setViewType(SettingItemView.SWITCH);
            setChecked(mData.getChecked());
        }

        final boolean enabled = mData.getEnabled();
        if (enabled != isEnabled()) {
            setEnabled(enabled);
            setEnable(enabled);
        }
    }
}
