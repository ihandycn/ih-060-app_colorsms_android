package com.android.messaging.font;

import android.graphics.Typeface;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.R;
import com.superapps.util.Fonts;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FontUtils {
    static final String LOCAL_DIRECTORY = "fonts" + File.separator;
    public static final String MESSAGE_FONT_FAMILY_DEFAULT_VALUE = "Default";

    public static final String[] sSupportGoogleFonts = {
            FontUtils.MESSAGE_FONT_FAMILY_DEFAULT_VALUE,
            "Krub", "Mali", "Averia_Libre",
            "Merienda", "Sarpanch", "K2D", "El_Messiri"
    };

    private static Map<String, Typeface> sTypefaceMap = new HashMap<>();
    public static String sTypefaceName = FontStyleManager.getInstance().getFontFamily();

    public static final int THIN = 100;
    public static final int EXTRA_LIGHT = 200;
    public static final int LIGHT = 300;
    public static final int REGULAR = 400;

    public static final int MEDIUM = 500;

    public static final int SEMI_BOLD = 600;
    public static final int BOLD = 700;
    public static final int EXTRA_BOLD = 800;
    public static final int BLACK = 900;

    @IntDef({THIN, EXTRA_LIGHT, LIGHT,
            REGULAR, MEDIUM, SEMI_BOLD,
            BOLD, EXTRA_BOLD, BLACK})
    public @interface FontWeight {
    }

    public static void onFontTypefaceChanged() {
        sTypefaceName = FontStyleManager.getInstance().getFontFamily();
        sTypefaceMap.clear();
    }

    public static Typeface getTypefaceByName(@NonNull String name, @FontWeight int weight) {
        String weightName;
        if (weight <= REGULAR) {
            weightName = "Regular";
        } else if (weight == MEDIUM) {
            weightName = "Medium";
        } else {
            weightName = "Semibold";
        }
        String fullName = name + "_" + weightName;
        if (sTypefaceMap.containsKey(fullName)) {
            return sTypefaceMap.get(fullName);
        }

        Typeface tp = loadTypeface(name, weightName);
        if (tp != null) {
            sTypefaceMap.put(fullName, tp);
        }
        return tp;
    }

    public static Typeface getTypeface(@FontWeight int weight) {
        if (TextUtils.isEmpty(sTypefaceName)) {
            sTypefaceName = "Default";
        }
        return getTypefaceByName(sTypefaceName, weight);
    }

    public static Typeface getTypeface() {
        return getTypeface(REGULAR);
    }

    public static Typeface getTypeface(int fontType, int fontStyle) {
        String typefaceName = sTypefaceName;
        String weight;
        if (fontType == R.string.custom_font_thin
                || fontType == R.string.custom_font_light
                || fontType == R.string.custom_font_regular
                || fontType == R.string.custom_font_regular_condensed) {
            weight = "Regular";
        } else if (fontType == R.string.custom_font_medium) {
            weight = "Medium";
        } else if (fontType == R.string.custom_font_semibold
                || fontType == R.string.custom_font_bold
                || fontType == R.string.custom_font_black) {
            weight = "Semibold";
        } else {
            //not custom type
            return Fonts.getTypeface(Fonts.Font.ofFontResId(fontType), fontStyle);
        }
        String fullName = typefaceName + "-" + weight;
        if (sTypefaceMap.containsKey(fullName)) {
            return sTypefaceMap.get(fullName);
        }

        Typeface tp = loadTypeface(typefaceName, weight);
        if (tp != null) {
            sTypefaceMap.put(fullName, tp);
        }
        return tp;
    }

    public static Typeface loadTypeface(String typefaceName, String weightName) {
        boolean isLocalFont = false;
        for (String s : sSupportGoogleFonts) {
            if (s.equals(typefaceName)) {
                isLocalFont = true;
                break;
            }
        }
        if (isLocalFont) {
            return getLocalTypeface(typefaceName, weightName);
        } else {
            return getRemoteTypeface(typefaceName, weightName);
        }
    }

    private static Typeface getLocalTypeface(String typefaceName, String weightName) {
        Typeface tp = null;
        try {
            tp = Typeface.createFromAsset(HSApplication.getContext().getAssets(), "fonts/"
                    + (MESSAGE_FONT_FAMILY_DEFAULT_VALUE.equals(typefaceName) ? "Custom" : typefaceName)
                    + "-" + weightName + ".ttf");
        } catch (Exception e) {
            if ("Medium".equals(weightName)) {
                try {
                    tp = Typeface.createFromAsset(HSApplication.getContext().getAssets(), "fonts/"
                            + (MESSAGE_FONT_FAMILY_DEFAULT_VALUE.equals(typefaceName)
                            ? "Custom" : typefaceName)
                            + "-Semibold.ttf");
                } catch (Exception e1) {
                    HSLog.e("load Semibold font", "create font from asset failed");
                    return null;
                }
            } else {
                HSLog.e("load font", "create font from asset failed");
                return null;
            }
        }
        return tp;
    }

    private static Typeface getRemoteTypeface(String typefaceName, String weightName) {
        File file;
        switch (weightName) {
            case "Medium":
                file = new File(getDirectory(LOCAL_DIRECTORY + typefaceName) + File.separator + "Medium.ttf");
                if (file.exists()) {
                    break;
                }
            case "Semibold":
                file = new File(getDirectory(LOCAL_DIRECTORY + typefaceName) + File.separator + "SemiBold.ttf");
                if (file.exists()) {
                    break;
                }
            default:
                file = new File(getDirectory(LOCAL_DIRECTORY + typefaceName) + File.separator + "Regular.ttf");
        }

        Typeface tp = null;
        if (file.exists()) {
            try {
                tp = Typeface.createFromFile(file);
            } catch (Exception e) {
                HSLog.e("load font", "create font from file failed");
            }
        }
        return tp;
    }

    public static @Nullable
    File getDirectory(String dirPath) {
        File file = HSApplication.getContext().getFilesDir();
        String[] path = dirPath.split(File.separator);
        for (String dir : path) {
            file = new File(file, dir);
            if (!file.exists() && !file.mkdir()) {
                return null;
            }
        }
        return file;
    }
}
