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
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerAdapter;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.DebugUtils;
import com.android.messaging.util.Trace;
import com.android.messaging.util.UiUtils;

public class ConversationListActivity extends AbstractConversationListActivity
        implements BottomNavigationView.OnItemSelectedListener {

    private ViewPager mViewPager;
    private BasePagerAdapter mPagerAdapter;
    private TextView mTitleTextView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Trace.beginSection("ConversationListActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_list_activity);
        Trace.endSection();

        initActionBar();
        initPager();
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        // set custom title visibility gone, when start MultiSelectActionMode etc.
        mTitleTextView.setVisibility(View.GONE);
        return super.startActionMode(callback);
    }

    @Override
    protected void updateActionBar(final ActionBar actionBar) {
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setBackgroundDrawable(new ColorDrawable(
                getResources().getColor(R.color.action_bar_background_color)));
        actionBar.show();

        //update statusBar color
        UiUtils.setStatusBarColor(this, getResources().getColor(R.color.action_bar_background_color));

        mTitleTextView.setVisibility(View.VISIBLE);
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
            case R.id.action_show_blocked_contacts:
                onActionBarBlockedParticipants();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onSelected(int position) {
        switch (position) {
            case BottomNavigationView.POSITION_MESSAGING:
                mViewPager.setCurrentItem(0);
                mTitleTextView.setText(R.string.bottom_navigation_item_messaging);
                break;
            case BottomNavigationView.POSITION_SMS_SHOW:
                if (isInConversationListSelectMode()) {
                    exitMultiSelectState();
                }
                mViewPager.setCurrentItem(1);
                mTitleTextView.setText(R.string.bottom_navigation_item_sms_show);
                break;
            case BottomNavigationView.POSITION_EMOJI:
                if (isInConversationListSelectMode()) {
                    exitMultiSelectState();
                }
                mViewPager.setCurrentItem(2);
                mTitleTextView.setText(R.string.bottom_navigation_item_emoji);
                break;
        }
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

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        mTitleTextView = findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        invalidateActionBar();
    }

    private void initPager() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        mViewPager = findViewById(R.id.fragment_pager);
        mPagerAdapter = new BasePagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedPositon(0);
    }
}
