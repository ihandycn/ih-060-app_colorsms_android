package com.android.messaging.ui.conversationlist;


public interface CustomizeGuide {

    void showGuideIfNeed(ConversationListActivity activity);

    boolean closeCustomizeGuide(boolean openDrawer);

    void logGuideShow();
}
