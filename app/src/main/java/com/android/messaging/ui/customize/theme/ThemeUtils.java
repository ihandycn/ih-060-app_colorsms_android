package com.android.messaging.ui.customize.theme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.android.messaging.Factory;
import com.android.messaging.font.FontUtils;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.AvatarBgDrawables;
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.util.BuglePrefsKeys;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.android.messaging.font.FontStyleManager;

import java.io.IOException;
import java.io.InputStream;

public class ThemeUtils {
    public static final String DEFAULT_THEME_KEY = "default";

    interface IThemeChangeListener{
        void onThemeChanged();
    }

    private static ThemeInfo sCurrentTheme;

    public static void applyTheme(ThemeInfo themeInfo) {
        sCurrentTheme = themeInfo;
        Factory.get().getCustomizePrefs().putString(BuglePrefsKeys.PREFS_KEY_THEME_NAME, themeInfo.mThemeKey);

        PrimaryColors.changePrimaryColor(Color.parseColor(themeInfo.themeColor));

        ConversationColors.get().setBubbleBackgroundColor(true, Color.parseColor(themeInfo.incomingBubbleBgColor));
        ConversationColors.get().setBubbleBackgroundColor(false, Color.parseColor(themeInfo.outgoingBubbleBgColor));
        ConversationColors.get().setMessageTextColor(true, Color.parseColor(themeInfo.incomingBubbleTextColor));
        ConversationColors.get().setMessageTextColor(false, Color.parseColor(themeInfo.outgoingBubbleTextColor));
        ConversationColors.get().setListTitleColor(Color.parseColor(themeInfo.listTitleColor));
        ConversationColors.get().setListSubTitleColor(Color.parseColor(themeInfo.listSubtitleColor));
        ConversationColors.get().setListTimeColor(Color.parseColor(themeInfo.listTimeColor));
        ConversationColors.get().setAdActionColor(Color.parseColor(themeInfo.bubbleAdColor));

        ToolbarDrawables.sToolbarBitmap = null;
        ToolbarDrawables.applyToolbarBg();
        WallpaperDrawables.sListWallpaperBitmap = null;
        WallpaperDrawables.sWallpaperBitmap = null;
        WallpaperDrawables.applyWallpaperBg(themeInfo.wallpaperUrl);
        WallpaperDrawables.applyListWallpaperBg();
        AvatarBgDrawables.sAvatarBg = null;
        AvatarBgDrawables.applyAvatarBg();
        CreateIconDrawable.sCreateIconBitmap = null;
        CreateIconDrawable.applyCreateIcon();

        ThemeManager.getInstance().clearCacheDrawable();

        if (!themeInfo.mThemeKey.equals(ThemeUtils.DEFAULT_THEME_KEY)) {
            BubbleDrawables.setSelectedIdentifier(-1);
        }

        if (themeInfo.mIsLocalTheme && !themeInfo.isInLocalFolder()) {
            ThemeDownloadManager.getInstance().copyFileFromAssetsAsync(themeInfo,
                    new ThemeDownloadManager.IThemeMoveListener() {
                        @Override
                        public void onMoveSuccess() {

                        }

                        @Override
                        public void onMoveFailed() {

                        }
                    });
        }

        FontStyleManager.getInstance().setFontFamily(themeInfo.fontName);
        FontUtils.onFontTypefaceChanged();

        HSGlobalNotificationCenter.sendNotification(ConversationListActivity.EVENT_MAINPAGE_RECREATE);
        WallpaperSizeManager.getInstance().loadWallpaperParams();
        Factory.get().reclaimMemory();
    }

    public static String getCurrentThemeName() {
        return getCurrentTheme().mThemeKey;
    }

    public static ThemeInfo getCurrentTheme() {
        // Default theme is not null
        if (sCurrentTheme == null) {
            String themeKey = Factory.get().getCustomizePrefs().getString(BuglePrefsKeys.PREFS_KEY_THEME_NAME, DEFAULT_THEME_KEY);
            sCurrentTheme = ThemeInfo.getThemeInfo(themeKey);
        }
        return sCurrentTheme;
    }

    public static boolean isDefaultTheme() {
        return getCurrentTheme().mThemeKey.equals(DEFAULT_THEME_KEY);
    }

    static Drawable getLocalThemeDrawableFromPath(String path) {
        try {
            InputStream ims = HSApplication.getContext().getAssets().open("themes/" + path);
            Bitmap bitmap = BitmapFactory.decodeStream(ims);
            return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
