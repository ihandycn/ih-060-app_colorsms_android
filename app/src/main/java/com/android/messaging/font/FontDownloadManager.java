package com.android.messaging.font;

import android.content.res.AssetManager;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontDownloadManager {
    public static final String LOCAL_DIRECTORY = "fonts" + File.separator;

    public interface FontDownloadListener {
        void onDownloadSuccess();

        void onDownloadFailed();

        void onDownloadUpdate(float rate);
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

    static List<FontInfo> getFontList() {
        List<FontInfo> list = new ArrayList<>();
        List<Map<String, ?>> fontList = (List<Map<String, ?>>) HSConfig.getList("Application", "Fonts", "FontList");
        for (Map item : fontList) {
            String fontName = (String) item.get("Name");
            List<String> styleList = (List<String>) item.get("Weight");
            boolean isLocal = (Boolean) item.get("IsLocalFont");
            FontInfo info = new FontInfo(fontName, styleList, isLocal);
            list.add(info);
        }
        return list;
    }

    public static FontInfo getFont(String fontName) {
        for (FontInfo info : getFontList()) {
            if (info.getFontName().equals(fontName)) {
                return info;
            }
        }

        return null;
    }

    public static boolean isFontDownloaded(FontInfo font) {
        if (FontUtils.MESSAGE_FONT_FAMILY_DEFAULT_VALUE.equalsIgnoreCase(font.getFontName()) || font.isLocalFont()) {
            return true;
        }

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
                downloadFontSync(basePath, font.getFontName(), font.getFontWeights(), font.getFontWeights().size());
                BugleAnalytics.logEvent("Customize_TextFont_Download");
            }
        }
    }


    private static void downloadFontSync(String basePath, String font, List<String> weights, int weightCount) {
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
                            downloadFontSync(basePath, font, weights, weightCount);
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

            float partRate = 1.0f / weightCount;
            float start = partRate * (weightCount - weights.size() - 1);
            connection.setDataReceivedListener((hsHttpConnection, bytes, l, l1) -> {
                if (sListeners.containsKey(font)) {
                    FontDownloadListener listener = sListeners.get(font);
                    if (listener != null) {
                        float rate = start + partRate * l / l1;
                        listener.onDownloadUpdate(rate);
                    }
                }
            });
            connection.startSync();
        });
    }

    public static void copyFontsFromAssetsAsync() {
        Threads.postOnThreadPoolExecutor( () ->{
            List<FontInfo> list = getFontList();
            for (FontInfo font : list) {
                if (font.isLocalFont() && !font.getFontName().equals(FontUtils.MESSAGE_FONT_FAMILY_DEFAULT_VALUE)) {
                    copyFont(font);
                }
            }
        });
    }

    public static void copyFont(FontInfo fontInfo) {
        boolean isInLocal = FontDownloadManager.isFontDownloaded(fontInfo);
        if (isInLocal) {
            return;
        }
        String folderName = fontInfo.getFontName();
        File fontFolder = new File(CommonUtils.getDirectory("fonts"), folderName);
        if (!fontFolder.exists()) {
            fontFolder.mkdirs();
        }
        AssetManager assetManager = HSApplication.getContext().getAssets();

        for (String weight : fontInfo.getFontWeights()) {
            String assetName = folderName + "/" + weight + ".ttf";
            File folderFile = new File(CommonUtils.getDirectory(
                    FontDownloadManager.LOCAL_DIRECTORY + folderName),
                    weight + ".ttf");
            try {
                InputStream in = assetManager.open("fonts/" + assetName);
                FileOutputStream out = new FileOutputStream(folderFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
