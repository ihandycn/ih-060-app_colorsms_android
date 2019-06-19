package com.android.messaging.ui.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.superapps.util.Dimensions;

import java.util.List;

public class EmojiItemPagerAdapter extends AbstractEmojiItemPagerAdapter{

    private final int EMOJI_COLUMNS = 8;
    private final int EMOJI_ROWS = 4;
    private TabLayout mTabLayout;
    private Context mContext;

    private List<EmojiPackageInfo> mData;
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;

    public EmojiItemPagerAdapter(Context context, List<EmojiPackageInfo> data, EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
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
        if(list.isEmpty()){
            view = LayoutInflater.from(context).inflate(R.layout.sticker_item_no_recent_layout, container, false);
        }else {
            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setPadding(Dimensions.pxFromDp(20), Dimensions.pxFromDp(17.7f), Dimensions.pxFromDp(20), Dimensions.pxFromDp(11.7f));
            EmojiItemRecyclerAdapter adapter = new EmojiItemRecyclerAdapter(list, mOnEmojiClickListener);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(context, EMOJI_COLUMNS));
            recyclerView.addItemDecoration(new EmojiItemDecoration(EMOJI_COLUMNS, EMOJI_ROWS, Dimensions.pxFromDp(29), Dimensions.pxFromDp(29)));
            view = recyclerView;
        }
        view.setTag(position+"");
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
        if(mData == null || mData.isEmpty())
            return ;
        EmojiPackageInfo recentInfo = mData.get(0);
        if(recentInfo.mEmojiPackageType != EmojiPackageType.RECENT)
            return ;
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
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(mContext).inflate(R.layout.emoji_tab_item_layout, null);
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            ImageView tabIconView = view.findViewById(R.id.tab_icon_view);
            ImageView newTabView = view.findViewById(R.id.tab_new_view);
            newTabView.setVisibility(View.GONE);
            GlideApp.with(mContext).load(info.mTabIconUrl).placeholder(R.drawable.emoji_normal_tab_icon).into(tabIconView);
            if (tab != null) {
                tab.setCustomView(view);
                tab.setTag(info);
            }
        }
    }
}
