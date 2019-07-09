package com.android.messaging.ui.customize.theme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CreateIconDrawable {
    static Bitmap sCreateIconBitmap;

    public static Drawable getCreateIconDrawable() {
        if (sCreateIconBitmap != null) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), sCreateIconBitmap);
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();

        if (TextUtils.isEmpty(info.newConversationIconUrl)) {
            return null;
        }

        File file = new File(CommonUtils.getDirectory(
                ThemeBubbleDrawables.THEME_BASE_PATH + info.mThemeKey),
                ThemeBubbleDrawables.CREATE_ICON_FILE_NAME);

        if (info.mIsLocalTheme) {
            try {
                String assetFileName = "themes/" + info.mThemeKey + "/" + info.newConversationIconUrl;
                InputStream ims = HSApplication.getContext().getAssets().open(assetFileName);
                sCreateIconBitmap = BitmapFactory.decodeStream(ims);
                if (ims != null) {
                    ims.close();
                }
                ThemeDownloadManager.getInstance().copyAssetFileAsync(file, assetFileName);
                return new BitmapDrawable(HSApplication.getContext().getResources(), sCreateIconBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (file.exists()) {
            try {
                sCreateIconBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                return new BitmapDrawable(HSApplication.getContext().getResources(), sCreateIconBitmap);
            } catch (Exception ignored) {

            }
        }

        return null;
    }
}
