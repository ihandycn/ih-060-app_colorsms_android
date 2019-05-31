package com.android.messaging.ui.customize.theme;

import android.content.res.AssetManager;
import android.text.TextUtils;

import com.android.messaging.font.FontDownloadManager;
import com.android.messaging.font.FontInfo;
import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.superapps.util.Threads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ThemeDownloadManager {
    private static final float CONVERSATION_WALLPAPER_START_RATE = 0;
    private static final float CONVERSATION_LIST_WALLPAPER_START_RATE = 0.2f;
    private static final float TOOLBAR_START_RATE = 0.4f;
    private static final float INCOMING_BUBBLE_START_RATE = 0.45f;
    private static final float OUTGOING_BUBBLE_START_RATE = 0.50f;
    private static final float CREATE_ICON_START_RATE = 0.60f;
    private static final float AVATAR_START_RATE = 0.65f;
    private static final float FONT_START_RATE = 0.70f;
    private static final float TOTAL_RATE = 1f;

    private static ThemeDownloadManager sInstance = new ThemeDownloadManager();

    public static ThemeDownloadManager getInstance() {
        return sInstance;
    }

    private ThemeDownloadManager() {

    }

    interface IThemeDownloadListener {
        void onDownloadSuccess();

        void onDownloadFailed();

        void onDownloadUpdate(float rate);
    }

    public interface IThemeMoveListener {
        void onMoveSuccess();

        void onMoveFailed();
    }

    boolean isThemeDownloaded(ThemeInfo theme) {
        if (!TextUtils.isEmpty(theme.wallpaperUrl)) {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + theme.mThemeKey),
                    ThemeManager.WALLPAPER_BG_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
        }

        if (!TextUtils.isEmpty(theme.listWallpaperUrl)) {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + theme.mThemeKey),
                    ThemeManager.LIST_VIEW_WALLPAPER_BG_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
        }

        if (!TextUtils.isEmpty(theme.toolbarBgUrl)) {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + theme.mThemeKey),
                    ThemeManager.TOOLBAR_BG_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
        }

        if (!TextUtils.isEmpty(theme.bubbleIncomingUrl)) {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + theme.mThemeKey),
                    ThemeManager.INCOMING_BUBBLE_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
        }

        if (!TextUtils.isEmpty(theme.bubbleOutgoingUrl)) {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + theme.mThemeKey),
                    ThemeManager.OUTGOING_BUBBLE_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
        }
        if (!TextUtils.isEmpty(theme.newConversationIconUrl)) {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + theme.mThemeKey),
                    ThemeManager.CREATE_ICON_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
        }

        if (!TextUtils.isEmpty(theme.avatarUrl)) {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + theme.mThemeKey),
                    ThemeManager.AVATAR_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }

    static String getBaseRemoteUrl() {
        return HSConfig.getString("Application", "Themes", "BasePath");
    }

    void downloadTheme(ThemeInfo theme, IThemeDownloadListener listener) {
        String folderName = theme.mThemeKey;

        String baseUrl = getBaseRemoteUrl() + folderName + "/";
        List<DownloadItemInfo> downloadItemInfoList = new ArrayList<>();
        FontInfo fontInfo = FontDownloadManager.getFont(theme.fontName);
        final boolean[] isFontDownload = {fontInfo == null || FontDownloadManager.isFontDownloaded(fontInfo)};

        if (!TextUtils.isEmpty(theme.wallpaperUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = baseUrl + theme.wallpaperUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.WALLPAPER_BG_FILE_NAME);
            downloadItemInfo.mStartProcessValue = CONVERSATION_WALLPAPER_START_RATE;
            downloadItemInfo.mEndProcessValue = CONVERSATION_LIST_WALLPAPER_START_RATE;

            downloadItemInfoList.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.listWallpaperUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = baseUrl + theme.listWallpaperUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.LIST_VIEW_WALLPAPER_BG_FILE_NAME);
            downloadItemInfo.mStartProcessValue = CONVERSATION_LIST_WALLPAPER_START_RATE;
            downloadItemInfo.mEndProcessValue = TOOLBAR_START_RATE;

            downloadItemInfoList.add(downloadItemInfo);
        }

        //toolbar
        if (!TextUtils.isEmpty(theme.toolbarBgUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = baseUrl + theme.toolbarBgUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.TOOLBAR_BG_FILE_NAME);
            downloadItemInfo.mStartProcessValue = TOOLBAR_START_RATE;
            downloadItemInfo.mEndProcessValue = INCOMING_BUBBLE_START_RATE;

            downloadItemInfoList.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.bubbleIncomingUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = baseUrl + theme.bubbleIncomingUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.INCOMING_BUBBLE_FILE_NAME);
            downloadItemInfo.mStartProcessValue = INCOMING_BUBBLE_START_RATE;
            downloadItemInfo.mEndProcessValue = OUTGOING_BUBBLE_START_RATE;

            downloadItemInfoList.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.bubbleOutgoingUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = baseUrl + theme.bubbleOutgoingUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.OUTGOING_BUBBLE_FILE_NAME);
            downloadItemInfo.mStartProcessValue = OUTGOING_BUBBLE_START_RATE;
            downloadItemInfo.mEndProcessValue = CREATE_ICON_START_RATE;

            downloadItemInfoList.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.newConversationIconUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = baseUrl + theme.newConversationIconUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.CREATE_ICON_FILE_NAME);
            downloadItemInfo.mStartProcessValue = CREATE_ICON_START_RATE;
            downloadItemInfo.mEndProcessValue = AVATAR_START_RATE;

            downloadItemInfoList.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.avatarUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = baseUrl + theme.avatarUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.AVATAR_FILE_NAME);
            downloadItemInfo.mStartProcessValue = AVATAR_START_RATE;
            if (isFontDownload[0]) {
                downloadItemInfo.mEndProcessValue = FONT_START_RATE;
            } else {
                downloadItemInfo.mEndProcessValue = TOOLBAR_START_RATE;
            }

            downloadItemInfoList.add(downloadItemInfo);
        }

        final boolean[] isFontDownloadSuccess = {isFontDownload[0]};
        final boolean[] isThemeDownloadSuccess = {false};
        // 0-1
        final float[] themeRate = {0};
        final float[] fontRate = {0};

        if (isFontDownload[0]) {
            fontRate[0] = TOTAL_RATE - FONT_START_RATE;
        }

        if (!isFontDownload[0]) {
            FontDownloadManager.downloadFont(fontInfo, new FontDownloadManager.FontDownloadListener() {
                @Override
                public void onDownloadSuccess() {
                    isFontDownloadSuccess[0] = true;
                    fontRate[0] = TOTAL_RATE - FONT_START_RATE;
                    if (isThemeDownloadSuccess[0]) {
                        listener.onDownloadSuccess();
                    } else {
                        listener.onDownloadUpdate(themeRate[0] + fontRate[0]);
                    }
                }

                @Override
                public void onDownloadFailed() {
                    //listener.onDownloadFailed();
                }

                @Override
                public void onDownloadUpdate(float rate) {
                    if (fontRate[0] < rate * (TOTAL_RATE - FONT_START_RATE)) {
                        fontRate[0] = rate * (TOTAL_RATE - FONT_START_RATE);
                        listener.onDownloadUpdate(themeRate[0] + fontRate[0]);
                    }
                }
            });
        }

        IThemeDownloadListener downloadListener = new IThemeDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                isThemeDownloadSuccess[0] = true;
                themeRate[0] = FONT_START_RATE;
                if (isFontDownloadSuccess[0]) {
                    listener.onDownloadSuccess();
                } else {
                    listener.onDownloadUpdate(FONT_START_RATE + fontRate[0]);
                }
            }

            @Override
            public void onDownloadFailed() {
                listener.onDownloadFailed();
            }

            @Override
            public void onDownloadUpdate(float rate) {
                if (themeRate[0] < rate) {
                    themeRate[0] = rate;
                    if (isFontDownload[0]) {
                        listener.onDownloadUpdate(rate * TOTAL_RATE / FONT_START_RATE);
                    } else {
                        listener.onDownloadUpdate(rate + fontRate[0]);
                    }
                }
            }
        };
        downloadResources(downloadItemInfoList, downloadListener);
    }

    private void downloadResources(List<DownloadItemInfo> downloadList,
                                   IThemeDownloadListener listener) {
        Threads.postOnThreadPoolExecutor(() -> {
            String remoteUrl = downloadList.get(0).mRemoteFileName;
            File localFile = downloadList.get(0).mLocalFile;
            float startRate = downloadList.get(0).mStartProcessValue;
            float endRate = downloadList.get(0).mEndProcessValue;
            final HSHttpConnection connection = new HSHttpConnection(remoteUrl, HttpRequest.Method.GET);
            connection.setDownloadFile(localFile);
            connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
                @Override
                public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                    if (hsHttpConnection.isSucceeded()) {
                        downloadList.remove(0);
                        if (downloadList.size() > 0) {
                            downloadResources(downloadList, listener);
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
                float percent = l * 1.0f / l1;
                float rate = startRate + percent * (endRate - startRate);
                listener.onDownloadUpdate(rate);
            });
            connection.startSync();
        });
    }

    public void copyFileFromAssetsAsync(ThemeInfo theme, IThemeMoveListener listener) {
        Threads.postOnThreadPoolExecutor(() -> copyFileFromAssetsSync(theme, listener));
    }

    public void copyFileFromAssetsSync(ThemeInfo theme, IThemeMoveListener listener) {
        String folderName = theme.mThemeKey;
        List<DownloadItemInfo> copyTask = new ArrayList<>();

        if (!TextUtils.isEmpty(theme.wallpaperUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = theme.wallpaperUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.WALLPAPER_BG_FILE_NAME);
            copyTask.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.listWallpaperUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = theme.listWallpaperUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.LIST_VIEW_WALLPAPER_BG_FILE_NAME);
            copyTask.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.toolbarBgUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = theme.toolbarBgUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.TOOLBAR_BG_FILE_NAME);
            copyTask.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.bubbleIncomingUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = theme.bubbleIncomingUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.INCOMING_BUBBLE_FILE_NAME);
            copyTask.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.bubbleOutgoingUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = theme.bubbleOutgoingUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.OUTGOING_BUBBLE_FILE_NAME);
            copyTask.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.newConversationIconUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = theme.newConversationIconUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.CREATE_ICON_FILE_NAME);
            copyTask.add(downloadItemInfo);
        }

        if (!TextUtils.isEmpty(theme.avatarUrl)) {
            DownloadItemInfo downloadItemInfo = new DownloadItemInfo();
            downloadItemInfo.mRemoteFileName = theme.avatarUrl;
            downloadItemInfo.mLocalFile = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + folderName),
                    ThemeManager.AVATAR_FILE_NAME);
            copyTask.add(downloadItemInfo);
        }

        AssetManager assetManager = HSApplication.getContext().getAssets();

        File folder = new File(CommonUtils.getDirectory("theme"), folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }


        for (DownloadItemInfo info : copyTask) {
            try {
                InputStream in = assetManager.open("themes/" + theme.mThemeKey + "/" + info.mRemoteFileName);
                FileOutputStream out = new FileOutputStream(info.mLocalFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                listener.onMoveFailed();
            }
        }
        listener.onMoveSuccess();
    }

    class DownloadItemInfo {
        String mRemoteFileName;
        File mLocalFile;
        float mStartProcessValue;
        float mEndProcessValue;
    }
}
