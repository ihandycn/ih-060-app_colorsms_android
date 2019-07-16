package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.ui.emoji.utils.emoispan.EmojiCache;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.List;

public class EmojiItemRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_TEXT = 2;
    private Context mContext;
    private int mItemHeight = -1;

    private List<BaseEmojiInfo> mData;
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;

    EmojiItemRecyclerAdapter(List<BaseEmojiInfo> data, EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
        mData = data;
        mOnEmojiClickListener = emojiClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if (viewType == TYPE_IMAGE) {
            return new EmojiImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_item_delete_layout, parent, false));
        } else {
            return new EmojiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_item_layout, parent, false));
        }
    }

    @SuppressWarnings("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmojiImageViewHolder) {
            ((EmojiImageViewHolder) holder).imageView.setOnClickListener(v -> {
                if (mOnEmojiClickListener != null) {
                    mOnEmojiClickListener.deleteEmoji();
                }
            });
        } else if (holder instanceof EmojiViewHolder) {
            final EmojiViewHolder emojiHolder = (EmojiViewHolder) holder;
            final BaseEmojiInfo info = mData.get(position);
            if (info.mEmojiType == EmojiType.EMOJI_EMPTY) {
                emojiHolder.emojiView.setVisibility(View.INVISIBLE);
            } else {
                final EmojiInfo emojiInfo = (EmojiInfo) info;
                emojiHolder.itemView.setTag(emojiInfo);

                Threads.postOnThreadPoolExecutor(new Runnable() {
                    @Override
                    public void run() {
                        Drawable emojiDrawable = EmojiCache.getInstance().getFromCache(emojiInfo);
                        Threads.postOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                emojiHolder.emojiView.setImageDrawable(emojiDrawable);
                            }
                        });
                    }
                });
                if(mItemHeight == -1) {
                    mItemHeight = Dimensions.getPhoneWidth(mContext) / 9;
                }
                emojiHolder.itemContainer.getLayoutParams().height = mItemHeight;

                emojiHolder.itemContainer.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        mContext.getResources().getColor(android.R.color.white), Dimensions.pxFromDp(21), true));

                emojiHolder.itemContainer.setOnClickListener(v -> {
                    if (mOnEmojiClickListener != null) {
                        mOnEmojiClickListener.emojiClick(emojiInfo, !emojiInfo.isRecent);
                    }
                });

                if (emojiInfo.hasVariant()) {
                    emojiHolder.itemContainer.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mOnEmojiClickListener != null) {
                                EmojiInfo info = (EmojiInfo) emojiHolder.itemView.getTag();
                                mOnEmojiClickListener.emojiLongClick(emojiHolder.emojiView, info);
                            }
                            return true;
                        }
                    });
                    emojiHolder.moreView.setVisibility(View.VISIBLE);
                } else {
                    emojiHolder.itemContainer.setOnLongClickListener(null);
                    emojiHolder.moreView.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        BaseEmojiInfo info = mData.get(position);
        if (info.mEmojiType == EmojiType.EMOJI_DELETE) {
            return TYPE_IMAGE;
        } else {
            return TYPE_TEXT;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class EmojiViewHolder extends RecyclerView.ViewHolder {
        private ImageView emojiView;
        private ImageView moreView;
        private ViewGroup itemContainer;

        EmojiViewHolder(View itemView) {
            super(itemView);
            emojiView = itemView.findViewById(R.id.emoji_view);
            moreView = itemView.findViewById(R.id.emoji_more);
            itemContainer = itemView.findViewById(R.id.emoji_item_container);
        }
    }

    static class EmojiImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        EmojiImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.emoji_delete_btn);
        }
    }

}
