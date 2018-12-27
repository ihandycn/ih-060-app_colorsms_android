package com.android.messaging.ui;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.android.messaging.ui.conversationlist.ConversationListFragment;
import com.android.messaging.ui.emoji.EmojiFragment;
import com.android.messaging.ui.smsshow.SmsShowListFragment;

public class BasePagerAdapter extends FragmentStatePagerAdapter {
    private static final int NUM_PAGES = 3;

    public BasePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0 :
                return new ConversationListFragment();
            case 1:
                return new SmsShowListFragment();
            case 2:
                return new EmojiFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}

