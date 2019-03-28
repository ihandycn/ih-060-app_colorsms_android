package com.android.messaging.ui.welcome;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.messaging.R;
import com.superapps.view.TypefacedTextView;

import java.util.ArrayList;
import java.util.List;

class WelcomePagerAdapter extends PagerAdapter {

    private final static int GUIDE_VIEWPAGER_COUNT = 5;

    private final static int[] VIEWPAGER_ITEMS_IDS_TITLE = new int[]{
            R.string.welcome_viewpager_item_title_0,
            R.string.welcome_viewpager_item_title_1,
            R.string.welcome_viewpager_item_title_2,
            R.string.welcome_viewpager_item_title_3,
            R.string.welcome_viewpager_item_title_4,
    };

    private final static int[] VIEWPAGER_ITEMS_IDS_BODY = new int[]{
            R.string.welcome_viewpager_item_body_0,
            R.string.welcome_viewpager_item_body_1,
            R.string.welcome_viewpager_item_body_2,
            R.string.welcome_viewpager_item_body_3,
            R.string.welcome_viewpager_item_body_4,
    };

    private final List<LinearLayout> mItemList = new ArrayList<>();

    WelcomePagerAdapter(Context context) {
        for (int i = 0; i < GUIDE_VIEWPAGER_COUNT; i++) {
            LinearLayout item = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.item_welcome_guide_viewpager, null);
            TypefacedTextView title = item.findViewById(R.id.welcome_guide_viewpager_title);
            TypefacedTextView body = item.findViewById(R.id.welcome_guide_viewpager_body);

            title.setText(context.getResources().getString(VIEWPAGER_ITEMS_IDS_TITLE[i]));
            body.setText(context.getResources().getString(VIEWPAGER_ITEMS_IDS_BODY[i]));
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

    @DrawableRes int getImageResId() {
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
