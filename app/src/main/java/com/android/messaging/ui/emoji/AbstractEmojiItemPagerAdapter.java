package com.android.messaging.ui.emoji;

import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;

public abstract class AbstractEmojiItemPagerAdapter extends PagerAdapter {

    public abstract void setTabLayout(TabLayout tabLayout);
    public abstract void updateRecentItem();
    public abstract void updateTabView();
}
