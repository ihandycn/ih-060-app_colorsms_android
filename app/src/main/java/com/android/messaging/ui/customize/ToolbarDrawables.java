package com.android.messaging.ui.customize;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.Factory;
import com.ihs.app.framework.HSApplication;

import java.io.IOException;
import java.io.InputStream;

public class ToolbarDrawables {

    private static final String PREF_KEY_CUSTOMIZE_TOOLBAR_BACKGROUND = "pref_key_customize_toolbar_background";

    public static void applyToolbarBg(String url) {
        Factory.get().getCustomizePrefs().putString(PREF_KEY_CUSTOMIZE_TOOLBAR_BACKGROUND, url);
    }

    public static Drawable getToolbarBg() {
        String url = Factory.get().getCustomizePrefs().getString(PREF_KEY_CUSTOMIZE_TOOLBAR_BACKGROUND, "");
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        if (url.startsWith("assets://")) {
            try {
                InputStream ims = HSApplication.getContext().getAssets().open(url.replace("assets://", ""));
                return Drawable.createFromStream(ims, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
