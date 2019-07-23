package com.android.messaging.ui.conversationlist;

import com.android.messaging.BuildConfig;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

class NavigationViewGuideTest {

    private static final String TOPIC_ID = "topic-771b35ehc";

    static String getDefaultType() {
        return AutopilotConfig.getStringToTestNow(TOPIC_ID, "guide_type", "default");
    }

    static void logGuideShow() {
        makeSureInTest();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "guide_show");
    }

    static void logNavigationViewShow() {
        makeSureInTest();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "menu_show");
    }

    static void logMenuThemeClick() {
        makeSureInTest();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "menu_theme_click");
    }

    static void logMenuFontClick() {
        makeSureInTest();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "menu_font_click");
    }

    static void logMenuBubbleClick() {
        makeSureInTest();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "menu_bubble_click");
    }

    static void logMenuBackgroundClick() {
        makeSureInTest();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "menu_chatbackground_click");
    }

    static void logMenuChatListClick() {
        makeSureInTest();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "menu_chat_list_click");
    }

    static void logHomePageShow() {
        AutopilotEvent.logTopicEvent(TOPIC_ID, "homepage_show");
    }

    private static void makeSureInTest() {
        if (BuildConfig.DEBUG) {
            AutopilotConfig.getStringToTestNow(TOPIC_ID, "guide_type", "default");
        }
    }
}
