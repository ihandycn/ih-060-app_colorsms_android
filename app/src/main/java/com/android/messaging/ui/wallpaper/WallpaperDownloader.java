package com.android.messaging.ui.wallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.messaging.R;
import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.android.messaging.ui.wallpaper.WallpaperManager.LOCAL_DIRECTORY;

public class WallpaperDownloader {
    private static final String TAG = WallpaperDownloader.class.getSimpleName();
    private static final String SOURCE_FILE_SUFFIX = ".png";
    private static final String TOOLBAR_FILE_SUFFIX = ".t";
    private static final String WALLPAPER_FILE_SUFFIX = ".w";

    public interface WallpaperDownloadListener {
        void onDownloadSuccess();

        void onDownloadFailed();
    }

    public static void download(WallpaperDownloadListener listener, String url) {
        Threads.postOnThreadPoolExecutor(() -> {
            final HSHttpConnection connection = new HSHttpConnection(url, HttpRequest.Method.GET);

            String fileName = getWallpaperPathString(url) + SOURCE_FILE_SUFFIX;
            File storedWallpaper = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY), fileName);

            connection.setDownloadFile(storedWallpaper);
            connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
                @Override
                public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                    if (hsHttpConnection.isSucceeded()) {
                        HSLog.d(TAG, "File download success");
                        cutSourceBitmap(url);
                        Threads.postOnMainThread(listener::onDownloadSuccess);
                    } else {
                        HSLog.d(TAG, "File download failed");
                        Threads.postOnMainThread(() -> {
                            listener.onDownloadFailed();
                            Toasts.showToast(R.string.sms_network_error);
                        });
                    }
                }

                @Override
                public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                    HSLog.d(TAG, "File download failed error = " + hsError.getMessage());
                    Threads.postOnMainThread(() -> {
                        listener.onDownloadFailed();
                        Toasts.showToast(R.string.sms_network_error);
                    });
                }
            });
            connection.startSync();
        });
    }

    public static void cutSourceBitmap(String url) {
        String wallpaperPath = getSourceLocalPath(url);
        File customWallpaperFile = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY), getWallpaperPathString(url) + WALLPAPER_FILE_SUFFIX);
        File customToolbarFile = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY), getWallpaperPathString(url) + TOOLBAR_FILE_SUFFIX);
        Bitmap bgBitmap = BitmapFactory.decodeFile(wallpaperPath);

        int height = Dimensions.getPhoneHeight(HSApplication.getContext());
        int width = Dimensions.getPhoneWidth(HSApplication.getContext());

        int bitmapHeight = bgBitmap.getHeight();
        int bitmapWidth = bgBitmap.getWidth();

        int left = 0;
        int top = 0;
        int resizeWidth = bitmapWidth;
        int resizeHeight = bitmapHeight;

        Bitmap resizedBitmap;
        if (height * bitmapWidth == width * bitmapHeight) {
            resizedBitmap = bgBitmap;
        } else {
            if (height * bitmapWidth < width * bitmapHeight) {
                resizeHeight = (int) (bitmapWidth * height * 1.0f / width);
                top = bitmapHeight / 2 - resizeHeight / 2;
            } else {
                resizeWidth = (int) (bitmapHeight * width * 1.0f / height);
                left = bitmapWidth / 2 - resizeWidth / 2;
            }
            resizedBitmap = Bitmap.createBitmap(bgBitmap, left, top, resizeWidth, resizeHeight);
        }

        int cutPointY = (int) (resizeWidth * 1.0f *
                (Dimensions.pxFromDp(56) + Dimensions.getStatusBarHeight(HSApplication.getContext())) / width);

        Bitmap toolbar = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), cutPointY);
        CommonUtils.saveBitmapToFile(toolbar, customToolbarFile);

        cutPointY++;
        Bitmap wallpaper = Bitmap.createBitmap(resizedBitmap, 0, cutPointY, resizedBitmap.getWidth(),
                resizedBitmap.getHeight() - cutPointY);
        CommonUtils.saveBitmapToFile(wallpaper, customWallpaperFile);
    }

    private static String getWallpaperPathString(String url) {
        return Utils.md5("OnlineWallpaper_" + url);
    }

    public static String getSourceLocalPath(String url) {
        return CommonUtils.getDirectory(LOCAL_DIRECTORY) + File.separator + getWallpaperPathString(url) + SOURCE_FILE_SUFFIX;
    }

    public static String getWallpaperLocalPath(String url) {
        return CommonUtils.getDirectory(LOCAL_DIRECTORY) + File.separator + getWallpaperPathString(url) + WALLPAPER_FILE_SUFFIX;
    }

    public static boolean isWallpaperDownloaded(String url) {
        String fileName = getWallpaperPathString(url) + SOURCE_FILE_SUFFIX;
        File storedWallpaper = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY), fileName);
        return storedWallpaper.exists();
    }
}
