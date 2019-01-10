package com.android.messaging.ui;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.android.messaging.ui.conversationlist.ConversationListFragment;
import com.android.messaging.ui.emoji.EmojiStoreFragment;
import com.android.messaging.ui.smsshow.SmsShowListFragment;

public class BasePagerAdapter extends FragmentStatePagerAdapter {
    private static final int NUM_PAGES = 3;
    private ViewPager mViewPager;

    public BasePagerAdapter(FragmentManager fm, ViewPager viewPager) {
        super(fm);
        mViewPager = viewPager;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ConversationListFragment();
            case 1:
                return new SmsShowListFragment();
            case 2:
                EmojiStoreFragment storeFragment = EmojiStoreFragment.newInstance("tab");
                storeFragment.setViewPager(mViewPager);
                return storeFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}

