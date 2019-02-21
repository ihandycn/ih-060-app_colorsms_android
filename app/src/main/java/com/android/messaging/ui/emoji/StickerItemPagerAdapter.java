package com.android.messaging.ui.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.superapps.util.Dimensions;

import org.qcode.fontchange.impl.FontManagerImpl;

import java.util.ArrayList;
import java.util.List;

public class StickerItemPagerAdapter extends PagerAdapter {

    private final int STICKER_COLUMNS = 4;
    private final int STICKER_ROWS = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final int STICKER_COUNT_ONE_PAGE = STICKER_COLUMNS * STICKER_ROWS;

    private List<List<BaseEmojiInfo>> mData;
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;
    private boolean mIsRecentPage;

    StickerItemPagerAdapter(List<BaseEmojiInfo> data, EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
        this(false, data, emojiClickListener);
    }

    StickerItemPagerAdapter(boolean isRecentPage, List<BaseEmojiInfo> data, EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
        mOnEmojiClickListener = emojiClickListener;
        mIsRecentPage = isRecentPage;
        if (data == null || data.isEmpty()) {
            mData = new ArrayList<>();
            mData.add(data);
        } else {
            mData = EmojiManager.subList(data, STICKER_COUNT_ONE_PAGE);
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

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (mIsRecentPage) {
            return POSITION_NONE;
        } else {
            return super.getItemPosition(object);
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Context context = container.getContext();
        View view;
        List<BaseEmojiInfo> list = mData.get(position);
        if (list.isEmpty()) {
            view = LayoutInflater.from(context).inflate(R.layout.sticker_item_no_recent_layout, container, false);
        } else {
            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            recyclerView.setPadding(Dimensions.pxFromDp(24.3f), Dimensions.pxFromDp(17.7f), Dimensions.pxFromDp(23.7f), Dimensions.pxFromDp(13.3f));
            StickerItemRecyclerAdapter adapter = new StickerItemRecyclerAdapter(position, mData.get(position), mOnEmojiClickListener);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(context, STICKER_COLUMNS));
            recyclerView.addItemDecoration(new EmojiItemDecoration(STICKER_COLUMNS, STICKER_ROWS, Dimensions.pxFromDp(69), Dimensions.pxFromDp(69)));
            view = recyclerView;
        }
        container.addView(view);
        FontManagerImpl.getInstance().applyFont(view, true);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    void updateRecentItem() {
        if (mData != null) {
            mData.clear();
        }
        List<BaseEmojiInfo> recent = EmojiManager.getRecentStickerInfo();
        mData = EmojiManager.subList(recent, STICKER_COUNT_ONE_PAGE);
        notifyDataSetChanged();
    }
}
