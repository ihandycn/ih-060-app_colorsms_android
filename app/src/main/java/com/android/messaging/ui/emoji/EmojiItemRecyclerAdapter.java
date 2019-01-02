package com.android.messaging.ui.emoji;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.R;

import java.util.List;

public class EmojiItemRecyclerAdapter extends RecyclerView.Adapter<EmojiItemRecyclerAdapter.EmojiViewHolder> {
    private List<BaseEmojiInfo> mData;
    private EmojiPickerFragment.OnEmojiClickListener mOnEmojiClickListener;

    EmojiItemRecyclerAdapter(List<BaseEmojiInfo> data, EmojiPickerFragment.OnEmojiClickListener emojiClickListener) {
        mData = data;
        mOnEmojiClickListener = emojiClickListener;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EmojiViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        BaseEmojiInfo info = mData.get(position);
        if (!(info instanceof EmojiInfo)) {
            throw new IllegalStateException("info must be EmojiInfo!!!");
        }
        EmojiInfo emojiInfo = (EmojiInfo) info;
        holder.textView.setText(emojiInfo.mEmoji);
        holder.textView.setOnClickListener(v -> {
            if (mOnEmojiClickListener != null) {
                mOnEmojiClickListener.emojiClick(emojiInfo);
            }
        });
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

}
