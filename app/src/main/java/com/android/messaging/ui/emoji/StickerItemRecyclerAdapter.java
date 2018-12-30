package com.android.messaging.ui.emoji;

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
import com.android.messaging.download.DownloadListener;
import com.android.messaging.download.Downloader;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.emoji.utils.EmojiConfig;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Networks;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class StickerItemRecyclerAdapter extends RecyclerView.Adapter<StickerItemRecyclerAdapter.StickerViewHolder> {

    private static final String TAG = StickerItemRecyclerAdapter.class.getSimpleName();

    private List<BaseEmojiInfo> mData;
    private int mMagicPreloadCount;
    private int mPositionInViewPager;

    StickerItemRecyclerAdapter(int position, List<BaseEmojiInfo> data) {
        mPositionInViewPager = position;
        mData = data;
        mMagicPreloadCount = EmojiConfig.getInstance().optInteger(0, "MagicPreloadCount");
    }

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StickerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerViewHolder holder, int position) {
        BaseEmojiInfo info = mData.get(position);
        if (!(info instanceof StickerInfo)) {
            throw new IllegalStateException("info must be instanceof StickerImageInfo!!!!");
        }
        StickerInfo stickerInfo = (StickerInfo) info;
        switch (stickerInfo.mEmojiType) {
            case STICKER_GIF:
                loadGif(holder, stickerInfo);
                break;
            case STICKER_MAGIC:
                loadMagic(holder, stickerInfo);
                break;
            case STICKER_IMAGE:
                loadImage(holder, stickerInfo);
                break;
            default:
                throw new IllegalStateException(stickerInfo.mEmojiType + " is illegal!!!");
        }

        holder.stickerImageView.setOnClickListener(v -> {
            switch (stickerInfo.mEmojiType) {
                case STICKER_IMAGE:
                case STICKER_GIF:
                    clickImageAndGif();
                    break;
                case STICKER_MAGIC:
                    clickMagic(stickerInfo, holder);
                    break;
                default:
                    throw new IllegalStateException(stickerInfo.mEmojiType + " is illegal!!!");
            }
        });
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

    private void loadGif(@NonNull StickerViewHolder holder, @NonNull StickerInfo stickerInfo) {
        holder.stickerImageView.setImageResource(R.drawable.emoji_item_loading_icon);
        GlideApp.with(holder.itemView.getContext())
                .as(GifDrawable.class)
                .load(stickerInfo.mStickerUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new CustomViewTarget<GifImageView, GifDrawable>(holder.stickerImageView) {
                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    }

                    @Override
                    public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                        this.view.setImageDrawable(resource);
                    }
                });
    }

    private void loadImage(@NonNull StickerViewHolder holder, @NonNull StickerInfo stickerInfo) {
        holder.stickerImageView.setImageResource(R.drawable.emoji_item_loading_icon);
        GlideApp.with(holder.itemView.getContext())
                .asBitmap()
                .load(stickerInfo.mStickerUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new CustomViewTarget<GifImageView, Bitmap>(holder.stickerImageView) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition transition) {
                        this.view.setImageBitmap(resource);
                    }

                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    }
                });
    }

    private void loadMagic(@NonNull StickerViewHolder holder, @NonNull StickerInfo stickerInfo) {
        if (stickerInfo.mIsDownloaded) {
            holder.magicStatusView.setImageResource(R.drawable.emoji_player_icon);
        } else {
            holder.magicStatusView.setImageResource(R.drawable.emoji_download_icon);
        }

        holder.stickerImageView.setImageResource(R.drawable.emoji_item_loading_icon);
        GlideApp.with(holder.itemView.getContext())
                .asBitmap()
                .load(stickerInfo.mStickerUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new CustomViewTarget<GifImageView, Bitmap>(holder.stickerImageView) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition transition) {
                        this.view.setImageBitmap(resource);
                        holder.magicStatusView.setVisibility(View.VISIBLE);
                        stickerInfo.mClickable = true;
                        if (mPositionInViewPager == 0 && !stickerInfo.mIsDownloaded && holder.getAdapterPosition() < mMagicPreloadCount) {
                            downloadMagicEmoji(stickerInfo, holder);
                        }
                    }

                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    }
                });
    }

    private void downloadMagicEmoji(@NonNull StickerInfo stickerInfo, @NonNull StickerViewHolder holder) {
        if (Downloader.getInstance().isDownloading(stickerInfo.mMagicUrl)) {
            return;
        }

        if (!Networks.isNetworkAvailable(-1)) {
            Toasts.showToast(R.string.network_error);
            return;
        }
        Downloader.getInstance().download(stickerInfo.mSoundUrl, null);
        Downloader.getInstance().download(stickerInfo.mMagicUrl, new DownloadListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onProgress(String url, float progressValue) {
                int value = (int) (progressValue * holder.progressBar.getMax());
                HSLog.d(TAG, "downloadMagicEmoji, onProgress: " + progressValue);
                holder.progressBar.setProgress(value);

            }

            @Override
            public void onSuccess(String url, File file) {
                HSLog.d(TAG, "downloadMagicEmoji, onComplete()");
                holder.progressBar.setProgress(100);
                Threads.postOnMainThreadDelayed(() -> {
                    stickerInfo.mIsDownloaded = true;
                    holder.magicStatusView.setImageResource(R.drawable.emoji_player_icon);
                    holder.progressLayout.setVisibility(View.GONE);
                    holder.magicStatusView.setVisibility(View.VISIBLE);
                }, 100);

            }

            @Override
            public void onCancel(String url) {
                HSLog.d(TAG, "downloadMagicEmoji, onCancel ");
                failure();
            }

            @Override
            public void onFail(String url, String failMsg) {
                HSLog.d(TAG, "downloadMagicEmoji, onFailed: " + failMsg);
                failure();
            }

            private void failure() {
                holder.progressLayout.setVisibility(View.GONE);
                holder.magicStatusView.setVisibility(View.VISIBLE);
                Toasts.showToast(R.string.network_error_and_try_again);
            }
        });
        holder.progressLayout.setVisibility(View.VISIBLE);
        holder.magicStatusView.setVisibility(View.INVISIBLE);
        holder.progressBar.setProgress(1);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class StickerViewHolder extends RecyclerView.ViewHolder {

        GifImageView stickerImageView;
        ImageView magicStatusView;
        View progressLayout;
        ProgressBar progressBar;

        StickerViewHolder(View itemView) {
            super(itemView);
            stickerImageView = itemView.findViewById(R.id.sticker_image);
            magicStatusView = itemView.findViewById(R.id.sticker_status);
            progressLayout = itemView.findViewById(R.id.download_progress_layout);
            progressBar = itemView.findViewById(R.id.download_progress_bar);
        }
    }


}
