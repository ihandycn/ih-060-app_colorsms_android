package com.android.messaging.ui.customize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.R;
import com.android.messaging.ui.customize.theme.ThemeDownloadManager;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeBubbleDrawables;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.UiUtils;
import com.ihs.app.framework.HSApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AvatarBgDrawables {
    public static Drawable sAvatarBg;
    public static Drawable sSolidAvatarBg;

    public static Drawable getAvatarBg(boolean forceCreate) {
        return getAvatarBg(forceCreate, false);
    }

    public static Drawable getAvatarBg(boolean forceCreate, boolean hasCustomBackground) {
        if (hasCustomBackground) {
            Drawable drawable = getsSolidAvatarBg(forceCreate);
            if (drawable != null) {
                return drawable;
            }
        }

        if (sAvatarBg != null && !forceCreate) {
            return sAvatarBg;
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();

        if (info.mThemeKey.equals(ThemeUtils.DEFAULT_THEME_KEY)) {
            if (UiUtils.isLongScreenDevice(HSApplication.getContext())) {
                sAvatarBg = HSApplication.getContext().getResources().getDrawable(R.drawable.default_theme_avatar_long_device_bg);
                sSolidAvatarBg = sAvatarBg;
                return sAvatarBg;
            }
        }

        // try to use asset file
        if (info.mIsLocalTheme) {
            try {
                InputStream ims = HSApplication.getContext().getAssets().open("themes/" + info.mThemeKey + "/"
                        + info.avatarUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(ims);
                sAvatarBg = new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                return sAvatarBg;
            } catch (IOException ignored) {
            }
        }

        //try to use local file
        File file = new File(CommonUtils.getDirectory(
                ThemeBubbleDrawables.THEME_BASE_PATH + info.mThemeKey),
                ThemeBubbleDrawables.AVATAR_FILE_NAME);

        if (file.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                sAvatarBg = new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                return sAvatarBg;
            } catch (Exception ignored) {

            }
        }

        //try to use default avatar
        if (UiUtils.isLongScreenDevice(HSApplication.getContext())) {
            sAvatarBg = HSApplication.getContext().getResources().getDrawable(R.drawable.default_theme_avatar_long_device_bg);
            sSolidAvatarBg = sAvatarBg;
            return sAvatarBg;
        }

        return null;
    }

    private static Drawable getsSolidAvatarBg(boolean forceCreate) {
        if (sSolidAvatarBg != null && !forceCreate) {
            return sSolidAvatarBg;
        }

        ThemeInfo info = ThemeUtils.getCurrentTheme();
        if (info.mThemeKey.equals(ThemeUtils.DEFAULT_THEME_KEY)) {
            return null;
        }

        if (TextUtils.isEmpty(info.mSolidAvatarUrl)) {
            return null;
        }

        File file = new File(CommonUtils.getDirectory(ThemeBubbleDrawables.THEME_BASE_PATH + info.mThemeKey),
                ThemeBubbleDrawables.SOLID_AVATAR_FILE_NAME);

        if (info.mIsLocalTheme) {
            try {
                String assetFileName = "themes/" + info.mThemeKey + "/" + info.mSolidAvatarUrl;
                InputStream ims = HSApplication.getContext().getAssets().open(assetFileName);
                Bitmap bitmap = BitmapFactory.decodeStream(ims);

                if (bitmap != null) {
                    sSolidAvatarBg = new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                    ThemeDownloadManager.getInstance().copyAssetFileAsync(file, assetFileName);
                    return sSolidAvatarBg;
                }
            } catch (Exception ignored) {
            }
        }

        if (file.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    sSolidAvatarBg = new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                    return sSolidAvatarBg;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
