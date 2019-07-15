package com.android.messaging.ui.wallpaper;

import com.android.messaging.R;

public class WallpaperChooserItem {
    public static final int TYPE_ADD_PHOTO = 0;
    public static final int TYPE_EMPTY = 1;
    public static final int TYPE_NORMAL_WALLPAPER = 2;

    private int mItemType = TYPE_NORMAL_WALLPAPER;

    private int mIndex;

    public static final int[] sThumbnailRes = {
            R.drawable.wallpaper_thumbnail_abstract,
            R.drawable.wallpaper_thumbnail_butterfly1,
            R.drawable.wallpaper_thumbnail_butterfly2,
            R.drawable.wallpaper_thumbnail_chocolate,
            R.drawable.wallpaper_thumbnail_dandelion1,
            R.drawable.wallpaper_thumbnail_dandelion2,
            R.drawable.wallpaper_thumbnail_dandelion3,
            R.drawable.wallpaper_thumbnail_flower,
            R.drawable.wallpaper_thumbnail_frost,
            R.drawable.wallpaper_thumbnail_gates_wood,
            R.drawable.wallpaper_thumbnail_iceland,
            R.drawable.wallpaper_thumbnail_sea,
            R.drawable.wallpaper_thumbnail_winter
    };

    public static final String[] sRemoteUrl = {
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_abstract.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_butterfly1.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_butterfly2.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_chocolate.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_dandelion1.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_dandeline2.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_dandelion3.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_flower.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_forst.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_gates_wood.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_iceland.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_sea.jpg",
            "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/wallpaper_winter.jpg"
    };

    public void setIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    public int getItemType() {
        return mItemType;
    }

    void setItemType(int mItemType) {
        this.mItemType = mItemType;
    }

    int getThumbnailResId() {
        return sThumbnailRes[mIndex];
    }

    public String getLocalPath() {
        return WallpaperDownloader.getWallPaperLocalPath(sRemoteUrl[mIndex]);
    }

    public String getAbsolutePath() {
        return WallpaperDownloader.getAbsolutePath(sRemoteUrl[mIndex]);
    }

    public String getRemoteUrl() {
        return sRemoteUrl[mIndex];
    }

    public boolean isDownloaded() {
        return WallpaperDownloader.isWallpaperDownloaded(sRemoteUrl[mIndex]);
    }
}
