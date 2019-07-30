package com.android.messaging.ui.customize.mainpage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.ui.customize.theme.ThemeBubbleDrawables;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

import java.io.File;

public class ChatListCustomizeManager {
    //if wallpaper, opacity or text color changed
    private static final String PREF_KEY_HAS_CHAT_LIST_CUSTOM = "pref_key_has_chat_list_custom";

    private static final String PREF_KEY_CONVERSATION_LIST_WALLPAPER_PATH = "pref_key_list_wallpaper_path";
    private static final String PREF_KEY_CHAT_LIST_TEXT_COLOR = "pref_key_chat_list_text_color";
    private static final String PREF_KEY_CHAT_LIST_MASK_OPACITY = "pref_key_chat_list_mask_opacity";
    private static final String PREF_KEY_CHAT_LIST_USE_THEME_TEXT_COLOR = "pref_key_chat_list_use_theme_text_color";

    private static final String CHAT_LIST_BASE_FOLDER = "theme" + File.separator + "chat_list";
    private static final String WALLPAPER_FILE_NAME = "wallpaper";
    private static final String TOOLBAR_FILE_NAME = "toolbar";

    private static Preferences sPref = Preferences.getDefault();
    private static boolean sHasCustomInfo = sPref.getBoolean(PREF_KEY_HAS_CHAT_LIST_CUSTOM, false);
    private static boolean sUseThemeColor = sPref.getBoolean(PREF_KEY_CHAT_LIST_USE_THEME_TEXT_COLOR, true);
    private static int sCurrentTextColor = sPref.getInt(PREF_KEY_CHAT_LIST_TEXT_COLOR, 0xffffffff);

    public static void resetAllCustomData() {
        sPref.remove(PREF_KEY_CONVERSATION_LIST_WALLPAPER_PATH);
        sPref.remove(PREF_KEY_CHAT_LIST_TEXT_COLOR);
        sPref.remove(PREF_KEY_CHAT_LIST_MASK_OPACITY);
        sPref.remove(PREF_KEY_CHAT_LIST_USE_THEME_TEXT_COLOR);

        sPref.remove(PREF_KEY_HAS_CHAT_LIST_CUSTOM);
        sPref.remove(ChatListCustomizeActivity.PREF_KEY_EVENT_CHANGE_COLOR_TYPE);
        sHasCustomInfo = false;
        sUseThemeColor = true;
    }

    public static boolean hasCustomWallpaper() {
        return sHasCustomInfo &&
                (!TextUtils.isEmpty(sPref.getString(PREF_KEY_CONVERSATION_LIST_WALLPAPER_PATH, null)) ||
                        Math.abs(sPref.getFloat(PREF_KEY_CHAT_LIST_MASK_OPACITY, 0)) > 0.0000001);
    }

    public static String getListWallpaperPath() {
        if (sHasCustomInfo) {
            return sPref.getString(PREF_KEY_CONVERSATION_LIST_WALLPAPER_PATH, null);
        }
        return null;
    }

    public static float getMaskOpacity() {
        if (sHasCustomInfo) {
            return sPref.getFloat(PREF_KEY_CHAT_LIST_MASK_OPACITY, 0);
        }
        return 0;
    }

    static boolean shouldUseThemeColor() {
        return !sHasCustomInfo || sUseThemeColor;
    }

    public static int getTextColor() {
        return sCurrentTextColor;
    }

    public static void changeDrawableColorIfNeed(Drawable drawable) {
        changeDrawableColorIfNeed(drawable, true);
    }

    public static void changeDrawableColorIfNeed(Drawable drawable, boolean clearFilterIfNotNeed) {
        if (!sHasCustomInfo || sUseThemeColor) {
            if (clearFilterIfNotNeed) {
                drawable.clearColorFilter();
            }
            return;
        }
        drawable.setColorFilter(sCurrentTextColor, PorterDuff.Mode.SRC_IN);
    }

    public static void changeViewColorIfNeed(View view) {
        if (!sHasCustomInfo) {
            return;
        }

        if (sUseThemeColor) {
            return;
        }

        if (view instanceof TextView) {
            ((TextView) view).setTextColor(sCurrentTextColor);
        } else if (view instanceof ImageView) {
            ((ImageView) view).setColorFilter(sCurrentTextColor);
        }
    }

    static boolean isCustomInfoChanged(String wallpaperPath, float opacity, boolean useThemeColor, int textColor) {
        String path = getListWallpaperPath();
        //if wallpaper file changed
        if (TextUtils.isEmpty(wallpaperPath)) {
            if (!TextUtils.isEmpty(path)) {
                return true;
            }
        } else {
            if (!wallpaperPath.equals(path)) {
                return true;
            }
        }
        //if opacity changed
        if (Math.abs(opacity - sPref.getFloat(PREF_KEY_CHAT_LIST_MASK_OPACITY, 0)) > 0.0000001) {
            return true;
        }

        //if text color changed
        if (sUseThemeColor != useThemeColor) {
            return true;
        } else if (!useThemeColor && textColor != sCurrentTextColor) {
            return true;
        }
        return false;
    }

    public static Drawable getWallpaperDrawable() {
        if (hasCustomWallpaper()) {
            File file = new File(CommonUtils.getDirectory(CHAT_LIST_BASE_FOLDER), WALLPAPER_FILE_NAME);
            if (file.exists()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap != null) {
                        return new BitmapDrawable(bitmap);
                    }
                } catch (Exception ignored) {

                }
            }
        }
        return null;
    }

    public static Drawable getToolbarDrawable() {
        if (hasCustomWallpaper()) {
            File file = new File(CommonUtils.getDirectory(CHAT_LIST_BASE_FOLDER), TOOLBAR_FILE_NAME);
            if (file.exists()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap != null) {
                        return new BitmapDrawable(bitmap);
                    }
                } catch (Exception ignored) {

                }
            }
        }
        return null;
    }

    static void saveChatListCustomizeInfo(String wallpaperPath, float opacity, boolean useThemeColor, int textColor) {
        File customWallpaperFile = new File(CommonUtils.getDirectory(CHAT_LIST_BASE_FOLDER), WALLPAPER_FILE_NAME);
        File customToolbarFile = new File(CommonUtils.getDirectory(CHAT_LIST_BASE_FOLDER), TOOLBAR_FILE_NAME);

        boolean hasCustomWallpaper = true;

        if (TextUtils.isEmpty(wallpaperPath)) {
            if (Math.abs(opacity) < 0.0000001) {
                hasCustomWallpaper = false;
            } else {
                //use theme bitmap
                String currentThemeKey = ThemeUtils.getCurrentThemeName();
                Bitmap toolbar = null;
                Bitmap wallpaper = null;
                File themeBaseFolder = CommonUtils.getDirectory(ThemeBubbleDrawables.THEME_BASE_PATH + currentThemeKey);
                File themeToolbarFile = new File(themeBaseFolder, ThemeBubbleDrawables.TOOLBAR_BG_FILE_NAME);
                if (themeToolbarFile.exists()) {
                    toolbar = BitmapFactory.decodeFile(themeToolbarFile.getAbsolutePath());
                }

                File themeWallpaperFile = new File(themeBaseFolder, ThemeBubbleDrawables.LIST_WALLPAPER_BG_FILE_NAME);
                if (themeWallpaperFile.exists()) {
                    wallpaper = BitmapFactory.decodeFile(themeWallpaperFile.getAbsolutePath());
                }

                int maskColor = ((int) (opacity * 255) << 24) | 0x00FFFFFF;
                if (toolbar != null) {
                    Bitmap bitmap = toolbar.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(maskColor);
                    toolbar = bitmap;
                }

                if (wallpaper != null) {
                    Bitmap bitmap = wallpaper.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(maskColor);
                    wallpaper = bitmap;
                }

                if (toolbar != null) {
                    CommonUtils.saveBitmapToFile(toolbar, customToolbarFile);
                }
                if (wallpaper != null) {
                    CommonUtils.saveBitmapToFile(wallpaper, customWallpaperFile);
                }
            }
        } else {
            Bitmap bgBitmap = BitmapFactory.decodeFile(wallpaperPath);

            int height = Dimensions.getPhoneHeight(HSApplication.getContext());
            int width = Dimensions.getPhoneWidth(HSApplication.getContext());

            if (bgBitmap == null || bgBitmap.getWidth() == 0 || bgBitmap.getHeight() == 0) {
                return;
            }

            int bitmapHeight = bgBitmap.getHeight();
            int bitmapWidth = bgBitmap.getWidth();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            RectF rectF = new RectF(0, 0, width - 1, height - 1);
            if (height * bitmapWidth > width * bitmapHeight) {
                float scale = bitmapHeight / (float) height;
                int scaledWidth = Math.min((int) (scale * width), bitmapWidth);
                Rect rect = new Rect((bitmapWidth - scaledWidth) / 2, 0, (bitmapWidth + scaledWidth) / 2, bitmapHeight);
                canvas.drawBitmap(bgBitmap, rect, rectF, null);
            } else {
                float scale = bitmapWidth / (float) width;
                int scaledHeight = Math.min((int) (scale * height), bitmapHeight);
                Rect rect = new Rect(0, (bitmapHeight - scaledHeight) / 2, bitmapWidth, (bitmapHeight + scaledHeight) / 2);
                canvas.drawBitmap(bgBitmap, rect, rectF, null);
            }
            if (opacity > 0) {
                int coverColor = ((int) (opacity * 255) << 24) | 0x00FFFFFF;
                canvas.drawColor(coverColor);
            }
            int cutPointY = Dimensions.pxFromDp(56) + Dimensions.getStatusBarHeight(HSApplication.getContext());
            if (cutPointY >= height) {
                return;
            }

            Bitmap toolbar = Bitmap.createBitmap(bitmap, 0, 0, width, cutPointY);
            Bitmap wallpaper = Bitmap.createBitmap(bitmap, 0, cutPointY, width, height - cutPointY);
            CommonUtils.saveBitmapToFile(toolbar, customToolbarFile);
            CommonUtils.saveBitmapToFile(wallpaper, customWallpaperFile);
        }

        if (!hasCustomWallpaper && useThemeColor) {
            resetAllCustomData();
            return;
        }

        sPref.putBoolean(PREF_KEY_HAS_CHAT_LIST_CUSTOM, true);
        if (!useThemeColor) {
            sPref.putBoolean(PREF_KEY_CHAT_LIST_USE_THEME_TEXT_COLOR, false);
            sPref.putInt(PREF_KEY_CHAT_LIST_TEXT_COLOR, textColor);
        }

        if (hasCustomWallpaper) {
            sPref.putString(PREF_KEY_CONVERSATION_LIST_WALLPAPER_PATH, wallpaperPath);
        } else {
            sPref.remove(PREF_KEY_CONVERSATION_LIST_WALLPAPER_PATH);
        }
        sUseThemeColor = useThemeColor;
        sCurrentTextColor = textColor;
        sHasCustomInfo = true;

        sPref.putFloat(PREF_KEY_CHAT_LIST_MASK_OPACITY, opacity);
    }
}
