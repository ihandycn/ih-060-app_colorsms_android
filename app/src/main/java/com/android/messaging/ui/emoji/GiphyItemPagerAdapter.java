package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.android.messaging.R;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import java.util.List;

public class GiphyItemPagerAdapter extends AbstractEmojiItemPagerAdapter {

    private static final int GIF_COLUMNS = 2;
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
        if (list != null && list.isEmpty()) {
            view = LayoutInflater.from(context).inflate(R.layout.sticker_item_no_recent_layout, container, false);
        } else {
            FrameLayout recyclerViewContainer = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.layout_giphy_page_item, container, false);
            RecyclerView recyclerView = recyclerViewContainer.findViewById(R.id.recycler_view);
            ProgressBar progressBar = recyclerViewContainer.findViewById(R.id.progress_bar);
            recyclerView.setPadding(0, Dimensions.pxFromDp(7), 0, 0);
            GiphyItemRecyclerAdapter adapter;

            GiphyItemRecyclerAdapter.OnDataFetchedListener onDataFetchedListener = new GiphyItemRecyclerAdapter.OnDataFetchedListener() {
                @Override
                public void onFetched() {
                    recyclerViewContainer.removeView(progressBar);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            };

            if (list == null) {
                adapter = new GiphyItemRecyclerAdapter(mOnEmojiClickListener, onDataFetchedListener, mData.get(position).mName);
            } else {
                adapter = new GiphyItemRecyclerAdapter(mOnEmojiClickListener, onDataFetchedListener, list);
            }

            final int itemOffsetInPixel = Dimensions.pxFromDp(2.5f);
            final int itemSideOffsetInPixel = Dimensions.pxFromDp(8.5f);
            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    super.getItemOffsets(outRect, view, parent, state);

                    int spanIndex = ((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();
                    if (spanIndex == 0) {
                        outRect.left = itemSideOffsetInPixel;
                        outRect.right = itemOffsetInPixel;
                    } else {
                        outRect.left = itemOffsetInPixel;
                        outRect.right = itemSideOffsetInPixel;
                    }

                    outRect.top = itemOffsetInPixel;
                    outRect.bottom = itemOffsetInPixel;
                }
            });

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                private boolean mIsSlideUpward;
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    int[] lastCompletelyVisibleItemPositions = new int[GIF_COLUMNS];

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        manager.findLastCompletelyVisibleItemPositions(lastCompletelyVisibleItemPositions);
                        int itemCount = manager.getItemCount();
                        boolean scrolledToBottom = false;
                        for (int i = 0; i < GIF_COLUMNS; i++) {
                            if (lastCompletelyVisibleItemPositions[i] == itemCount - 1) {
                                scrolledToBottom = true;
                            }
                        }
                        if (scrolledToBottom &&  mIsSlideUpward) {
                            adapter.loadMore();
                        }
                    }

                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    mIsSlideUpward = dy > 0;
                }
            });
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            StaggeredGridLayoutManager staggeredGridLayoutManager =
                    new StaggeredGridLayoutManager(GIF_COLUMNS, OrientationHelper.VERTICAL);
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
            view = recyclerViewContainer;
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
        if (mData == null || mData.isEmpty()) {
            return;
        }
        EmojiPackageInfo recentInfo = mData.get(0);
        if (recentInfo.mEmojiPackageType != EmojiPackageType.RECENT) {
            return;
        }
        mData.get(0).mEmojiInfoList.clear();
        recentInfo.mEmojiInfoList = EmojiManager.getRecentInfo(EmojiPackageType.GIF);
        updateSinglePage(0);
        updateTabView();
    }

    @Override
    public void updateTabView() {
        int count = mTabLayout.getTabCount();
        for (int i = 0; i < count; i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab == null) {
                return;
            }

            if (i == 0) {
                tab.setIcon(R.drawable.ic_gif_recent);
                tab.setText(null);
            } else {
                EmojiPackageInfo info = mData.get(i);
                tab.setText(info.mName);
            }
        }
    }
}
