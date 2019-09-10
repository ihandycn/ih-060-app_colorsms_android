package com.android.messaging.ui.wallpaper;

public class WallpaperChooserItem {
    public static final int TYPE_ADD_PHOTO = 0;
    public static final int TYPE_EMPTY = 1;
    public static final int TYPE_NORMAL_WALLPAPER = 2;

    private int mItemType = TYPE_NORMAL_WALLPAPER;
    private String mThumbUrl;
    private String mSourceUrl;
    private boolean mIsWallpaperDownloading = false;
    private boolean mIsItemSelected = false;
    private boolean mIsItemPreSelected = false;
    private WallpaperChooserItemView mItemView;
    private WallpaperDownloader.WallpaperDownloadListener mDownloadListener;

    public void downloadWallpaper() {
        if (mIsWallpaperDownloading) {
            return;
        }
        mIsWallpaperDownloading = true;
        if (mItemView != null) {
            mItemView.onDownloadStart();
        }
        WallpaperDownloader.download(new WallpaperDownloader.WallpaperDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                if (mItemView != null) {
                    if (mIsItemPreSelected) {
                        mItemView.onDownloadSuccessAndChecked();
                    } else {
                        mItemView.onDownloadFinish();
                    }
                }
                mIsItemSelected = mIsItemPreSelected;
                mIsWallpaperDownloading = false;
                mIsItemPreSelected = false;
                if (mDownloadListener != null) {
                    mDownloadListener.onDownloadSuccess();
                }
            }

            @Override
            public void onDownloadFailed() {
                if (mItemView != null) {
                    mItemView.onDownloadFinish();
                }
                mIsWallpaperDownloading = false;
                mIsItemSelected = false;
                mIsItemPreSelected = false;
                if (mDownloadListener != null) {
                    mDownloadListener.onDownloadFailed();
                }
            }
        }, getRemoteUrl());
    }

    public void bindView(WallpaperChooserItemView view) {
        mItemView = view;
        if (view != null) {
            view.initViewByItemState(this);
        }
    }

    public boolean isItemDownloading() {
        return mIsWallpaperDownloading;
    }

    public void setSelectedState(boolean isSelected) {
        if (isSelected == mIsItemSelected) {
            return;
        }
        mIsItemSelected = isSelected;
        if (mItemView != null) {
            if (isSelected) {
                mItemView.setCheckedState();
            } else {
                mItemView.setUncheckedState();
            }
        }
    }

    public void setDownloadListener(WallpaperDownloader.WallpaperDownloadListener listener){
        mDownloadListener = listener;
    }

    public void setPreSelectState(boolean isSelected) {
        mIsItemPreSelected = isSelected;
    }

    public boolean isItemChecked() {
        return mIsItemSelected;
    }

    public void setThumbUrl(String thumbUrl) {
        this.mThumbUrl = thumbUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.mSourceUrl = sourceUrl;
    }

    public int getItemType() {
        return mItemType;
    }

    void setItemType(int mItemType) {
        this.mItemType = mItemType;
    }

    String getThumbnailUrl() {
        return mThumbUrl;
    }

    public String getSourceLocalPath() {
        return WallpaperDownloader.getSourceLocalPath(mSourceUrl);
    }

    public String getWallpaperLocalPath() {
        return WallpaperDownloader.getWallpaperLocalPath(mSourceUrl);
    }

    public String getRemoteUrl() {
        return mSourceUrl;
    }

    public boolean isDownloaded() {
        return WallpaperDownloader.isWallpaperDownloaded(mSourceUrl);
    }
}
