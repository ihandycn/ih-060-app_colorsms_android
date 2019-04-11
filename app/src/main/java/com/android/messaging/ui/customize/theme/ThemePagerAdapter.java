package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.superapps.view.MessagesTextView;

import java.util.ArrayList;
import java.util.List;


public class ThemePagerAdapter extends PagerAdapter {

    private final static int GUIDE_VIEWPAGER_COUNT = 3;

    private final static int[] VIEWPAGER_ITEMS_IDS_TITLE = new int[]{
            R.string.welcome_viewpager_item_title_0,
            R.string.welcome_viewpager_item_title_1,
            R.string.welcome_viewpager_item_title_2
    };

    private final List<View> mItemList = new ArrayList<>();

    public ThemePagerAdapter(Context context) {
        for (int i = 0; i < GUIDE_VIEWPAGER_COUNT; i++) {
            View item = (View) LayoutInflater.from(context)
                    .inflate(R.layout.choose_theme_pager_item, null);
            mItemList.add(item);
        }
    }

    @Override
    public int getCount() {
        return GUIDE_VIEWPAGER_COUNT;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @DrawableRes
    int getImageResId() {
        return 0;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mItemList.get(position));
        return mItemList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mItemList.get(position));
    }
}
