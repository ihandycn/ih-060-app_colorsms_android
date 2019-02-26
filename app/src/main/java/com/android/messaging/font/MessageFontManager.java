package com.android.messaging.font;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.util.BuglePrefs;

import org.qcode.fontchange.FontManager;
import org.qcode.fontchange.impl.QueryBuilder;

import java.lang.ref.WeakReference;

import static com.ihs.app.framework.HSApplication.getContext;

public class MessageFontManager {

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

    public static void downloadAndSetTypeface(TextView tv) {
        WeakReference<TextView> t = new WeakReference<>(tv);
        String familyName = BuglePrefs.getApplicationPrefs().getString(FontManager.MESSAGE_FONT_FAMILY, "");
        if (familyName.isEmpty()) {
            return;
        }
        int weight = 400;    // regular
        QueryBuilder queryBuilder = new QueryBuilder(familyName)
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


        FontsContractCompat.FontRequestCallback callback = new FontsContractCompat
                .FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                //textView.setTypeface(typeface);
                if (t != null) {
                    t.get().setTypeface(typeface);
                }
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                Toast.makeText(getContext(),
                        getContext().getString(com.iflytek.android_font_loader_lib.R.string.request_failed, reason), Toast.LENGTH_SHORT)
                        .show();
            }
        };
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
