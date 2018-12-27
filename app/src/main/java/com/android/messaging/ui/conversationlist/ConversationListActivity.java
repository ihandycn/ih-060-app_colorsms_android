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
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerAdapter;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.DebugUtils;
import com.android.messaging.util.Trace;

public class ConversationListActivity extends AbstractConversationListActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ViewPager mViewPager;
    private BasePagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Trace.beginSection("ConversationListActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_list_activity);
        Trace.endSection();
        invalidateActionBar();
        initPager();
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setBackgroundDrawable(new ColorDrawable(
                getResources().getColor(R.color.action_bar_background_color)));
        actionBar.show();
        super.updateActionBar(actionBar);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Invalidate the menu as items that are based on settings may have changed
        // while not in the app (e.g. Talkback enabled/disable affects new conversation
        // button)
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (super.onCreateOptionsMenu(menu)) {
            return true;
        }
        getMenuInflater().inflate(R.menu.conversation_list_fragment_menu, menu);
        final MenuItem item = menu.findItem(R.id.action_debug_options);
        if (item != null) {
            final boolean enableDebugItems = DebugUtils.isDebugEnabled();
            item.setVisible(enableDebugItems).setEnabled(enableDebugItems);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_start_new_conversation:
                onActionBarStartNewConversation();
                return true;
            case R.id.action_settings:
                onActionBarSettings();
                return true;
            case R.id.action_debug_options:
                onActionBarDebug();
                return true;
            case R.id.action_show_archived:
                onActionBarArchived();
                return true;
            case R.id.action_show_blocked_contacts:
                onActionBarBlockedParticipants();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.messaging:
                mViewPager.setCurrentItem(0);
                return true;
            case R.id.sms_show:
                if (isInConversationListSelectMode()) {
                    exitMultiSelectState();
                }
                mViewPager.setCurrentItem(1);
                return true;
            case R.id.emoji:
                if (isInConversationListSelectMode()) {
                    exitMultiSelectState();
                }
                mViewPager.setCurrentItem(2);
                return true;
        }
        return false;
    }

    @Override
    public void onActionBarHome() {
        exitMultiSelectState();
    }

    public void onActionBarStartNewConversation() {
        UIIntents.get().launchCreateNewConversationActivity(this, null);
    }

    public void onActionBarSettings() {
        UIIntents.get().launchSettingsActivity(this);
    }

    public void onActionBarBlockedParticipants() {
        UIIntents.get().launchBlockedParticipantsActivity(this);
    }

    public void onActionBarArchived() {
        UIIntents.get().launchArchivedConversationsActivity(this);
    }

    @Override
    public boolean isSwipeAnimatable() {
        return !isInConversationListSelectMode();
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // When the screen is turned on, the last used activity gets resumed, but it gets
        // window focus only after the lock screen is unlocked.
//        if (hasFocus && mViewPager.getCurrentItem() == 0) {
//            ((ConversationListFragment) mPagerAdapter.getItem(0)).setScrolledToNewestConversationIfNeeded();
//        }
    }

    private void initPager() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        mViewPager = findViewById(R.id.fragment_pager);
        mPagerAdapter = new BasePagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }
}
