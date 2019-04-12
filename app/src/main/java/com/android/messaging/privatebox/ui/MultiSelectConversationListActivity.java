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
package com.android.messaging.privatebox.ui;

import android.app.Fragment;
import android.content.Intent;

import com.android.messaging.datamodel.data.ConversationListData;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.conversationlist.ConversationListItemView;
import com.android.messaging.util.BugleAnalytics;

import java.util.List;

/**
 * Base class for many Conversation List activities. This will handle the common actions of multi
 * select and common launching of intents.
 */
public abstract class MultiSelectConversationListActivity extends BugleActionBarActivity
        implements PrivateConversationListFragment.PrivateSelectModeHost {

    private static final int REQUEST_SET_DEFAULT_SMS_APP = 1;

    protected PrivateConversationListFragment mConversationListFragment;

    @Override
    public void onAttachFragment(final Fragment fragment) {
        // Fragment could be debug dialog
        if (fragment instanceof PrivateConversationListFragment) {
            mConversationListFragment = (PrivateConversationListFragment) fragment;
            mConversationListFragment.setHost(this);
        }
    }

    @Override
    public void onBackPressed() {
        // If action mode is active dismiss it
        if (getActionMode() != null) {
            dismissActionMode();
            return;
        }
        super.onBackPressed();
    }

    protected void startMultiSelectActionMode() {
        startActionMode(new PrivateMultiSelectActionModeCallback(this));
        BugleAnalytics.logEvent("PrivateBox_EditMode_Show");
    }

    public void exitMultiSelectState() {
        dismissActionMode();
        updateUi();
    }

    public abstract void updateUi();

    protected boolean isInConversationListSelectMode() {
        return getActionModeCallback() instanceof PrivateMultiSelectActionModeCallback;
    }

    @Override
    public boolean isSelectionMode() {
        return isInConversationListSelectMode();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    }

    @Override
    public void onConversationClick(final ConversationListData listData,
                                    final ConversationListItemData conversationListItemData,
                                    final boolean isLongClick,
                                    final ConversationListItemView conversationView) {
        if (isLongClick && !isInConversationListSelectMode()) {
            startMultiSelectActionMode();
        }

        if (isInConversationListSelectMode()) {
            final PrivateMultiSelectActionModeCallback multiSelectActionMode =
                    (PrivateMultiSelectActionModeCallback) getActionModeCallback();
            multiSelectActionMode.toggleSelect(listData, conversationListItemData);
            mConversationListFragment.updateUi();
        } else {
            BugleAnalytics.logEvent("PrivateBox_Messages_Click");
            final String conversationId = conversationListItemData.getConversationId();
            BugleAnalytics.logEvent("PrivateBox_DetailPage_Show", true);
            UIIntents.get().launchConversationActivity(
                    this, conversationId, null,
                    null,
                    false);
        }
    }

    public abstract List<SnackBarInteraction> getSnackBarInteractions();

    @Override
    public boolean isConversationSelected(final String conversationId) {
        return isInConversationListSelectMode() &&
                ((PrivateMultiSelectActionModeCallback) getActionModeCallback()).isSelected(conversationId);
    }
}
