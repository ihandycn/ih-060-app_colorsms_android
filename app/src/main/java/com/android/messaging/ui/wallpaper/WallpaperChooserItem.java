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
            R.drawable.wallpaper_thumbnail_winter,
            R.drawable.wallpaper_thumbnail_001,
            R.drawable.wallpaper_thumbnail_002,
            R.drawable.wallpaper_thumbnail_003,
            R.drawable.wallpaper_thumbnail_004,
            R.drawable.wallpaper_thumbnail_005,
            R.drawable.wallpaper_thumbnail_006,
            R.drawable.wallpaper_thumbnail_007,
            R.drawable.wallpaper_thumbnail_008,
            R.drawable.wallpaper_thumbnail_009,
            R.drawable.wallpaper_thumbnail_010,
            R.drawable.wallpaper_thumbnail_011,
            R.drawable.wallpaper_thumbnail_012,
            R.drawable.wallpaper_thumbnail_013,
            R.drawable.wallpaper_thumbnail_014,
            R.drawable.wallpaper_thumbnail_015,
            R.drawable.wallpaper_thumbnail_016,
            R.drawable.wallpaper_thumbnail_017,
            R.drawable.wallpaper_thumbnail_018,
            R.drawable.wallpaper_thumbnail_019,
            R.drawable.wallpaper_thumbnail_020,
            R.drawable.wallpaper_thumbnail_021
    };

    private static final String sBaseUrl = "http://cdn.appcloudbox.net/smoothappsstudio/apps/bubble/chatbackground/";

    public static final String[] sRemoteUrl = {
            sBaseUrl + "wallpaper_abstract.jpg",
            sBaseUrl + "wallpaper_butterfly1.jpg",
            sBaseUrl + "wallpaper_butterfly2.jpg",
            sBaseUrl + "wallpaper_chocolate.jpg",
            sBaseUrl + "wallpaper_dandelion1.jpg",
            sBaseUrl + "wallpaper_dandeline2.jpg",
            sBaseUrl + "wallpaper_dandelion3.jpg",
            sBaseUrl + "wallpaper_flower.jpg",
            sBaseUrl + "wallpaper_forst.jpg",
            sBaseUrl + "wallpaper_gates_wood.jpg",
            sBaseUrl + "wallpaper_iceland.jpg",
            sBaseUrl + "wallpaper_sea.jpg",
            sBaseUrl + "wallpaper_winter.jpg",
            sBaseUrl + "wallpaper_001.jpg",
            sBaseUrl + "wallpaper_002.jpg",
            sBaseUrl + "wallpaper_003.jpg",
            sBaseUrl + "wallpaper_004.jpg",
            sBaseUrl + "wallpaper_005.jpg",
            sBaseUrl + "wallpaper_006.jpg",
            sBaseUrl + "wallpaper_007.jpg",
            sBaseUrl + "wallpaper_008.jpg",
            sBaseUrl + "wallpaper_009.jpg",
            sBaseUrl + "wallpaper_010.jpg",
            sBaseUrl + "wallpaper_011.jpg",
            sBaseUrl + "wallpaper_012.jpg",
            sBaseUrl + "wallpaper_013.jpg",
            sBaseUrl + "wallpaper_014.jpg",
            sBaseUrl + "wallpaper_015.jpg",
            sBaseUrl + "wallpaper_016.jpg",
            sBaseUrl + "wallpaper_017.jpg",
            sBaseUrl + "wallpaper_018.jpg",
            sBaseUrl + "wallpaper_019.jpg",
            sBaseUrl + "wallpaper_020.jpg",
            sBaseUrl + "wallpaper_021.jpg"
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
