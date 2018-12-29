package com.android.messaging.ui.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.superapps.view.ViewPagerFixed;

import java.util.ArrayList;
import java.util.List;

public class EmojiPackagePagerAdapter extends PagerAdapter {

    private List<EmojiPackageInfo> mData;
    private TabLayout mTabLayout;
    private Context mContext;

    EmojiPackagePagerAdapter(Context context, TabLayout tabLayout) {
        mContext = context;
        mTabLayout = tabLayout;
        mData = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        EmojiPackageInfo info = mData.get(position);
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.emoji_page_item_layout, container, false);
        ViewPagerFixed itemPager = view.findViewById(R.id.emoji_item_pager);
        ViewPagerDotIndicatorView dotIndicatorView = view.findViewById(R.id.dot_indicator_view);
        itemPager.addOnPageChangeListener(dotIndicatorView);
        PagerAdapter adapter = getPagerAdapter(info);
        itemPager.setAdapter(adapter);
        dotIndicatorView.initDot(adapter.getCount(), 0);
        container.addView(view);
        return view;
    }

    private PagerAdapter getPagerAdapter(EmojiPackageInfo info) {
        switch (info.mEmojiPackageType) {
            case STICKER:
                return new StickerItemPagerAdapter(info.mEmojiInfoList);
            case EMOJI:
                return new EmojiItemPagerAdapter(info.mEmojiInfoList);
            default:
                throw new IllegalStateException("There is no this type: " + info.mEmojiPackageType + "!!!");
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public void update(List<EmojiPackageInfo> dataList) {
        mData.clear();
        mData.addAll(dataList);
        notifyDataSetChanged();

        int count = mTabLayout.getTabCount();
        for (int i = 0; i < count; i++) {
            EmojiPackageInfo info = mData.get(i);
            View view = LayoutInflater.from(mContext).inflate(R.layout.emoji_tab_item_layout, null);
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            ImageView tabIconView = view.findViewById(R.id.tab_icon_view);
            if (info.mTabDrawableIconRes > 0) {
                tabIconView.setImageResource(info.mTabDrawableIconRes);
            }
            if (tab != null) {
                tab.setCustomView(view);
            }
        }
    }
}
