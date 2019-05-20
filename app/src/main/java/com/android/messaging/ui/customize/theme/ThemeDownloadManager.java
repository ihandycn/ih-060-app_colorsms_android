package com.android.messaging.ui.customize.theme;

import android.text.TextUtils;

import com.android.messaging.util.CommonUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.superapps.util.Threads;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ThemeDownloadManager {
    public static final int CONVERSATION_LIST_WALLPAPER_WEIGHT = 20;
    public static final int CONVERSATION_WALPAPER_WEIGHT = 20;
    public static final int TOOLBAR_WEIGHT = 15;
    public static final int INCOMING_BUBBLE_WEIGHT = 5;
    public static final int OUTGOING_BUBBLE_WEIGHT = 5;
    public static final int CREATE_ICON_WEIGHT = 5;
    public static final int FONT_WEIGHT = 30;

    interface IThemeDownloadListener {
        void onDownloadSuccess();

        void onDownloadFailed();

        void onDownloadUpdate(int process);
    }

    private static String getBaseRemoteUrl() {
        return HSConfig.getString("Application", "Customize", "Theme", "BasePath");
    }

    public void downloadTheme(ThemeInfo theme, IThemeDownloadListener listener) {
        Threads.postOnThreadPoolExecutor(() -> {
            String baseUrl = getBaseRemoteUrl() + "/" + theme.name + "/";
            List<DownloadItemInfo> downloadItemInfoList = new ArrayList<>();
            String wallpaper;

            if (!TextUtils.isEmpty(wallpaper = theme.wallpaperUrl)) {
                DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
                downloadItemInfo.mRemoteFileName = baseUrl + theme.wallpaperUrl;
                downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                        ThemeManager.THEME_BASE_PATH + theme.name),
                        ThemeManager.WALLPAPER_BG_FILE_NAME);

                downloadItemInfoList.add(downloadItemInfo);
            }

            if (!TextUtils.isEmpty(theme.listWallpaperUrl)) {
                DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
                downloadItemInfo.mRemoteFileName = baseUrl + theme.listWallpaperUrl;
                downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                        ThemeManager.THEME_BASE_PATH + theme.name),
                        ThemeManager.LIST_VIEW_WALLPAPER_BG_FILE_NAME);

                downloadItemInfoList.add(downloadItemInfo);
            }

            if (!TextUtils.isEmpty(theme.toolbarBgUrl)) {
                DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
                downloadItemInfo.mRemoteFileName = baseUrl + theme.toolbarBgUrl;
                downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                        ThemeManager.THEME_BASE_PATH + theme.name),
                        ThemeManager.TOOLBAR_BG_FILE_NAME);

                downloadItemInfoList.add(downloadItemInfo);
            }

            if (!TextUtils.isEmpty(theme.bubbleIncomingUrl)) {
                DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
                downloadItemInfo.mRemoteFileName = baseUrl + theme.bubbleIncomingUrl;
                downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                        ThemeManager.THEME_BASE_PATH + theme.name),
                        ThemeManager.INCOMING_BUBBLE_FILE_NAME);

                downloadItemInfoList.add(downloadItemInfo);
            }

            if (!TextUtils.isEmpty(theme.bubbleOutgoingUrl)) {
                DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
                downloadItemInfo.mRemoteFileName = baseUrl + theme.bubbleOutgoingUrl;
                downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                        ThemeManager.THEME_BASE_PATH + theme.name),
                        ThemeManager.OUTGOING_BUBBLE_FILE_NAME);

                downloadItemInfoList.add(downloadItemInfo);
            }

            if (!TextUtils.isEmpty(theme.avatarUrl)) {
                DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
                downloadItemInfo.mRemoteFileName = baseUrl + theme.avatarUrl;
                downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                        ThemeManager.THEME_BASE_PATH + theme.name),
                        ThemeManager.AVATAR_FILE_NAME);

                downloadItemInfoList.add(downloadItemInfo);
            }

            downloadResources(downloadItemInfoList, listener, 0);
        });
    }

    private void downloadResources(List<DownloadItemInfo> downloadList,
                                   IThemeDownloadListener listener, int startProcess) {
        String remoteUrl = downloadList.get(0).mRemoteFileName;
        File localFile = downloadList.get(0).mLocalFile;
        final HSHttpConnection connection = new HSHttpConnection(remoteUrl, HttpRequest.Method.GET);
        connection.setDownloadFile(localFile);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded()) {
                    downloadList.remove(0);
                    if (downloadList.size() > 0) {
                        downloadResources(downloadList, listener, startProcess);
                    } else {
                        listener.onDownloadSuccess();
                    }
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                listener.onDownloadFailed();
            }
        });

        connection.setDataReceivedListener((hsHttpConnection, bytes, l, l1) -> {

        });
        connection.startSync();
    }

    class DownloadItemInfo {
        String mRemoteFileName;
        File mLocalFile;
        int mStartProcessValue;
        int mEndProcessValue;
    }
}
