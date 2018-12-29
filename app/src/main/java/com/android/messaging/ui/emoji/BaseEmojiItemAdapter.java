package com.android.messaging.ui.emoji;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.messaging.R;

import java.util.List;

public abstract class BaseEmojiItemAdapter extends RecyclerView.Adapter {

    List<BaseEmojiInfo> mData;

    BaseEmojiItemAdapter(List<BaseEmojiInfo> data) {
        mData = data;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EmojiViewHolder(createView(parent));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindViewHolder((EmojiViewHolder) holder, mData.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
        });
    }

    abstract View createView(ViewGroup parent);

    abstract void bindViewHolder(@NonNull EmojiViewHolder holder, BaseEmojiInfo info);

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class EmojiViewHolder extends RecyclerView.ViewHolder {

        TextView emojiText;
        ImageView emojiImageView;
        ImageView magicStatusView;
        View progressLayout;
        ProgressBar progressBar;

        EmojiViewHolder(View itemView) {
            super(itemView);
            emojiText = itemView.findViewById(R.id.emoji_text);
//            emojiImageView = itemView.findViewById(R.id.emoji_image);
//            magicStatusView = itemView.findViewById(R.id.emoji_status);
//            progressLayout = itemView.findViewById(R.id.download_progress_layout);
//            progressBar = itemView.findViewById(R.id.download_progress_bar);
        }
    }
}
