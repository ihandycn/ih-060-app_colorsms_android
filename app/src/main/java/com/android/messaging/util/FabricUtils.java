package com.android.messaging.util;

import com.android.messaging.BugleApplication;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.core.CrashlyticsCore;
import com.superapps.debug.CrashlyticsLog;

import java.util.ArrayList;
import java.util.List;

public class FabricUtils {
    private static List<CustomEvent> sEventList = new ArrayList<>();

    public static boolean isFabricInited() {
        return BugleApplication.isFabricInited();
    }

    public static void queueEvent(CustomEvent event) {
        synchronized (FabricUtils.class) {
            if (!isFabricInited()) {
                sEventList.add(event);
            } else {
                Answers.getInstance().logCustom(event);
            }
        }
    }

    public static void logQueueEvent() {
        synchronized (FabricUtils.class) {
            for (CustomEvent event : sEventList) {
                Answers.getInstance().logCustom(event);
            }
            sEventList.clear();
        }
    }

    public static void logNonFatal(String msg) {
        if (isFabricInited()) {
            CrashlyticsCore.getInstance().logException(
                    new CrashlyticsLog(msg));
        }
    }
}
