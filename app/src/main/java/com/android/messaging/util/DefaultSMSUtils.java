package com.android.messaging.util;

import android.provider.Telephony;

import com.android.messaging.Factory;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

public class DefaultSMSUtils {

    private static Object cacheValidationLock = null;
    private static boolean sIsDefautSMS = false;

    /**
     * Is Messaging the default SMS app?
     * - On KLP+ this checks the system setting.
     * - On JB (and below) this always returns true, since the setting was added in KLP.
     */
    public static boolean isDefaultSmsApp() {
        if (OsUtil.isAtLeastKLP()) {
            if (cacheValidationLock != null && Factory.get().getIsForeground()) {
                HSLog.d("detect is default app : hit cache");
                return sIsDefautSMS;
            }

            long time = System.currentTimeMillis();
            final String configuredApplication = Telephony.Sms.getDefaultSmsPackage(HSApplication.getContext());
            HSLog.d("detect is default app : cost time " + (System.currentTimeMillis() - time));
            sIsDefautSMS = HSApplication.getContext().getPackageName().equals(configuredApplication);
            cacheValidationLock = new Object();
            return sIsDefautSMS;
        }
        return true;
    }

    public static void invalidateCache() {
        cacheValidationLock = null;
        HSLog.d("detect is default app : invalidate cache");
    }
}
