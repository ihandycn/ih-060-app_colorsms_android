package com.android.messaging.ui.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.view.ViewPagerFixed;

import java.util.ArrayList;
import java.util.List;

public class EmojiPackagePagerAdapter extends PagerAdapter {

    private List<EmojiPackageInfo> mData;
    private TabLayout mTabLayout;
    private Context mContext;
    private StickerItemPagerAdapter mRecentPagerAdapter;
    private OnEmojiClickListener mOnEmojiClickListener;

    EmojiPackagePagerAdapter(Context context, TabLayout tabLayout, OnEmojiClickListener emojiClickListener) {
        mContext = context;
        mTabLayout = tabLayout;
        mOnEmojiClickListener = emojiClickListener;
        mData = new ArrayList<>();
    }

    void updateRecentItem() {
        if (mRecentPagerAdapter != null) {
            mRecentPagerAdapter.updateRecentItem();
        }
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
        itemPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int currentPosition = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position > currentPosition) {
                    BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Page_Slideleft", true);
                }
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        dotIndicatorView.initDot(adapter.getCount(), 0);
        container.addView(view);
        return view;
    }

    private PagerAdapter getPagerAdapter(EmojiPackageInfo info) {
        switch (info.mEmojiPackageType) {
            case STICKER:
                return new StickerItemPagerAdapter(info.mEmojiInfoList, mOnEmojiClickListener);
            case EMOJI:
                return new EmojiItemPagerAdapter(info.mEmojiInfoList, mOnEmojiClickListener);
            case RECENT:
                mRecentPagerAdapter = new StickerItemPagerAdapter(true, EmojiManager.getRecentStickerInfo(), mOnEmojiClickListener);
                return mRecentPagerAdapter;
            default:
                throw new IllegalStateException("There is no this type: " + info.mEmojiPackageType + "!!!");
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public void update(List<EmojiPackageInfo> dataList) {
        mData.clear();
        mData.addAll(dataList);
        notifyDataSetChanged();

        updateTabView();
    }

    void insertThirdItem(EmojiPackageInfo packageInfo) {
        if (mData.size() < 3) {
            mData.add(packageInfo);
        }
        mData.add(2, packageInfo);
        notifyDataSetChanged();

        updateTabView();
    }

    private void updateTabView() {
        int count = mTabLayout.getTabCount();
        for (int i = 0; i < count; i++) {
            EmojiPackageInfo info = mData.get(i);
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(mContext).inflate(R.layout.emoji_tab_item_layout, null);
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            ImageView tabIconView = view.findViewById(R.id.tab_icon_view);
            ImageView newTabView = view.findViewById(R.id.tab_new_view);
            if (EmojiManager.isNewTabSticker(info.mName)) {
                newTabView.setVisibility(View.VISIBLE);
            } else {
                newTabView.setVisibility(View.GONE);
            }
            GlideApp.with(mContext).load(info.mTabIconUrl).placeholder(R.drawable.emoji_normal_tab_icon).into(tabIconView);
            if (tab != null) {
                tab.setCustomView(view);
                tab.setTag(info);
            }
        }
    }

    public interface OnEmojiClickListener {

        void emojiClick(EmojiInfo emojiInfo);

        void stickerClickExcludeMagic(StickerInfo stickerInfo);

        void deleteEmoji();
    }
}
