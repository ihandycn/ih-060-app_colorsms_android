package com.android.messaging.ui.customize;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.ihs.app.framework.HSApplication;

import java.io.IOException;
import java.io.InputStream;

public class AvatarBgDrawables {

    private static final String PREF_KEY_CUSTOMIZE_AVATARBG_BACKGROUND = "pref_key_customize_avatarbg_background";
    public static Drawable sAvatarBg;

    public static void applyAvatarBg(String url) {
        Factory.get().getCustomizePrefs().putString(PREF_KEY_CUSTOMIZE_AVATARBG_BACKGROUND, url);
    }

    public static Drawable getAvatarBg() {
        String url = Factory.get().getCustomizePrefs().getString(PREF_KEY_CUSTOMIZE_AVATARBG_BACKGROUND, "");
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        if (sAvatarBg != null) {
            return sAvatarBg;
        }

        if (url.startsWith("assets://")) {
            try {
                InputStream ims = HSApplication.getContext().getAssets().open(url.replace("assets://", ""));
                Bitmap bitmap = BitmapFactory.decodeStream(ims);
                sAvatarBg = new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
                return sAvatarBg;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
