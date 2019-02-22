package com.android.messaging.wallpaper;

import com.android.messaging.R;

public class WallpaperChooserItem {
    static final int TYPE_ADD_PHOTO = 0;
    static final int TYPE_EMPTY = 1;
    static final int TYPE_NORMAL_WALLPAPER = 2;

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
            R.drawable.wallpaper_thumbnail_neon,
            R.drawable.wallpaper_thumbnail_sea,
            R.drawable.wallpaper_thumbnail_winter
    };

    private static final String[] sRemoteUrl = {
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_abstract.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_butterfly1.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_butterfly2.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_chocolate.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_dandelion1.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_dandeline2.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_dandelion3.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_flower.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_forst.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_gates_wood.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_iceland.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_neon.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_sea.jpg",
            "https://s3.amazonaws.com/superapps-dev/ColorSMS/Chat%20Background/wallpaper_winter.jpg"
    };

    public void setIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    int getItemType() {
        return mItemType;
    }

    void setItemType(int mItemType) {
        this.mItemType = mItemType;
    }

    int getThumbnailResId() {
        return sThumbnailRes[mIndex];
    }

    String getLocalPath() {
        return WallpaperDownloader.getWallPaperLocalPath(sRemoteUrl[mIndex]);
    }

    public String getAbsolutePath() {
        return WallpaperDownloader.getAbsolutePath(sRemoteUrl[mIndex]);
    }

    String getRemoteUrl() {
        return sRemoteUrl[mIndex];
    }

    public boolean isDownloaded() {
        return WallpaperDownloader.isWallpaperDownloaded(sRemoteUrl[mIndex]);
    }
}
