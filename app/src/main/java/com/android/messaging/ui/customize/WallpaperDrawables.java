package com.android.messaging.ui.customize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.android.messaging.ui.wallpaper.WallpaperManager;
import com.ihs.app.framework.HSApplication;

import java.io.IOException;
import java.io.InputStream;

public class WallpaperDrawables {
    //ConversationActivity Wallpaper
    public static final String PREF_KEY_CUSTOMIZE_WALLPAPER_BACKGROUND = "pref_key_customize_wallpaper_background";
    //ConversationListActivity Wallpaper
    private static final String PREF_KEY_CUSTOMIZE_LIST_WALLPAPER_BACKGROUND = "pref_key_customize_list_wallpaper_background";
    public static Bitmap sConversationWallpaperBitmap;
    public static Bitmap sConversationListWallpaperBitmap;

    public static void applyWallpaperBg(String url) {
        Factory.get().getCustomizePrefs().putString(PREF_KEY_CUSTOMIZE_WALLPAPER_BACKGROUND, url);
        if (!TextUtils.isEmpty(url)) {
            WallpaperManager.resetGlobalWallpaper();
        }
    }

    public static void applyListWallpaperBg(String url) {
        Factory.get().getCustomizePrefs().putString(PREF_KEY_CUSTOMIZE_LIST_WALLPAPER_BACKGROUND, url);
    }

    public static Drawable getConversationWallpaperBg() {
        if (sConversationWallpaperBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), sConversationWallpaperBitmap);
        }

        String url = Factory.get().getCustomizePrefs().getString(PREF_KEY_CUSTOMIZE_WALLPAPER_BACKGROUND, "");
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        if (url.startsWith("assets://")) {
            try {
                InputStream ims = HSApplication.getContext().getAssets().open(url.replace("assets://", ""));
                Bitmap bitmap = BitmapFactory.decodeStream(ims);
                sConversationWallpaperBitmap = bitmap;
                return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Bitmap getConversationListWallpaperBitmap() {
        if (sConversationListWallpaperBitmap != null) {
            return sConversationListWallpaperBitmap;
        }

        String url = Factory.get().getCustomizePrefs().getString(PREF_KEY_CUSTOMIZE_LIST_WALLPAPER_BACKGROUND, "");
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        if (url.startsWith("assets://")) {
            try {
                InputStream ims = HSApplication.getContext().getAssets().open(url.replace("assets://", ""));
                Bitmap bitmap = BitmapFactory.decodeStream(ims);
                sConversationListWallpaperBitmap = bitmap;
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Drawable getConversationListWallpaperDrawable() {
        getConversationListWallpaperBitmap();

        if (sConversationListWallpaperBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), sConversationListWallpaperBitmap);
        }

        return null;
    }

    public static boolean hasWallpaper() {
        return !TextUtils.isEmpty(Factory.get().getCustomizePrefs().getString(PREF_KEY_CUSTOMIZE_WALLPAPER_BACKGROUND, ""));
    }
}
