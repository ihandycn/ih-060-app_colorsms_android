package com.android.messaging.font;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
import android.text.TextUtils;

import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;
import com.superapps.util.Preferences;
import com.superapps.view.TypefacedTextView;

import org.qcode.fontchange.FontManager;
import org.qcode.fontchange.impl.QueryBuilder;

import static com.ihs.app.framework.HSApplication.getContext;

public class MessageFontManager {
    //字体请求错误码
    public static final int RESULT_CODE_PROVIDER_NOT_FOUND = -1;
    public static final int RESULT_CODE_WRONG_CERTIFICATES = -2;
    public static final int FAIL_REASON_FONT_LOAD_ERROR = -3;
    public static final int RESULT_CODE_FONT_NOT_FOUND = 1;
    public static final int RESULT_CODE_FONT_UNAVAILABLE = 2;
    public static final int RESULT_CODE_MALFORMED_QUERY = 3;

    //字重
    public static final int FONT_WEIGHT_THIN = 100;
    public static final int FONT_WEIGHT_EXTRA_LIGHT = 200;
    public static final int FONT_WEIGHT_LIGHT = 300;
    public static final int FONT_WEIGHT_REGULAR = 400;
    public static final int FONT_WEIGHT_MEDIUM = 500;
    public static final int FONT_WEIGHT_SEMI_BOLD = 600;
    public static final int FONT_WEIGHT_BOLD = 700;
    public static final int FONT_WEIGHT_EXTRA_BOLD = 800;
    public static final int FONT_WEIGHT_BLACK = 900;

    public static void setFontScale(int level) {
        BuglePrefs.getApplicationPrefs().putInt(FontManager.MESSAGE_FONT_SCALE, level);
    }

    public static float getFontScale() {
        int level = BuglePrefs.getApplicationPrefs().getInt(FontManager.MESSAGE_FONT_SCALE, 2);
        return getScaleFromLevel(level);
    }

    public static float getFontScaleByLevel(int level) {
        return getScaleFromLevel(level);
    }

    private static float getScaleFromLevel(int level) {
        float scale = 1;
        switch (level) {
            case 0:
                scale = 0.72f;
                break;
            case 1:
                scale = 0.85f;
                break;
            case 2:
                // normal
                scale = 1;
                break;
            case 3:
                scale = 1.15f;
                break;
            case 4:
                scale = 1.32f;
                break;
            case 5:
                scale = 1.52f;
                break;
        }
        return scale;
    }

    public static void loadTypeface(int weight, @NonNull FontsContractCompat.FontRequestCallback callback) {
        String fontName = Preferences.getDefault().getString(TypefacedTextView.MESSAGE_FONT_FAMILY, "Default");
        if (TextUtils.isEmpty(fontName) || fontName.equals("Default")) {
            callback.onTypefaceRetrieved(null);
        } else if (fontName.equals("System")) {
            fontName = "Roboto";
        }
        loadTypeface(fontName, weight, callback);
    }

    public static void loadTypeface(String fontName, int weight, @NonNull FontsContractCompat.FontRequestCallback callback) {
        if (TextUtils.isEmpty(fontName)) {
            return;
        }

        QueryBuilder queryBuilder = new QueryBuilder(fontName)
                .withWidth(100f)
                .withWeight(weight)
                .withItalic(0.0f)
                .withBestEffort(true);
        final String query = queryBuilder.build();

        FontRequest request = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                query,
                R.array.com_google_android_gms_fonts_certs);

        FontsContractCompat
                .requestFont(getContext(), request, callback,
                        getHandlerThreadHandler());
    }

    private static Handler getHandlerThreadHandler() {
        HandlerThread handlerThread = new HandlerThread("fonts");
        handlerThread.start();
        return new Handler(handlerThread.getLooper());
    }
}
