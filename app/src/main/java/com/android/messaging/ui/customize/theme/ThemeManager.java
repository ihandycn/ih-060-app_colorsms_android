package com.android.messaging.ui.customize.theme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ThemeManager {
    public static final String THEME_BASE_PATH = "theme" + File.separator;

    public static final String WALLPAPER_BG_FILE_NAME = "wallpaper";
    public static final String LIST_VIEW_WALLPAPER_BG_FILE_NAME = "list_wallpaper";
    public static final String TOOLBAR_BG_FILE_NAME = "toolbar";
    static final String INCOMING_BUBBLE_FILE_NAME = "incoming_bubble";
    static final String OUTGOING_BUBBLE_FILE_NAME = "outgoing_bubble";
    static final String CREATE_ICON_FILE_NAME = "icon";
    public static final String AVATAR_FILE_NAME = "avatar";

    private Bitmap mIncomingBitmap;
    private Bitmap mOutgoingBitmap;

    private static ThemeManager sInstance = new ThemeManager();

    public static ThemeManager getInstance() {
        return sInstance;
    }

    private ThemeManager() {

    }

    void clearCacheDrawable() {
        mIncomingBitmap = null;
        mOutgoingBitmap = null;
    }

    public Drawable getIncomingBubbleDrawable() {
        ThemeInfo theme = ThemeUtils.getCurrentTheme();
        if (mIncomingBitmap != null) {
            byte[] chunk = mIncomingBitmap.getNinePatchChunk();
            Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
            return new NinePatchDrawable(HSApplication.getContext().getResources(),
                    mIncomingBitmap, chunk, rect, null);
        }

        Bitmap bitmap = null;
        try {
            if (!theme.isInLocalFolder()) {
                if (theme.mIsLocalTheme) {
                    InputStream ims = HSApplication.getContext().getAssets().open(
                            "themes/" + theme.mThemeKey + "/" + theme.bubbleIncomingUrl);
                    bitmap = BitmapFactory.decodeStream(ims);
                }
            } else {
                bitmap = loadBitmap(new File(CommonUtils.getDirectory(THEME_BASE_PATH
                        + theme.mThemeKey), INCOMING_BUBBLE_FILE_NAME));
            }
        } catch (Exception ignored) {

        }
        if (bitmap != null) {
            mIncomingBitmap = bitmap;
            return getIncomingBubbleDrawable();
        }
        return null;
    }

    public Drawable getOutgoingBubbleDrawable() {

        if (mOutgoingBitmap != null) {
            byte[] chunk = mOutgoingBitmap.getNinePatchChunk();
            Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;

            return new NinePatchDrawable(HSApplication.getContext().getResources(),
                    mOutgoingBitmap, chunk, rect, null);
        }

        ThemeInfo theme = ThemeUtils.getCurrentTheme();

        Bitmap bitmap = null;
        try {
            if (!theme.isInLocalFolder()) {
                if (theme.mIsLocalTheme) {
                    InputStream ims = HSApplication.getContext().getAssets().open(
                            "themes/" + theme.mThemeKey + "/" + theme.bubbleOutgoingUrl);
                    bitmap = BitmapFactory.decodeStream(ims);
                }
            } else {
                bitmap = loadBitmap(new File(CommonUtils.getDirectory(THEME_BASE_PATH
                        + theme.mThemeKey), OUTGOING_BUBBLE_FILE_NAME));
            }
        } catch (Exception ignored) {

        }
        if (bitmap != null) {
            mOutgoingBitmap = bitmap;
            return getOutgoingBubbleDrawable();
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
        } catch (Exception ignored) {

        }
        return bitmap;
    }
}
