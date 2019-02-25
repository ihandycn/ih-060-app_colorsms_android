package com.android.messaging.wallpaper;

import android.text.TextUtils;

import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.TextUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WallpaperManager {

    private static final String PREF_KEY_WALLPAPER_PATH = "pref_key_wallpaper_path";
    static final String LOCAL_DIRECTORY = "wallpapers" + File.separator + "local";
    private static List<WallpaperChangeListener> sWallpaperChangeListeners;

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

    public static String getWallpaperPathByThreadId(String threadId) {
        String threadWallpaperPath = BuglePrefs.getApplicationPrefs().
                getString(PREF_KEY_WALLPAPER_PATH + "_" + threadId, "");
        if (threadWallpaperPath != null && !threadWallpaperPath.equals("")) {
            return threadWallpaperPath;
        } else {
            return getWallpaperPath();
        }
    }

    private static String getWallpaperPath() {
        String wallpaperPath = BuglePrefs.getApplicationPrefs().
                getString(PREF_KEY_WALLPAPER_PATH, "");
        if (wallpaperPath != null && !wallpaperPath.equals("")) {
            return wallpaperPath;
        } else {
            return null;
        }
    }
    
    static void setWallpaperPath(String threadId, String path) {
        if (!TextUtils.isEmpty(threadId)) {
            BuglePrefs.getApplicationPrefs().putString(PREF_KEY_WALLPAPER_PATH + "_" + threadId, path);
        } else {
            BuglePrefs.getApplicationPrefs().putString(PREF_KEY_WALLPAPER_PATH, path);
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
