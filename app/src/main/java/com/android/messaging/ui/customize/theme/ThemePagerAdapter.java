package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ThemePagerAdapter extends PagerAdapter {

    private final int mCount;

    private final List<View> mItemList;
    private final List<ThemeItem> mThemeItemList;
    private final ThemeItem mDefaultThemeItem;

    public ThemePagerAdapter(Context context) {
        mCount = 5;
        mItemList = new ArrayList<>(mCount);
        mThemeItemList = new ArrayList<>(mCount - 1);
        mDefaultThemeItem = new ThemeItem(ThemeName.THEME_DEFAULT, R.drawable.image_default_theme);

        mThemeItemList.add(new ThemeItem(ThemeName.THEME_DIAMOND, R.drawable.image_diamond_theme));
        mThemeItemList.add(new ThemeItem(ThemeName.THEME_NEON, R.drawable.image_neon_theme));
        mThemeItemList.add(new ThemeItem(ThemeName.THEME_SIMPLE, R.drawable.image_simple_theme));
        mThemeItemList.add(new ThemeItem(ThemeName.THEME_WATER_DROP, R.drawable.image_waterdrop_theme));

        Collections.shuffle(mThemeItemList);

        for (int i = 0; i < mCount; i++) {
            View item = LayoutInflater.from(context).inflate(R.layout.choose_theme_pager_item, null);
            ImageView imageView = item.findViewById(R.id.theme_preview_image);
            if (i == 0) {
                imageView.setImageResource(mDefaultThemeItem.mDrawableRes);
            } else {
                imageView.setImageResource(mThemeItemList.get(i - 1).mDrawableRes);
            }
            mItemList.add(item);
        }
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public ThemeInfo getThemeInfo(int index) {
        if (index == 0) {
            return mDefaultThemeItem.mThemeInfo;
        } else {
            return mThemeItemList.get(index - 1).mThemeInfo;
        }
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

    private static class ThemeItem {
        private ThemeInfo mThemeInfo;
        private int mDrawableRes;

        ThemeItem(String themeName, int drawableRes) {
            mThemeInfo = ThemeInfo.getThemeInfo(themeName);
            mDrawableRes = drawableRes;
        }
    }
}
