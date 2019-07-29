package com.android.messaging.ui.customize.theme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.android.messaging.Factory;
import com.android.messaging.font.FontStyleManager;
import com.android.messaging.font.FontUtils;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.AvatarBgDrawables;
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.ui.customize.mainpage.ChatListCustomizeManager;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.BuglePrefsKeys;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ThemeUtils {
    public static final String DEFAULT_THEME_KEY = "default";

    interface IThemeChangeListener {
        void onThemeChanged();
    }

    private static ThemeInfo sCurrentTheme;

    public static void applyTheme(ThemeInfo themeInfo, int delay) {
        sCurrentTheme = themeInfo;
        Factory.get().getCustomizePrefs().putString(BuglePrefsKeys.PREFS_KEY_THEME_NAME, themeInfo.mThemeKey);

        if (delay > 0) {
            Threads.postOnMainThreadDelayed(() -> {
                applyTheme(themeInfo);
            }, delay);
        } else {
            applyTheme(themeInfo);
        }
    }

    public static void applyThemeFirstTime(ThemeInfo themeInfo, Runnable endRunnable) {
        sCurrentTheme = themeInfo;
        Factory.get().getCustomizePrefs().putString(BuglePrefsKeys.PREFS_KEY_THEME_NAME, themeInfo.mThemeKey);

        if (!themeInfo.mThemeKey.equals(ThemeUtils.DEFAULT_THEME_KEY)) {
            ConversationColors.get().setBubbleBackgroundColor(false, Color.parseColor(themeInfo.outgoingBubbleBgColor));
        } else {
            ConversationColors.get().setBubbleBackgroundColor(false, Color.parseColor(themeInfo.themeColor));
        }
        // change theme color should be after bubble color set
        PrimaryColors.changePrimaryColor(Color.parseColor(themeInfo.themeColor));

        ConversationColors.get().setBubbleBackgroundColor(true, Color.parseColor(themeInfo.incomingBubbleBgColor));
        ConversationColors.get().setMessageTextColor(true, Color.parseColor(themeInfo.incomingBubbleTextColor));
        ConversationColors.get().setMessageTextColor(false, Color.parseColor(themeInfo.outgoingBubbleTextColor));
        ConversationColors.get().setListTitleColor(Color.parseColor(themeInfo.listTitleColor));
        ConversationColors.get().setListSubTitleColor(Color.parseColor(themeInfo.listSubtitleColor));
        ConversationColors.get().setListTimeColor(Color.parseColor(themeInfo.listTimeColor));
        ConversationColors.get().setAdActionColor(Color.parseColor(themeInfo.bubbleAdColor));

        ToolbarDrawables.sToolbarBitmap = null;
        WallpaperDrawables.sListWallpaperBitmap = null;
        WallpaperDrawables.sWallpaperBitmap = null;
        WallpaperDrawables.applyWallpaperBg(themeInfo.wallpaperUrl);
        AvatarBgDrawables.sAvatarBg = null;
        AvatarBgDrawables.sSolidAvatarBg = null;
        CreateIconDrawable.sCreateIconBitmap = null;

        ThemeBubbleDrawables.getInstance().clearCacheDrawable();

        if (!themeInfo.mThemeKey.equals(ThemeUtils.DEFAULT_THEME_KEY)) {
            BubbleDrawables.setSelectedIdentifier(-1);
        }
        ChatListCustomizeManager.resetAllCustomData();
        FontStyleManager.getInstance().setFontFamily(themeInfo.fontName);
//
//        ThemeDownloadManager.getInstance().copyFileFromAssetsSync(themeInfo,
//                new ThemeDownloadManager.IThemeMoveListener() {
//                    @Override
//                    public void onMoveSuccess() {
//                        FontUtils.onFontTypefaceChanged();
//
//                        //WallpaperSizeManager.resizeThemeBitmap(themeInfo);
//
//                        if (endRunnable != null) {
//                            Threads.postOnMainThread(endRunnable);
//                        }
//                    }
//
//                    @Override
//                    public void onMoveFailed() {
//
//                    }
//                });
        if (endRunnable != null) {
            Threads.postOnMainThread(endRunnable);
        }
        Factory.get().reclaimMemory();
    }

    private static void applyTheme(ThemeInfo themeInfo) {
        Threads.postOnThreadPoolExecutor(() -> {
            if (!themeInfo.mThemeKey.equals(ThemeUtils.DEFAULT_THEME_KEY)) {
                ConversationColors.get().setBubbleBackgroundColor(false, Color.parseColor(themeInfo.outgoingBubbleBgColor));
            } else {
                ConversationColors.get().setBubbleBackgroundColor(false, Color.parseColor(themeInfo.themeColor));
            }
            // change theme color should be after bubble color set
            PrimaryColors.changePrimaryColor(Color.parseColor(themeInfo.themeColor));

            ConversationColors.get().setBubbleBackgroundColor(true, Color.parseColor(themeInfo.incomingBubbleBgColor));
            ConversationColors.get().setMessageTextColor(true, Color.parseColor(themeInfo.incomingBubbleTextColor));
            ConversationColors.get().setMessageTextColor(false, Color.parseColor(themeInfo.outgoingBubbleTextColor));
            ConversationColors.get().setListTitleColor(Color.parseColor(themeInfo.listTitleColor));
            ConversationColors.get().setListSubTitleColor(Color.parseColor(themeInfo.listSubtitleColor));
            ConversationColors.get().setListTimeColor(Color.parseColor(themeInfo.listTimeColor));
            ConversationColors.get().setAdActionColor(Color.parseColor(themeInfo.bubbleAdColor));

            ToolbarDrawables.sToolbarBitmap = null;
            WallpaperDrawables.sListWallpaperBitmap = null;
            WallpaperDrawables.sWallpaperBitmap = null;
            WallpaperDrawables.applyWallpaperBg(themeInfo.wallpaperUrl);
            AvatarBgDrawables.sAvatarBg = null;
            AvatarBgDrawables.sSolidAvatarBg = null;
            CreateIconDrawable.sCreateIconBitmap = null;

            ThemeBubbleDrawables.getInstance().clearCacheDrawable();

            if (!themeInfo.mThemeKey.equals(ThemeUtils.DEFAULT_THEME_KEY)) {
                BubbleDrawables.setSelectedIdentifier(-1);
            }

            ChatListCustomizeManager.resetAllCustomData();

            FontStyleManager.getInstance().setFontFamily(themeInfo.fontName);

            String copyKey = ThemeDownloadManager.getInstance().getPrefKeyByThemeName(themeInfo.mThemeKey);
            if (themeInfo.mIsLocalTheme
                    && !Preferences.getDefault().getBoolean(copyKey, false)) {
                ThemeDownloadManager.getInstance().copyFileFromAssetsSync(themeInfo,
                        new ThemeDownloadManager.IThemeMoveListener() {
                            @Override
                            public void onMoveSuccess() {
                                WallpaperSizeManager.resizeThemeBitmap(themeInfo);
                                Preferences.getDefault().putBoolean(copyKey, true);
                                FontUtils.onFontTypefaceChanged();
                                Threads.postOnMainThread(() ->
                                        HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE));
                            }

                            @Override
                            public void onMoveFailed() {

                            }
                        });
            } else {
                FontUtils.onFontTypefaceChanged();
                Threads.postOnMainThread(() ->
                        HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE));
            }

            Factory.get().reclaimMemory();
        });
    }

    public static String getCurrentThemeName() {
        return getCurrentTheme().mThemeKey;
    }

    public static ThemeInfo getCurrentTheme() {
        // Default theme is not null
        if (sCurrentTheme == null) {

            String themeKey = Factory.get().getCustomizePrefs().getString(BuglePrefsKeys.PREFS_KEY_THEME_NAME, DEFAULT_THEME_KEY);

            String currentKey = themeKey;
            switch (themeKey) {
                case "CuteGraffiti":
                    themeKey = "cutegraffiti";
                    break;
                case "CoolGraffiti":
                    themeKey = "coolgraffiti";
                    break;
                case "Starry":
                    themeKey = "starry";
                    break;
                case "Unicorn":
                    themeKey = "unicorn";
                    break;
                case "Technology":
                    themeKey = "technology";
                    break;
                case "Diamond":
                    themeKey = "diamond";
                    break;
                case "Neno":
                    themeKey = "neon";
                    break;
                case "SimpleBusiness":
                    themeKey = "simplebusiness";
                    break;
                case "WaterDrop":
                    themeKey = "waterdrop";
                    break;
                case "GoldenDiamond":
                    themeKey = "goldendiamond";
                    break;
                case "Default":
                    themeKey = ThemeUtils.DEFAULT_THEME_KEY;
                    break;
            }

            if (!themeKey.equals(currentKey)) {
                Factory.get().getCustomizePrefs().putString(BuglePrefsKeys.PREFS_KEY_THEME_NAME, themeKey);
            }

            Map<String, ?> map = HSConfig.getMap("Application", "Themes", "ThemeList", themeKey);
            if (map.isEmpty()) {
                BugleAnalytics.logEvent("Error_Theme_Key", "key", themeKey);
                themeKey = DEFAULT_THEME_KEY;
            }
            sCurrentTheme = ThemeInfo.getThemeInfo(themeKey);
        }
        return sCurrentTheme;
    }

    public static boolean isDefaultTheme() {
        return getCurrentTheme().mThemeKey.equals(DEFAULT_THEME_KEY);
    }

    static Drawable getLocalThemeDrawableFromPath(String path) {
        try {
            InputStream ims = HSApplication.getContext().getAssets().open("themes" + File.separator + path);
            Bitmap bitmap = BitmapFactory.decodeStream(ims);
            return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
