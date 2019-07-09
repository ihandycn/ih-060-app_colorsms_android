package com.android.messaging.ui.customize.theme;

import android.graphics.Bitmap;

import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Dimensions;

public class WallpaperSizeManager {

    public static WallpaperSizeManager sInstance;
    private int mPhoneWidth;
    private int mPhoneHeight;
    private int mToolbarDrawableWidth;
    private int mToolbarDrawableHeight;
    private int mWallpaperDrawableWidth;
    private int mWallpaperDrawableHeight;

    private int mToolbarViewWidth;
    private int mToolbarViewHeight;
    private int mWallpaperViewWidth;
    private int mWallpaperViewHeight;

    private WallpaperSizeManager() {
        mPhoneWidth = Dimensions.getPhoneWidth(HSApplication.getContext());
        mPhoneHeight = Dimensions.getPhoneHeight(HSApplication.getContext());
        loadWallpaperParams();
    }

    public static WallpaperSizeManager getInstance() {
        if (sInstance == null) {
            sInstance = new WallpaperSizeManager();
        }
        return sInstance;
    }

    public synchronized void loadWallpaperParams() {
        mWallpaperViewWidth = 0;
        mWallpaperViewHeight = 0;
        mToolbarViewWidth = 0;
        mToolbarViewHeight = 0;
        Bitmap toolbarBg = ToolbarDrawables.getToolbarBgBitmap();
        if (toolbarBg != null) {
            mToolbarDrawableWidth = toolbarBg.getWidth();
            mToolbarDrawableHeight = toolbarBg.getHeight();

            Bitmap bg = WallpaperDrawables.getConversationWallpaperBitmap();
            if (bg != null) {
                mWallpaperDrawableWidth = bg.getWidth();
                mWallpaperDrawableHeight = bg.getHeight();
                computeParam();
            }
        }
    }

    private void computeParam() {
        //we need to make sure the toolbar drawable width = wallpaper drawable width;
        float toolbarHeightRadio = mToolbarDrawableHeight * 1.0f
                / (Dimensions.pxFromDp(56) + Dimensions.getStatusBarHeight(HSApplication.getContext()));
        float widthRadio = mToolbarDrawableWidth * 1.0f / mPhoneWidth;
        float wallpaperHeightRadio = mWallpaperDrawableHeight * 1.0f /
                (mPhoneHeight - Dimensions.pxFromDp(56) -
                        Dimensions.getStatusBarHeight(HSApplication.getContext()));

        if (toolbarHeightRadio <= widthRadio && toolbarHeightRadio <= wallpaperHeightRadio) {
            //toolbar drawable height is the shortest length;
            mToolbarViewHeight = Dimensions.pxFromDp(56) + Dimensions.getStatusBarHeight(HSApplication.getContext());

            mToolbarViewWidth = (int) (mToolbarDrawableWidth * mToolbarViewHeight * 1.0f / mToolbarDrawableHeight);
            mWallpaperViewWidth = mToolbarViewWidth;
            mWallpaperViewHeight = (int) (mWallpaperViewWidth * mWallpaperDrawableHeight * 1.0f / mWallpaperDrawableWidth);
        } else if (widthRadio <= toolbarHeightRadio && widthRadio <= wallpaperHeightRadio) {
            mWallpaperViewWidth = mPhoneWidth;
            mToolbarViewWidth = mPhoneWidth;

            mWallpaperViewHeight = (int) (mWallpaperViewWidth * mWallpaperDrawableHeight * 1.0f / mWallpaperDrawableWidth);
            mToolbarViewHeight = (int) (mToolbarDrawableHeight * mToolbarViewWidth * 1.0f / mToolbarDrawableWidth);

        } else {
            mWallpaperViewHeight = mPhoneHeight - Dimensions.pxFromDp(56) -
                    Dimensions.getStatusBarHeight(HSApplication.getContext());

            mWallpaperViewWidth = (int) (mWallpaperViewHeight * mWallpaperDrawableWidth * 1.0f / mWallpaperDrawableHeight);
            mToolbarViewWidth = mWallpaperViewWidth;
            mToolbarViewHeight = (int) (mToolbarDrawableHeight * mToolbarViewWidth * 1.0f / mToolbarDrawableWidth);
        }
    }

    public synchronized int[] getToolbarFrameSize() {
        if (mToolbarViewWidth > 0) {
            int[] param = new int[2];
            param[0] = mToolbarViewWidth;
            param[1] = mToolbarViewHeight;
            return param;
        } else {
            return null;
        }
    }

    public synchronized int[] getWallpaperFrameSize() {
        if (mWallpaperViewWidth > 0) {
            int[] param = new int[2];
            param[0] = mWallpaperViewWidth;
            param[1] = mWallpaperViewHeight;
            return param;
        } else {
            return null;
        }
    }
}
