package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.android.messaging.glide.GlideApp;
import com.android.messaging.util.BugleAnalytics;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.IOException;

public class ThemeSelectItemView extends ConstraintLayout implements ThemeUtils.IThemeChangeListener {
    private ImageView mThemePreviewImg;
    private TextView mThemeNameTv;
    private ImageView mThemeState;
    private TextView mThemeDownloadTimes;
    private LottieAnimationView mDownloadSuccessLottie;
    private ThemeDownloadingView mDownloadingView;
    private ThemeInfo mThemeInfo;
    private ThemeDownloadManager.IThemeDownloadListener mThemeDownloadListener;
    private View mButtonGroupContainer;
    private ThemeUtils.IThemeChangeListener mThemeChangeListener;

    public ThemeSelectItemView(Context context) {
        super(context);
    }

    public ThemeSelectItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemeSelectItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void initView() {
        mThemePreviewImg = findViewById(R.id.theme_thumbnail);
        mThemeNameTv = findViewById(R.id.theme_name);
        mThemeDownloadTimes = findViewById(R.id.theme_downloaded_times);
        mThemeState = findViewById(R.id.theme_state);
        mDownloadSuccessLottie = findViewById(R.id.theme_download_success);
        mDownloadingView = findViewById(R.id.theme_downloading_view);
        mButtonGroupContainer = findViewById(R.id.theme_button_group_container);
    }

    public void resetStateByTheme() {
        ThemeInfo info = ThemeInfo.getDownloadingTheme(mThemeInfo);
        if (info != null) {
            mThemeInfo = info;
        }

        if (mThemeInfo.isDownloaded()) {
            mDownloadingView.updatePercent(0);
            if (mThemeInfo.mThemeKey.equals(ThemeUtils.getCurrentTheme().mThemeKey)) {
                setSelectedState();
            } else {
                setDownloadedState();
            }
        } else {
            mThemeDownloadListener = new ThemeDownloadManager.IThemeDownloadListener() {
                @Override
                public void onDownloadSuccess() {
                    Threads.postOnMainThread(() -> setDownloadSuccessState());
                }

                @Override
                public void onDownloadFailed() {
                    Threads.postOnMainThread(() -> setNormalState());
                }

                @Override
                public void onDownloadUpdate(float process) {
                    Threads.postOnMainThread(() -> mDownloadingView.updatePercent(process));
                }
            };
            mThemeInfo.addDownloadListener(mThemeDownloadListener);
            setNormalState();
        }
    }

    public void setThemeData(ThemeInfo info) {
        mThemeInfo = info;
        if (info.mThemeKey.equals(ThemeUtils.DEFAULT_THEME_KEY)) {
            findViewById(R.id.theme_download_times_container).setVisibility(INVISIBLE);
        } else {
            findViewById(R.id.theme_download_times_container).setVisibility(VISIBLE);
        }

        mThemePreviewImg.setOnClickListener(v -> {
            ThemePreviewActivity.startThemePreviewActivity(getContext(), info);
            BugleAnalytics.logEvent("Customize_ThemeCenter_Theme_Click", true,
                    "theme", info.mThemeKey);
        });

        if (info.isDownloaded()) {
            mDownloadingView.updatePercent(0);
            if (info.mThemeKey.equals(ThemeUtils.getCurrentTheme().mThemeKey)) {
                setSelectedState();
            } else {
                setDownloadedState();
            }
        } else {
            mThemeDownloadListener = new ThemeDownloadManager.IThemeDownloadListener() {
                @Override
                public void onDownloadSuccess() {
                    Threads.postOnMainThread(() -> setDownloadSuccessState());
                }

                @Override
                public void onDownloadFailed() {
                    Threads.postOnMainThread(() -> setNormalState());
                }

                @Override
                public void onDownloadUpdate(float process) {
                    Threads.postOnMainThread(() -> mDownloadingView.updatePercent(process));
                }
            };
            mThemeInfo.addDownloadListener(mThemeDownloadListener);
            setNormalState();
        }

        mThemeNameTv.setText(info.name);
        mThemeDownloadTimes.setText(String.valueOf(info.mThemeDownloadTimes));

        if (info.mIsLocalTheme) {
            Drawable drawable = null;
            try {
                drawable = new BitmapDrawable(getResources(), getResources().getAssets()
                        .open("themes/" + info.mThemeKey + "/" + info.previewUrl));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mThemePreviewImg.setImageDrawable(drawable);
        } else {
            GlideApp.with(getContext())
                    .asBitmap()
                    .load(ThemeDownloadManager.getBaseRemoteUrl() + info.mThemeKey + "/" + info.previewUrl)
                    .placeholder(R.drawable.theme_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.theme_glide_failed)
                    .into(mThemePreviewImg);
        }
    }

    public void addThemeChangeListener(ThemeUtils.IThemeChangeListener listener) {
        mThemeChangeListener = listener;
    }

    private void setNormalState() {
        mDownloadingView.updatePercent(0);
        mThemeState.setVisibility(VISIBLE);
        mThemeState.setImageResource(R.drawable.theme_need_download);
        mDownloadSuccessLottie.setVisibility(INVISIBLE);
        mButtonGroupContainer.setEnabled(true);
        mButtonGroupContainer.setOnClickListener(v -> {
            mButtonGroupContainer.setEnabled(false);
            mThemeInfo.downloadTheme();
            BugleAnalytics.logEvent("Customize_ThemeCenter_Theme_Download", true,
                    "theme", mThemeInfo.mThemeKey, "from", "list");
        });
    }

    private void setDownloadSuccessState() {
        mThemeState.setImageDrawable(null);
        mDownloadingView.updatePercent(0);
        mDownloadSuccessLottie.setVisibility(VISIBLE);
        mDownloadSuccessLottie.playAnimation();
        mButtonGroupContainer.setEnabled(true);
        mButtonGroupContainer.setOnClickListener(v -> applyTheme());
    }

    private void setDownloadedState() {
        mThemeState.setImageDrawable(null);
        mDownloadingView.updatePercent(0);
        mDownloadSuccessLottie.setVisibility(VISIBLE);
        mDownloadSuccessLottie.setProgress(1);
        mButtonGroupContainer.setEnabled(true);
        mButtonGroupContainer.setOnClickListener(v -> applyTheme());
    }

    private void setSelectedState() {
        mDownloadSuccessLottie.setVisibility(INVISIBLE);
        mThemeState.setImageResource(R.drawable.theme_applied);
        mButtonGroupContainer.setEnabled(false);
    }

    private void applyTheme() {
        setSelectedState();
        ThemeUtils.applyTheme(mThemeInfo);
        Toasts.showToast(R.string.apply_theme_success);
        if (mThemeChangeListener != null) {
            mThemeChangeListener.onThemeChanged();
        }
        BugleAnalytics.logEvent("Customize_ThemeCenter_Theme_Apply", true,
                "theme", mThemeInfo.mThemeKey, "from", "list");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mThemeInfo != null && mThemeDownloadListener != null) {
            mThemeInfo.removeDownloadListener(mThemeDownloadListener);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (mButtonGroupContainer != null) {
                mButtonGroupContainer.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff,
                        mButtonGroupContainer.getWidth() / 2, true));
            }
        }
    }

    @Override
    public void onThemeChanged() {
        resetStateByTheme();
    }
}
