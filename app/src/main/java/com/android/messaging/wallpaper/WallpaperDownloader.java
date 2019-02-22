package com.android.messaging.wallpaper;

import android.graphics.Bitmap;

import com.android.messaging.util.CommonUtils;
import com.bumptech.glide.Glide;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Threads;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.android.messaging.wallpaper.WallpaperManager.LOCAL_DIRECTORY;

public class WallpaperDownloader {

    interface WallpaperDownloadListener {
        void onDownloadSuccess(String path);

        void onDownloadFailed();
    }

    public static void download(WallpaperDownloadListener listener, String url) {
        Threads.postOnThreadPoolExecutor(() -> {
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(HSApplication.getContext())
                        .asBitmap()
                        .load(url)
                        .submit(1080, 1363)
                        .get();

            } catch (Exception e) {
                listener.onDownloadFailed();
                e.printStackTrace();
            }

            if (bitmap != null) {
                String fileName = getWallpaperPathString(url) + ".png";
                File storedWallpaper = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY), fileName);

                FileOutputStream out;
                try {
                    out = new FileOutputStream(storedWallpaper);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    listener.onDownloadFailed();
                    return;
                }

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                final String storedPath = storedWallpaper.getAbsolutePath();
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onDownloadFailed();
                }
                listener.onDownloadSuccess(storedPath);
            }
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
