package com.android.messaging.ui.customize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.ui.customize.theme.ThemeDownloadManager;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeBubbleDrawables;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ToolbarDrawables {
    public static Bitmap sToolbarBitmap;

    public static Drawable getToolbarBg() {
        Bitmap bitmap = getToolbarBgBitmap();
        if (bitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), getToolbarBgBitmap());
        } else {
            return null;
        }
    }

    public static Bitmap getToolbarBgBitmap() {
        if (sToolbarBitmap != null) {
            return sToolbarBitmap;
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();
        if (TextUtils.isEmpty(info.toolbarBgUrl)) {
            return null;
        }

        File file = new File(CommonUtils.getDirectory(ThemeBubbleDrawables.THEME_BASE_PATH + info.mThemeKey),
                ThemeBubbleDrawables.TOOLBAR_BG_FILE_NAME);

        //load toolbar image resource from asset first,
        if (info.mIsLocalTheme) {
            try {
                String assetFileName = "themes/" + info.mThemeKey + "/" + info.toolbarBgUrl;
                InputStream ims = HSApplication.getContext().getAssets().open(assetFileName);
                sToolbarBitmap = BitmapFactory.decodeStream(ims);
                if (ims != null) {
                    ims.close();
                }
                ThemeDownloadManager.getInstance().copyAssetFileAsync(file, assetFileName);
                return sToolbarBitmap;
            } catch (IOException ignored) {

            }
        }

        if (file.exists()) {
            try {
                sToolbarBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                return sToolbarBitmap;
            } catch (Exception ignored) {

            }
        }

        return null;
    }
}
