package com.android.messaging.ui.emoji;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.emoji.utils.EmojiConfig;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;
import com.superapps.view.MessagesTextView;

import java.util.List;

public class EmojiLottieDetailAdapter extends BaseStickerItemRecyclerAdapter {

    private static final int TYPE_HEADER_VIEW = 0;
    private static final int TYPE_NORMAL = 1;
    private List<BaseEmojiInfo> mData;
    private boolean mCouldClickSticker = true;

    EmojiLottieDetailAdapter(List<BaseEmojiInfo> data) {
        mData = data;
        setFrom(StickerMagicDetailActivity.FROM_EMOJ_STORE);
    }

    @Override
    public RecyclerView.ViewHolder createItemViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER_VIEW) {
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_detail_header_layout, parent, false));
        } else {
            return new StickerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_item_layout, parent, false));
        }
    }

    @Override
    public void bindItemViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            setupImageAndText(((HeaderViewHolder) holder).image, ((HeaderViewHolder) holder).text);
            setupGetButton(((HeaderViewHolder) holder).button);
        } else if (holder instanceof StickerViewHolder) {
            BaseEmojiInfo info = mData.get(position - 1);
            if (info instanceof StickerInfo) {
                StickerInfo stickerInfo = (StickerInfo) info;
                StickerViewHolder stickerHolder = (StickerViewHolder) holder;
                bindStickerInfo(stickerHolder, stickerInfo);

                stickerHolder.stickerImageView.setOnClickListener(v -> {
                    if (!mCouldClickSticker) {
                        return;
                    }
                    mCouldClickSticker = false;
                    Threads.postOnMainThreadDelayed(() -> mCouldClickSticker = true, 200);
                    switch (stickerInfo.mEmojiType) {
                        case STICKER_MAGIC:
                            clickMagic(v, stickerInfo, stickerHolder);
                            break;
                        default:
                            throw new IllegalStateException(stickerInfo.mEmojiType + " is illegal!!!");
                    }
                });
            }
        }
    }

    @Override
    public void onMagicItemLoadingFinish(StickerInfo stickerInfo, StickerViewHolder holder) {
        if (!stickerInfo.mIsDownloaded && holder.getAdapterPosition() - 1 < EmojiConfig.getInstance().optInteger(0, "MagicPreloadCount")) {
            downloadMagicEmoji(true, stickerInfo, holder);
        }
    }

    private void clickMagic(View v, @NonNull StickerInfo stickerInfo, StickerViewHolder holder) {
        if (!stickerInfo.mClickable) {
            return;
        }
        if (!stickerInfo.mIsDownloaded) {
            downloadMagicEmoji(false, stickerInfo, holder);
        } else {
            if (!TextUtils.isEmpty(stickerInfo.mLottieZipUrl)) {
                EmojiManager.makeGifRelateToLottie(stickerInfo.mMagicUrl, stickerInfo.mLottieZipUrl, stickerInfo.mSoundUrl);
            } else {
                EmojiManager.makeGifRelateToSound(stickerInfo.mMagicUrl, stickerInfo.mSoundUrl);
            }
            Rect rect = new Rect();
            v.getGlobalVisibleRect(rect);
            stickerInfo.mStartRect = rect;
            StickerMagicDetailActivity.start(holder.itemView.getContext(), stickerInfo, StickerMagicDetailActivity.FROM_EMOJ_STORE);
        }
    }

    private void setupImageAndText(ImageView image, MessagesTextView text) {
        GlideApp.with(image)
                .asBitmap()
                .load(R.drawable.icon_emoji_banner)
                .placeholder(BackgroundDrawables.createBackgroundDrawable(0xffeaeaea, 0, false))
                .error(BackgroundDrawables.createBackgroundDrawable(0xffeaeaea, 0, false))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(image);
        text.setText("Magic Emoji");
    }

    private void setupGetButton(MessagesTextView getBtn) {
        Resources res = getBtn.getResources();
        getBtn.setText(res.getString(R.string.sms_emoji_added));
        getBtn.setTextColor(0xFFFFFFFF);
        getBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFD6D6D6, Dimensions.pxFromDp(20), true));
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() + 1 : 1;
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? TYPE_HEADER_VIEW : TYPE_NORMAL;
    }

    boolean isHeader(int position) {
        return position == 0;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        MessagesTextView text;
        MessagesTextView button;

        HeaderViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.emoji_detail_image);
            text = itemView.findViewById(R.id.emoji_detail_text);
            button = itemView.findViewById(R.id.emoji_detail_get_btn);
        }
    }
}
