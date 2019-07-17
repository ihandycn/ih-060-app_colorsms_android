package com.android.messaging.util;

import android.os.Bundle;
import android.text.TextUtils;

import com.android.messaging.BuildConfig;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BugleFirebaseAnalytics {
    private static final String TAG = BugleFirebaseAnalytics.class.getSimpleName();

    public static FirebaseAnalytics sFirebaseAnalytics;

    private static HashMap<String, List<String>> sDebugEventMap = null;

    static {
        sFirebaseAnalytics = FirebaseAnalytics.getInstance(HSApplication.getContext());
    }

    public static void logEvent(String eventID) {
        logEvent(eventID, (Map<String, String>) null);
    }

    public static void logEvent(String eventID, String... vars) {
        HashMap<String, String> eventValue = new HashMap<>(5);
        if (null != vars) {
            int length = vars.length;
            if (length % 2 != 0) {
                --length;
                if (BuildConfig.DEBUG) {
                    throw new AssertionError("Illegal event params in + " + eventID +
                            " please check your params key and values");
                }
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
        logEvent(eventID, eventValue);
    }

    public static void logEvent(final String eventID, final Map<String, String> eventValues) {
        assertEventIdOrKeyOrValueLengthNoMoreThan40(eventID);
        assertEventIdOrKeyStartLegal(eventID);
        assertEventIdValid(eventID);
        Bundle params = new Bundle();
        if (eventValues != null) {
            for (Map.Entry<String, String> entry : eventValues.entrySet()) {
                String key = entry.getKey();

                assertEventIdOrKeyOrValueLengthNoMoreThan40(key);
                assertEventIdOrKeyStartLegal(key);
                assertEventKeyNoMoreThan25(eventID, key);

                String value = entry.getValue();
                assertEventIdOrKeyOrValueLengthNoMoreThan40(value);

                params.putString(key, value);
            }
        }
        onLogEvent(eventID, eventValues);
        if (!BuildConfig.DEBUG) {
            sFirebaseAnalytics.logEvent(eventID, params);
        }
    }


    private static void onLogEvent(String eventID, Map<String, String> eventValues) {
        if (BuildConfig.DEBUG) {
            String eventDescription = getEventInfoDescription(eventID, eventValues);
            HSLog.d(TAG, eventDescription);
        }
    }

    private static String getEventInfoDescription(String eventID, Map<String, String> eventValues) {
        String scope = "Analytics_Firebase";
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

    private static void assertEventIdValid(String eventId) {
        if (BuildConfig.DEBUG && !FireBaseEventIdMap.getIsValidEventId(eventId)) {
            throw new AssertionError("Illegal eventId : + " + eventId +
                    " in firebase, please add your new event in FireBaseEventIdMap if needed");
        }
    }

    private static void assertEventIdOrKeyOrValueLengthNoMoreThan40(String eventIdOrKeyOrValue) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (TextUtils.isEmpty(eventIdOrKeyOrValue)) {
            throw new AssertionError("the eventIdOrKeyOrValue is null!!!");
        }

        if (eventIdOrKeyOrValue.length() > 40) {
            throw new AssertionError("The length of " + eventIdOrKeyOrValue + " already more than 40 which is illegal!!!!!");
        }
    }

    private static void assertEventIdOrKeyStartLegal(String eventIdOrKey) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (TextUtils.isEmpty(eventIdOrKey)) {
            throw new AssertionError("the eventIdOrKey is null!!!");
        }

        if (eventIdOrKey.startsWith("firebase_")
                || eventIdOrKey.startsWith("google_")
                || eventIdOrKey.startsWith("ga_")) {
            throw new AssertionError(eventIdOrKey + " is start with \"firebase_\" or \"google_\" or \"ga_\" which is illegal");
        }
    }

    private static void assertEventKeyNoMoreThan25(String eventId, String key) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        if (sDebugEventMap == null) {
            sDebugEventMap = new HashMap<>();
        }

        List<String> keyList = sDebugEventMap.get(eventId);
        if (keyList == null) {
            keyList = new ArrayList<>();
        }
        if (!keyList.contains(key)) {
            keyList.add(key);
            if (keyList.size() > 25) {
                throw new AssertionError("The parameters of event must not exceed 25，" +
                        "but the parameters of " + eventId + " has been more than 25!!! Please check it.");
            }

            sDebugEventMap.put(eventId, keyList);
        }
    }

    public static void logUserProperty(String key, String value) {
        if (!TextUtils.isEmpty(value) && value.length() > 36) {
            value = value.substring(0, 36);
        }
        sFirebaseAnalytics.setUserProperty(key, value);
    }
}
