package com.android.messaging.util;

import android.os.Handler;
import android.provider.Telephony;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import hugo.weaving.DebugLog;

public class DefaultSMSUtils {

    private static final int EVENT_CACHE_VALIDATION_PERIOD = 0;
    private static final int CACHE_INVALIDATE_INTERVAL = 2000;

    private static Object cacheValidationLock = null;
    private static boolean sIsDefautSMS = false;
    private static Handler sHandler = new Handler();

    /**
     * Is Messaging the default SMS app?
     * - On KLP+ this checks the system setting.
     * - On JB (and below) this always returns true, since the setting was added in KLP.
     */
    @DebugLog
    public static boolean isDefaultSmsApp() {
        if (!OsUtil.isAtLeastKLP()) {
            return true;
        } 

        if ((cacheValidationLock != null)
                || sHandler.hasMessages(EVENT_CACHE_VALIDATION_PERIOD)) {
            HSLog.d("detect is default app : hit cache");
            return sIsDefautSMS;
        }

        long time = System.currentTimeMillis();
        final String configuredApplication = Telephony.Sms.getDefaultSmsPackage(HSApplication.getContext());
        HSLog.d("detect is default app : cost time " + (System.currentTimeMillis() - time));
        sIsDefautSMS = HSApplication.getContext().getPackageName().equals(configuredApplication);
        cacheValidationLock = new Object();
        sHandler.sendEmptyMessageDelayed(EVENT_CACHE_VALIDATION_PERIOD, CACHE_INVALIDATE_INTERVAL);
        return sIsDefautSMS;
    }

    public static void invalidateCache() {
        cacheValidationLock = null;
        sHandler.removeCallbacksAndMessages(null);
        HSLog.d("detect is default app : invalidate cache");
    }
}
