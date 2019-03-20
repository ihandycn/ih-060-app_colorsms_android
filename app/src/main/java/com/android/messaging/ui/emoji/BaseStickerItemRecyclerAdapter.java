package com.android.messaging.ui.emoji;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.messaging.R;
import com.android.messaging.download.DownloadListener;
import com.android.messaging.download.Downloader;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.android.messaging.util.BugleAnalytics;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Networks;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public abstract class BaseStickerItemRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = BaseStickerItemRecyclerAdapter.class.getSimpleName();

    private static ArrayList<DownloadListener> mEmojiDownloadListener = new ArrayList<>();

    public abstract RecyclerView.ViewHolder createItemViewHolder(@NonNull ViewGroup parent, int viewType);

    public abstract void bindItemViewHolder(@NonNull RecyclerView.ViewHolder holder, int position);

    private String from;

    public void onMagicItemLoadingFinish(StickerInfo stickerInfo, StickerViewHolder holder) {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return createItemViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        bindItemViewHolder(holder, position);
    }

    void bindStickerInfo(StickerViewHolder holder, StickerInfo stickerInfo) {
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
                        stickerInfo.mStickerWidth = resource.getMinimumWidth();
                        stickerInfo.mStickerHeight = resource.getMinimumHeight();
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
                        stickerInfo.mStickerWidth = resource.getWidth();
                        stickerInfo.mStickerHeight = resource.getHeight();
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
        if (!stickerInfo.mIsDownloaded) {
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
                            onMagicItemLoadingFinish(stickerInfo, holder);
                        }

                        @Override
                        protected void onResourceCleared(@Nullable Drawable placeholder) {
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        }
                    });
        } else {
            File file = Downloader.getInstance().getDownloadFile(stickerInfo.mMagicUrl);
            if (file.exists()) {
                Uri gifUri = Uri.fromFile(file);
                EmojiManager.addStickerMagicFileUri(gifUri.toString());

                GlideApp.with(holder.itemView.getContext())
                        .as(GifDrawable.class)
                        .load(gifUri)
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
                                stickerInfo.mStickerWidth = resource.getMinimumWidth();
                                stickerInfo.mStickerHeight = resource.getMinimumHeight();
                                stickerInfo.mClickable = true;
                                holder.magicStatusView.setVisibility(View.VISIBLE);
                                this.view.setImageDrawable(resource);
                            }
                        });
            }
        }
    }

    void downloadMagicEmoji(boolean isAutoDownload, @NonNull StickerInfo stickerInfo, @NonNull StickerViewHolder holder) {
        String url = TextUtils.isEmpty(stickerInfo.mLottieZipUrl) ? stickerInfo.mMagicUrl : stickerInfo.mLottieZipUrl;
        if (Downloader.getInstance().isDownloading(url)) {
            return;
        }

        if (!Networks.isNetworkAvailable(-1)) {
            Toasts.showToast(R.string.network_error);
            return;
        }
        Downloader.getInstance().download(stickerInfo.mSoundUrl, null);
        DownloadListener downloadListener = new DownloadListener() {
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
                HSLog.d(TAG, "downloadMagicEmoji, gif download successfully!!!");
                if (!isAutoDownload) {
                    if (TextUtils.equals(from, StickerMagicDetailActivity.FROM_EMOJ_STORE)) {
                        BugleAnalytics.logEvent("SMSEmoji_Store_Magic_Download", true, "type1", StickerInfo.getNumFromUrl(url), "type2", "success");
                    } else {
                        BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_Download", true, "type1", StickerInfo.getNumFromUrl(url), "type2", "success");
                    }
                }
                holder.progressBar.setProgress(100);
                Threads.postOnMainThreadDelayed(() -> {
                    stickerInfo.mIsDownloaded = true;
                    holder.magicStatusView.setImageResource(R.drawable.emoji_player_icon);
                    holder.progressLayout.setVisibility(View.GONE);
                    holder.magicStatusView.setVisibility(View.VISIBLE);

                    if (file.exists()) {
                        Uri gifUri = Uri.fromFile(file);
                        EmojiManager.addStickerMagicFileUri(gifUri.toString());

                        if (((Activity) holder.itemView.getContext()).isFinishing()
                                || ((Activity) holder.itemView.getContext()).isDestroyed()) {
                            return;
                        }

                        GlideApp.with(holder.itemView.getContext())
                                .as(GifDrawable.class)
                                .load(gifUri)
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
                                        stickerInfo.mStickerWidth = resource.getMinimumWidth();
                                        stickerInfo.mStickerHeight = resource.getMinimumHeight();
                                        stickerInfo.mClickable = true;
                                        this.view.setImageDrawable(resource);
                                    }
                                });
                    }
                }, 100);
            }

            @Override
            public void onCancel(String url) {
                HSLog.d(TAG, "downloadMagicEmoji, onCancel ");
                failure(url);
            }

            @Override
            public void onFail(String url, String failMsg) {
                HSLog.d(TAG, "downloadMagicEmoji, onFailed: " + failMsg);
                failure(url);
            }

            private void failure(String url) {
                if (!isAutoDownload) {
                    if (TextUtils.equals(from, StickerMagicDetailActivity.FROM_EMOJ_STORE)) {
                        BugleAnalytics.logEvent("SMSEmoji_Store_Magic_Download", true, "type1", StickerInfo.getNumFromUrl(url), "type2", "fail");
                    } else {
                        BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_Download", true, "type1", StickerInfo.getNumFromUrl(url), "type2", "fail");
                    }
                }
                holder.progressLayout.setVisibility(View.GONE);
                holder.magicStatusView.setVisibility(View.VISIBLE);
                Toasts.showToast(R.string.network_error);
            }
        };
        if (!TextUtils.isEmpty(stickerInfo.mLottieZipUrl)) {
            DownloadListener lottieDownloadListener = new DownloadListener() {
                @Override
                public void onStart(String url) {

                }

                @Override
                public void onProgress(String url, float progressValue) {
                    HSLog.d(TAG, "downloadMagicEmoji, Lottie - onProgress: " + progressValue);
                }

                @Override
                public void onSuccess(String url, File file) {
                    HSLog.d(TAG, "downloadMagicEmoji, lottie download successfully!!!");
                    Downloader.getInstance().download(stickerInfo.mMagicUrl, downloadListener);
                    mEmojiDownloadListener.add(downloadListener);
                }

                @Override
                public void onCancel(String url) {
                    failure(url);
                }

                @Override
                public void onFail(String url, String failMsg) {
                    failure(url);
                }

                private void failure(String url) {
                    if (!isAutoDownload) {
                        BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_Download", true, "type1", StickerInfo.getNumFromUrl(url), "type2", "fail");
                    }
                    holder.progressLayout.setVisibility(View.GONE);
                    holder.magicStatusView.setVisibility(View.VISIBLE);
                    Toasts.showToast(R.string.network_error_and_try_again);
                }
            };
            Downloader.getInstance().download(stickerInfo.mLottieZipUrl, lottieDownloadListener);
            mEmojiDownloadListener.add(lottieDownloadListener);
        } else {
            Downloader.getInstance().download(stickerInfo.mMagicUrl, downloadListener);
            mEmojiDownloadListener.add(downloadListener);
        }

        holder.progressLayout.setVisibility(View.VISIBLE);
        holder.magicStatusView.setVisibility(View.INVISIBLE);
        holder.progressBar.setProgress(1);
    }

    public void setFrom(String from) {
        this.from = from;
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

    static void releaseListener() {
        mEmojiDownloadListener.clear();
    }
}
