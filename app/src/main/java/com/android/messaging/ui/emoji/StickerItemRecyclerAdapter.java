package com.android.messaging.ui.emoji;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.emoji.utils.EmojiConfig;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.superapps.util.Threads;

import java.util.List;

public class StickerItemRecyclerAdapter extends BaseStickerItemRecyclerAdapter {

    private List<BaseEmojiInfo> mData;
    private int mMagicPreloadCount;
    private int mPositionInViewPager;
    private EmojiPackagePagerAdapter.OnEmojiClickListener mOnEmojiClickListener;
    private boolean mCouldClickSticker = true;

    StickerItemRecyclerAdapter(int position, List<BaseEmojiInfo> data, EmojiPackagePagerAdapter.OnEmojiClickListener emojiClickListener) {
        mPositionInViewPager = position;
        mOnEmojiClickListener = emojiClickListener;
        mData = data;
        mMagicPreloadCount = EmojiConfig.getInstance().optInteger(0, "MagicPreloadCount");
    }

    @Override
    public RecyclerView.ViewHolder createItemViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StickerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_item_layout, parent, false));
    }

    @Override
    public void bindItemViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof StickerViewHolder)) {
            return;
        }
        BaseEmojiInfo info = mData.get(position);
        if (!(info instanceof StickerInfo)) {
            throw new IllegalStateException("info must be instanceof StickerImageInfo!!!!");
        }
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
                case STICKER_IMAGE:
                case STICKER_GIF:
                    clickImageAndGif(v, stickerInfo);
                    break;
                case STICKER_MAGIC:
                    clickMagic(v, stickerInfo, stickerHolder);
                    break;
                default:
                    throw new IllegalStateException(stickerInfo.mEmojiType + " is illegal!!!");
            }
        });
    }

    @Override
    public void onMagicItemLoadingFinish(StickerInfo stickerInfo, StickerViewHolder holder) {
        if (!stickerInfo.mIsDownloaded && holder.getAdapterPosition() < mMagicPreloadCount) {
            downloadMagicEmoji(true, stickerInfo, holder);
        }
    }

    private void clickImageAndGif(View v, StickerInfo stickerInfo) {
        if (mOnEmojiClickListener != null) {
            Rect rect = new Rect();
            v.getGlobalVisibleRect(rect);
            stickerInfo.mStartRect = rect;
            mOnEmojiClickListener.stickerClickExcludeMagic(stickerInfo);
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
            StickerMagicDetailActivity.start(holder.itemView.getContext(), stickerInfo);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
