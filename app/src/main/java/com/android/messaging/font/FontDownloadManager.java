package com.android.messaging.font;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.android.messaging.R;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.superapps.util.Networks;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontDownloadManager {
    static final String LOCAL_DIRECTORY = "fonts" + File.separator;

    interface FontDownloadListener {
        void onDownloadSuccess();

        void onDownloadFailed();
    }

    private static Map<String, FontDownloadListener> sListeners = new HashMap<>();

    static void removeListener(String fontName) {
        if (TextUtils.isEmpty(fontName)) {
            return;
        }
        synchronized (FontDownloadManager.class) {
            if (sListeners.containsKey(fontName)) {
                sListeners.remove(fontName);
            }
        }
    }

    static List<FontInfo> getRemoteFonts() {
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

    static void downloadFont(FontInfo font, FontDownloadListener listener) {
        if (isFontDownloaded(font)) {
            listener.onDownloadSuccess();
        } else {
            if (!Networks.isNetworkAvailable(-1)) {
                listener.onDownloadFailed();
                Toasts.showToast(R.string.sms_network_error);
                return;
            }
            synchronized (FontDownloadManager.class) {
                if (sListeners.containsKey(font.getFontName())) {
                    sListeners.remove(font.getFontName());
                }
                sListeners.put(font.getFontName(), listener);
            }
            String basePath = getBaseRemoteUrl();
            if (font.getFontWeights() != null && font.getFontWeights().size() > 0) {
                downloadFontSync(basePath, font.getFontName(), font.getFontWeights());
                BugleAnalytics.logEvent("Customize_TextFont_Download");
            }
        }
    }


    private static void downloadFontSync(String basePath, String font, List<String> weights) {
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
                            downloadFontSync(basePath, font, weights);
                        } else {
                            Threads.postOnMainThread(() -> {
                                synchronized (FontDownloadManager.class) {
                                    if (sListeners.containsKey(font)) {
                                        FontDownloadListener listener = sListeners.get(font);
                                        if (listener != null) {
                                            listener.onDownloadSuccess();
                                        }
                                    }
                                }
                            });
                            BugleAnalytics.logEvent("Customize_TextFont_DownloadSuccess");
                        }
                    } else {
                        Threads.postOnMainThread(() -> {
                            synchronized (FontDownloadManager.class) {
                                if (sListeners.containsKey(font)) {
                                    FontDownloadListener listener = sListeners.get(font);
                                    if (listener != null) {
                                        listener.onDownloadFailed();
                                    }
                                }
                            }
                            Toasts.showToast(R.string.sms_network_error);
                        });
                    }
                }

                @Override
                public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                    Threads.postOnMainThread(() -> {
                        synchronized (FontDownloadManager.class) {
                            if (sListeners.containsKey(font)) {
                                FontDownloadListener listener = sListeners.get(font);
                                if (listener != null) {
                                    listener.onDownloadFailed();
                                }
                            }
                        }
                        Toasts.showToast(R.string.sms_network_error);
                    });
                }
            });
            connection.startSync();
        });
    }

    private static String getBaseRemoteUrl() {
        return HSConfig.getString("Application", "Fonts", "BasePath");
    }

    static Drawable getDrawableByName(String fontName) {
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
