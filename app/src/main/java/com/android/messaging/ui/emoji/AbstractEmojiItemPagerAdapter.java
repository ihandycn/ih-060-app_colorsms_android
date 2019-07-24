package com.android.messaging.ui.emoji;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import java.util.List;

public abstract class AbstractEmojiItemPagerAdapter extends PagerAdapter {

    private int mNeedUpdatePagePosition = -1;

    @Override
    public int getItemPosition(@NonNull Object object) {
        View view = (View)object;
        // if want to update a single page, you must set position in view's tag
        if((view.getTag() != null && view.getTag().equals(mNeedUpdatePagePosition+"")) || mNeedUpdatePagePosition < 0){
            return POSITION_NONE;
        }else{
            return POSITION_UNCHANGED;
        }
    }

    public void updateSinglePage(int position){
        mNeedUpdatePagePosition = position;
        notifyDataSetChanged();
        mNeedUpdatePagePosition = -1;
    }

    public abstract void setTabLayout(TabLayout tabLayout);
    public abstract void updateRecentItem();
    public abstract void updateTabView();
    // lazy load
    public abstract void loadData(List<EmojiPackageInfo> infoList);
}
