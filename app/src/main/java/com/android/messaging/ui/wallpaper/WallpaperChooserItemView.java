package com.android.messaging.ui.wallpaper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class WallpaperChooserItemView extends FrameLayout {

    private ImageView mWallpaperIv;
    private View mAddPhotosContainer;
    private View mLoadingBg;
    private ImageView mLoadingIv;
    private LottieAnimationView mCheckedLottie;

    private ObjectAnimator mDownloadingAnimator;
    private boolean mIsDownloadAnimationPlaying;

    public WallpaperChooserItemView(@NonNull Context context) {
        super(context);
    }

    public WallpaperChooserItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WallpaperChooserItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init() {
        mWallpaperIv = findViewById(R.id.wallpaper_chooser_item_iv);
        mAddPhotosContainer = findViewById(R.id.wallpaper_chooser_add_photo_container);
        mLoadingBg = findViewById(R.id.wallpaper_loading_bg);
        mLoadingIv = findViewById(R.id.wallpaper_loading_image);
        mCheckedLottie = findViewById(R.id.wallpaper_loading_end);
        mLoadingBg.setBackground(BackgroundDrawables.createBackgroundDrawable(
                getContext().getResources().getColor(R.color.black_20_transparent),
                Dimensions.pxFromDp(16), false));

        mLoadingIv.setImageResource(R.drawable.wallpaper_loading);

        mLoadingBg.setVisibility(GONE);
        mLoadingIv.setVisibility(GONE);
        mCheckedLottie.setVisibility(GONE);

        mDownloadingAnimator = ObjectAnimator.ofFloat(mLoadingIv, "rotation", 0, 360);
        mDownloadingAnimator.setInterpolator(new LinearInterpolator());
        mDownloadingAnimator.setDuration(1000);
        mDownloadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDownloadingAnimator != null) {
            mDownloadingAnimator.cancel();
        }
    }

    public void initViewByItemState(WallpaperChooserItem item) {
        int viewType = item.getItemType();
        if (viewType == WallpaperChooserItem.TYPE_ADD_PHOTO) {
            mAddPhotosContainer.setVisibility(View.VISIBLE);
            mWallpaperIv.setVisibility(View.GONE);
        } else if (viewType == WallpaperChooserItem.TYPE_EMPTY) {
            mWallpaperIv.setVisibility(VISIBLE);
            if (WallpaperDrawables.getConversationWallpaperBg() != null) {
                mWallpaperIv.setImageDrawable(WallpaperDrawables.getConversationWallpaperBg());
            } else {
                mWallpaperIv.setImageDrawable(BackgroundDrawables.createBackgroundDrawable(
                        0xffffffff, Dimensions.pxFromDp(3.3f), false));
            }
            mAddPhotosContainer.setVisibility(View.GONE);
            setBackground(null);
        } else {
            mWallpaperIv.setVisibility(VISIBLE);
            GlideApp.with(mWallpaperIv).load(item.getThumbnailUrl()).into(mWallpaperIv);
            mAddPhotosContainer.setVisibility(View.GONE);
            setBackground(null);
        }

        if (mDownloadingAnimator.isRunning()) {
            mDownloadingAnimator.cancel();
        }
        if (mCheckedLottie.isAnimating()) {
            mCheckedLottie.cancelAnimation();
        }
        mIsDownloadAnimationPlaying = false;
        if (item.isItemDownloading()) {
            mIsDownloadAnimationPlaying = true;
            mCheckedLottie.setVisibility(GONE);
            mLoadingBg.setVisibility(VISIBLE);
            mLoadingBg.setAlpha(1);
            mLoadingIv.setVisibility(VISIBLE);
            ((View)mLoadingIv).setAlpha(1);
            mDownloadingAnimator.start();
        } else if (item.isItemChecked()) {
            mCheckedLottie.setProgress(1);
            mCheckedLottie.setVisibility(VISIBLE);
            mLoadingBg.setAlpha(1);
            mLoadingBg.setVisibility(VISIBLE);
            mLoadingIv.setVisibility(GONE);
        } else {
            mCheckedLottie.setVisibility(GONE);
            mLoadingBg.setVisibility(GONE);
            mLoadingIv.setVisibility(GONE);
        }
    }

    public void onDownloadStart() {
        if (mIsDownloadAnimationPlaying) {
            return;
        }
        mIsDownloadAnimationPlaying = true;
        mLoadingBg.setVisibility(VISIBLE);
        mLoadingIv.setVisibility(VISIBLE);
        mCheckedLottie.setVisibility(GONE);

        ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(mLoadingBg, "alpha", 0, 1f);
        bgAnimator.setDuration(120);
        ObjectAnimator ivAnimator = ObjectAnimator.ofFloat(mLoadingIv, "alpha", 0, 1f);
        ivAnimator.setDuration(120);
        bgAnimator.start();
        ivAnimator.start();
        if (mCheckedLottie.isAnimating()) {
            mCheckedLottie.cancelAnimation();
        }
        mDownloadingAnimator.start();
    }

    public void setUncheckedState() {
        mIsDownloadAnimationPlaying = false;
        if (mCheckedLottie.isAnimating()) {
            mCheckedLottie.cancelAnimation();
        }
        mCheckedLottie.setVisibility(GONE);
        mLoadingIv.setVisibility(GONE);

        if (mLoadingBg.getVisibility() == VISIBLE
                && mLoadingBg.getAlpha() == 1) {
            ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(mLoadingBg, "alpha", 0f);
            bgAnimator.setDuration(120);
            bgAnimator.start();
        }
    }

    public void setCheckedState() {
        mIsDownloadAnimationPlaying = false;
        mLoadingBg.setVisibility(VISIBLE);
        mLoadingBg.setAlpha(0);

        mCheckedLottie.setProgress(0);
        mCheckedLottie.setVisibility(VISIBLE);

        ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(mLoadingBg, "alpha", 1f);
        bgAnimator.setDuration(120);
        bgAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCheckedLottie.playAnimation();
            }
        });
        bgAnimator.start();
    }

    //download finish but not check
    public void onDownloadFinish() {
        // if (mDownloadingAnimator != null && mDownloadingAnimator.isRunning()) {
        if (mLoadingIv.getVisibility() == VISIBLE && mLoadingIv.getAlpha() == 1) {
            ObjectAnimator ivAnimator = ObjectAnimator.ofFloat(mLoadingIv, "alpha", 1, 0);
            ivAnimator.setDuration(200);
            ivAnimator.start();
        }
        if (mLoadingBg.getVisibility() == VISIBLE && mLoadingBg.getAlpha() == 1) {
            ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(mLoadingBg, "alpha", 1, 0);
            bgAnimator.setDuration(200);
            bgAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDownloadingAnimator.cancel();
                }
            });
            bgAnimator.start();
        } else {
            mDownloadingAnimator.cancel();
        }
        //   }
        mIsDownloadAnimationPlaying = false;
    }

    //download finish & check
    public void onDownloadSuccessAndChecked() {
        if (mIsDownloadAnimationPlaying) {
            ObjectAnimator ivAnimator = ObjectAnimator.ofFloat(mLoadingIv, "alpha", 1, 0);
            ivAnimator.setDuration(200);
            ivAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDownloadingAnimator.cancel();
                    mCheckedLottie.setVisibility(VISIBLE);
                    mCheckedLottie.playAnimation();
                }
            });
            ivAnimator.start();
        } else {
            mLoadingIv.setVisibility(GONE);
            ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(mLoadingBg, "alpha", 0, 1);
            bgAnimator.setDuration(120);
            bgAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCheckedLottie.setVisibility(VISIBLE);
                    mCheckedLottie.playAnimation();
                }
            });
            bgAnimator.start();
        }

        mIsDownloadAnimationPlaying = false;
    }
}
