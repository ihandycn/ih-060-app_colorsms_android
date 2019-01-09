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
import android.view.View;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.BasePagerAdapter;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.dialog.FiveStarRateDialog;
import com.android.messaging.util.Trace;
import com.android.messaging.util.UiUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class ConversationListActivity extends AbstractConversationListActivity
        implements BottomNavigationView.OnItemSelectedListener {

    private ViewPager mViewPager;
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
    protected void updateActionBar(final ActionBar actionBar) {
        actionBar.setTitle("");
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setBackgroundDrawable(new ColorDrawable(
                getResources().getColor(R.color.action_bar_background_color)));
        actionBar.show();

        //update statusBar color
        UiUtils.setStatusBarColor(this, getResources().getColor(R.color.action_bar_background_color));

        super.updateActionBar(actionBar);
    }

    private boolean showRate = false;

    @Override
    public void onBackPressed() {
        if (isInConversationListSelectMode()) {
            exitMultiSelectState();
        } else {
            if (showRate || !FiveStarRateDialog.showShowFiveStarRateDialogOnBackToDesktopIfNeed(this)) {
                super.onBackPressed();
            } else {
                showRate = true;
            }
        }
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
        View setting = findViewById(R.id.toolbar_img);
        setting.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff,
                Dimensions.pxFromDp(20), true));
        setting.setOnClickListener(v ->
                UIIntents.get().launchSettingsActivity(this));
        setSupportActionBar(toolbar);
        invalidateActionBar();
    }

    private void initPager() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        mViewPager = findViewById(R.id.fragment_pager);
        BasePagerAdapter mPagerAdapter = new BasePagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedPosition(0);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.setSelectedPosition(position);
            }
        });
    }
}
