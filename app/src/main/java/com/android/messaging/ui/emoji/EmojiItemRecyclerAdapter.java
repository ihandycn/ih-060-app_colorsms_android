package com.android.messaging.ui.emoji;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;

import java.util.List;

public class EmojiItemRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_TEXT = 2;

    private List<BaseEmojiInfo> mData;
    private EmojiPickerFragment.OnEmojiClickListener mOnEmojiClickListener;

    EmojiItemRecyclerAdapter(List<BaseEmojiInfo> data, EmojiPickerFragment.OnEmojiClickListener emojiClickListener) {
        mData = data;
        mOnEmojiClickListener = emojiClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            return new EmojiImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_item_delete_layout, parent, false));
        } else {
            return new EmojiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_item_layout, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmojiImageViewHolder) {
            ((EmojiImageViewHolder) holder).imageView.setOnClickListener(v -> {
                if (mOnEmojiClickListener != null) {
                    mOnEmojiClickListener.delete();
                }
            });
        } else if (holder instanceof EmojiViewHolder) {
            EmojiViewHolder emojiHolder = (EmojiViewHolder) holder;
            BaseEmojiInfo info = mData.get(position);
            if (info.mEmojiType == EmojiType.EMOJI_EMPTY) {
                emojiHolder.textView.setVisibility(View.INVISIBLE);
            } else {
                EmojiInfo emojiInfo = (EmojiInfo) info;
                emojiHolder.textView.setText(emojiInfo.mEmoji);
                emojiHolder.textView.setOnClickListener(v -> {
                    if (mOnEmojiClickListener != null) {
                        mOnEmojiClickListener.emojiClick(emojiInfo);
                    }
                });
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

        EmojiViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.emoji_text);
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
