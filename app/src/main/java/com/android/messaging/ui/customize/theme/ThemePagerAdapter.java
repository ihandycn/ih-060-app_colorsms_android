package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.ui.welcome.WelcomeChooseThemeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThemePagerAdapter extends PagerAdapter {

    private final int mCount;

    private final List<View> mItemList;
    private final List<ThemeInfo> mShuffledThemeItemList;
    private ThemeInfo mCurrentTheme;

    public ThemePagerAdapter(Context context) {
        List<ThemeInfo> themeInfos = ThemeInfo.getLocalThemes();
        mCount = themeInfos.size();

        mItemList = new ArrayList<>(mCount);
        mShuffledThemeItemList = new ArrayList<>(mCount - 1);

        mCurrentTheme = themeInfos.get(0);

        String currentThemeName = ThemeUtils.getCurrentThemeName();

        for (ThemeInfo themeInfo : themeInfos) {
            if (currentThemeName.equals(themeInfo.mThemeKey)) {
                mCurrentTheme = themeInfo;
            } else {
                mShuffledThemeItemList.add(themeInfo);
            }
        }

        if (context instanceof WelcomeChooseThemeActivity) {
            Collections.shuffle(mShuffledThemeItemList);
        }

        for (int i = 0; i < mCount; i++) {
            View item = LayoutInflater.from(context).inflate(R.layout.choose_theme_pager_item, null);

            ImageView imageView = item.findViewById(R.id.theme_preview_image);
            TextView currentThemeTag = item.findViewById(R.id.current_theme_tag);
            if (i == 0) {
                imageView.setImageDrawable(ThemeUtils.getLocalThemeDrawableFromPath(
                        mCurrentTheme.mThemeKey + "/" + mCurrentTheme.mPreviewList.get(0)));
            } else {
                imageView.setImageDrawable(ThemeUtils.getLocalThemeDrawableFromPath(
                        mShuffledThemeItemList.get(i - 1).mThemeKey
                                + "/" + mShuffledThemeItemList.get(i - 1).mPreviewList.get(0)));
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
            return mCurrentTheme;
        } else {
            return mShuffledThemeItemList.get(index - 1);
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

}
