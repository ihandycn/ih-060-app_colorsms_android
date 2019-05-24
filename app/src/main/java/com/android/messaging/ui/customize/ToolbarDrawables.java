package com.android.messaging.ui.customize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeManager;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ToolbarDrawables {
    public static Bitmap sToolbarBitmap;

    public static void applyToolbarBg() {
        getToolbarBgBitmap();
    }

    public static Drawable getToolbarBg() {
        if (sToolbarBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), sToolbarBitmap);
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();
        if (TextUtils.isEmpty(info.toolbarBgUrl)) {
            return null;
        }

        if (!info.isInLocalFolder()) {
            if (info.mIsLocalTheme) {
                try {
                    InputStream ims = HSApplication.getContext().getAssets().open("themes/"
                            + info.mThemeKey + "/" + info.toolbarBgUrl);
                    sToolbarBitmap = BitmapFactory.decodeStream(ims);
                    return new BitmapDrawable(HSApplication.getContext().getResources(), sToolbarBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + info.mThemeKey),
                    ThemeManager.TOOLBAR_BG_FILE_NAME);
            if (file.exists()) {
                try {
                    sToolbarBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    return new BitmapDrawable(HSApplication.getContext().getResources(), sToolbarBitmap);
                } catch (Exception e) {

                }
            }
        }

        return null;
    }

    public static Bitmap getToolbarBgBitmap() {
        ThemeInfo info = ThemeUtils.getCurrentTheme();
        if (TextUtils.isEmpty(info.toolbarBgUrl)) {
            return null;
        }

        if (sToolbarBitmap != null) {
            return sToolbarBitmap;
        }

        if (info.mIsLocalTheme) {
            try {
                InputStream ims = HSApplication.getContext().getAssets().open("themes/" + info.mThemeKey + "/"
                        + info.toolbarBgUrl);
                sToolbarBitmap = BitmapFactory.decodeStream(ims);
                return sToolbarBitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + info.mThemeKey),
                    ThemeManager.TOOLBAR_BG_FILE_NAME);
            if (file.exists()) {
                try {
                    sToolbarBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    return sToolbarBitmap;
                } catch (Exception e) {

                }
            }
        }

        return null;
    }
}
