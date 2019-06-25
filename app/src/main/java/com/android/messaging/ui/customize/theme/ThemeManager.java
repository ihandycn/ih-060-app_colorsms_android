package com.android.messaging.ui.customize.theme;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.DisplayMetrics;

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

    private NinePatchDrawable mIncomingDrawable;
    private NinePatchDrawable mOutgoingDrawable;

    private static ThemeManager sInstance = new ThemeManager();

    public static ThemeManager getInstance() {
        return sInstance;
    }

    private ThemeManager() {

    }

    void clearCacheDrawable() {
        mIncomingDrawable = null;
        mOutgoingDrawable = null;
    }

    public Drawable getIncomingBubbleDrawable() {
        if (mIncomingDrawable != null) {
            return mIncomingDrawable;
        }

        ThemeInfo theme = ThemeUtils.getCurrentTheme();
        Bitmap bitmap = null;
        try {
            if (!theme.isInLocalFolder()) {
                if (theme.mIsLocalTheme) {
                    InputStream ims = HSApplication.getContext().getAssets().open(
                            "themes/" + theme.mThemeKey + "/" + theme.bubbleIncomingUrl);
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inScaled = true;
                    opt.inDensity = DisplayMetrics.DENSITY_XXHIGH;
                    opt.inTargetDensity = Resources.getSystem().getDisplayMetrics().densityDpi;
                    bitmap = BitmapFactory.decodeStream(ims, null, opt);
                }
            } else {
                bitmap = loadBitmap(new File(CommonUtils.getDirectory(THEME_BASE_PATH
                        + theme.mThemeKey), INCOMING_BUBBLE_FILE_NAME));
            }
        } catch (Exception ignored) {
        }

        if (bitmap != null) {
            byte[] chunk = bitmap.getNinePatchChunk();
            Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
            mIncomingDrawable = new NinePatchDrawable(HSApplication.getContext().getResources(),
                    bitmap, chunk, rect, null);
            return mIncomingDrawable;
        }
        return null;
    }

    public Drawable getOutgoingBubbleDrawable() {
        if (mOutgoingDrawable != null) {
            return mOutgoingDrawable;
        }

        ThemeInfo theme = ThemeUtils.getCurrentTheme();

        Bitmap bitmap = null;
        try {
            if (!theme.isInLocalFolder()) {
                if (theme.mIsLocalTheme) {
                    InputStream ims = HSApplication.getContext().getAssets().open(
                            "themes/" + theme.mThemeKey + "/" + theme.bubbleOutgoingUrl);
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inScaled = true;
                    opt.inDensity = DisplayMetrics.DENSITY_XXHIGH;
                    opt.inTargetDensity = Resources.getSystem().getDisplayMetrics().densityDpi;
                    bitmap = BitmapFactory.decodeStream(ims, null, opt);
                }
            } else {
                bitmap = loadBitmap(new File(CommonUtils.getDirectory(THEME_BASE_PATH
                        + theme.mThemeKey), OUTGOING_BUBBLE_FILE_NAME));
            }
        } catch (Exception ignored) {

        }
        if (bitmap != null) {
            byte[] chunk = bitmap.getNinePatchChunk();
            Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
            mOutgoingDrawable = new NinePatchDrawable(HSApplication.getContext().getResources(),
                    bitmap, chunk, rect, null);
            return mOutgoingDrawable;
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
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inScaled = true;
            opt.inDensity = DisplayMetrics.DENSITY_XXHIGH;
            opt.inTargetDensity = Resources.getSystem().getDisplayMetrics().densityDpi;
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opt);
        } catch (Exception ignored) {

        }
        return bitmap;
    }
}
