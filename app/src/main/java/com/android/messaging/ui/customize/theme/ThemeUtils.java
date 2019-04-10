package com.android.messaging.ui.customize.theme;

import android.graphics.Color;

import com.android.messaging.ui.customize.AvatarBgDrawables;
import com.android.messaging.ui.customize.BubbleDrawables;
import com.android.messaging.ui.customize.ConversationColors;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.customize.ToolbarDrawables;
import com.android.messaging.ui.customize.WallpaperDrawables;
import com.superapps.font.FontStyleManager;

public class ThemeUtils {

    public static void downloadTheme(ThemeInfo themeInfo) {

    }

    public static void applyTheme(ThemeInfo themeInfo) {
        PrimaryColors.changePrimaryColor(Color.parseColor(themeInfo.themeColor));

        ConversationColors.get().setBubbleBackgroundColor(true, Color.parseColor(themeInfo.incomingBubbleBgColor));
        ConversationColors.get().setBubbleBackgroundColor(false, Color.parseColor(themeInfo.outgoingBubbleBgColor));
        ConversationColors.get().setMessageTextColor(true, Color.parseColor(themeInfo.incomingBubbleTextColor));
        ConversationColors.get().setMessageTextColor(false, Color.parseColor(themeInfo.outgoingBubbleTextColor));
        ConversationColors.get().setListTitleColor(Color.parseColor(themeInfo.listTitleColor));
        ConversationColors.get().setListSubTitleColor(Color.parseColor(themeInfo.listSubtitleColor));
        ConversationColors.get().setListTimeColor(Color.parseColor(themeInfo.listTimeColor));

        ToolbarDrawables.applyToolbarBg(themeInfo.toolbarBgUrl);
        WallpaperDrawables.applyWallpaperBg(themeInfo.wallpaperUrl);
        AvatarBgDrawables.applyAvatarBg(themeInfo.avatarUrl);

        if (themeInfo.bubbleIncomingUrl.startsWith("assets://")) {

        } else {
            BubbleDrawables.setSelectedIndex(Integer.parseInt(themeInfo.bubbleIncomingUrl));
        }
        FontStyleManager.getInstance().setFontFamily(themeInfo.fontName);
    }
}
