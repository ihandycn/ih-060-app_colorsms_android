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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.superapps.view.ViewPagerFixed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmojiPackagePagerAdapter extends PagerAdapter {

    private List<EmojiPackageInfo> mData;
    private TabLayout mTabLayout;
    private Context mContext;
    private StickerItemPagerAdapter mStickerAdapter;
    private EmojiItemPagerAdapter mEmojiAdapter;
    private OnEmojiClickListener mOnEmojiClickListener;

    EmojiPackagePagerAdapter(Context context, TabLayout tabLayout, OnEmojiClickListener emojiClickListener) {
        mContext = context;
        mTabLayout = tabLayout;
        mOnEmojiClickListener = emojiClickListener;
        mData = new ArrayList<>();
    }

    void updateRecentSticker() {
        if (mStickerAdapter != null) {
            mStickerAdapter.updateRecentItem();
        }
    }

    void updateRecentEmoji(){
        if (mEmojiAdapter != null) {
            mEmojiAdapter.updateRecentItem();
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

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        EmojiPackageInfo info = mData.get(position);
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.emoji_page_item_layout, container, false);
        ViewPagerFixed itemPager = view.findViewById(R.id.emoji_item_pager);
        TabLayout itemTabLayout = view.findViewById(R.id.emoji_item_tab_layout);
        AbstractEmojiItemPagerAdapter adapter = getPagerAdapter(info);
        adapter.setTabLayout(itemTabLayout);
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
        itemTabLayout.setupWithViewPager(itemPager);
        adapter.updateTabView();
        container.addView(view);

        if(adapter instanceof StickerItemPagerAdapter) {
            View storeBtn = view.findViewById(R.id.emoji_store_btn);
            storeBtn.setVisibility(View.VISIBLE);
            storeBtn.setOnClickListener(v -> {
                BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Store_Click", true, true, "type", "chat_tab");
                EmojiStoreActivity.start(container.getContext());
            });
        }
        return view;
    }

    private AbstractEmojiItemPagerAdapter getPagerAdapter(EmojiPackageInfo info) {
        switch (info.mEmojiPackageType) {
            case STICKER:
                return mStickerAdapter;
            case EMOJI:
                return mEmojiAdapter;
            default:
                throw new IllegalStateException("There is no this type: " + info.mEmojiPackageType + "!!!");
        }
    }

    public void setData(Map<EmojiPackageType, List<EmojiPackageInfo>> data){
        if(data.containsKey(EmojiPackageType.STICKER)) {
            mStickerAdapter = new StickerItemPagerAdapter(data.get(EmojiPackageType.STICKER), mContext, mOnEmojiClickListener);
        }
        if(data.containsKey(EmojiPackageType.EMOJI)){
            mEmojiAdapter = new EmojiItemPagerAdapter(mContext, data.get(EmojiPackageType.EMOJI), mOnEmojiClickListener);
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

    public void updateTab(List<EmojiPackageInfo> dataList) {
        mData.clear();
        mData.addAll(dataList);
        notifyDataSetChanged();

        updateTabView();
    }

    void insertStickItem(int position, EmojiPackageInfo packageInfo) {
        mStickerAdapter.insertItem(position, packageInfo);
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
            GlideApp.with(mContext).load(info.mTabIconUrl).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.emoji_normal_tab_icon).into(tabIconView);
            if (tab != null) {
                tab.setCustomView(view);
                tab.setTag(info);
            }
        }
    }

    public interface OnEmojiClickListener {

        void emojiClick(EmojiInfo emojiInfo);

        void emojiLongClick(View view, EmojiInfo emojiInfo);

        void stickerClickExcludeMagic(StickerInfo stickerInfo);

        void deleteEmoji();
    }
}
