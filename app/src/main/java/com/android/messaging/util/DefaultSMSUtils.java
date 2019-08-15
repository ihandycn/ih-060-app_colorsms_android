package com.android.messaging.util;

import android.provider.Telephony;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import hugo.weaving.DebugLog;

public class DefaultSMSUtils {

    private static volatile boolean sIsDefaultSms;
    private static volatile boolean sInvalidateCache;

    static {
        final String configuredApplication = Telephony.Sms.getDefaultSmsPackage(HSApplication.getContext());
        sIsDefaultSms = HSApplication.getContext().getPackageName().equals(configuredApplication);
    }

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
        if (sInvalidateCache) {
            sInvalidateCache = false;
            return isDefaultSmsApp(true);
        }
        return sIsDefaultSms;
    }

    public static boolean isDefaultSmsApp(boolean forceRefresh) {
        if (!OsUtil.isAtLeastKLP()) {
            return true;
        }
        if (forceRefresh) {
            final String configuredApplication = Telephony.Sms.getDefaultSmsPackage(HSApplication.getContext());
            sIsDefaultSms = HSApplication.getContext().getPackageName().equals(configuredApplication);
        }
        return sIsDefaultSms;
    }

    public static void setIsDefaultSms(boolean isDefault) {
        sIsDefaultSms = isDefault;
    }

    public static void invalidateCache() {
        sInvalidateCache = true;
        HSLog.d("detect is default app : invalidate cache");
    }
}
