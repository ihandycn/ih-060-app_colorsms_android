package com.android.messaging.ui.customize.theme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Dimensions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WallpaperSizeManager {
    public static void resizeThemeBitmap(ThemeInfo theme) {
        if (!TextUtils.isEmpty(theme.toolbarBgUrl)) {
            File baseFolder = CommonUtils.getDirectory(ThemeBubbleDrawables.THEME_BASE_PATH + theme.mThemeKey);
            File toolbarFile = new File(baseFolder, ThemeBubbleDrawables.TOOLBAR_BG_FILE_NAME);
            if (!toolbarFile.exists()) {
                return;
            }
            int phoneWidth = Dimensions.getPhoneWidth(HSApplication.getContext());
            int phoneHeight = Dimensions.getPhoneHeight(HSApplication.getContext());
            Bitmap toolbarBitmap = BitmapFactory.decodeFile(toolbarFile.getAbsolutePath());
            Bitmap wallpaperBitmap = null;
            File wallpaperFile = new File(baseFolder, ThemeBubbleDrawables.WALLPAPER_BG_FILE_NAME);
            if (!TextUtils.isEmpty(theme.wallpaperUrl)) {
                if (wallpaperFile.exists()) {
                    wallpaperBitmap = BitmapFactory.decodeFile(wallpaperFile.getAbsolutePath());
                }
            }

            if (toolbarBitmap == null || wallpaperBitmap == null) {
                return;
            }

            int toolbarViewHeight = Dimensions.pxFromDp(56) + Dimensions.getStatusBarHeight(HSApplication.getContext());
            int mToolbarDrawableWidth = toolbarBitmap.getWidth();
            int mToolbarDrawableHeight = toolbarBitmap.getHeight();
            int mWallpaperDrawableWidth = wallpaperBitmap.getWidth();
            int mWallpaperDrawableHeight = wallpaperBitmap.getHeight();
            float toolbarHeightRadio = mToolbarDrawableHeight * 1.0f / toolbarViewHeight;

            float widthRadio = mToolbarDrawableWidth * 1.0f / phoneWidth;
            float wallpaperHeightRadio = mWallpaperDrawableHeight * 1.0f / (phoneHeight - toolbarViewHeight);

            int mToolbarViewWidth;
            int mToolbarViewHeight;
            int mWallpaperViewWidth;
            int mWallpaperViewHeight;

            if (toolbarHeightRadio <= widthRadio && toolbarHeightRadio <= wallpaperHeightRadio) {
                //toolbar drawable height is the shortest length;
                mToolbarViewHeight = toolbarViewHeight;

            } else if (widthRadio <= toolbarHeightRadio && widthRadio <= wallpaperHeightRadio) {
                mToolbarViewWidth = phoneWidth;
                mToolbarViewHeight = (int) (mToolbarDrawableHeight * mToolbarViewWidth * 1.0f / mToolbarDrawableWidth);
            } else {
                mWallpaperViewHeight = phoneHeight - toolbarViewHeight;

                mWallpaperViewWidth = (int) (mWallpaperViewHeight * mWallpaperDrawableWidth * 1.0f / mWallpaperDrawableHeight);
                mToolbarViewWidth = mWallpaperViewWidth;
                mToolbarViewHeight = (int) (mToolbarDrawableHeight * mToolbarViewWidth * 1.0f / mToolbarDrawableWidth);
            }

            float toolbarScale = mToolbarDrawableHeight * 1.0f / mToolbarViewHeight;
            int resizedToolbarHeight = (int) (toolbarScale * toolbarViewHeight);
            int resizedWidth = (int) (toolbarScale * phoneWidth);

            int resizedWallpaperHeight = (int) (toolbarScale * (phoneHeight - toolbarViewHeight));

            int startX = (toolbarBitmap.getWidth() - resizedWidth) / 2;
            int toolbarStartY = toolbarBitmap.getHeight() - resizedToolbarHeight;

            //resize toolbar bitmap
            Bitmap resizedToolbarBitmap = Bitmap.createBitmap(toolbarBitmap, Math.max(0, startX), toolbarStartY,
                    resizedWidth, resizedToolbarHeight);
            CommonUtils.saveBitmapToFile(resizedToolbarBitmap, toolbarFile);

            //resize wallpaper
            Bitmap resizedWallpaper = Bitmap.createBitmap(wallpaperBitmap, Math.max(0, startX), 0,
                    resizedWidth, Math.min(resizedWallpaperHeight, wallpaperBitmap.getHeight()));
            CommonUtils.saveBitmapToFile(resizedWallpaper, wallpaperFile);

            //resize and replace wallpaper for list activity
            Bitmap listWallpaperBitmap = null;
            File listWallpaperFile = new File(baseFolder, ThemeBubbleDrawables.LIST_WALLPAPER_BG_FILE_NAME);
            if (!TextUtils.isEmpty(theme.listWallpaperUrl)) {
                if (listWallpaperFile.exists()) {
                    listWallpaperBitmap = BitmapFactory.decodeFile(listWallpaperFile.getAbsolutePath());
                }
            }
            if (listWallpaperBitmap == null) {
                return;
            }
            Bitmap resizedListWallpaper = Bitmap.createBitmap(listWallpaperBitmap, Math.max(0, startX), 0,
                    resizedWidth, Math.min(resizedWallpaperHeight, listWallpaperBitmap.getHeight()));
            CommonUtils.saveBitmapToFile(resizedListWallpaper, listWallpaperFile);
        }
    }
}
