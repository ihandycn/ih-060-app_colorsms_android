package com.android.messaging.ui.customize.theme;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ThemeBubbleDrawables {
    public static final String THEME_BASE_PATH = "theme" + File.separator;

    public static final String WALLPAPER_BG_FILE_NAME = "wallpaper";
    public static final String LIST_VIEW_WALLPAPER_BG_FILE_NAME = "list_wallpaper";
    public static final String TOOLBAR_BG_FILE_NAME = "toolbar";
    static final String INCOMING_BUBBLE_FILE_NAME = "incoming_bubble";
    static final String OUTGOING_BUBBLE_FILE_NAME = "outgoing_bubble";
    static final String INCOMING_SOLID_BUBBLE_FILE_NAME = "incoming_solid_bubble";
    static final String OUTGOING_SOLID_BUBBLE_FILE_NAME = "outgoing_solid_bubble";
    static final String CREATE_ICON_FILE_NAME = "icon";
    public static final String AVATAR_FILE_NAME = "avatar";
    public static final String SOLID_AVATAR_FILE_NAME = "solid_avatar";

    private NinePatchDrawable mIncomingDrawable;
    private NinePatchDrawable mOutgoingDrawable;
    private NinePatchDrawable mIncomingSolidDrawable;
    private NinePatchDrawable mOutgoingSolidDrawable;

    private static ThemeBubbleDrawables sInstance = new ThemeBubbleDrawables();

    public static ThemeBubbleDrawables getInstance() {
        return sInstance;
    }

    private ThemeBubbleDrawables() {

    }

    void clearCacheDrawable() {
        mIncomingDrawable = null;
        mOutgoingDrawable = null;
        mIncomingSolidDrawable = null;
        mOutgoingSolidDrawable = null;
    }

    public Drawable getIncomingBubbleDrawable(boolean hasCustomBackground) {
        if (hasCustomBackground) {
            Drawable drawable = getIncomingSolidBubbleDrawable();
            if (drawable != null) {
                return drawable;
            }
        }

        if (mIncomingDrawable != null) {
            return mIncomingDrawable;
        }

        ThemeInfo theme = ThemeUtils.getCurrentTheme();
        Bitmap bitmap = null;

        File localFile = new File(CommonUtils.getDirectory(
                ThemeBubbleDrawables.THEME_BASE_PATH + theme.mThemeKey),
                ThemeBubbleDrawables.INCOMING_BUBBLE_FILE_NAME);

        //from local file
        try {
            if (localFile.exists()) {
                bitmap = loadBitmap(localFile);
            }

            if (bitmap != null) {
                byte[] chunk = bitmap.getNinePatchChunk();
                Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
                mIncomingDrawable = new NinePatchDrawable(HSApplication.getContext().getResources(),
                        bitmap, chunk, rect, null);
                return mIncomingDrawable;
            }
        } catch (Exception ignored) {
        }

        //from asset
        if (theme.mIsLocalTheme) {
            String assetFileName = "themes" + File.separator+ theme.mThemeKey + File.separator + theme.bubbleIncomingUrl;
            try {
                InputStream ims = HSApplication.getContext().getAssets().open(assetFileName);
                bitmap = loadBitmap(ims);

                if (ims != null) {
                    ims.close();
                }

                if (bitmap != null) {
                    //copy file
                    //ThemeDownloadManager.getInstance().copyAssetFileAsync(localFile, assetFileName);
                    //load .9
                    byte[] chunk = bitmap.getNinePatchChunk();
                    Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
                    mIncomingDrawable = new NinePatchDrawable(HSApplication.getContext().getResources(),
                            bitmap, chunk, rect, null);
                    return mIncomingDrawable;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    public Drawable getOutgoingBubbleDrawable(boolean hasCustomBackground) {
        if (hasCustomBackground) {
            Drawable drawable = getOutgoingSolidBubbleDrawable();
            if (drawable != null) {
                return drawable;
            }
        }

        if (mOutgoingDrawable != null) {
            return mOutgoingDrawable;
        }

        ThemeInfo theme = ThemeUtils.getCurrentTheme();

        Bitmap bitmap = null;
        File localFile = new File(CommonUtils.getDirectory(
                ThemeBubbleDrawables.THEME_BASE_PATH + theme.mThemeKey),
                ThemeBubbleDrawables.OUTGOING_BUBBLE_FILE_NAME);

        try {
            if (localFile.exists()) {
                bitmap = loadBitmap(localFile);
            }

            if (bitmap != null) {
                byte[] chunk = bitmap.getNinePatchChunk();
                Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
                mOutgoingDrawable = new NinePatchDrawable(HSApplication.getContext().getResources(),
                        bitmap, chunk, rect, null);
                return mOutgoingDrawable;
            }
        } catch (Exception ignored) {
        }

        if (theme.mIsLocalTheme) {
            try {
                String assetFileName = "themes" + File.separator + theme.mThemeKey + File.separator + theme.bubbleOutgoingUrl;
                InputStream ims = HSApplication.getContext().getAssets().open(assetFileName);
                bitmap = loadBitmap(ims);
                if (ims != null) {
                    ims.close();
                }

                if (bitmap != null) {
                    //ThemeDownloadManager.getInstance().copyAssetFileAsync(localFile, assetFileName);

                    byte[] chunk = bitmap.getNinePatchChunk();
                    Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
                    mOutgoingDrawable = new NinePatchDrawable(HSApplication.getContext().getResources(),
                            bitmap, chunk, rect, null);
                    return mOutgoingDrawable;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Drawable getIncomingSolidBubbleDrawable() {
        if (mIncomingSolidDrawable != null) {
            return mIncomingSolidDrawable;
        }

        ThemeInfo theme = ThemeUtils.getCurrentTheme();
        if (TextUtils.isEmpty(theme.mSolidBubbleIncomingUrl)) {
            return null;
        }

        File file = new File(CommonUtils.getDirectory(THEME_BASE_PATH
                + theme.mThemeKey), INCOMING_SOLID_BUBBLE_FILE_NAME);
        Bitmap bitmap = null;

        //use local file
        try {
            if (file.exists()) {
                bitmap = loadBitmap(file);
            }

            if (bitmap != null) {
                byte[] chunk = bitmap.getNinePatchChunk();
                Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
                mIncomingSolidDrawable = new NinePatchDrawable(HSApplication.getContext().getResources(),
                        bitmap, chunk, rect, null);
                return mIncomingSolidDrawable;
            }
        } catch (Exception ignored) {
        }

        //use asset file and copy
        if (theme.mIsLocalTheme) {
            try {
                String assetFileName = "themes" + File.separator + theme.mThemeKey + File.separator + theme.mSolidBubbleIncomingUrl;
                InputStream ims = HSApplication.getContext().getAssets().open(assetFileName);
                bitmap = loadBitmap(ims);

                if (ims != null) {
                    ims.close();
                }

                if (bitmap != null) {
                    //ThemeDownloadManager.getInstance().copyAssetFileAsync(file, assetFileName);

                    byte[] chunk = bitmap.getNinePatchChunk();
                    Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
                    mIncomingSolidDrawable = new NinePatchDrawable(HSApplication.getContext().getResources(),
                            bitmap, chunk, rect, null);
                    return mIncomingSolidDrawable;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private Drawable getOutgoingSolidBubbleDrawable() {
        if (mOutgoingSolidDrawable != null) {
            return mOutgoingSolidDrawable;
        }

        ThemeInfo theme = ThemeUtils.getCurrentTheme();

        if (TextUtils.isEmpty(theme.mSolidBubbleOutgoingUrl)) {
            return null;
        }

        File file = new File(CommonUtils.getDirectory(THEME_BASE_PATH
                + theme.mThemeKey), OUTGOING_SOLID_BUBBLE_FILE_NAME);
        Bitmap bitmap = null;

        // use local file
        try {
            if (file.exists()) {
                bitmap = loadBitmap(file);
            }

            if (bitmap != null) {
                byte[] chunk = bitmap.getNinePatchChunk();
                Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
                mOutgoingSolidDrawable = new NinePatchDrawable(HSApplication.getContext().getResources(),
                        bitmap, chunk, rect, null);
                return mOutgoingSolidDrawable;
            }
        } catch (Exception ignored) {
        }

        //use asset file and copy
        if (theme.mIsLocalTheme) {
            try {
                String assetFileName = "themes" + File.separator + theme.mThemeKey + File.separator + theme.mSolidBubbleOutgoingUrl;
                InputStream ims;
                ims = HSApplication.getContext().getAssets().open(assetFileName);
                bitmap = loadBitmap(ims);
                if (ims != null) {
                    ims.close();
                }

                if (bitmap != null) {
                    //the file is in asset but not in local folder
                    //ThemeDownloadManager.getInstance().copyAssetFileAsync(file, assetFileName);

                    byte[] chunk = bitmap.getNinePatchChunk();
                    Rect rect = NinePatchChunk.deserialize(chunk).mPaddings;
                    mOutgoingSolidDrawable = new NinePatchDrawable(HSApplication.getContext().getResources(),
                            bitmap, chunk, rect, null);
                    return mOutgoingSolidDrawable;
                }
            } catch (Exception ignored) {
            }
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

    private Bitmap loadBitmap(InputStream in) {
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inScaled = true;
            opt.inDensity = DisplayMetrics.DENSITY_XXHIGH;
            opt.inTargetDensity = Resources.getSystem().getDisplayMetrics().densityDpi;
            return BitmapFactory.decodeStream(in, null, opt);
        } catch (Exception ignored) {
            return null;
        }
    }
}
