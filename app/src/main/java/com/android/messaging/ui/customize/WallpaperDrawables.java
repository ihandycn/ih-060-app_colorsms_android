package com.android.messaging.ui.customize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeManager;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class WallpaperDrawables {

    public static Bitmap sWallpaperBitmap;
    public static Bitmap sListWallpaperBitmap;

    public static void applyWallpaperBg(String url) {
        if (!TextUtils.isEmpty(url)) {
            WallpaperManager.resetGlobalWallpaper();
        }
    }

    public static Drawable getWallpaperBg() {
        if (sWallpaperBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), sWallpaperBitmap);
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();

        if (!info.isInLocalFolder()) {
            if (info.mIsLocalTheme) {
                try {
                    InputStream ims = HSApplication.getContext().getAssets().open("themes/" + info.mThemeKey + "/"
                            + info.wallpaperUrl);
                    Bitmap bitmap = BitmapFactory.decodeStream(ims);
                    sWallpaperBitmap = bitmap;
                    return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + info.mThemeKey),
                    ThemeManager.WALLPAPER_BG_FILE_NAME);
            if (file.exists()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    sWallpaperBitmap = bitmap;
                    return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                } catch (Exception ignored) {

                }
            }
        }

        return null;
    }

    public static Bitmap getWallpaperBgBitmap() {
        if (sWallpaperBitmap != null) {
            return sWallpaperBitmap;
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();

        if (!info.isInLocalFolder()) {
            if (info.mIsLocalTheme) {
                try {
                    InputStream ims = HSApplication.getContext().getAssets().open("themes/" + info.mThemeKey + "/"
                            + info.wallpaperUrl);
                    sWallpaperBitmap = BitmapFactory.decodeStream(ims);
                    return sWallpaperBitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + info.mThemeKey),
                    ThemeManager.WALLPAPER_BG_FILE_NAME);
            if (file.exists()) {
                try {
                    sWallpaperBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    return sWallpaperBitmap;
                } catch (Exception ignored) {

                }
            }
        }

        return null;
    }


    public static Drawable getListWallpaperBg() {
        if (sListWallpaperBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), sListWallpaperBitmap);
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();

        if (!info.isInLocalFolder()) {
            if (info.mIsLocalTheme) {
                try {
                    InputStream ims = HSApplication.getContext().getAssets().open("themes/" + info.mThemeKey
                            + "/" + info.listWallpaperUrl);
                    Bitmap bitmap = BitmapFactory.decodeStream(ims);
                    sListWallpaperBitmap = bitmap;
                    return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + info.mThemeKey),
                    ThemeManager.LIST_VIEW_WALLPAPER_BG_FILE_NAME);
            if (file.exists()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    sListWallpaperBitmap = bitmap;
                    return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                } catch (Exception ignored) {

                }
            }
        }

        return null;
    }

    public static boolean hasWallpaper() {
        return !TextUtils.isEmpty(ThemeUtils.getCurrentTheme().listWallpaperUrl);
    }
}
