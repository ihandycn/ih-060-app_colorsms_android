package com.android.messaging.wallpaper;

import com.android.messaging.R;
import com.android.messaging.util.CommonUtils;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;

import static com.android.messaging.wallpaper.WallpaperManager.LOCAL_DIRECTORY;

public class WallpaperDownloader {
    private static final String TAG = WallpaperDownloader.class.getSimpleName();

    interface WallpaperDownloadListener {
        void onDownloadSuccess(String path);

        void onDownloadFailed();
    }

    public static void download(WallpaperDownloadListener listener, String url) {
        Threads.postOnThreadPoolExecutor(() -> {
            final HSHttpConnection connection = new HSHttpConnection(url, HttpRequest.Method.GET);

            String fileName = getWallpaperPathString(url) + ".png";
            File storedWallpaper = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY), fileName);

            connection.setDownloadFile(storedWallpaper);
            connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
                @Override
                public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                    if (hsHttpConnection.isSucceeded()) {
                        HSLog.d(TAG, "File download success");
                        final String storedPath = storedWallpaper.getAbsolutePath();
                        Threads.postOnMainThread(() -> listener.onDownloadSuccess(storedPath));
                    } else {
                        HSLog.d(TAG, "File download failed");
                        Threads.postOnMainThread(() -> {
                            listener.onDownloadFailed();
                            Toasts.showToast(R.string.wallpaper_download_failed);
                        });
                    }
                }

                @Override
                public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                    HSLog.d(TAG, "File download failed error = " + hsError.getMessage());
                    Threads.postOnMainThread(() -> {
                        listener.onDownloadFailed();
                        Toasts.showToast(R.string.wallpaper_download_failed);
                    });
                }
            });
            connection.startSync();
        });
    }

    private static String getWallpaperPathString(String url) {
        return Utils.md5("OnlineWallpaper_" + url);
    }

    static String getWallPaperLocalPath(String url) {
        return CommonUtils.getDirectory(LOCAL_DIRECTORY) + File.separator + getWallpaperPathString(url) + ".png";
    }

    public static String getAbsolutePath(String url) {
        return new File(CommonUtils.getDirectory(LOCAL_DIRECTORY), getWallpaperPathString(url) + ".png").getAbsolutePath();
    }

    static boolean isWallpaperDownloaded(String url) {
        String fileName = getWallpaperPathString(url) + ".png";
        File storedWallpaper = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY), fileName);
        return storedWallpaper.exists();
    }
}
