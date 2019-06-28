package com.android.messaging.ui.customize.theme;

import android.text.TextUtils;

import com.android.messaging.util.CommonUtils;
import com.ihs.commons.config.HSConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ThemeInfo {
    private static final Map<String, ThemeInfo> sDownloadingThemeList = new ConcurrentHashMap<>();

    public String mThemeKey;
    public boolean mIsLocalTheme;
    public String name;
    public String previewUrl;
    public String themeColor;
    public String toolbarBgUrl;
    public String avatarUrl;
    public String mSolidAvatarUrl;
    public String avatarForegroundColor;
    public String listTitleColor;
    public String listSubtitleColor;
    public String listTimeColor;
    public String newConversationIconUrl;
    public String wallpaperUrl;
    public String listWallpaperUrl;
    public String bubbleIncomingUrl;
    public String bubbleOutgoingUrl;
    public String mSolidBubbleIncomingUrl;
    public String mSolidBubbleOutgoingUrl;
    public String incomingBubbleBgColor;
    public String outgoingBubbleBgColor;
    public String incomingBubbleTextColor;
    public String outgoingBubbleTextColor;
    public String bubbleAdColor;
    public String fontName;
    public String bannerAdBgColor;
    public String bannerAdActionColor;
    public String bannerAdActionTextColor;
    public int mThemeDownloadTimes;
    public List<String> mPreviewList;

    private boolean mIsDownloading;

    private List<ThemeDownloadManager.IThemeDownloadListener> mDownloadListeners = new ArrayList<>();

    public static ThemeInfo ofConfig(String name, Map<String, ?> themeConfig) {
        ThemeInfo themeInfo = new ThemeInfo();

        themeInfo.mThemeKey = name;
        themeInfo.name = (String) themeConfig.get("Name");
        themeInfo.mIsLocalTheme = (Boolean) themeConfig.get("IsLocalTheme");
        themeInfo.previewUrl = (String) themeConfig.get("PreviewUrl");
        themeInfo.themeColor = (String) themeConfig.get("ThemeColor");
        themeInfo.avatarForegroundColor = (String) themeConfig.get("AvatarForegroundColor");
        themeInfo.toolbarBgUrl = (String) themeConfig.get("ToolbarBgUrl");
        themeInfo.avatarUrl = (String) themeConfig.get("AvatarUrl");
        themeInfo.listTitleColor = (String) themeConfig.get("ListTitleColor");
        themeInfo.listSubtitleColor = (String) themeConfig.get("ListSubtitleColor");
        themeInfo.listTimeColor = (String) themeConfig.get("ListTimeColor");
        themeInfo.newConversationIconUrl = (String) themeConfig.get("NewConversationIconUrl");
        themeInfo.wallpaperUrl = (String) themeConfig.get("WallpaperUrl");
        themeInfo.listWallpaperUrl = (String) themeConfig.get("ListWallpaperUrl");
        themeInfo.bubbleIncomingUrl = (String) themeConfig.get("BubbleIncomingUrl");
        themeInfo.bubbleOutgoingUrl = (String) themeConfig.get("BubbleOutgoingUrl");
        themeInfo.incomingBubbleBgColor = (String) themeConfig.get("IncomingBubbleBgColor");
        themeInfo.outgoingBubbleBgColor = (String) themeConfig.get("OutgoingBubbleBgColor");
        themeInfo.incomingBubbleTextColor = (String) themeConfig.get("IncomingBubbleTextColor");
        themeInfo.outgoingBubbleTextColor = (String) themeConfig.get("OutgoingBubbleTextColor");
        themeInfo.bubbleAdColor = (String) themeConfig.get("BubbleAdColor");
        themeInfo.fontName = (String) themeConfig.get("FontName");
        themeInfo.bannerAdBgColor = (String) themeConfig.get("BannerAdBgColor");
        themeInfo.bannerAdActionColor = (String) themeConfig.get("BannerAdActionColor");
        themeInfo.bannerAdActionTextColor = (String) themeConfig.get("BannerAdActionTextColor");
        themeInfo.mThemeDownloadTimes = (Integer) themeConfig.get("Hot");
        if (themeConfig.containsKey("BubbleIncomingSolidUrl")) {
            themeInfo.mSolidBubbleIncomingUrl = (String) themeConfig.get("BubbleIncomingSolidUrl");
        }
        if (themeConfig.containsKey("BubbleOutgoingSolidUrl")) {
            themeInfo.mSolidBubbleOutgoingUrl = (String) themeConfig.get("BubbleOutgoingSolidUrl");
        }
        if (themeConfig.containsKey("AvatarSolidUrl")) {
            themeInfo.mSolidAvatarUrl = (String) themeConfig.get("AvatarSolidUrl");
        }
        themeInfo.mPreviewList = (List<String>) HSConfig.getList("Application", "Themes", "ThemeList", name, "PreviewList");

        return themeInfo;
    }

    public boolean isDownloaded() {
        if (mIsLocalTheme) {
            return true;
        }
        return isAllFileInLocalFolder();
    }

    public boolean isNecessaryFilesInLocalFolder() {
        return ThemeDownloadManager.getInstance().isThemeDownloaded(this);
    }

    public boolean isAllFileInLocalFolder() {
        if (!isNecessaryFilesInLocalFolder()) {
            return false;
        }

        if (!TextUtils.isEmpty(mSolidBubbleIncomingUrl)) {
            File file = new File(CommonUtils.getDirectory(
                    ThemeBubbleDrawables.THEME_BASE_PATH + mThemeKey),
                    ThemeBubbleDrawables.INCOMING_SOLID_BUBBLE_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
        }

        if (!TextUtils.isEmpty(mSolidBubbleOutgoingUrl)) {
            File file = new File(CommonUtils.getDirectory(
                    ThemeBubbleDrawables.THEME_BASE_PATH + mThemeKey),
                    ThemeBubbleDrawables.OUTGOING_SOLID_BUBBLE_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
        }

        if (!TextUtils.isEmpty(mSolidAvatarUrl)) {
            File file = new File(CommonUtils.getDirectory(
                    ThemeBubbleDrawables.THEME_BASE_PATH + mThemeKey),
                    ThemeBubbleDrawables.SOLID_AVATAR_FILE_NAME);
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }

    public boolean isDownloading() {
        return mIsDownloading;
    }

    public void downloadTheme() {
        sDownloadingThemeList.put(mThemeKey, this);
        mIsDownloading = true;

        ThemeDownloadManager.IThemeDownloadListener listener = new ThemeDownloadManager.IThemeDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                mIsDownloading = false;
                sDownloadingThemeList.remove(mThemeKey);
                for (ThemeDownloadManager.IThemeDownloadListener listener : mDownloadListeners) {
                    listener.onDownloadSuccess();
                }
            }

            @Override
            public void onDownloadFailed() {
                mIsDownloading = false;
                sDownloadingThemeList.remove(mThemeKey);
                for (ThemeDownloadManager.IThemeDownloadListener listener : mDownloadListeners) {
                    listener.onDownloadFailed();
                }
            }

            @Override
            public void onDownloadUpdate(float process) {
                for (ThemeDownloadManager.IThemeDownloadListener listener : mDownloadListeners) {
                    listener.onDownloadUpdate(process);
                }
            }
        };

        ThemeDownloadManager.getInstance().downloadTheme(this, listener);
    }

    public void addDownloadListener(ThemeDownloadManager.IThemeDownloadListener listener) {
        if (!mDownloadListeners.contains(listener)) {
            mDownloadListeners.add(listener);
        }
    }

    public void removeDownloadListener(ThemeDownloadManager.IThemeDownloadListener listener) {
        Iterator<ThemeDownloadManager.IThemeDownloadListener> listenerIterator = mDownloadListeners.iterator();
        while (listenerIterator.hasNext()) {
            ThemeDownloadManager.IThemeDownloadListener listener1 = listenerIterator.next();
            if (listener1.equals(listener)) {
                listenerIterator.remove();
            }
        }
    }

    public static ThemeInfo getDownloadingTheme(ThemeInfo info) {
        return sDownloadingThemeList.containsKey(info.mThemeKey)
                ? sDownloadingThemeList.get(info.mThemeKey) : null;
    }

    public static ThemeInfo getThemeInfo(String themeKey) {
        if (sDownloadingThemeList.containsKey(themeKey)) {
            return sDownloadingThemeList.get(themeKey);
        }
        return ThemeInfo.ofConfig(themeKey, HSConfig.getMap("Application", "Themes", "ThemeList", themeKey));
    }

    public static List<ThemeInfo> getAllThemes() {
        List<ThemeInfo> list = new ArrayList<>();
        Map<String, ?> themeMap = HSConfig.getMap("Application", "Themes", "ThemeList");
        Set<String> themeKeys = themeMap.keySet();

        for (String themeKey : themeKeys) {
            if (sDownloadingThemeList.containsKey(themeKey)) {
                list.add(sDownloadingThemeList.get(themeKey));
            } else {
                ThemeInfo themeInfo = ThemeInfo.ofConfig(themeKey, (Map<String, ?>) themeMap.get(themeKey));
                list.add(themeInfo);
            }
        }

        Collections.sort(list, (o1, o2) -> {
            if (ThemeUtils.DEFAULT_THEME_KEY.equals(o1.mThemeKey)) {
                return -1;
            } else if (ThemeUtils.DEFAULT_THEME_KEY.equals(o2.mThemeKey)) {
                return 1;
            }
            return o2.mThemeDownloadTimes - o1.mThemeDownloadTimes;
        });
        return list;
    }

    public static List<ThemeInfo> getLocalThemes() {
        List<ThemeInfo> list = new ArrayList<>();
        Map<String, ?> themeMap = HSConfig.getMap("Application", "Themes", "ThemeList");
        Set<String> themeNames = themeMap.keySet();

        for (String themeName : themeNames) {
            ThemeInfo themeInfo = ThemeInfo.ofConfig(themeName, (Map<String, ?>) themeMap.get(themeName));
            if (themeInfo.mIsLocalTheme) {
                list.add(themeInfo);
            }
        }

        Collections.sort(list, (o1, o2) -> {
            if (ThemeUtils.DEFAULT_THEME_KEY.equals(o1.mThemeKey)) {
                return -1;
            } else if (ThemeUtils.DEFAULT_THEME_KEY.equals(o2.mThemeKey)) {
                return 1;
            }
            return o2.mThemeDownloadTimes - o1.mThemeDownloadTimes;
        });
        return list;
    }
}
