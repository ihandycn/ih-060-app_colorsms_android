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
import com.android.messaging.util.BugleAnalytics;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class StickerMagicDetailActivity extends HSAppCompatActivity implements View.OnClickListener {

    private static final String TAG = StickerMagicDetailActivity.class.getSimpleName();
    static final String INTENT_KEY_EMOJI_INFO = "emoji_info";

    public final static String NOTIFICATION_SEND_MAGIC_STICKER = "notification_send_magic_sticker";
    public final static String BUNDLE_SEND_MAGIC_STICKER_DATA = "bundle_send_magic_sticker_data";

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
        findViewById(R.id.send_btn).setOnClickListener(this);
        mGifImageView = findViewById(R.id.emoji_show_image);

        mStickerInfo = getIntent().getParcelableExtra(INTENT_KEY_EMOJI_INFO);

        File soundFile = Downloader.getInstance().getDownloadFile(mStickerInfo.mSoundUrl);
        if (soundFile.exists()) {
            prepareGifSound(soundFile.getPath());
        } else {
            HSLog.w(TAG, "Sound not found and play gif only.");
            playGif();
        }
        BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_View", true,"type", StickerInfo.getNumFromUrl(mStickerInfo.mMagicUrl));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emoji_show_close:
                BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_Cancel",true);
                finish();
                break;
            case R.id.send_btn:
                BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_Select_Click", true,"type", StickerInfo.getNumFromUrl(mStickerInfo.mMagicUrl));
                HSBundle bundle = new HSBundle();
                bundle.putObject(BUNDLE_SEND_MAGIC_STICKER_DATA, mStickerInfo);
                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_SEND_MAGIC_STICKER, bundle);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        BugleAnalytics.logEvent("SMSEmoji_ChatEmoji_Magic_Cancel",true);
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
                        mStickerInfo.mStickerWidth = resource.getMinimumWidth();
                        mStickerInfo.mStickerHeight = resource.getMinimumHeight();
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
