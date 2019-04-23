package com.android.messaging.ui.customize.theme;

import android.support.annotation.DrawableRes;

import com.ihs.commons.config.HSConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThemeInfo {

    private static List<ThemeInfo> sThemeInfoList;

    public String name;

    public String previewUrl;

    public String themeColor;

    public String toolbarBgUrl;

    public String avatarUrl;

    public String listTitleColor;

    public String listSubtitleColor;

    public String listTimeColor;

    public String newConversationIconUrl;

    public String wallpaperUrl;

    public String listWallpaperUrl;

    public String bubbleIncomingUrl;

    public String bubbleOutgoingUrl;

    public String incomingBubbleBgColor;

    public String outgoingBubbleBgColor;

    public String incomingBubbleTextColor;

    public String outgoingBubbleTextColor;

    public String fontUrl;

    public String fontName;

    @DrawableRes
    public int drawableRes;

    public static ThemeInfo ofConfig(String name, Map<String, ?> themeConfig) {
        ThemeInfo themeInfo = new ThemeInfo();

        themeInfo.name = name;
        themeInfo.previewUrl = (String) themeConfig.get("PreviewUrl");
        themeInfo.themeColor = (String) themeConfig.get("ThemeColor");
        themeInfo.toolbarBgUrl = (String) themeConfig.get("ToolbarBgUrl");
        themeInfo.avatarUrl = (String) themeConfig.get("AvatarUrl");
        themeInfo.listTitleColor = (String) themeConfig.get("ListTitleColor");
        themeInfo.listSubtitleColor = (String) themeConfig.get("ListSubtitleColor");
        themeInfo.listTimeColor = (String) themeConfig.get("ListTimeColor");
        themeInfo.newConversationIconUrl = (String) themeConfig.get("NewConversationIconUrl");
        themeInfo.wallpaperUrl = (String) themeConfig.get("WallpaperUrl");
        themeInfo.listWallpaperUrl = (String) themeConfig.get("ListWallpaperUrl");
        themeInfo.bubbleIncomingUrl = (String) themeConfig.get("BubbleIncomingUrl");
        themeInfo.bubbleOutgoingUrl = (String) themeConfig.get("BubbleOutgoingUrl");
        themeInfo.incomingBubbleBgColor = (String) themeConfig.get("IncomingBubbleBgColor");
        themeInfo.outgoingBubbleBgColor = (String) themeConfig.get("OutgoingBubbleBgColor");
        themeInfo.incomingBubbleTextColor = (String) themeConfig.get("IncomingBubbleTextColor");
        themeInfo.outgoingBubbleTextColor = (String) themeConfig.get("OutgoingBubbleTextColor");
        themeInfo.fontUrl = (String) themeConfig.get("FontUrl");
        themeInfo.fontName = (String) themeConfig.get("FontName");

        return themeInfo;
    }

    public static ThemeInfo getThemeInfo(String themeName) {
        return ThemeInfo.ofConfig(themeName, HSConfig.getMap("Application", "Themes", themeName));
    }

    public static List<ThemeInfo> getAllThemes() {
        if (sThemeInfoList == null) {
            Map<String, ?> themeMap = HSConfig.getMap("Application", "Themes");
            sThemeInfoList = new ArrayList<>();
            Set<String> themeNames = themeMap.keySet();

            for (String themeName : themeNames) {
                ThemeInfo themeInfo = ThemeInfo.ofConfig(themeName, (Map<String, ?>) themeMap.get(themeName));
                sThemeInfoList.add(themeInfo);
            }
        }
        return sThemeInfoList;
    }
}
