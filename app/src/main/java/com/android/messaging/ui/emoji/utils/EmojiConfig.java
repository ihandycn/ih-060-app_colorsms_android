package com.android.messaging.ui.emoji.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.os.TraceCompat;
import android.text.TextUtils;

import com.android.messaging.BuildConfig;
import com.android.messaging.download.Downloader;
import com.android.messaging.ui.emoji.BaseEmojiInfo;
import com.android.messaging.ui.emoji.EmojiPackageInfo;
import com.android.messaging.ui.emoji.EmojiPackageType;
import com.android.messaging.ui.emoji.EmojiType;
import com.android.messaging.ui.emoji.StickerInfo;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.libraryconfig.HSLibraryConfig;
import com.ihs.commons.libraryconfig.HSLibrarySessionManager;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;

import net.appcloudbox.common.utils.AcbParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EmojiConfig implements HSLibraryConfig.ILibraryListener {

    private static final String TAG = EmojiConfig.class.getSimpleName();
    private static final int CONFIG_VERSION = HSApplication.getCurrentLaunchInfo().appVersionCode;
    private static final String MESSAGE_EMOJI_CONFIG = "message-emoji-config";
    private static final String LOCAL_COMMON_CONFIG_NAME = "emoji-config.sa";
    private Map<String, ?> sConfigMap;

    private EmojiConfig() {
    }

    public static EmojiConfig getInstance() {
        return ClassHolder.INSTANCE;
    }

    /**
     * Performs a local load and starts an async remote fetch.
     */
    public void doInit() {
        try {
            Map<String, ?> localCommonData = parseLocalConfig();
            ConfigRegionsSupport.mergeRegions(localCommonData);

            HSLog.d(TAG, "Load emoji config, fire a remote fetch");
            HSLibrarySessionManager librarySessionManager = HSLibrarySessionManager.getInstance();
            librarySessionManager.startSessionForLibrary(sEmojiProvider);

            HSLibraryConfig libraryConfig = HSLibraryConfig.getInstance();
            libraryConfig.startForLibrary(BuildConfig.EMOJI_CONFIG,
                    localCommonData, sEmojiProvider, this);

            setConfigData();
        } finally {
        }
    }

    public int optInteger(int defaultValue, String... path) {
        return HSMapUtils.optInteger(sConfigMap, defaultValue, path);
    }

    public @NonNull
    List<EmojiPackageInfo> getAddedEmojiFromConfig() {
        return getEmojiFromConfig(true);
    }

    public @NonNull
    List<EmojiPackageInfo> getStoreEmojiFromConfig() {
        return getEmojiFromConfig(false);
    }

    @SuppressWarnings("unchecked")
    private @NonNull
    List<EmojiPackageInfo> getEmojiFromConfig(boolean isMustBeAdded) {
        if (sConfigMap == null || sConfigMap.isEmpty()) {
            throw new IllegalStateException("The emoji config is null!!!!");
        }

        List<String> allAddedSticker = EmojiManager.getTabSticker();
        int initSize = allAddedSticker.size();
        Context context = HSApplication.getContext();
        String baseUrl = HSMapUtils.getString(sConfigMap, "BaseUrl");
        String bannerFileName = HSMapUtils.getString(sConfigMap, "BannerFileName");
        int presetEmojiCount = HSMapUtils.optInteger(sConfigMap, 0, "PresetEmojiCount");
        String lottieMagicStickerStr = HSMapUtils.getString(sConfigMap, "LottieMagic");

        List<Map<String, ?>> configMaps = (List<Map<String, ?>>) HSMapUtils.getList(sConfigMap, "Collections");
        List<EmojiPackageInfo> result = new ArrayList<>(configMaps.size());
        for (int i = 0; i < configMaps.size(); i++) {
            EmojiPackageInfo packageInfo = new EmojiPackageInfo();
            packageInfo.mEmojiPackageType = EmojiPackageType.STICKER;
            Map<String, ?> configMap = configMaps.get(i);

            packageInfo.mName = (String) configMap.get("Name");
            if (isMustBeAdded) {
                if (i >= presetEmojiCount && !allAddedSticker.contains(packageInfo.mName)) {
                    continue;
                }
                if (!allAddedSticker.contains(packageInfo.mName)) {
                    allAddedSticker.add(packageInfo.mName);
                }
            } else {
                // store emoji
                if (i < presetEmojiCount) {
                    continue;
                }
            }

            String icon = (String) configMap.get("Icon");
            if (!TextUtils.isEmpty(icon) && icon.startsWith("http")) {
                packageInfo.mTabIconUrl = Uri.parse(icon).toString();
            } else {
                packageInfo.mTabIconUrl = Uri.parse("android.resource://" + context.getPackageName() +
                        "/" + context.getResources().getIdentifier(icon, "drawable",
                        context.getPackageName())).toString();
            }

            packageInfo.mBannerUrl = baseUrl + bannerFileName + "/" + configMap.get("Banner") + ".png";

            List<BaseEmojiInfo> stickerInfoList;
            String gifFiles = (String) configMap.get("GifFiles");
            String magicFiles = (String) configMap.get("MagicFiles");
            String pngFiles = (String) configMap.get("PngFiles");
            if (!TextUtils.isEmpty(gifFiles)) {
                List<String> fileList = getFileNameList(gifFiles);
                if (fileList.isEmpty()) {
                    throw new IllegalStateException("gifFiles is not null, but fileList is null which is impossible!!!!");
                }

                stickerInfoList = new ArrayList<>(fileList.size());
                String imageSubPath = (String) configMap.get("ImageSubPath");
                for (int k = 0; k < fileList.size(); k++) {
                    String fileName = fileList.get(k);
                    StickerInfo stickerInfo = new StickerInfo();
                    stickerInfo.mPackageName = packageInfo.mName;
                    stickerInfo.mEmojiType = EmojiType.STICKER_GIF;
                    stickerInfo.mStickerUrl = baseUrl + imageSubPath + "/" + fileName + ".gif";
                    stickerInfoList.add(stickerInfo);
                }
            } else if (!TextUtils.isEmpty(magicFiles)) {
                List<String> fileList = getFileNameList(magicFiles);
                if (fileList.isEmpty()) {
                    throw new IllegalStateException("magicFiles is not null, but fileList is null which is impossible!!!!");
                }

                stickerInfoList = new ArrayList<>(fileList.size());
                String imageSubPath = (String) configMap.get("ImageSubPath");
                String previewImageSubPath = (String) configMap.get("PreviewImageSubPath");
                String soundSubPath = (String) configMap.get("SoundSubPath");
                for (int k = 0; k < fileList.size(); k++) {
                    String fileName = fileList.get(k);
                    String voiceFormat = ".mp3";
                    StickerInfo stickerInfo = new StickerInfo();
                    if (!TextUtils.isEmpty(lottieMagicStickerStr) && lottieMagicStickerStr.contains(fileName)) {
                        voiceFormat = ".aac";
                        stickerInfo.mLottieZipUrl = baseUrl + imageSubPath + "/" + fileName + ".zip";
                    }
                    stickerInfo.mPackageName = packageInfo.mName;
                    stickerInfo.mEmojiType = EmojiType.STICKER_MAGIC;
                    stickerInfo.mStickerUrl = baseUrl + previewImageSubPath + "/" + fileName + ".png";
                    stickerInfo.mMagicUrl = baseUrl + imageSubPath + "/" + fileName + ".gif";
                    stickerInfo.mSoundUrl = baseUrl + soundSubPath + "/" + fileName + voiceFormat;
                    stickerInfo.mIsDownloaded = Downloader.getInstance().isDownloaded(stickerInfo.mMagicUrl);
                    stickerInfoList.add(stickerInfo);
                }
            } else if (!TextUtils.isEmpty(pngFiles)) {
                List<String> fileList = getFileNameList(pngFiles);
                if (fileList.isEmpty()) {
                    throw new IllegalStateException("pngFiles is not null, but fileList is null which is impossible!!!!");
                }

                stickerInfoList = new ArrayList<>(fileList.size());
                String imageSubPath = (String) configMap.get("ImageSubPath");
                for (int k = 0; k < fileList.size(); k++) {
                    String fileName = fileList.get(k);
                    StickerInfo stickerInfo = new StickerInfo();
                    stickerInfo.mPackageName = packageInfo.mName;
                    stickerInfo.mEmojiType = EmojiType.STICKER_IMAGE;
                    stickerInfo.mStickerUrl = baseUrl + imageSubPath + "/" + fileName + ".png";
                    stickerInfoList.add(stickerInfo);
                }
            } else {
                throw new IllegalStateException("gifFiles、magicFiles、pngFiles could not be null together!!!");
            }

            packageInfo.mEmojiInfoList = stickerInfoList;

            result.add(packageInfo);
        }

        if (isMustBeAdded) {
            if (allAddedSticker.size() != initSize) {
                EmojiManager.addTabSticker(allAddedSticker);
            }
            result = order(result, allAddedSticker);
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    public @NonNull
    List<BaseEmojiInfo> getMajicEmoj() {
        if (sConfigMap == null || sConfigMap.isEmpty()) {
            throw new IllegalStateException("The emoji config is null!!!!");
        }
        String baseUrl = HSMapUtils.getString(sConfigMap, "BaseUrl");
        String lottieMagicStickerStr = HSMapUtils.getString(sConfigMap, "LottieMagic");
        List<Map<String, ?>> configMaps = (List<Map<String, ?>>) HSMapUtils.getList(sConfigMap, "Collections");
        for (int i = 0; i < configMaps.size(); i++) {
            Map<String, ?> configMap = configMaps.get(i);
            String name = (String) configMap.get("Name");
            List<BaseEmojiInfo> stickerInfoList;
            String magicFiles = (String) configMap.get("MagicFiles");
            if (!TextUtils.isEmpty(magicFiles)) {
                List<String> fileList = getFileNameList(magicFiles);
                if (fileList.isEmpty()) {
                    throw new IllegalStateException("magicFiles is not null, but fileList is null which is impossible!!!!");
                }

                stickerInfoList = new ArrayList<>(fileList.size());
                String imageSubPath = (String) configMap.get("ImageSubPath");
                String previewImageSubPath = (String) configMap.get("PreviewImageSubPath");
                String soundSubPath = (String) configMap.get("SoundSubPath");
                for (int k = 0; k < fileList.size(); k++) {
                    String fileName = fileList.get(k);
                    String voiceFormat = ".mp3";
                    StickerInfo stickerInfo = new StickerInfo();
                    if (!TextUtils.isEmpty(lottieMagicStickerStr) && lottieMagicStickerStr.contains(fileName)) {
                        voiceFormat = ".aac";
                        stickerInfo.mLottieZipUrl = baseUrl + imageSubPath + "/" + fileName + ".zip";
                    }
                    stickerInfo.mPackageName = name;
                    stickerInfo.mEmojiType = EmojiType.STICKER_MAGIC;
                    stickerInfo.mStickerUrl = baseUrl + previewImageSubPath + "/" + fileName + ".png";
                    stickerInfo.mMagicUrl = baseUrl + imageSubPath + "/" + fileName + ".gif";
                    stickerInfo.mSoundUrl = baseUrl + soundSubPath + "/" + fileName + voiceFormat;
                    stickerInfo.mIsDownloaded = Downloader.getInstance().isDownloaded(stickerInfo.mMagicUrl);
                    stickerInfoList.add(stickerInfo);
                }
                return stickerInfoList;
            }
        }
        return new ArrayList<>();
    }

    private boolean isLottieMagicEmoji(String fileName) {
        return "25".equals(fileName);
    }

    private List<EmojiPackageInfo> order(List<EmojiPackageInfo> data, List<String> orderList) {
        List<EmojiPackageInfo> result = new ArrayList<>(data.size());
        for (int i = 0; i < orderList.size(); i++) {
            String name = orderList.get(i);
            for (EmojiPackageInfo info : data) {
                if (info.mName.equals(name)) {
                    result.add(info);
                    break;
                }
            }
        }
        return result;
    }

    private List<String> getFileNameList(String fileListStr) {
        List<String> result = new ArrayList<>();
        if (TextUtils.isEmpty(fileListStr)) {
            return result;
        }

        String trimmed = fileListStr.replaceAll(" ", "");
        String[] split = trimmed.split(",");
        for (String rangeStr : split) {
            String[] bounds = rangeStr.split("-");
            switch (bounds.length) {
                case 1:
                    result.add(bounds[0]);
                    break;
                case 2:
                    int startIndex = Integer.valueOf(bounds[0]);
                    int endIndex = Integer.valueOf(bounds[1]);

                    for (int i = startIndex; i <= endIndex; i++) {
                        result.add(String.valueOf(i));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Illegal range: " + fileListStr);
            }
        }
        return result;
    }

    private Map<String, ?> parseLocalConfig() {
        HSLog.d(TAG, "Load local emoji config");
        return AcbParser.parse(HSApplication.getContext().getAssets(), LOCAL_COMMON_CONFIG_NAME);
    }

    @Override
    public void onRemoteConfigDataChanged() {
        HSLog.i(TAG, "Emoji config data changed");
        setConfigData();
    }

    private void setConfigData() {
        sConfigMap = HSLibraryConfig.getInstance().getDataForLibrary(sEmojiProvider);
    }

    private static HSLibraryConfig.ILibraryProvider sEmojiProvider = new ConfigProvider() {
        @Override
        public String getLibraryName() {
            return MESSAGE_EMOJI_CONFIG;
        }
    };

    private abstract static class ConfigProvider implements HSLibraryConfig.ILibraryProvider {
        @Override
        public int getLibraryVersionNumber() {
            return CONFIG_VERSION;
        }

        @Override
        public int getUpdateIntervalInSeconds() {
            return HSConfig.optInteger((int) TimeUnit.DAYS.toSeconds(1),
                    "Application", "LibraryConfig", "EmojiUpdateInterval");
        }
    }

    private static class ClassHolder {
        private final static EmojiConfig INSTANCE = new EmojiConfig();
    }
}
