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
            int toolbarBitmapWidth = toolbarBitmap.getWidth();
            int toolbarBitmapHeight = toolbarBitmap.getHeight();
            int wallpaperBitmapWidth = wallpaperBitmap.getWidth();
            int wallpaperBitmapHeight = wallpaperBitmap.getHeight();
            float toolbarHeightRadio = toolbarBitmapHeight * 1.0f / toolbarViewHeight;

            float widthRadio = toolbarBitmapWidth * 1.0f / phoneWidth;
            float wallpaperHeightRadio = wallpaperBitmapHeight * 1.0f / (phoneHeight - toolbarViewHeight);

            int scaledToolbarWidth;
            int scaledToolbarHeight;
            int scaledWallpaperWidth;
            int scaledWallpaperHeight;

            if (toolbarHeightRadio <= widthRadio && toolbarHeightRadio <= wallpaperHeightRadio) {
                //toolbar drawable height is the shortest length;
                scaledToolbarHeight = toolbarViewHeight;

            } else if (widthRadio <= toolbarHeightRadio && widthRadio <= wallpaperHeightRadio) {
                scaledToolbarWidth = phoneWidth;
                scaledToolbarHeight = (int) (toolbarBitmapHeight * scaledToolbarWidth * 1.0f / toolbarBitmapWidth);
            } else {
                scaledWallpaperHeight = phoneHeight - toolbarViewHeight;

                scaledWallpaperWidth = (int) (scaledWallpaperHeight * wallpaperBitmapWidth * 1.0f / wallpaperBitmapHeight);
                scaledToolbarWidth = scaledWallpaperWidth;
                scaledToolbarHeight = (int) (toolbarBitmapHeight * scaledToolbarWidth * 1.0f / toolbarBitmapWidth);
            }

            float toolbarScale = toolbarBitmapHeight * 1.0f / scaledToolbarHeight;
            int resizedToolbarHeight = (int) (toolbarScale * toolbarViewHeight);
            int resizedWidth = (int) (toolbarScale * phoneWidth);

            int resizedWallpaperHeight = (int) (toolbarScale * (phoneHeight - toolbarViewHeight));

            int startX = (toolbarBitmap.getWidth() - resizedWidth) / 2;
            int toolbarStartY = toolbarBitmap.getHeight() - resizedToolbarHeight;

            //resize toolbar bitmap
            Bitmap resizedToolbarBitmap = Bitmap.createBitmap(toolbarBitmap, Math.max(0, startX),
                    Math.max(toolbarStartY, 0),
                    Math.min(resizedWidth, toolbarBitmap.getWidth() - Math.max(0, startX)),
                    Math.min(resizedToolbarHeight, toolbarBitmap.getHeight() - Math.max(toolbarStartY, 0)));
            CommonUtils.saveBitmapToFile(resizedToolbarBitmap, toolbarFile);

            //resize wallpaper
            Bitmap resizedWallpaper = Bitmap.createBitmap(wallpaperBitmap,
                    Math.max(0, startX),
                    0,
                    Math.min(resizedWidth, wallpaperBitmap.getWidth() - Math.max(0, startX)),
                    Math.min(resizedWallpaperHeight, wallpaperBitmap.getHeight()));
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
            Bitmap resizedListWallpaper = Bitmap.createBitmap(listWallpaperBitmap,
                    Math.max(0, startX),
                    0,
                    Math.min(resizedWidth, listWallpaperBitmap.getWidth() - Math.max(0, startX)),
                    Math.min(resizedWallpaperHeight, listWallpaperBitmap.getHeight()));
            CommonUtils.saveBitmapToFile(resizedListWallpaper, listWallpaperFile);
        }
    }
}
