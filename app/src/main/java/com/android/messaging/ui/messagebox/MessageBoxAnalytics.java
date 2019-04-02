package com.android.messaging.ui.messagebox;

import com.android.messaging.util.BugleAnalytics;

public class MessageBoxAnalytics {

    private static boolean sIsMultiConversation;

    static void setIsMultiConversation(boolean isMultiConversation) {
        sIsMultiConversation = isMultiConversation;
    }

   static String getConversationType() {
        return sIsMultiConversation ? "singlecontact" : "multicontact";
    }

    static void logEvent(String eventName) {
        BugleAnalytics.logEvent(eventName, true, "type", getConversationType());
    }
}
