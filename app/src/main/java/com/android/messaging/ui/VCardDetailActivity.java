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
package com.android.messaging.ui;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.util.Assert;
import com.android.messaging.util.UiUtils;

/**
 * An activity that hosts VCardDetailFragment that shows the content of a VCard that contains one
 * or more contacts.
 */
public class VCardDetailActivity extends BugleActionBarActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vcard_detail_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.setTitleBarBackground(toolbar, this);
        TextView title = toolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.vcard_detail_activity_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public void onAttachFragment(final Fragment fragment) {
        try {
            Assert.isTrue(fragment instanceof VCardDetailFragment);
            final Uri vCardUri = getIntent().getParcelableExtra(UIIntents.UI_INTENT_EXTRA_VCARD_URI);
            Assert.notNull(vCardUri);
            final VCardDetailFragment vCardDetailFragment = (VCardDetailFragment) fragment;
            vCardDetailFragment.setVCardUri(vCardUri);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Treat the home press as back press so that when we go back to
                // ConversationActivity, it doesn't lose its original intent (conversation id etc.)
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
