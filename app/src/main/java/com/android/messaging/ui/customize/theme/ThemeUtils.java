package com.android.messaging.ui.customize.theme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import com.android.messaging.BugleApplication;
import com.android.messaging.Factory;
import com.android.messaging.ui.customize.AvatarBgDrawables;
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.android.messaging.util.BugleApplicationPrefs;
import com.android.messaging.util.BuglePrefsKeys;
import com.ihs.app.framework.HSApplication;
import com.superapps.font.FontStyleManager;

import java.io.IOException;
import java.io.InputStream;

public class ThemeUtils {

    public static void downloadTheme(ThemeInfo themeInfo) {

    }

    public static void applyTheme(ThemeInfo themeInfo) {
        PrimaryColors.changePrimaryColor(Color.parseColor(themeInfo.themeColor));

        ConversationColors.get().setBubbleBackgroundColor(true, Color.parseColor(themeInfo.incomingBubbleBgColor));
        ConversationColors.get().setBubbleBackgroundColor(false, Color.parseColor(themeInfo.outgoingBubbleBgColor));
        ConversationColors.get().setMessageTextColor(true, Color.parseColor(themeInfo.incomingBubbleTextColor));
        ConversationColors.get().setMessageTextColor(false, Color.parseColor(themeInfo.outgoingBubbleTextColor));
        ConversationColors.get().setListTitleColor(Color.parseColor(themeInfo.listTitleColor));
        ConversationColors.get().setListSubTitleColor(Color.parseColor(themeInfo.listSubtitleColor));
        ConversationColors.get().setListTimeColor(Color.parseColor(themeInfo.listTimeColor));

        ToolbarDrawables.applyToolbarBg(themeInfo.toolbarBgUrl);
        WallpaperDrawables.applyWallpaperBg(themeInfo.wallpaperUrl);
        AvatarBgDrawables.applyAvatarBg(themeInfo.avatarUrl);

        BubbleDrawables.setSelectedIdentifier(Integer.parseInt(themeInfo.bubbleIncomingUrl));

        FontStyleManager.getInstance().setFontFamily(themeInfo.fontName);

        Factory.get().getCustomizePrefs().putString(BuglePrefsKeys.PREFS_KEY_THEME_NAME, themeInfo.name);


    }

    public static String getCurrentThemeName() {
        return Factory.get().getCustomizePrefs().getString(BuglePrefsKeys.PREFS_KEY_THEME_NAME, "Default");
    }

    public static Drawable getSelectedDrawable(String url) {
        Bitmap bitmap = null;

        if (url.startsWith("assets://")) {
            try {
                InputStream ims = HSApplication.getContext().getAssets().open(url.replace("assets://", ""));

                bitmap = BitmapFactory.decodeStream(ims);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Drawable drawable = null;
        if (bitmap != null) {
            byte[] chunk = bitmap.getNinePatchChunk();
            if (NinePatch.isNinePatchChunk(chunk)) {
                drawable = new NinePatchDrawable(bitmap, chunk, NinePatchChunk.deserialize(chunk).mPaddings, null);
            } else {
                drawable = new BitmapDrawable(bitmap);
            }
        }

        return drawable;

    }


    public static Drawable getDrawableFromUrl(String url) {
        if (url.startsWith("assets://")) {
            try {
                InputStream ims = HSApplication.getContext().getAssets().open(url.replace("assets://", ""));
                return Drawable.createFromStream(ims, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
