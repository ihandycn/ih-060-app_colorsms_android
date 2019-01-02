package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.glide.GlideRequests;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.superapps.util.Dimensions;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class EmojiStorePreviewLayout extends ViewGroup {

    private static final int COLUMN_COUNT = 4;
    private static final int ROW_COUNT = 2;
    private static final int VERTICAL_SPACING = Dimensions.pxFromDp(5);
    private static final int HORIZONTAL_SPACING = Dimensions.pxFromDp(7);
    private static final int CELL_SIZE = Dimensions.pxFromDp(28.33f);

    public EmojiStorePreviewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int left = (i % COLUMN_COUNT) * (CELL_SIZE + HORIZONTAL_SPACING);
            int top = (i / COLUMN_COUNT) * (CELL_SIZE + VERTICAL_SPACING);
            child.layout(left, top, left + CELL_SIZE, top + CELL_SIZE);
        }
    }

    void bindEmojiItems(EmojiPackageInfo packageInfo) {
        removeAllViews();
        int itemSize = Math.min(COLUMN_COUNT * ROW_COUNT, packageInfo.mEmojiInfoList.size());
        for (int i = 0; i < itemSize; i++) {
            BaseEmojiInfo info = packageInfo.mEmojiInfoList.get(i);
            if (!(info instanceof StickerInfo)) {
                continue;
            }
            StickerInfo stickerInfo = (StickerInfo) info;
            Context context = getContext();
            GifImageView child = new GifImageView(context);
            switch (stickerInfo.mEmojiType) {
                case STICKER_IMAGE:
                    GlideRequests imageRequest = GlideApp.with(context);
                    imageRequest.asBitmap()
                            .load(stickerInfo.mStickerUrl)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .placeholder(R.drawable.emoji_item_placeholder)
                            .error(R.drawable.emoji_item_placeholder)
                            .into(child);
                    break;
                case STICKER_GIF:
                    child.setImageResource(R.drawable.emoji_item_placeholder);
                    GlideRequests gifRequest = GlideApp.with(context);
                    gifRequest.as(GifDrawable.class)
                            .load(stickerInfo.mStickerUrl)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .into(new CustomViewTarget<GifImageView,GifDrawable>(child) {
                                @Override protected void onResourceCleared(@Nullable Drawable placeholder) {
                                }

                                @Override public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                }

                                @Override
                                public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                                    this.view.setImageDrawable(resource);
                                }
                            });
                    break;
                default:
                    break;
            }
            addView(child);
        }
    }
}
