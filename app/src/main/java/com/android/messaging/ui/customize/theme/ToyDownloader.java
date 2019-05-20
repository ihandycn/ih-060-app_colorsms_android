package com.android.messaging.ui.customize.theme;

import android.support.annotation.IntDef;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.io.File;
import java.io.IOException;

public class ToyDownloader {

//    private static final String PREF_KEY_DOWNLOAD_SUCCESS = "pref_key_download_success";
//
//    private static MediaDownloadManager sDownloadManager = new MediaDownloadManager();
//    private static boolean isResPackage1Downloading = false;
//    private static boolean isResPackage2Downloading = false;
//
//    public static final int LUCKY_TOY_RESOURCE_PACKAGE_FIRST = 1;
//    public static final int LUCKY_TOY_RESOURCE_PACKAGE_SECOND = 2;
//    public static final int LUCKY_TOY_RESOURCE_PACKAGE_ALL = 3;
//
//    @IntDef({LUCKY_TOY_RESOURCE_PACKAGE_FIRST, LUCKY_TOY_RESOURCE_PACKAGE_SECOND, LUCKY_TOY_RESOURCE_PACKAGE_ALL})
//    @interface LuckyResPackageIndex {
//    }
//
//    public static boolean isToyDownloadSuccess(@LuckyResPackageIndex int packageIndex) {
//        return Preferences.get(LauncherFiles.LUCKY_PREFS).getBoolean(
//                PREF_KEY_DOWNLOAD_SUCCESS + FileUtils.md5(getToyResourceUrl(LUCKY_TOY_RESOURCE_PACKAGE_ALL)), false)
//                || Preferences.get(LauncherFiles.LUCKY_PREFS).getBoolean(
//                PREF_KEY_DOWNLOAD_SUCCESS + FileUtils.md5(getToyResourceUrl(packageIndex)), false);
//    }
//
//    public static void cacheToyResourceIfNeeded() {
//        if (ToyPreference.getCollectedToyCount() >= 1) {
//            cacheToyResourceIfNeeded(LUCKY_TOY_RESOURCE_PACKAGE_FIRST);
//        }
//
//        if (ToyPreference.getCollectedToyCount() >= 10) {
//            cacheToyResourceIfNeeded(LUCKY_TOY_RESOURCE_PACKAGE_SECOND);
//        }
//    }
//
//    public static void cacheToyResourceIfNeeded(@LuckyResPackageIndex int packageIndex) {
//        if (isToyDownloadSuccess(packageIndex)) {
//            HSLog.d("ToyDownloader", "package " + packageIndex + " already cached, no need to download");
//            return;
//        }
//        if ((packageIndex == LUCKY_TOY_RESOURCE_PACKAGE_FIRST && isResPackage1Downloading)
//                || (packageIndex == LUCKY_TOY_RESOURCE_PACKAGE_SECOND && isResPackage2Downloading)) {
//            return;
//        }
//
//        HSLog.d("ToyDownloader", "lucky resource package " + packageIndex + " start download");
//        HSLog.d("ToyDownloader", "package url" + getToyResourceUrl(packageIndex));
//        sDownloadManager.downloadMedia(getToyResourceUrl(packageIndex), new MediaDownloadManager.DownloadCallback() {
//            @Override
//            public void onUpdate(long progress) {
//                HSLog.d("ToyDownloader", "download progress : " + progress);
//            }
//
//            @Override
//            public void onFail(MediaDownloadManager.MediaDownLoadTask task, String msg) {
//                HSLog.d("ToyDownloader", "download toy package " + packageIndex + " failed! " + msg);
//                setDownloadState(packageIndex, false);
//            }
//
//            @Override
//            public void onSuccess(MediaDownloadManager.MediaDownLoadTask task) {
//                HSLog.d("ToyDownloader", "download toy package " + packageIndex + " success!");
//                unzipToyResource(packageIndex);
//                setDownloadState(packageIndex, false);
//            }
//
//            @Override
//            public void onCancel() {
//                setDownloadState(packageIndex, false);
//            }
//        });
//        setDownloadState(packageIndex, true);
//    }
//
//    private static void setDownloadState(@LuckyResPackageIndex int packageIndex, boolean isDownloading) {
//        if (packageIndex == LUCKY_TOY_RESOURCE_PACKAGE_FIRST) {
//            isResPackage1Downloading = isDownloading;
//        }
//        if (packageIndex == LUCKY_TOY_RESOURCE_PACKAGE_SECOND) {
//            isResPackage2Downloading = isDownloading;
//        }
//    }
//
//    private static String getToyResourceUrl(@LuckyResPackageIndex int packageIndex) {
//        if (packageIndex == LUCKY_TOY_RESOURCE_PACKAGE_FIRST) {
//            return HSConfig.optString("", "Application", "GameCenter", "Lucky", "ToyResourceUrl_36_package_1");
//            //return "http://cdn.appcloudbox.net/launcherapps/apps/launcher/lucky/luckyresouce1.zip";
//        } else if (packageIndex == LUCKY_TOY_RESOURCE_PACKAGE_SECOND) {
//            return HSConfig.optString("", "Application", "GameCenter", "Lucky", "ToyResourceUrl_36_package_2");
//            //return "http://cdn.appcloudbox.net/launcherapps/apps/launcher/lucky/luckyresouce2.zip";
//        } else {
//            return HSConfig.optString("", "Application", "GameCenter", "Lucky", "ToyResourceUrl_36");
//        }
//    }
//
//    private static void unzipToyResource(@LuckyResPackageIndex int packageIndex) {
//        File media = new File(FileUtils.getMediaDirectory(),
//                FileUtils.md5(getToyResourceUrl(packageIndex)) + "." + FileUtils.getRemoteFileExtension(getToyResourceUrl(packageIndex)));
//        Threads.postOnThreadPoolExecutor(() -> {
//            try {
//                ZipUtils.upZipFile(media.getPath(), getExtractedPath());
//
//                File themeDirectory = new File(getExtractedPath());
//                File[] files = themeDirectory.listFiles();
//                if (files != null) {
//                    for (File fileItem : files) {
//                        HSLog.d("ToyDownloader", "unzip success list all files : file name is " + fileItem.getPath());
//                    }
//                }
//
//                Preferences.get(LauncherFiles.LUCKY_PREFS).putBoolean(
//                        PREF_KEY_DOWNLOAD_SUCCESS + FileUtils.md5(getToyResourceUrl(packageIndex)), true);
//            } catch (IOException e) {
//                e.printStackTrace();
//                File file = new File(getExtractedPath());
//                com.acb.call.utils.FileUtils.recursiveRemove(file);
//            }
//        });
//    }
//
//    private static String getExtractedPath() {
//        return HSApplication.getContext().getFilesDir() + "/lucky";
//    }
}
