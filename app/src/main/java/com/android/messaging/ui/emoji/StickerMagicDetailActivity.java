package com.android.messaging.ui.emoji;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.android.messaging.R;
import com.android.messaging.download.Downloader;
import com.android.messaging.glide.GlideApp;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class StickerMagicDetailActivity extends HSAppCompatActivity implements View.OnClickListener {

    private static final String TAG = StickerMagicDetailActivity.class.getSimpleName();
    static final String INTENT_KEY_EMOJI_INFO = "emoji_info";

    public static void start(Context context, StickerInfo stickerInfo) {
        Intent starter = new Intent(context, StickerMagicDetailActivity.class);
        starter.putExtra(INTENT_KEY_EMOJI_INFO, stickerInfo);
        context.startActivity(starter);
    }

    private GifImageView mGifImageView;
    private MediaPlayer mSoundPlayer;
    private StickerInfo mStickerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_magic_layout);

        findViewById(R.id.emoji_show_close).setOnClickListener(this);
        findViewById(R.id.emoji_show_share_btn).setOnClickListener(this);
        mGifImageView = findViewById(R.id.emoji_show_image);

        mStickerInfo = getIntent().getParcelableExtra(INTENT_KEY_EMOJI_INFO);

        File soundFile = Downloader.getInstance().getDownloadFile(mStickerInfo.mSoundUrl);
        if (soundFile.exists()) {
            prepareGifSound(soundFile.getPath());
        } else {
            HSLog.w(TAG, "Sound not found and play gif only.");
            playGif();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emoji_show_close:
                finish();
                break;
            case R.id.emoji_show_share_btn:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    private void playGif() {
        File file = Downloader.getInstance().getDownloadFile(mStickerInfo.mMagicUrl);
        if (!file.exists()) {
            finish();
            return;
        }
        GlideApp.with(this)
                .as(GifDrawable.class)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(new CustomViewTarget<GifImageView, GifDrawable>(mGifImageView) {
                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    }

                    @Override
                    public void onResourceReady(@NonNull GifDrawable resource, @Nullable Transition<? super GifDrawable> transition) {
                        resource.reset();
                        this.view.setImageDrawable(resource);
                        if (mSoundPlayer != null) {
                            HSLog.d(TAG, "GifDuration = " + resource.getDuration() + ", SoundDuration = " + mSoundPlayer.getDuration());
                            mSoundPlayer.start();
                        }
                    }
                });
    }

    private void release() {
        GifDrawable gifDrawable = (GifDrawable) mGifImageView.getDrawable();
        if (gifDrawable != null) {
            gifDrawable.stop();
        }

        if (mSoundPlayer != null) {
            mSoundPlayer.release();
            mSoundPlayer = null;
        }
    }

    private void prepareGifSound(String filePath) {
        mSoundPlayer = new MediaPlayer();
        try {
            mSoundPlayer.setDataSource(filePath);
            mSoundPlayer.prepareAsync();
            mSoundPlayer.setLooping(true);
            mSoundPlayer.setOnPreparedListener(mp -> playGif());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
