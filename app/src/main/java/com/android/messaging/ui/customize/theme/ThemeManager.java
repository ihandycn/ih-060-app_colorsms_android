package com.android.messaging.ui.customize.theme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.font.FontDownloadManager;
import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ThemeManager {
    public static final String THEME_BASE_PATH = "theme" + File.separator;

    public static final String WALLPAPER_BG_FILE_NAME = "wallpaper";
    public static final String LIST_VIEW_WALLPAPER_BG_FILE_NAME = "list_wallpaper";
    public static final String TOOLBAR_BG_FILE_NAME = "toolbar";
    public static final String INCOMING_BUBBLE_FILE_NAME = "incoming_bubble";
    public static final String OUTGOING_BUBBLE_FILE_NAME = "outgoing_bubble";
    public static final String CREATE_ICON_FILE_NAME = "icon";
    public static final String AVATAR_FILE_NAME = "avatar";

    private Bitmap mWallpaperBitmap;
    private Bitmap mToolbarBitmap;
    private Bitmap mIncomingBitmap;
    private Bitmap mOutgoingBitmap;
    private Bitmap mCreateBitmap;

    //
    public boolean isThemeDownload(ThemeInfo info) {
        File wallpaper = new File(CommonUtils.getDirectory(THEME_BASE_PATH + info.name),
                WALLPAPER_BG_FILE_NAME);
        if (!wallpaper.exists()) {
            return false;
        }

        File toolbar = new File(CommonUtils.getDirectory(THEME_BASE_PATH + info.name),
                TOOLBAR_BG_FILE_NAME);
        if (!toolbar.exists()) {
            return false;
        }

        if (!TextUtils.isEmpty(info.newConversationIconUrl)
                && !new File(CommonUtils.getDirectory(THEME_BASE_PATH + info.name),
                CREATE_ICON_FILE_NAME).exists()) {
            return false;
        }
        if (!new File(CommonUtils.getDirectory(THEME_BASE_PATH + info.name),
                INCOMING_BUBBLE_FILE_NAME).exists()) {
            return false;
        }

        if (!new File(CommonUtils.getDirectory(THEME_BASE_PATH + info.name),
                OUTGOING_BUBBLE_FILE_NAME).exists()) {
            return false;
        }

        if (!FontDownloadManager.isFontDownloaded(null)) {
            return false;
        }
        return true;
    }

    public Drawable getThemeWallpaperDrawable(String name) {
        if (mWallpaperBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), mWallpaperBitmap);
        }

        Bitmap bitmap = loadBitmap(new File(CommonUtils.getDirectory(THEME_BASE_PATH + name), WALLPAPER_BG_FILE_NAME));
        if (bitmap != null) {
            mWallpaperBitmap = bitmap;
            return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
        }
        return null;
    }

    public Drawable getThemeToolbarDrawable(String name) {
        if (mToolbarBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), mToolbarBitmap);
        }

        Bitmap bitmap = null;
        try {
            bitmap = loadBitmap(new File(CommonUtils.getDirectory(THEME_BASE_PATH + name), TOOLBAR_BG_FILE_NAME));
        } catch (Exception e) {

        }
        if (bitmap != null) {
            mToolbarBitmap = bitmap;
            return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
        }
        return null;
    }

    public void saveWallpaperDrawable(Bitmap bitmap, String themeName) {
        File file = new File(CommonUtils.getDirectory(THEME_BASE_PATH + themeName), WALLPAPER_BG_FILE_NAME);
        saveBitmap(bitmap, file);
    }

    public Drawable getIncomingBubbleDrawable(String name) {
        if (mIncomingBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), mIncomingBitmap);
        }

        Bitmap bitmap = null;
        try {
            bitmap = loadBitmap(new File(CommonUtils.getDirectory(THEME_BASE_PATH + name), INCOMING_BUBBLE_FILE_NAME));
        } catch (Exception x) {

        }
        if (bitmap != null) {
            mIncomingBitmap = bitmap;
            return getIncomingBubbleDrawable(name);
        }
        return null;
    }

    public Drawable getOutgoingBubbleDrawable(String name) {
        if (mOutgoingBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), mOutgoingBitmap);
        }

        Bitmap bitmap = null;
        try {
            bitmap = loadBitmap(new File(CommonUtils.getDirectory(THEME_BASE_PATH + name), OUTGOING_BUBBLE_FILE_NAME));
        } catch (Exception x) {

        }
        if (bitmap != null) {
            mOutgoingBitmap = bitmap;
            return getOutgoingBubbleDrawable(name);
        }
        return null;
    }

    private void saveBitmap(Bitmap bitmap, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap loadBitmap(File file) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Exception e) {

        }
        return bitmap;
    }
}
