package com.android.messaging.ui.customize.theme;

import com.android.messaging.util.VersionUtil;
import com.ihs.app.framework.HSApplication;

import java.util.HashMap;
import java.util.Map;

public class LocalThemeVersion {
    private static Map<String, Integer> sThemeVersionMap = new HashMap<>();

    static {
        sThemeVersionMap.put("default", 0);
        sThemeVersionMap.put("coolgraffiti", 68);
        sThemeVersionMap.put("cutegraffiti", 68);
        sThemeVersionMap.put("diamond", 68);
        sThemeVersionMap.put("neon", 68);
        sThemeVersionMap.put("raindrop", 68);
        sThemeVersionMap.put("redrose", 68);
        sThemeVersionMap.put("simplebusiness", 68);
        sThemeVersionMap.put("unicorn", 68);
    }

    public static int getNewestVersion(String themeName) {
        return sThemeVersionMap.containsKey(themeName)
                ? sThemeVersionMap.get(themeName)
                : HSApplication.getCurrentLaunchInfo().appVersionCode;
    }
}
