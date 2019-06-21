package com.android.messaging.ui.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.List;

public class EmojiItemRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_TEXT = 2;
    private Context mContext;

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
                emojiHolder.textView.setVisibility(View.INVISIBLE);
            } else {
                final EmojiInfo emojiInfo = (EmojiInfo) info;
                emojiHolder.itemView.setTag(emojiInfo);
                emojiHolder.textView.setText(emojiInfo.mEmoji);

                emojiHolder.textView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        mContext.getResources().getColor(android.R.color.white), Dimensions.pxFromDp(16), true));

                emojiHolder.textView.setOnClickListener(v -> {
                    if (mOnEmojiClickListener != null) {
                        mOnEmojiClickListener.emojiClick(emojiInfo);
                    }
                });

                if (emojiInfo.hasVariant()) {
                    emojiHolder.textView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (mOnEmojiClickListener != null) {
                                EmojiInfo info = (EmojiInfo) emojiHolder.itemView.getTag();
                                mOnEmojiClickListener.emojiLongClick(emojiHolder.itemView, info);
                            }
                            return true;
                        }
                    });
                    emojiHolder.imageView.setVisibility(View.VISIBLE);
                }else{
                    emojiHolder.textView.setOnLongClickListener(null);
                    emojiHolder.imageView.setVisibility(View.GONE);
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
        private TextView textView;
        private ImageView imageView;

        EmojiViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.emoji_text);
            imageView = itemView.findViewById(R.id.emoji_more);
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
