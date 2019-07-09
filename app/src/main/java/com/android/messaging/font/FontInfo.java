package com.android.messaging.font;

import java.util.ArrayList;
import java.util.List;

public class FontInfo {
    private String mFontName;
    private List<String> mWeightList = new ArrayList<>();
    private boolean mIsLocalFont;

    public FontInfo(String fontName, List<String> styleList, boolean isLocal) {
        this.mFontName = fontName;
        this.mWeightList = new ArrayList<>(styleList);
        this.mIsLocalFont = isLocal;
    }

    public String getFontName() {
        return mFontName;
    }

    public List<String> getFontWeights() {
        return mWeightList;
    }

    public boolean isFontDownloaded() {
        if (!mIsLocalFont) {
            return FontDownloadManager.isFontDownloaded(this);
        } else {
            return true;
        }
    }

    public boolean isLocalFont() {
        return mIsLocalFont;
    }
}
