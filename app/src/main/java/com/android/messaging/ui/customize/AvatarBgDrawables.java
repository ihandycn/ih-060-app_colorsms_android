package com.android.messaging.ui.customize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeManager;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AvatarBgDrawables {
    public static Drawable sAvatarBg;

    public static Drawable getAvatarBg(boolean forceCreate) {
        if (sAvatarBg != null && !forceCreate) {
            return sAvatarBg;
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();

        if (info.mThemeKey.equals(ThemeUtils.DEFAULT_THEME_KEY)) {

        }

        if (!info.isInLocalFolder()) {
            if (info.mIsLocalTheme) {
                try {
                    InputStream ims = HSApplication.getContext().getAssets().open("themes/" + info.mThemeKey + "/"
                            + info.avatarUrl);
                    Bitmap bitmap = BitmapFactory.decodeStream(ims);
                    sAvatarBg = new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                    return sAvatarBg;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File file = new File(CommonUtils.getDirectory(
                    ThemeManager.THEME_BASE_PATH + info.mThemeKey),
                    ThemeManager.AVATAR_FILE_NAME);
            if (file.exists()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    sAvatarBg = new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                    return sAvatarBg;
                } catch (Exception e) {

                }
            }
        }

        return null;
    }
}
