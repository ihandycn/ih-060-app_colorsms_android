package com.android.messaging.font;

import android.support.annotation.IntDef;

import java.util.ArrayList;
import java.util.List;

public class FontInfo {

    public static final int LOCAL_FONT = 1;
    public static final int REMOTE_FONT = 2;
    @IntDef ({LOCAL_FONT, REMOTE_FONT})
    public @interface FontType {
    }

    private int mType;
    private String mFontName;
    private List<String> mWeightList = new ArrayList<>();

    public FontInfo(@FontType int type, String fontName) {
        mType = type;
        this.mFontName = fontName;
    }

    public FontInfo(@FontType int type, String fontName, List<String> styleList) {
        mType = type;
        this.mFontName = fontName;
        this.mWeightList = new ArrayList<>(styleList);
    }

    public String getFontName() {
        return mFontName;
    }

    public List<String> getFontWeights() {
        return mWeightList;
    }

    public int getType() {
        return mType;
    }

    public boolean isFontDownloaded() {
        if (mType == LOCAL_FONT) {
            return true;
        }
        else {
            return FontDownloadManager.isFontDownloaded(this);
        }
    }
}
