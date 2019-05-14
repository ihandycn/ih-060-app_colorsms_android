package com.android.messaging.font;

import com.superapps.util.Preferences;

public class FontStyleManager {
    private static final String PREF_KEY_MESSAGE_FONT_SCALE_LEVEL = "message_font_scale";
    private static final String PREF_KEY_MESSAGE_FONT_TYPE = "message_font_family";
    private static FontStyleManager mInstance;
    private String mFontName;
    private int mFontLevel;

    public static FontStyleManager getInstance() {
        if (mInstance == null) {
            synchronized (FontStyleManager.class) {
                if (mInstance == null) {
                    mInstance = new FontStyleManager();
                }
            }
        }
        return mInstance;
    }

    private FontStyleManager(){
        mFontName = Preferences.getDefault().getString(PREF_KEY_MESSAGE_FONT_TYPE, FontUtils.MESSAGE_FONT_FAMILY_DEFAULT_VALUE);
        mFontLevel = Preferences.getDefault().getInt(PREF_KEY_MESSAGE_FONT_SCALE_LEVEL, 2);
    }

    public void setFontScaleLevel(int level) {
        Preferences.getDefault().putInt(PREF_KEY_MESSAGE_FONT_SCALE_LEVEL, level);
        mFontLevel = level;
    }

    public int getFontScaleLevel() {
        return mFontLevel;
    }

    public float getFontScale() {
        return getScaleByLevel(mFontLevel);
    }

    public void setFontFamily(String typeName) {
        Preferences.getDefault().putString(PREF_KEY_MESSAGE_FONT_TYPE, typeName);
        mFontName = typeName;
    }

    public String getFontFamily() {
        return mFontName;
    }

    public static float getScaleByLevel(int level) {
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
}
