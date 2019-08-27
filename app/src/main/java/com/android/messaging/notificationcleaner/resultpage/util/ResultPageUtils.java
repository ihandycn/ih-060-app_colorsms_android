package com.android.messaging.notificationcleaner.resultpage.util;

import com.android.messaging.notificationcleaner.resultpage.ResultContentType;
import com.android.messaging.notificationcleaner.resultpage.data.ResultConstants;
import com.android.messaging.util.BugleAnalytics;

import java.util.HashMap;
import java.util.Map;

public class ResultPageUtils {

    public static void logResultPageShown(ResultContentType type) {
        Map<String, String> params = new HashMap<>();
        switch (type) {
            case AD:
                params.put("Card", ResultConstants.AD);
                break;
            case OPTIMAL:
                params.put("Card", "OptimalPage");
                break;
        }
        BugleAnalytics.logEvent("ResultPage_Show", params);
    }

    public static void logViewEvent(ResultContentType type) {
        if (type == ResultContentType.AD) {
            BugleAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.AD);
        }
    }
}
