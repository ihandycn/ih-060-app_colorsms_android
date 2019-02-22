package com.android.messaging.wallpaper;

import com.android.messaging.util.BuglePrefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WallpaperManager {

    private static final String PREF_KEY_WALLPAPER_PATH = "pref_key_wallpaper_path";
    static final String LOCAL_DIRECTORY = "wallpapers" + File.separator + "local";

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

    static void setWallpaperPath(String path) {
        BuglePrefs.getApplicationPrefs().putString(PREF_KEY_WALLPAPER_PATH, path);
    }

    static void setWallpaperPath(String threadId, String path) {
        BuglePrefs.getApplicationPrefs().putString(PREF_KEY_WALLPAPER_PATH + "_" + threadId, path);
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
