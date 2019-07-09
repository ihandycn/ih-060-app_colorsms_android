package com.android.messaging.ui.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class StickerItemPagerAdapter extends AbstractEmojiItemPagerAdapter{

    @SuppressWarnings("FieldCanBeLocal")
    private final int STICKER_COLUMNS = 4;
    @SuppressWarnings("FieldCanBeLocal")
    private final int STICKER_ROWS = 2;
    private TabLayout mTabLayout;

    private List<EmojiPackageInfo> mData;
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;
    private Context mContext;
    private boolean mIsFirst = true;

    StickerItemPagerAdapter(List<EmojiPackageInfo> data, Context context, EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
        mOnEmojiClickListener = emojiClickListener;
        mData = new ArrayList<>();
        if(data!=null){
            mData.addAll(data);
        }
        mContext = context;
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
        Context context = container.getContext();
        View view;
        List<BaseEmojiInfo> list = mData.get(position).mEmojiInfoList;
        if (list.isEmpty()) {
            view = LayoutInflater.from(context).inflate(R.layout.sticker_item_no_recent_layout, container, false);
        } else {
            RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.vertical_recycler_view, container, false);
            recyclerView.setPadding(0, 0, 0, Dimensions.pxFromDp(3f));
            recyclerView.addItemDecoration(new EmojiItemDecoration(STICKER_COLUMNS, Dimensions.pxFromDp(69), Dimensions.pxFromDp(20)));
            StickerItemRecyclerAdapter adapter = new StickerItemRecyclerAdapter(position, list, mOnEmojiClickListener);
            recyclerView.setAdapter(adapter);
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            recyclerView.setLayoutManager(new GridLayoutManager(context, STICKER_COLUMNS));
            view = recyclerView;
        }
        view.setTag(position+"");
        container.addView(view);
        return view;
    }

    public void initData(List<EmojiPackageInfo> infoList){
        for (int i = 0; i < infoList.size(); i++) {
            mData.get(i).mEmojiInfoList.clear();
            mData.get(i).mEmojiInfoList.addAll(infoList.get(i).mEmojiInfoList);
        }
        notifyDataSetChanged();
        if (mTabLayout != null) {
            updateTabView();
        }
    }

    @Override
    public void updateTabView() {
        int count = mTabLayout.getTabCount();
        int width = (int) (Dimensions.getPhoneWidth(mContext) / 9 + 0.5f);
        int primaryColor = PrimaryColors.getPrimaryColor();
        for (int i = 0; i < count; i++) {
            EmojiPackageInfo info = mData.get(i);
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(mContext).inflate(R.layout.emoji_tab_emoji_cateogry, mTabLayout, false);
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = width;
            view.setLayoutParams(lp);
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            ImageView tabIconView = view.findViewById(R.id.tab_icon_view);
            ImageView newTabView = view.findViewById(R.id.tab_new_view);
            View tabIndicator = view.findViewById(R.id.tab_indicator);
            tabIndicator.setBackgroundColor(primaryColor);
            if (EmojiManager.isNewTabSticker(info.mName)) {
                newTabView.setVisibility(View.VISIBLE);
            } else {
                newTabView.setVisibility(View.GONE);
            }
            // cancel cache to avoid showing wrong image
            GlideApp.with(mContext).load(info.mTabIconUrl).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.emoji_tab_normal_icon).into(tabIconView);
            if (tab != null) {
                tab.setCustomView(view);
                tab.setTag(info);
            }
        }
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getCustomView() == null)
                    return ;
                tab.getCustomView().findViewById(R.id.tab_indicator).setVisibility(View.VISIBLE);
                EmojiPackageInfo info = getPackageInfo(tab);
                if(info == null)
                    return ;
                if(info.mTabIconSelectedUrl != null) {
                    getImageView(tab).setImageURI(Uri.parse(info.mTabIconSelectedUrl));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                EmojiPackageInfo packageInfo = getPackageInfo(tab);
                if(tab.getCustomView() == null)
                    return ;
                if (packageInfo != null && EmojiManager.isNewTabSticker(packageInfo.mName)) {
                    EmojiManager.removeNewTabSticker(packageInfo.mName);
                    tab.getCustomView().findViewById(R.id.tab_new_view).setVisibility(View.GONE);
                }
                tab.getCustomView().findViewById(R.id.tab_indicator).setVisibility(View.GONE);
                EmojiPackageInfo info = getPackageInfo(tab);
                if(info == null)
                    return ;
                if(info.mTabIconSelectedUrl != null) {
                    getImageView(tab).setImageURI(Uri.parse(info.mTabIconUrl));
                }

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

        List<BaseEmojiInfo> emojiInfos = mData.get(0).mEmojiInfoList;
        if ((emojiInfos == null || emojiInfos.isEmpty()) && mIsFirst) {
            mTabLayout.getTabAt(1).select();
            mIsFirst = false;
        }
        mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).select();
    }


    void insertItem(int position, EmojiPackageInfo packageInfo) {
        if (position < 0) {
            position = 0;
        }
        if (mData.size() < position) {
            mData.add(packageInfo);
        }

        mData.add(position, packageInfo);
        notifyDataSetChanged();

        updateTabView();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public void updateRecentItem() {
        if(mData == null || mData.isEmpty())
            return ;
        EmojiPackageInfo recentInfo = mData.get(0);
        if(recentInfo.mEmojiPackageType != EmojiPackageType.RECENT)
            return ;
        recentInfo.mEmojiInfoList.clear();
        recentInfo.mEmojiInfoList.addAll(EmojiManager.getRecentInfo(EmojiPackageType.STICKER));
        updateSinglePage(0);
        updateTabView();

    }

    @Override
    public void setTabLayout(TabLayout tabLayout) {
        this.mTabLayout = tabLayout;
    }
}
