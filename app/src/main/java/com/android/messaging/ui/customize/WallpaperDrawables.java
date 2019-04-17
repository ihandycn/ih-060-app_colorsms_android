package com.android.messaging.ui.customize;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.ihs.app.framework.HSApplication;

import java.io.IOException;
import java.io.InputStream;

public class WallpaperDrawables {

    private static final String PREF_KEY_CUSTOMIZE_WALLPAPER_BACKGROUND = "pref_key_customize_wallpaper_background";
    private static final String PREF_KEY_CUSTOMIZE_LIST_WALLPAPER_BACKGROUND = "pref_key_customize_list_wallpaper_background";

    public static void applyWallpaperBg(String url) {
        Factory.get().getCustomizePrefs().putString(PREF_KEY_CUSTOMIZE_WALLPAPER_BACKGROUND, url);
        if (!TextUtils.isEmpty(url)) {
            WallpaperManager.resetGlobalWallpaper();
        }
    }
    public static void applyListWallpaperBg(String url) {
        Factory.get().getCustomizePrefs().putString(PREF_KEY_CUSTOMIZE_LIST_WALLPAPER_BACKGROUND, url);
    }

    public static Drawable getWallpaperBg() {
        String url = Factory.get().getCustomizePrefs().getString(PREF_KEY_CUSTOMIZE_WALLPAPER_BACKGROUND, "");
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        if (url.startsWith("assets://")) {
            try {
                InputStream ims = HSApplication.getContext().getAssets().open(url.replace("assets://", ""));
                return Drawable.createFromStream(ims, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Drawable getListWallpaperBg() {
        String url = Factory.get().getCustomizePrefs().getString(PREF_KEY_CUSTOMIZE_LIST_WALLPAPER_BACKGROUND, "");
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        if (url.startsWith("assets://")) {
            try {
                InputStream ims = HSApplication.getContext().getAssets().open(url.replace("assets://", ""));
                return Drawable.createFromStream(ims, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static boolean hasWallpaper() {
        return !TextUtils.isEmpty(Factory.get().getCustomizePrefs().getString(PREF_KEY_CUSTOMIZE_WALLPAPER_BACKGROUND, ""));
    }
}