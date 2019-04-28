package com.android.messaging.font;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FontDownloadManager {
    static final String LOCAL_DIRECTORY = "fonts" + File.separator;

    interface FontDownloadListener {
        void onDownloadSuccess();

        void onDownloadFailed();
    }

    public static List<FontInfo> getRemoteFonts() {
        List<FontInfo> list = new ArrayList<>();
        List<Map<String, ?>> fontList = (List<Map<String, ?>>) HSConfig.getList("Application", "Fonts", "FontList");
        for (Map item : fontList) {
            String fontName = (String) item.get("Name");
            List<String> styleList = (List<String>) item.get("Weight");
            FontInfo info = new FontInfo(FontInfo.REMOTE_FONT, fontName, styleList);
            list.add(info);
        }
        return list;
    }

    public static boolean isFontDownloaded(FontInfo font) {
        for (String style : font.getFontWeights()) {
            File file = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY + font.getFontName()), style + ".ttf");
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }

    public static void downloadFont(FontInfo font, FontDownloadListener listener) {
        if (isFontDownloaded(font)) {
            listener.onDownloadSuccess();
            return;
        } else {
            String basePath = getBaseRemoteUrl();
            WeakReference<FontDownloadListener> listenerWeakReference = new WeakReference<>(listener);
            if (font.getFontWeights() != null && font.getFontWeights().size() > 0) {
                downloadFontSync(basePath, font.getFontName(), font.getFontWeights(), listenerWeakReference);
                BugleAnalytics.logEvent("Customize_TextFont_Download");
            }
        }
    }


    public static void downloadFontSync(String basePath, String font, List<String> weights,
                                        WeakReference<FontDownloadListener> listenerWeakReference) {
        Threads.postOnThreadPoolExecutor(() -> {
            String weight = weights.remove(0);

            String regularUrl = basePath + "/" + font + "/" + weight + ".ttf";
            final HSHttpConnection connection = new HSHttpConnection(regularUrl, HttpRequest.Method.GET);
            File fontFile = new File(CommonUtils.getDirectory(LOCAL_DIRECTORY + font), weight + ".ttf");
            connection.setDownloadFile(fontFile);
            connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
                @Override
                public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                    if (hsHttpConnection.isSucceeded()) {
                        if (weights.size() > 0) {
                            downloadFontSync(basePath, font, weights, listenerWeakReference);
                        } else {
                            Threads.postOnMainThread(() -> {
                                if (listenerWeakReference.get() != null) {
                                    listenerWeakReference.get().onDownloadSuccess();
                                }
                            });
                            BugleAnalytics.logEvent("Customize_TextFont_DownloadSuccess");
                        }
                    } else {
                        Threads.postOnMainThread(() -> {
                            if (listenerWeakReference.get() != null) {
                                listenerWeakReference.get().onDownloadFailed();
                            }
                            Toasts.showToast(R.string.network_error);
                        });
                    }
                }

                @Override
                public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                    Threads.postOnMainThread(() -> {
                        if (listenerWeakReference.get() != null) {
                            listenerWeakReference.get().onDownloadFailed();
                        }
                        Toasts.showToast(R.string.network_error);
                    });
                }
            });
            connection.startSync();
        });
    }

    public static String getBaseRemoteUrl() {
        return HSConfig.getString("Application", "Fonts", "BasePath");
    }

    public static Drawable getDrawableByName(String fontName) {
        try {
            InputStream ims = HSApplication.getContext().getAssets().open("font_preview/font_preview_" + fontName + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(ims);
            return new BitmapDrawable(HSApplication.getContext().getResources(), bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
