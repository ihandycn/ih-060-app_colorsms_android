package com.android.messaging.ui.wallpaper;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.android.messaging.Factory;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.util.BuglePrefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WallpaperManager {


    private static final String PREF_KEY_WALLPAPER_PATH = "pref_key_wallpaper_path";
    static final String LOCAL_DIRECTORY = "wallpapers" + File.separator + "local";
    private static List<WallpaperChangeListener> sWallpaperChangeListeners;

    private static BuglePrefs sPrefs = Factory.get().getCustomizePrefs();

    public interface WallpaperChangeListener {
        void onWallpaperChanged();
        void onOnlineWallpaperChanged();
    }

    public static void addWallpaperChangeListener(WallpaperChangeListener listener) {
        if (sWallpaperChangeListeners == null) {
            sWallpaperChangeListeners = new ArrayList<>();
        }
        sWallpaperChangeListeners.add(listener);
    }

    public static void removeWallpaperChangeListener(WallpaperChangeListener listener) {
        if (sWallpaperChangeListeners != null) {
            sWallpaperChangeListeners.remove(listener);
        }
        if (sWallpaperChangeListeners.size() == 0) {
            sWallpaperChangeListeners.clear();
            sWallpaperChangeListeners = null;
        }
    }

    public static void onWallpaperChanged() {
        if (sWallpaperChangeListeners == null || sWallpaperChangeListeners.size() == 0) {
            return;
        }

        for (WallpaperChangeListener l : sWallpaperChangeListeners) {
            l.onWallpaperChanged();
        }
    }

    public static void onOnlineWallpaperChanged() {
        if (sWallpaperChangeListeners == null || sWallpaperChangeListeners.size() == 0) {
            return;
        }

        for (WallpaperChangeListener l : sWallpaperChangeListeners) {
            l.onOnlineWallpaperChanged();
        }
    }


    public static void setWallPaperOnView(final ImageView view, String conversationId) {
        if (!TextUtils.isEmpty(WallpaperManager.getThreadWallpaperPath(conversationId))) {
            view.setImageDrawable(
                    new BitmapDrawable(WallpaperManager.getThreadWallpaperPath(conversationId)));
        } else {
            Drawable wallpaperDrawable = WallpaperDrawables.getWallpaperBg();
            if (wallpaperDrawable != null) {
                view.setImageDrawable(wallpaperDrawable);
            } else if (!TextUtils.isEmpty(WallpaperManager.getWallpaperPathByThreadId(conversationId))) {
                view.setImageDrawable(
                        new BitmapDrawable(WallpaperManager.getWallpaperPathByThreadId(conversationId)));
            } else {
                view.setImageDrawable(null);
            }
        }
    }

    public static boolean hasWallpaper(String conversationId) {
        if (TextUtils.isEmpty(WallpaperManager.getWallpaperPathByThreadId(conversationId))) {
            return WallpaperDrawables.hasWallpaper();
        }
        return false;
    }

    public static String getWallpaperPathByThreadId(String threadId) {
        String threadWallpaperPath = sPrefs.
                getString(PREF_KEY_WALLPAPER_PATH + "_" + threadId, "");
        if (!TextUtils.isEmpty(threadWallpaperPath)) {
            if (threadWallpaperPath.equals("empty")) {
                return null;
            }
            return threadWallpaperPath;
        } else {
            return getWallpaperPath();
        }
    }


    public static String getThreadWallpaperPath(String threadId) {
        String threadWallpaperPath = sPrefs.
                getString(PREF_KEY_WALLPAPER_PATH + "_" + threadId, "");
        if (!TextUtils.isEmpty(threadWallpaperPath)) {
            if (threadWallpaperPath.equals("empty")) {
                return null;
            }
            return threadWallpaperPath;
        }
        return null;
    }

    public static void resetConversationCustomization(String threadId) {
        sPrefs.remove(PREF_KEY_WALLPAPER_PATH + "_" + threadId);
    }

    private static String getWallpaperPath() {
        String wallpaperPath = sPrefs.
                getString(PREF_KEY_WALLPAPER_PATH, "");
        if (wallpaperPath != null && !wallpaperPath.equals("")) {
            return wallpaperPath;
        } else {
            return null;
        }
    }

    static void setWallpaperPath(String threadId, String path) {
        if (!TextUtils.isEmpty(threadId)) {
            sPrefs.putString(PREF_KEY_WALLPAPER_PATH + "_" + threadId, path);
        } else {
            sPrefs.putString(PREF_KEY_WALLPAPER_PATH, path);
        }
    }

    static List<WallpaperChooserItem> getWallpaperChooserList() {
        List<WallpaperChooserItem> list = new ArrayList<>();
        WallpaperChooserItem addItem = new WallpaperChooserItem();
        addItem.setItemType(WallpaperChooserItem.TYPE_ADD_PHOTO);
        list.add(addItem);
        WallpaperChooserItem emptyItem = new WallpaperChooserItem();
        emptyItem.setItemType(WallpaperChooserItem.TYPE_EMPTY);
        list.add(emptyItem);

        for (int i = 0; i < WallpaperChooserItem.sThumbnailRes.length; i++) {
            WallpaperChooserItem item = new WallpaperChooserItem();
            item.setItemType(WallpaperChooserItem.TYPE_NORMAL_WALLPAPER);
            item.setIndex(i);
            list.add(item);
        }
        return list;
    }
}
