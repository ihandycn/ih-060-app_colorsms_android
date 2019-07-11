package com.android.messaging.ui.customize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.ui.customize.theme.ThemeDownloadManager;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeBubbleDrawables;
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

    public static Drawable getConversationWallpaperBg() {
        if (sWallpaperBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), sWallpaperBitmap);
        } else {
            Bitmap bitmap = getConversationWallpaperBitmap();
            if (bitmap != null) {
                return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
            }
        }
        return null;
    }

    public static Bitmap getConversationWallpaperBitmap() {
        if (sWallpaperBitmap != null) {
            return sWallpaperBitmap;
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();

        if (TextUtils.isEmpty(info.wallpaperUrl)) {
            return null;
        }

        File file = new File(CommonUtils.getDirectory(
                ThemeBubbleDrawables.THEME_BASE_PATH + info.mThemeKey),
                ThemeBubbleDrawables.WALLPAPER_BG_FILE_NAME);

        if (file.exists()) {
            try {
                sWallpaperBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                return sWallpaperBitmap;
            } catch (Exception ignored) {

            }
        }

        if (info.mIsLocalTheme) {
            try {
                String assetFileName = "themes/" + info.mThemeKey + "/" + info.wallpaperUrl;
                InputStream ims = HSApplication.getContext().getAssets().open(assetFileName);
                sWallpaperBitmap = BitmapFactory.decodeStream(ims);
                if (ims != null) {
                    ims.close();
                }
                //ThemeDownloadManager.getInstance().copyAssetFileAsync(file, assetFileName);
                return sWallpaperBitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Drawable getConversationListWallpaperDrawable() {
        if (sListWallpaperBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), sListWallpaperBitmap);
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();
        if (TextUtils.isEmpty(info.listWallpaperUrl)) {
            return null;
        }

        File file = new File(CommonUtils.getDirectory(
                ThemeBubbleDrawables.THEME_BASE_PATH + info.mThemeKey),
                ThemeBubbleDrawables.LIST_VIEW_WALLPAPER_BG_FILE_NAME);

        if (file.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                sListWallpaperBitmap = bitmap;
                return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
            } catch (Exception ignored) {

            }
        }

        if (info.mIsLocalTheme) {
            try {
                String assetFileName = "themes/" + info.mThemeKey + "/" + info.listWallpaperUrl;
                InputStream ims = HSApplication.getContext().getAssets().open(assetFileName);
                Bitmap bitmap = BitmapFactory.decodeStream(ims);
                sListWallpaperBitmap = bitmap;
                if (ims != null) {
                    ims.close();
                }
                //ThemeDownloadManager.getInstance().copyAssetFileAsync(file, assetFileName);
                return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static boolean hasWallpaper() {
        return !TextUtils.isEmpty(ThemeUtils.getCurrentTheme().listWallpaperUrl);
    }
}
