package com.android.messaging.ui.customize.theme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CreateIconDrawable {
    public static Bitmap sCreateIconBitmap;

    public static void applyCreateIcon() {
        getCreateIconDrawable();
    }

    public static Drawable getCreateIconDrawable() {
        if (sCreateIconBitmap != null ) {
            return new BitmapDrawable(HSApplication.getContext().getResources(), sCreateIconBitmap);
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();

        if (!info.isInLocalFolder()) {
            if (info.mIsLocalTheme) {
                try {
                    InputStream ims = HSApplication.getContext().getAssets().open("themes/" + info.mThemeKey + "/"
                            + info.newConversationIconUrl);
                    Bitmap bitmap = BitmapFactory.decodeStream(ims);
                    sCreateIconBitmap = bitmap;
                    return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + info.mThemeKey),
                    ThemeManager.CREATE_ICON_FILE_NAME);
            if (file.exists()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    sCreateIconBitmap = bitmap;
                    return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                } catch (Exception e) {

                }
            }
        }

        return null;
    }
}
