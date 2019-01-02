package com.android.messaging.ui.emoji;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.ui.emoji.utils.EmojiConfig;

import java.util.List;

public class StickerItemRecyclerAdapter extends BaseStickerItemRecyclerAdapter {

    private List<BaseEmojiInfo> mData;
    private int mMagicPreloadCount;
    private int mPositionInViewPager;
    private EmojiPickerFragment.OnEmojiClickListener mOnEmojiClickListener;

    StickerItemRecyclerAdapter(int position, List<BaseEmojiInfo> data, EmojiPickerFragment.OnEmojiClickListener emojiClickListener) {
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
            if (mOnEmojiClickListener != null) {
                mOnEmojiClickListener.emojiClick(stickerInfo);
            }
            switch (stickerInfo.mEmojiType) {
                case STICKER_IMAGE:
                case STICKER_GIF:
                    clickImageAndGif();
                    break;
                case STICKER_MAGIC:
                    clickMagic(stickerInfo, stickerHolder);
                    break;
                default:
                    throw new IllegalStateException(stickerInfo.mEmojiType + " is illegal!!!");
            }
        });
    }

    @Override
    public void onMagicItemLoadingFinish(StickerInfo stickerInfo, StickerViewHolder holder) {
        if (mPositionInViewPager == 0 && !stickerInfo.mIsDownloaded && holder.getAdapterPosition() < mMagicPreloadCount) {
            downloadMagicEmoji(stickerInfo, holder);
        }
    }

    private void clickImageAndGif() {

    }

    private void clickMagic(@NonNull StickerInfo stickerInfo, StickerViewHolder holder) {
        if (!stickerInfo.mClickable) {
            return;
        }
        if (!stickerInfo.mIsDownloaded) {
            downloadMagicEmoji(stickerInfo, holder);
        } else {
            StickerMagicDetailActivity.start(holder.itemView.getContext(), stickerInfo);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
