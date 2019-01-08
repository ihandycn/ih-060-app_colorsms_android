package com.android.messaging.ui.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class EmojiItemPagerAdapter extends PagerAdapter {

    private final int EMOJI_COLUMNS = 8;
    private final int EMOJI_ROWS = 4;
    @SuppressWarnings("FieldCanBeLocal")
    private final int EMOJI_COUNT_ONE_PAGE = EMOJI_COLUMNS * EMOJI_ROWS - 1;

    private List<List<BaseEmojiInfo>> mData;
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;

    EmojiItemPagerAdapter(List<BaseEmojiInfo> data, EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
        if (data == null || data.isEmpty()) {
            return;
        }
        mOnEmojiClickListener = emojiClickListener;
        List<List<BaseEmojiInfo>> list = EmojiManager.subList(data, EMOJI_COUNT_ONE_PAGE);
        mData = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            List<BaseEmojiInfo> subList = list.get(i);
            List<BaseEmojiInfo> copySubList = new ArrayList<>(subList);
            if (subList.size() == EMOJI_COUNT_ONE_PAGE) {
                BaseEmojiInfo info = new BaseEmojiInfo();
                info.mEmojiType = EmojiType.EMOJI_DELETE;
                copySubList.add(info);
            } else {
                for (int k = 0; k < EMOJI_COUNT_ONE_PAGE - subList.size(); k++) {
                    BaseEmojiInfo emptyInfo = new BaseEmojiInfo();
                    emptyInfo.mEmojiType = EmojiType.EMOJI_EMPTY;
                    copySubList.add(emptyInfo);
                }
                BaseEmojiInfo info = new BaseEmojiInfo();
                info.mEmojiType = EmojiType.EMOJI_DELETE;
                copySubList.add(info);
            }
            mData.add(copySubList);
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
        Context context = container.getContext();
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.setPadding(Dimensions.pxFromDp(20), Dimensions.pxFromDp(17.7f), Dimensions.pxFromDp(20), Dimensions.pxFromDp(11.7f));
        EmojiItemRecyclerAdapter adapter = new EmojiItemRecyclerAdapter(mData.get(position), mOnEmojiClickListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(context, EMOJI_COLUMNS));
        recyclerView.addItemDecoration(new EmojiItemDecoration(EMOJI_COLUMNS, EMOJI_ROWS, Dimensions.pxFromDp(29), Dimensions.pxFromDp(29)));
        container.addView(recyclerView);
        return recyclerView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
