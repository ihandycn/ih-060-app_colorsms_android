package com.android.messaging.ui.customize.mainpage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChatListDrawableManager {
    private static final String PREF_KEY_HAS_CHAT_LIST_CUSTOM = "pref_key_has_chat_list_custom";

    private static final String PREF_KEY_CONVERSATION_LIST_WALLPAPER_PATH = "pref_key_list_wallpaper_path";
    private static final String PREF_KEY_CHAT_LIST_TEXT_COLOR = "pref_key_chat_list_text_color";
    private static final String PREF_KEY_CHAT_LIST_MASK_OPACITY = "pref_key_chat_list_mask_opacity";
    private static final String PREF_KEY_CHAT_LIST_USE_THEME_TEXT_COLOR = "";

    private static final String CHAT_LIST_BASE_FOLDER = "theme" + File.separator + "chat_list";
    private static final String WALLPAPER_FILE_NAME = "wallpaper";
    private static final String TOOLBAR_FILE_NAME = "toolbar";

    private static Preferences sPref = Preferences.getDefault();
    private static boolean sHasCustomWallpaper = sPref.getBoolean(PREF_KEY_HAS_CHAT_LIST_CUSTOM, false);
    private static boolean sUseThemeColor = sPref.getBoolean(PREF_KEY_CHAT_LIST_USE_THEME_TEXT_COLOR, true);
    private static int sCurrentTextColor = sPref.getInt(PREF_KEY_CHAT_LIST_TEXT_COLOR, 0xffffffff);

    public static void resetAllCustomData() {
        sPref.remove(PREF_KEY_CONVERSATION_LIST_WALLPAPER_PATH);
        sPref.remove(PREF_KEY_CHAT_LIST_TEXT_COLOR);
        sPref.remove(PREF_KEY_CHAT_LIST_MASK_OPACITY);
        sPref.remove(PREF_KEY_CHAT_LIST_USE_THEME_TEXT_COLOR);

        sPref.remove(PREF_KEY_HAS_CHAT_LIST_CUSTOM);
        sHasCustomWallpaper = false;
    }

    static String getListWallpaperPath() {
        if (sHasCustomWallpaper) {
            return sPref.getString(PREF_KEY_CONVERSATION_LIST_WALLPAPER_PATH, null);
        }
        return null;
    }

    static float getMaskOpacity() {
        if (sHasCustomWallpaper) {
            return sPref.getFloat(PREF_KEY_CHAT_LIST_MASK_OPACITY, 0);
        }
        return 0;
    }

    static boolean shouldUseThemeColor() {
        return !sHasCustomWallpaper || sUseThemeColor;
    }

    public static int getTextColor() {
        return sCurrentTextColor;
    }

    public static void changeDrawableColorIfNeed(Drawable drawable) {
        if (!sHasCustomWallpaper) {
            return;
        }

        if (sUseThemeColor) {
            return;
        }
        drawable.setColorFilter(sCurrentTextColor, PorterDuff.Mode.SRC_IN);

    }

    public static void changeViewColorIfNeed(View view) {
        if (!sHasCustomWallpaper) {
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
        if (sHasCustomWallpaper) {
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
        if (sHasCustomWallpaper) {
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
        if (customWallpaperFile.exists()) {
            customWallpaperFile.delete();
        }
        File customToolbarFile = new File(CommonUtils.getDirectory(CHAT_LIST_BASE_FOLDER), TOOLBAR_FILE_NAME);
        if (customToolbarFile.exists()) {
            customToolbarFile.delete();
        }

        if (TextUtils.isEmpty(wallpaperPath)) {
            //use theme document
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

            if (opacity > 0) {
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
            }

            if (toolbar != null) {
                saveBitmapToLocal(toolbar, customToolbarFile);
            }

            if (wallpaper != null) {
                saveBitmapToLocal(wallpaper, customWallpaperFile);
            }
        } else {
            Bitmap bgBitmap = BitmapFactory.decodeFile(wallpaperPath);

            int height = Dimensions.getPhoneHeight(HSApplication.getContext());
            int width = Dimensions.getPhoneWidth(HSApplication.getContext());

            int bitmapHeight = bgBitmap.getHeight();
            int bitmapWidth = bgBitmap.getWidth();

            int left = 0;
            int top = 0;
            int resizeWidth = bitmapWidth;
            int resizeHeight = bitmapHeight;

            Bitmap resizedBitmap;
            if (height * bitmapWidth == width * bitmapHeight) {
                resizedBitmap = bgBitmap;
            } else {
                if (height * bitmapWidth < width * bitmapHeight) {
                    resizeHeight = (int) (bitmapWidth * height * 1.0f / width);
                    top = bitmapHeight / 2 - resizeHeight / 2;
                } else {
                    resizeWidth = (int) (bitmapHeight * width * 1.0f / height);
                    left = bitmapWidth / 2 - resizeWidth / 2;
                }
                resizedBitmap = Bitmap.createBitmap(bgBitmap, left, top, resizeWidth, resizeHeight);
            }

            if (opacity > 0) {
                Bitmap bmp = Bitmap.createBitmap(resizeWidth, resizeHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);
                canvas.drawBitmap(resizedBitmap, 0, 0, null);
                int coverColor = ((int) (opacity * 255) << 24) | 0x00FFFFFF;
                canvas.drawColor(coverColor);
                resizedBitmap = bmp;
            }

            int cutPointY = (int) (resizeWidth * 1.0f *
                    (Dimensions.pxFromDp(56) + Dimensions.getStatusBarHeight(HSApplication.getContext())) / width);

            Bitmap toolbar = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), cutPointY);
            saveBitmapToLocal(toolbar, customToolbarFile);

            cutPointY++;
            Bitmap wallpaper = Bitmap.createBitmap(resizedBitmap, 0, cutPointY, resizedBitmap.getWidth(),
                    resizedBitmap.getHeight() - cutPointY);
            saveBitmapToLocal(wallpaper, customWallpaperFile);
        }

        if (!useThemeColor) {
            sPref.putBoolean(PREF_KEY_CHAT_LIST_USE_THEME_TEXT_COLOR, false);
            sPref.putInt(PREF_KEY_CHAT_LIST_TEXT_COLOR, textColor);
        }
        sUseThemeColor = useThemeColor;
        sCurrentTextColor = textColor;
        sHasCustomWallpaper = true;

        sPref.putBoolean(PREF_KEY_HAS_CHAT_LIST_CUSTOM, true);
        sPref.putFloat(PREF_KEY_CHAT_LIST_MASK_OPACITY, opacity);
        sPref.putString(PREF_KEY_CONVERSATION_LIST_WALLPAPER_PATH, wallpaperPath);
        sHasCustomWallpaper = true;
    }

    private static void saveBitmapToLocal(Bitmap bitmap, File file) {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
