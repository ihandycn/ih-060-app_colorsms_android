package com.android.messaging.ui.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieCompositionFactory;
import com.android.messaging.R;
import com.android.messaging.download.Downloader;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.emoji.utils.EmojiManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.commons.utils.HSLog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class StickerMagicView {

    private static final String TAG = StickerMagicView.class.getSimpleName();

    private GifImageView mGifImageView;
    private LottieAnimationView mLottieAnimationView;



    private MediaPlayer mSoundPlayer;
    private ViewGroup mContainerView;
    private Context mContext;
    private Uri mGifUri;
    private String mLottieUrl;
    private String mSoundUrl;
    private RecordViewSize mRecordViewSize;

    void setupView(ViewGroup container, Uri gifUri, String soundUrl, RecordViewSize recordViewSize) {
        mContainerView = container;
        mContext = mContainerView.getContext();
        mGifUri = gifUri;
        mSoundUrl = soundUrl;
        mRecordViewSize = recordViewSize;
        mLottieUrl = EmojiManager.getLottieUrlByGifUriStr(mGifUri.toString());
        if (isLottieMagic()) {
            initLottieView();
        } else {
            initGifView();
        }
        startPlay();
    }

    private void startPlay() {
        File soundFile = Downloader.getInstance().getDownloadFile(mSoundUrl);
        if (soundFile.exists()) {
            prepareGifSound(soundFile.getPath());
        } else {
            if (isLottieMagic()) {
                playLottie();
            } else {
                playGif(true);
            }
        }
    }

    private boolean isLottieMagic() {
        return !TextUtils.isEmpty(mLottieUrl);
    }

    private void initGifView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.magic_gif_emoji_layout, mContainerView);
        mGifImageView = view.findViewById(R.id.gif_image_view);
    }

    private void initLottieView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.magic_lottie_emoji_layout, mContainerView);
        mGifImageView = view.findViewById(R.id.gif_image_view);
        mLottieAnimationView = view.findViewById(R.id.lottie_image_view);
        mLottieAnimationView.useHardwareAcceleration();

    }
    public MediaPlayer getSoundPlayer() {
        return mSoundPlayer;
    }

    private void playGif(boolean isPlay) {
        GlideApp.with(mContext)
                .as(GifDrawable.class)
                .load(mGifUri)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new CustomViewTarget<GifImageView, GifDrawable>(mGifImageView) {
                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                        HSLog.d(TAG, "Resource cleared: " + mGifUri);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        HSLog.d(TAG, "Resource load failed: " + mGifUri);
                    }

                    @Override
                    public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                        if (mRecordViewSize != null) {
                            mRecordViewSize.record(resource.getMinimumWidth(), resource.getMinimumHeight());
                        }
                        if (isPlay) {
                            resource.reset();
                            this.view.setVisibility(View.VISIBLE);
                            this.view.setImageDrawable(resource);
                            if (mSoundPlayer != null) {
                                HSLog.d(TAG, "GifDuration = " + resource.getDuration() + ", SoundDuration = " + mSoundPlayer.getDuration());
                                mSoundPlayer.start();
                            }
                        } else {
                            resource.stop();
                            this.view.setVisibility(View.GONE);
                        }
                    }
                });
    }


    private void playLottie() {
        playGif(false);
        File file = Downloader.getInstance().getDownloadFile(mLottieUrl);
        try {
            InputStream inputStream = new FileInputStream(file.getAbsolutePath());
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            LottieCompositionFactory.fromZipStream(zipInputStream, null).addListener(result -> {
                mLottieAnimationView.setComposition(result);
                mLottieAnimationView.playAnimation();

                if (mSoundPlayer != null) {
                    mSoundPlayer.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (mGifImageView != null) {
            GifDrawable gifDrawable = (GifDrawable) mGifImageView.getDrawable();
            if (gifDrawable != null) {
                gifDrawable.stop();
            }
        }

        if (mSoundPlayer != null) {
            mSoundPlayer.release();
            mSoundPlayer = null;
        }

        if (mLottieAnimationView != null) {
            mLottieAnimationView.cancelAnimation();
            mLottieAnimationView = null;
        }
    }

    private void prepareGifSound(String filePath) {
        mSoundPlayer = new MediaPlayer();
        try {
            mSoundPlayer.setDataSource(filePath);
            mSoundPlayer.prepareAsync();
            mSoundPlayer.setLooping(true);
            mSoundPlayer.setOnPreparedListener(mp -> {
                if (isLottieMagic()) {
                    playLottie();
                } else {
                    playGif(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface RecordViewSize {
        void record(int width, int height);
    }
}
