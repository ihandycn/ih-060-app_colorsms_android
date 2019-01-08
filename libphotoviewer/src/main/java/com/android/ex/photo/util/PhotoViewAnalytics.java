package com.android.ex.photo.util;

public class PhotoViewAnalytics {

    private static AnalyticsListener sAnalyticListener = null;

    public static void initAnalytics(AnalyticsListener analyticsListener) {
        sAnalyticListener = analyticsListener;
    }

    public static void logEvent(String eventID, boolean alsoLogToFlurry) {
        if (sAnalyticListener != null) {
            sAnalyticListener.logEvent(eventID, alsoLogToFlurry);
        }
    }

    public interface AnalyticsListener {
        void logEvent(String eventID, boolean alsoLogToFlurry);
    }
}
