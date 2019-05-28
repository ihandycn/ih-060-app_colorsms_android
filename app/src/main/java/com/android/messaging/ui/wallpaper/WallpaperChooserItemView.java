package com.android.messaging.ui.wallpaper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;


public class WallpaperChooserItemView extends FrameLayout {

    private ImageView mWallpaperIv;
    private View mAddPhotosContainer;
    private View mLoadingBg;
    private ImageView mLoadingIv;
    private LottieAnimationView mLoadEndLottie;

    private ObjectAnimator mLoadingAnimator;
    private boolean mIsItemSelected;
    private boolean mIsLoadingPlaying;
    private boolean mIsItemPreSelected;

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
        mLoadEndLottie = findViewById(R.id.wallpaper_loading_end);
        mLoadingBg.setBackground(BackgroundDrawables.createBackgroundDrawable(
                getContext().getResources().getColor(R.color.black_20_transparent),
                Dimensions.pxFromDp(16), false));

        mLoadingBg.setVisibility(GONE);
        mLoadingIv.setVisibility(GONE);
        mLoadEndLottie.setVisibility(GONE);

        mLoadingAnimator = ObjectAnimator.ofFloat(mLoadingIv, "rotation", 0, 360);
        mLoadingAnimator.setInterpolator(new LinearInterpolator());
        mLoadingAnimator.setDuration(1000);
        mLoadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mLoadingAnimator != null) {
            mLoadingAnimator.cancel();
        }
    }

    public void setChooserItem(WallpaperChooserItem item) {
        int viewType = item.getItemType();
        if (viewType == WallpaperChooserItem.TYPE_ADD_PHOTO) {
            mAddPhotosContainer.setVisibility(View.VISIBLE);
            mWallpaperIv.setVisibility(View.GONE);
        } else if (viewType == WallpaperChooserItem.TYPE_EMPTY) {
            if (WallpaperDrawables.getConversationWallpaperBg() != null) {
                mWallpaperIv.setBackground(WallpaperDrawables.getConversationWallpaperBg());
            } else {
                mWallpaperIv.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        0xffffffff, Dimensions.pxFromDp(3.3f), false));
            }
        } else {
            setThumbnail(item.getThumbnailResId());
        }
    }

    public void setThumbnail(@DrawableRes int resId) {
        mWallpaperIv.setImageResource(resId);
    }

    boolean isItemSelected() {
        return mIsItemSelected;
    }

    void onItemPreSelected() {
        mIsItemPreSelected = true;
    }

    boolean isItemPreSelected() {
        return mIsItemPreSelected;
    }

    void onItemSelected() {
        mIsItemSelected = true;
        onSelected();
    }

    void onItemDeselected() {
        mIsItemPreSelected = false;
        if (mIsItemSelected) {
            mIsItemSelected = false;
            if (!mIsLoadingPlaying) {
                onDeselected();
            }
        }
        if (mLoadEndLottie.isAnimating()) {
            mLoadEndLottie.cancelAnimation();
        }
    }

    public void onLoadingDone() {
        if (mIsItemSelected) {
            onLoadingSuccessAndSelected();
        } else if (mLoadingAnimator != null && mLoadingAnimator.isRunning()) {
            ObjectAnimator ivAnimator = ObjectAnimator.ofFloat(mLoadingIv, "alpha", 1, 0);
            ivAnimator.setDuration(200);
            ivAnimator.start();
            ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(mLoadingBg, "alpha", 1, 0);
            bgAnimator.setDuration(200);
            bgAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoadingAnimator.cancel();
                }
            });
            bgAnimator.start();
        }
        mIsLoadingPlaying = false;
    }

    public void onLoadingStart() {
        if (mIsLoadingPlaying) {
            return;
        }
        mIsLoadingPlaying = true;
        mLoadingBg.setVisibility(VISIBLE);
        mLoadingIv.setVisibility(VISIBLE);
        mLoadingBg.setAlpha(0);
        mLoadingIv.setImageResource(R.drawable.wallpaper_loading);
        mLoadEndLottie.setVisibility(GONE);

        ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(mLoadingBg, "alpha", 0, 1f);
        bgAnimator.setDuration(120);
        ObjectAnimator ivAnimator = ObjectAnimator.ofFloat(mLoadingIv, "alpha", 0, 1f);
        ivAnimator.setDuration(120);
        bgAnimator.start();
        ivAnimator.start();
        mLoadingAnimator.start();
    }

    private void onDeselected() {
        ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(mLoadingBg, "alpha", 0f);
        bgAnimator.setDuration(120);

        mLoadEndLottie.setVisibility(GONE);
        bgAnimator.start();
    }

    private void onSelected() {
        mLoadingBg.setVisibility(VISIBLE);
        mLoadingBg.setAlpha(0);

        mLoadEndLottie.setProgress(0);
        mLoadEndLottie.setVisibility(VISIBLE);

        ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(mLoadingBg, "alpha", 1f);
        bgAnimator.setDuration(120);
        bgAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoadEndLottie.playAnimation();
            }
        });

        bgAnimator.start();
    }

    private void onLoadingSuccessAndSelected() {
        if (mLoadingAnimator != null && mLoadingAnimator.isRunning()) {
            ObjectAnimator ivAnimator = ObjectAnimator.ofFloat(mLoadingIv, "alpha", 1, 0);
            ivAnimator.setDuration(200);
            ivAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoadEndLottie.setVisibility(VISIBLE);
                    mLoadEndLottie.playAnimation();
                }
            });
            ivAnimator.start();
        } else {
            ObjectAnimator bgAnimator = ObjectAnimator.ofFloat(mLoadingBg, "alpha", 0, 1);
            bgAnimator.setDuration(120);
            bgAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoadEndLottie.setVisibility(VISIBLE);
                    mLoadEndLottie.playAnimation();
                }
            });
            bgAnimator.start();
        }
    }
}
