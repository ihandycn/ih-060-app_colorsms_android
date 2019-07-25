package com.android.messaging.font;

import java.util.ArrayList;
import java.util.List;

public class FontInfo {
    private String mFontName;
    private List<String> mWeightList = new ArrayList<>();
    private boolean mIsLocalFont;

    FontInfo(String fontName, List<String> styleList, boolean isLocal) {
        this.mFontName = fontName;
        this.mWeightList = new ArrayList<>(styleList);
        this.mIsLocalFont = isLocal;
    }

    String getFontName() {
        return mFontName;
    }

    List<String> getFontWeights() {
        return mWeightList;
    }

    boolean isFontDownloaded() {
        if (!mIsLocalFont) {
            return FontDownloadManager.isFontDownloaded(this);
        } else {
            return true;
        }
    }

    boolean isLocalFont() {
        return mIsLocalFont;
    }
}
