package com.android.messaging.ui.emoji.config;

import android.support.v4.os.TraceCompat;

import com.android.messaging.BuildConfig;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.libraryconfig.HSLibraryConfig;
import com.ihs.commons.libraryconfig.HSLibrarySessionManager;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.common.utils.AcbParser;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EmojiConfig implements HSLibraryConfig.ILibraryListener {

    private static class ClassHolder {
        private final static EmojiConfig INSTANCE = new EmojiConfig();
    }

    public static EmojiConfig getInstance() {
        return ClassHolder.INSTANCE;
    }

    private static final String TAG = EmojiConfig.class.getSimpleName();
    private static final int CONFIG_VERSION = HSApplication.getCurrentLaunchInfo().appVersionCode;
    private static final String MESSAGE_EMOJI_CONFIG = "message-emoji-config";
    private static final String LOCAL_COMMON_CONFIG_NAME = "emoji-config.sa";

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

    private static Map<String, ?> sConfigMap;

    private EmojiConfig() {
    }

    public Map<String, ?> getConfigMap() {
        return sConfigMap;
    }

    /**
     * Performs a local load and starts an async remote fetch.
     */
    public void doInit() {
        TraceCompat.beginSection("EmojiConfig Init");
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
            TraceCompat.endSection();
        }
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
}
