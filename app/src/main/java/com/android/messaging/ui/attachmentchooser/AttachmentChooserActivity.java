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

package com.android.messaging.ui.attachmentchooser;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BugleActionBarActivity;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.attachmentchooser.AttachmentChooserFragment.AttachmentChooserFragmentHost;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.Assert;
import com.android.messaging.util.Typefaces;
import com.android.messaging.util.UiUtils;

public class AttachmentChooserActivity extends BugleActionBarActivity implements
        AttachmentChooserFragmentHost {

    private TextView mTitleTextView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attachment_chooser_activity);
        initActionBar();
    }

    @Override
    public void onAttachFragment(final Fragment fragment) {
        if (fragment instanceof AttachmentChooserFragment) {
            final String conversationId =
                    getIntent().getStringExtra(UIIntents.UI_INTENT_EXTRA_CONVERSATION_ID);
            Assert.notNull(conversationId);
            final AttachmentChooserFragment chooserFragment =
                    (AttachmentChooserFragment) fragment;
            chooserFragment.setConversationId(conversationId);
            chooserFragment.setHost(this);
        }
    }

    public void setActionBarTitle(String title) {
        if (mTitleTextView != null) {
            mTitleTextView.setText(title);
        }
    }

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        mTitleTextView = findViewById(R.id.toolbar_title);
        mTitleTextView.setTypeface(Typefaces.getCustomSemiBold());
        mTitleTextView.setText(R.string.attachment_chooser_activity_title);
        setSupportActionBar(toolbar);
        UiUtils.setTitleBarBackground(toolbar, this);
        invalidateActionBar();
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Reset the back arrow to its default
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        boolean superConsumed = super.onOptionsItemSelected(menuItem);
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return superConsumed;
    }

    @Override
    public void onConfirmSelection() {
        setResult(RESULT_OK);
        finish();
    }
}
