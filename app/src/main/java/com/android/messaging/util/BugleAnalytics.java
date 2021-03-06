package com.android.messaging.util;

import com.android.messaging.BuildConfig;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSGdprConsent;
import com.ihs.commons.utils.HSLog;

import java.util.HashMap;
import java.util.Map;

/**
 * For v1.4.5 (78) release: to cut down number of events reported to Flurry, we log most events only to Fabric.
 * Give {@code true} for {@code alsoLogToFlurry} to log an event also to Flurry.
 */
public class BugleAnalytics {

    private static final String TAG = BugleAnalytics.class.getSimpleName();

    public static void logEvent(String eventID) {
        logEvent(eventID, false);
    }

    public static void logEvent(String eventID, boolean alsoLogToFlurry) {
        logEvent(eventID, alsoLogToFlurry, (HashMap<String, String>) null);
    }

    public static void logEvent(String eventID, String... vars) {
        logEvent(eventID, false, vars);
    }

    public static void logEvent(String eventID, boolean alsoLogToFlurry, String... vars) {
        HashMap<String, String> eventValue = new HashMap<>();
        if (null != vars) {
            int length = vars.length;
            if (length % 2 != 0) {
                --length;
            }

            String key;
            String value;
            int i = 0;

            while (i < length) {
                key = vars[i++];
                value = vars[i++];
                eventValue.put(key, value);
            }
        }

        logEvent(eventID, alsoLogToFlurry, eventValue);
    }

    public static void logEvent(final String eventID, final Map<String, String> eventValues) {
        logEvent(eventID, false, eventValues);
    }

    public static void logEvent(final String eventID, boolean alsoLogToFlurry,  Map<String, String> eventValues) {
        if (HSGdprConsent.getConsentState() != HSGdprConsent.ConsentState.ACCEPTED) {
            return;
        }

        try {
            CustomEvent event = new CustomEvent(eventID);

            if (eventValues != null) {
                for (Map.Entry<String, String> entry : eventValues.entrySet()) {
                    event.putCustomAttribute(entry.getKey(), entry.getValue());
                }
            }

            if (FabricUtils.isFabricInited()) {
                Answers.getInstance().logCustom(event);
            } else {
                FabricUtils.queueEvent(event);
            }

            if (alsoLogToFlurry) {
                if (eventValues == null) {
                    eventValues = new HashMap<>(1);
                }
                HSAnalytics.logEvent(eventID, eventValues);
            }
            onLogEvent(eventID, alsoLogToFlurry, eventValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void onLogEvent(String eventID, boolean alsoLogToFlurry, Map<String, String> eventValues) {
        if (BuildConfig.DEBUG) {
            String eventDescription = getEventInfoDescription(eventID, alsoLogToFlurry, eventValues);
            HSLog.d(TAG, eventDescription);
        }
    }

    private static String getEventInfoDescription(String eventID, boolean alsoLogToFlurry, Map<String, String> eventValues) {
        String scope = (alsoLogToFlurry ? "F" : " ") + "|A";
        StringBuilder values = new StringBuilder();
        if (eventValues != null) {
            for (Map.Entry<String, String> valueEntry : eventValues.entrySet()) {
                values.append(valueEntry).append(", ");
            }
        }

        if (values.length() > 0) {
            values = new StringBuilder(": " + values.substring(0, values.length() - 2)); // At ": " at front and remove ", " in the end
        }
        return "(" + scope + ") " + eventID + values;
    }
}
