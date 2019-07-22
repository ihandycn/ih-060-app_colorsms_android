package com.android.messaging.ui.customize.theme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;

public class ThemePreviewPagerView extends ConstraintLayout {

    private OnClickListener mApplyClickListener;
    private ThemeInfo mThemeInfo;
    private ThemeDownloadManager.IThemeDownloadListener mDownloadListener;
    private TextView mButton;
    private float mTargetDownloadRate;
    private float mCurrentRate;
    private boolean mIsThemeDownloading;
    private Choreographer.FrameCallback mFrameCallback;
    private Choreographer mChoreographer;
    private final float[] mSpeed = {0};
    private boolean mDownloadCompleted = false;
    private boolean mIsThemeAppling;

    public ThemePreviewPagerView(Context context) {
        super(context);
    }

    public ThemePreviewPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setThemeInfo(ThemeInfo info) {
        mThemeInfo = info;

        final LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.theme_preview_layout, this, true);

        findViewById(R.id.apply_bg).setBackground(BackgroundDrawables.createBackgroundDrawable(
                0xffc9cacd, Dimensions.pxFromDp(6.7f), false));

        mButton = findViewById(R.id.apply);
        Drawable applyDrawable = BackgroundDrawables.createBackgroundDrawable(
                Color.parseColor(mThemeInfo.themeColor), Dimensions.pxFromDp(6.7f), true);
        ClipDrawable clipBg = new ClipDrawable(applyDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
        clipBg.setLevel(10000);
        mButton.setBackground(clipBg);

        changeThemeState();

        mDownloadListener = new ThemeDownloadManager.IThemeDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                //mIsThemeDownloading = false;
                mDownloadCompleted = true;
                mTargetDownloadRate = 1;
                if (mTargetDownloadRate > mCurrentRate) {
                    mSpeed[0] = (mTargetDownloadRate - mCurrentRate) / 30;
                }
            }

            @Override
            public void onDownloadFailed() {
                mIsThemeDownloading = false;
                mDownloadCompleted = true;
                Threads.postOnMainThread(() -> {
                    clipBg.setLevel(10000);
                    changeThemeState();
                });
            }

            @Override
            public void onDownloadUpdate(float rate) {
                if (rate < mTargetDownloadRate) {
                    return;
                }
                if (mDownloadCompleted) {
                    return;
                }
                mTargetDownloadRate = rate;
                if (mTargetDownloadRate > mCurrentRate) {
                    mSpeed[0] = (mTargetDownloadRate - mCurrentRate) / 30;
                }
            }
        };

        mThemeInfo.addDownloadListener(mDownloadListener);
        if (mThemeInfo.isDownloading() || !mThemeInfo.isDownloaded()) {
            mChoreographer = Choreographer.getInstance();
            mFrameCallback = frameTimeNanos -> {
                if (mIsThemeDownloading) {
                    if (1 - mCurrentRate < 0.00001) {
                        mButton.getBackground().setLevel(10000);
                        mCurrentRate = 1;
                        if (mDownloadCompleted) {
                            mIsThemeDownloading = false;
                            changeThemeState();
                        }
                    }
                    if (mCurrentRate < mTargetDownloadRate) {
                        mCurrentRate += mSpeed[0];
                        mButton.getBackground().setLevel((int) (mCurrentRate * 10000));
                    }
                    mChoreographer.postFrameCallback(mFrameCallback);
                }
            };
            if (mThemeInfo.isDownloading()) {
                mIsThemeDownloading = true;
                mButton.getBackground().setLevel(10);
                mChoreographer.postFrameCallback(mFrameCallback);
            }
        }

        //set view pager
        List<View> guideViewList = new ArrayList<>();
        Drawable selectedDrawable = BackgroundDrawables.createBackgroundDrawable(
                Color.parseColor(mThemeInfo.themeColor), Dimensions.pxFromDp(7.3f / 2), false);
        Drawable unselectedDrawable = BackgroundDrawables.createBackgroundDrawable(
                0xffffffff, 0xffffffff, Dimensions.pxFromDp(0.7f), 0xff848694,
                Dimensions.pxFromDp(7.3f / 2), false, false
        );

        ViewPager mPager = findViewById(R.id.pager);
        ThemePreviewPagerAdapter mAdapter = new ThemePreviewPagerAdapter(getContext(), mThemeInfo);
        mPager.setAdapter(mAdapter);
        mPager.setPageTransformer(false, new ThemePagerTransformer(getContext()));
        mPager.setPageMargin(Dimensions.pxFromDp(16));

        mAdapter.setOnPageClickListener(mPager::setCurrentItem);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                BugleAnalytics.logEvent("Customize_ThemeCenter_Detail_Slide", true);
                for (int i = 0; i < guideViewList.size(); i++) {
                    View v = guideViewList.get(i);
                    if (i == position) {
                        v.setBackground(selectedDrawable);
                    } else {
                        v.setBackground(unselectedDrawable);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //set pager guide
        LinearLayout pagerGuide = findViewById(R.id.pager_guide);
        for (int i = 0; i < mThemeInfo.mPreviewList.size(); i++) {
            View v = new View(getContext());
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(Dimensions.pxFromDp(7.3f), Dimensions.pxFromDp(7.3f));
            params.leftMargin = Dimensions.pxFromDp(4);
            params.rightMargin = Dimensions.pxFromDp(4);
            v.setLayoutParams(params);
            pagerGuide.addView(v);
            guideViewList.add(v);

            if (i == 0) {
                v.setBackground(selectedDrawable);
            } else {
                v.setBackground(unselectedDrawable);
            }
        }
    }

    private void changeThemeState() {
        if (mThemeInfo.isDownloading()) {
            mButton.setText(R.string.downloading);
            mButton.setEnabled(false);
        } else if (ThemeUtils.getCurrentThemeName().equals(mThemeInfo.mThemeKey)) {
            mButton.setText(R.string.welcome_choose_theme_current);
            mButton.setEnabled(false);
        } else if (mThemeInfo.isDownloaded()) {
            mButton.setText(R.string.sms_show_apply);
            mButton.setEnabled(true);
            mButton.setOnClickListener(v -> {
                if (mIsThemeAppling) {
                    return;
                }
                mIsThemeAppling = true;
                if (mApplyClickListener != null) {
                    mApplyClickListener.onClick(mButton);
                }
                ThemeUtils.applyTheme(mThemeInfo, 0);
                BugleAnalytics.logEvent("Customize_ThemeCenter_Theme_Apply", true, true,
                        "theme", mThemeInfo.mThemeKey, "from", "detail");
            });
        } else {
            mButton.setText(R.string.get_now);
            mButton.setEnabled(true);
            mButton.setOnClickListener(v -> {
                mIsThemeDownloading = true;
                mChoreographer.postFrameCallback(mFrameCallback);
                mThemeInfo.downloadTheme();
                BugleAnalytics.logEvent("Customize_ThemeCenter_Theme_Download", true,
                        "theme", mThemeInfo.mThemeKey, "from", "detail");
                changeThemeState();
                mButton.getBackground().setLevel(0);
            });
        }
    }

    public void setOnApplyClickListener(OnClickListener listener) {
        mApplyClickListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mThemeInfo != null && mDownloadListener != null) {
            mThemeInfo.removeDownloadListener(mDownloadListener);
        }
        if (mFrameCallback != null && mChoreographer != null) {
            mIsThemeDownloading = false;
            mChoreographer.removeFrameCallback(mFrameCallback);
        }
    }
}
