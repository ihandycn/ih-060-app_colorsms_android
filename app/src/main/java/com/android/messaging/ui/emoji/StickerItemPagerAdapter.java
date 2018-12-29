package com.android.messaging.ui.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.superapps.util.Dimensions;

import java.util.List;

public class StickerItemPagerAdapter extends PagerAdapter {

    private final int STICKER_COLUMNS = 4;
    private final int STICKER_ROWS = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final int STICKER_COUNT_ONE_PAGE = STICKER_COLUMNS * STICKER_ROWS;

    private List<List<BaseEmojiInfo>> mData;

    StickerItemPagerAdapter(List<BaseEmojiInfo> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        mData = EmojiUtils.subList(data, STICKER_COUNT_ONE_PAGE);
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
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setPadding(Dimensions.pxFromDp(24.3f), Dimensions.pxFromDp(17.7f), Dimensions.pxFromDp(23.7f), Dimensions.pxFromDp(13.3f));
        StickerItemRecyclerAdapter adapter = new StickerItemRecyclerAdapter(mData.get(position));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(context, STICKER_COLUMNS));
        recyclerView.addItemDecoration(new EmojiItemDecoration(STICKER_COLUMNS, STICKER_ROWS, Dimensions.pxFromDp(69), Dimensions.pxFromDp(69)));
        container.addView(recyclerView);
        return recyclerView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
