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

package com.android.messaging.ui.conversationlist;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerAdapter;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.Trace;
import com.android.messaging.util.UiUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class ConversationListActivity extends AbstractConversationListActivity {

    private TextView mTitleTextView;
    private View mSettingsBtn;

    private boolean mShowRateAlert = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Trace.beginSection("ConversationListActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_list_activity);
        Trace.endSection();

        initActionBar();
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        actionBar.setTitle("");
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setBackgroundDrawable(new ColorDrawable(
                getResources().getColor(R.color.action_bar_background_color)));
        actionBar.show();

        if (mTitleTextView != null && mTitleTextView.getVisibility() == View.GONE) {
            mTitleTextView.setVisibility(View.VISIBLE);
            mSettingsBtn.setVisibility(View.VISIBLE);
        }
        //update statusBar color
        UiUtils.setStatusBarColor(this, getResources().getColor(R.color.action_bar_background_color));

        super.updateActionBar(actionBar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FiveStarRateDialog.dismissDialogs();
    }

    @Override
    public void onBackPressed() {
        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
        } else {
            if (mShowRateAlert || !FiveStarRateDialog.showShowFiveStarRateDialogOnBackToDesktopIfNeed(this)) {
                super.onBackPressed();
            } else {
                mShowRateAlert = true;
            }
        }
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        mTitleTextView.setVisibility(View.GONE);
        mSettingsBtn.setVisibility(View.GONE);
        return super.startActionMode(callback);
    }

    @Override
    public void onActionBarHome() {
        exitMultiSelectState();
    }

    @Override
    public boolean isSwipeAnimatable() {
        return !isInConversationListSelectMode();
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        final ConversationListFragment conversationListFragment =
                (ConversationListFragment) getFragmentManager().findFragmentById(
                        R.id.conversation_list_fragment);
        // When the screen is turned on, the last used activity gets resumed, but it gets
        // window focus only after the lock screen is unlocked.
        if (hasFocus && conversationListFragment != null) {
            conversationListFragment.setScrolledToNewestConversationIfNeeded();
        }
    }

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        mTitleTextView = findViewById(R.id.toolbar_title);
        mSettingsBtn = findViewById(R.id.toolbar_img);
        mSettingsBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff,
                Dimensions.pxFromDp(20), true));
        mSettingsBtn.setOnClickListener(v -> {
            UIIntents.get().launchSettingsActivity(this);
            BugleAnalytics.logEvent("SMS_Mainpage_Settings_Click", true);
        });
        setSupportActionBar(toolbar);
        invalidateActionBar();
    }

}
