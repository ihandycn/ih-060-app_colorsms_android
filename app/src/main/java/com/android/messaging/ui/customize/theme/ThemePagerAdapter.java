package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.welcome.WelcomeChooseThemeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThemePagerAdapter extends PagerAdapter {
    public interface OnPageClickedListener {
        void onPageClicked(int position);
    }

    private final int mCount;

    private final List<View> mItemList;
    private final List<ThemeInfo> mShuffledThemeItemList;
    private OnPageClickedListener mPageClickListener;

    public ThemePagerAdapter(Context context) {
        mShuffledThemeItemList = ThemeInfo.getLocalThemes();
        mCount = mShuffledThemeItemList.size();
        mItemList = new ArrayList<>(mCount);

        if (context instanceof WelcomeChooseThemeActivity) {
            Collections.sort(mShuffledThemeItemList, (t1, t2) -> t1.mLocalIndex - t2.mLocalIndex);
        }

        for (int i = 0; i < mCount; i++) {
            View item = LayoutInflater.from(context).inflate(R.layout.choose_theme_pager_item, null);

            ImageView imageView = item.findViewById(R.id.theme_preview_image);
            imageView.setImageDrawable(ThemeUtils.getLocalThemeDrawableFromPath(
                    mShuffledThemeItemList.get(i).mThemeKey
                            + "/" + mShuffledThemeItemList.get(i).mPreviewList.get(0)));
            mItemList.add(item);
            final int position = i;
            item.setOnClickListener(v -> {
                if (mPageClickListener != null) {
                    mPageClickListener.onPageClicked(position);
                }
            });
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
        return mShuffledThemeItemList.get(index);
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

    void setOnPageClickListener(OnPageClickedListener listener) {
        mPageClickListener = listener;
    }
}
