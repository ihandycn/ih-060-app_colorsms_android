package com.android.messaging.ui.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.emoji.utils.EmojiConfig;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.List;

public class GiphyItemPagerAdapter extends AbstractEmojiItemPagerAdapter {

    private final int EMOJI_COLUMNS = 2;
    private final int EMOJI_ROWS = 4;
    private TabLayout mTabLayout;
    private Context mContext;

    private List<EmojiPackageInfo> mData;
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;

    public GiphyItemPagerAdapter(Context context, List<EmojiPackageInfo> data, EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
        if (data == null || data.isEmpty()) {
            return;
        }
        mOnEmojiClickListener = emojiClickListener;
        mData = data;
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
//            recyclerView.setPadding(Dimensions.pxFromDp(7), Dimensions.pxFromDp(7), Dimensions.pxFromDp(7), 0);
            GiphyItemRecyclerAdapter adapter = new GiphyItemRecyclerAdapter(mOnEmojiClickListener);
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            StaggeredGridLayoutManager staggeredGridLayoutManager =
                    new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL);
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
            view = recyclerView;
        }
        view.setTag(position + "");
        container.addView(view);
        return view;
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
        mData.get(0).mEmojiInfoList.clear();
        recentInfo.mEmojiInfoList = EmojiManager.getRecentInfo(EmojiPackageType.EMOJI);
        updateSinglePage(0);
        updateTabView();
    }


    @Override
    public void updateTabView() {
        int count = mTabLayout.getTabCount();
        for (int i = 0; i < count; i++) {
            EmojiPackageInfo info = mData.get(i);
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab == null) {
                return;
            }

            if (i == 0) {
                tab.setIcon(R.drawable.emoji_recent_tab_icon);
                tab.setText(null);
            } else {
                tab.setText("cats");
            }


        }
    }
}
