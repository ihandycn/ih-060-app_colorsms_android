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
import com.android.messaging.ui.appsettings.GeneralSettingItemView;
import com.android.messaging.util.Assert;

/**
 * The view for a single entry in the options section of people & options activity.
 */
public class PeopleOptionsItemView extends GeneralSettingItemView {
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
        super.blockSwitchAutoCheck();
        mSummaryView.setMaxLines(1);
        mSummaryView.setEllipsize(TextUtils.TruncateAt.END);
        mData = DataModel.get().createPeopleOptionsItemData(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnItemClickListener(() -> mHostInterface.onOptionsItemViewClicked(mData, !mData.getChecked()));
    }

    public void bind(final Cursor cursor,
                     int columnIndex,
                     ParticipantData otherParticipant,
                     final HostInterface hostInterface,
                     final String conversationId) {
        Assert.isTrue(columnIndex < PeopleOptionsItemData.SETTINGS_TOTAL_COUNT && columnIndex >= 0);

        mData.bind(cursor, otherParticipant, columnIndex, conversationId);

        mHostInterface = hostInterface;

        setViewType(mData.getType());
        setTitle(mData.getTitle());
        final String subtitle = mData.getSubtitle();
        if (!TextUtils.isEmpty(subtitle)) {
            setSummary(subtitle);
        }
        if (mData.getCheckable()) {
            setViewType(GeneralSettingItemView.SWITCH);
            setChecked(mData.getChecked());
        }
        final boolean enabled = mData.getEnabled();
        if (enabled != isEnabled()) {
            setEnabled(enabled);
            setEnable(enabled);
        }
    }

    public PeopleOptionsItemData getData() {
        return mData;
    }
}
