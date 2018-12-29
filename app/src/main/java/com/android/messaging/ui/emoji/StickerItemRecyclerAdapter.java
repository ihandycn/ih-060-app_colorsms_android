package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class StickerItemRecyclerAdapter extends RecyclerView.Adapter<StickerItemRecyclerAdapter.StickerViewHolder> {

    private List<BaseEmojiInfo> mData;

    StickerItemRecyclerAdapter(List<BaseEmojiInfo> data) {
        mData = data;
    }

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StickerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerViewHolder holder, int position) {
        BaseEmojiInfo info = mData.get(position);
        if (!(info instanceof StickerImageInfo)) {
            throw new IllegalStateException("info must be instanceof StickerImageInfo!!!!");
        }
        StickerImageInfo imageInfo = (StickerImageInfo) info;
        holder.emojiImageView.setImageResource(R.drawable.emoji_item_loading_icon);
        Context context = holder.itemView.getContext();
        GlideApp.with(context)
                .asBitmap()
                .load(imageInfo.mImageUrl)
                .into(new CustomViewTarget<GifImageView, Bitmap>(holder.emojiImageView) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition transition) {
                        holder.emojiImageView.setImageBitmap(resource);
                    }

                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class StickerViewHolder extends RecyclerView.ViewHolder {

        GifImageView emojiImageView;
        ImageView magicStatusView;
        View progressLayout;
        ProgressBar progressBar;

        StickerViewHolder(View itemView) {
            super(itemView);
            emojiImageView = itemView.findViewById(R.id.emoji_image);
            magicStatusView = itemView.findViewById(R.id.emoji_status);
            progressLayout = itemView.findViewById(R.id.download_progress_layout);
            progressBar = itemView.findViewById(R.id.download_progress_bar);
        }
    }


}
