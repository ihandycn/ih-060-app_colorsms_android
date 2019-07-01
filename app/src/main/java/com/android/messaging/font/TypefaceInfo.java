package com.android.messaging.font;

import android.graphics.Typeface;

public class TypefaceInfo {
    private Typeface mTypeface;
    private float mDefaultSizeScale;

    public TypefaceInfo(Typeface typeface, float defaultSizeRatio) {
        mTypeface = typeface;
        mDefaultSizeScale = defaultSizeRatio;
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public float getDefaultSizeScale() {
        return mDefaultSizeScale;
    }
}
