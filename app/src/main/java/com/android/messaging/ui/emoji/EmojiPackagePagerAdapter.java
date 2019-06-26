package com.android.messaging.ui.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
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
    private GiphyItemPagerAdapter mGiphyAdapter;
    private OnEmojiClickListener mOnEmojiClickListener;

    private final int DEFAULT_PAGE = 1;

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

    void updateRecentEmoji() {
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
        View view = LayoutInflater.from(container.getContext()).inflate(getLayoutRes(info), container, false);
        ViewPagerFixed itemPager = view.findViewById(R.id.emoji_item_pager);
        TabLayout itemTabLayout = view.findViewById(R.id.emoji_item_tab_layout);

        AbstractEmojiItemPagerAdapter adapter = getPagerAdapter(info);
        adapter.setTabLayout(itemTabLayout);
        itemPager.setAdapter(adapter);
        if(adapter instanceof EmojiItemPagerAdapter) {
            itemPager.setOffscreenPageLimit(10);
        }
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
        itemTabLayout.setSelectedTabIndicatorColor(PrimaryColors.getPrimaryColor());
        itemTabLayout.setupWithViewPager(itemPager);
        adapter.updateTabView();
        itemTabLayout.getTabAt(0).select();
        container.addView(view);

        if (adapter instanceof StickerItemPagerAdapter) {
            View storeBtn = view.findViewById(R.id.emoji_store_btn);
            storeBtn.setVisibility(View.VISIBLE);
            storeBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    mContext.getResources().getColor(R.color.white), Dimensions.pxFromDp(17), true));
            storeBtn.setOnClickListener(v -> {
                BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Store_Click", true, true, "type", "chat_tab");
                EmojiStoreActivity.start(container.getContext());
            });
        }

        return view;
    }

    @LayoutRes
    private int getLayoutRes(EmojiPackageInfo info) {
        switch (info.mEmojiPackageType) {
            case GIF:
                return R.layout.emoji_gif_page_item_layout;
            default:
                return R.layout.emoji_page_item_layout ;
        }

    }

    private AbstractEmojiItemPagerAdapter getPagerAdapter(EmojiPackageInfo info) {
        switch (info.mEmojiPackageType) {
            case STICKER:
                return mStickerAdapter;
            case EMOJI:
                return mEmojiAdapter;
            case GIF:
                return mGiphyAdapter;
            default:
                throw new IllegalStateException("There is no this type: " + info.mEmojiPackageType + "!!!");
        }
    }

    public void setData(Map<EmojiPackageType, List<EmojiPackageInfo>> data) {
        if (data.containsKey(EmojiPackageType.STICKER)) {
            mStickerAdapter = new StickerItemPagerAdapter(data.get(EmojiPackageType.STICKER), mContext, mOnEmojiClickListener);
        }
        if (data.containsKey(EmojiPackageType.EMOJI)) {
            mEmojiAdapter = new EmojiItemPagerAdapter(mContext, data.get(EmojiPackageType.EMOJI), mOnEmojiClickListener);
        }
        if (data.containsKey(EmojiPackageType.GIF)) {
            mGiphyAdapter = new GiphyItemPagerAdapter(mContext, data.get(EmojiPackageType.GIF), mOnEmojiClickListener);
        }
    }

    public EmojiItemPagerAdapter getEmojiAdapter() {
        return mEmojiAdapter;
    }

    public StickerItemPagerAdapter getStickerAdapter() {
        return mStickerAdapter;
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
        int primaryColors = PrimaryColors.getPrimaryColor();
        for (int i = 0; i < count; i++) {
            EmojiPackageInfo info = mData.get(i);
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(mContext).inflate(R.layout.emoji_tab_item_layout, null);
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            ImageView tabIconView = view.findViewById(R.id.tab_icon_view);
            tabIconView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    mContext.getResources().getColor(R.color.white), Dimensions.pxFromDp(20), true));
            tabIconView.setImageURI(Uri.parse(info.mTabIconUrl));
            if (tab != null) {
                tab.setCustomView(view);
                tab.setTag(info);
            }
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                EmojiPackageInfo info = getPackageInfo(tab);
                if (info == null)
                    return;

                BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Tab_Click", true, true, "type", info.mName);

                ImageView view = getImageView(tab);
                if (view == null)
                    return;
                view.setImageURI(Uri.parse(info.mTabIconSelectedUrl));
                view.getDrawable().setColorFilter(primaryColors, PorterDuff.Mode.SRC_ATOP);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                EmojiPackageInfo info = getPackageInfo(tab);
                if (info == null)
                    return;
                ImageView view = getImageView(tab);
                if (view == null)
                    return;
                view.setImageURI(Uri.parse(info.mTabIconUrl));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }

            private EmojiPackageInfo getPackageInfo(TabLayout.Tab tab) {
                Object object = tab.getTag();
                if (object instanceof EmojiPackageInfo) {
                    return (EmojiPackageInfo) object;
                }
                return null;
            }

            private ImageView getImageView(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view == null)
                    return null;
                return view.findViewById(R.id.tab_icon_view);
            }
        });


    }

    public interface OnEmojiClickListener {

        void emojiClick(EmojiInfo emojiInfo);

        void emojiLongClick(View view, EmojiInfo emojiInfo);

        void stickerClickExcludeMagic(StickerInfo stickerInfo);

        void gifClick(GiphyInfo gifInfo);

        void deleteEmoji();
    }
}
