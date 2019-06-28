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
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class EmojiItemPagerAdapter extends AbstractEmojiItemPagerAdapter {

    private final int EMOJI_COLUMNS = 9;
    private final int EMOJI_ROWS = 4;
    private TabLayout mTabLayout;
    private Context mContext;

    private List<EmojiPackageInfo> mData = new ArrayList<>();
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;

    public EmojiItemPagerAdapter(Context context, List<EmojiPackageInfo> data, EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
        if (data == null || data.isEmpty()) {
            return;
        }
        mOnEmojiClickListener = emojiClickListener;
        mData.addAll(data);
        this.mContext = context;
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
            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setPadding(Dimensions.pxFromDp(6.7f), 0, Dimensions.pxFromDp(6.7f), 0);
            EmojiItemRecyclerAdapter adapter = new EmojiItemRecyclerAdapter(list, mOnEmojiClickListener);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(context, EMOJI_COLUMNS));
//            recyclerView.addItemDecoration(new EmojiItemDecoration(EMOJI_COLUMNS, EMOJI_ROWS, Dimensions.pxFromDp(29), Dimensions.pxFromDp(29)));
            view = recyclerView;
        }
        view.setTag(position + "");
        container.addView(view);
        return view;
    }

    public void initData(List<EmojiPackageInfo> infoList) {
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
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public void setTabLayout(TabLayout tabLayout) {
        this.mTabLayout = tabLayout;
    }

    @Override
    public void updateRecentItem() {
        if (mData == null || mData.isEmpty())
            return;
        EmojiPackageInfo recentInfo = mData.get(0);
        if (recentInfo.mEmojiPackageType != EmojiPackageType.RECENT)
            return;
        recentInfo.mEmojiInfoList.clear();
        recentInfo.mEmojiInfoList.addAll(EmojiManager.getRecentInfo(EmojiPackageType.EMOJI));
        updateSinglePage(0);
        updateTabView();
    }

    @Override
    public void updateTabView() {
        int count = mTabLayout.getTabCount();
        int width = (int) (Dimensions.getPhoneWidth(mContext) / (double) count + 0.5);
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
            newTabView.setVisibility(View.GONE);
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
                if (info == null){
                    return;
                }
                ImageView imageView = getImageView(tab);
                if (imageView == null) {
                    return;
                }
                imageView.setImageURI(Uri.parse(info.mTabIconSelectedUrl));
                View indicatorView = getIndicatorView(tab);
                if (indicatorView != null) {
                    indicatorView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                EmojiPackageInfo info = getPackageInfo(tab);
                if (info == null)
                    return;
                ImageView imageView = getImageView(tab);
                if (imageView == null)
                    return;
                imageView.setImageURI(Uri.parse(info.mTabIconUrl));
                View indicatorView = getIndicatorView(tab);
                if (indicatorView != null) {
                    indicatorView.setVisibility(View.GONE);
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

            private View getIndicatorView(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view == null)
                    return null;
                return view.findViewById(R.id.tab_indicator);
            }

        });
        mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).select();
    }
}
