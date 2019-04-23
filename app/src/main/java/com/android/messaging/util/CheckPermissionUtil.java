package com.android.messaging.util;

import com.android.messaging.sms.MmsSmsUtils;
import com.ihs.app.framework.HSApplication;

public class CheckPermissionUtil {
    //if "Manifest.permission.READ_SMS" is not granted,
    //method getOrCreateThreadId() will throw IllegalArgumentException("Unable to find or allocate a thread ID.")
    //we cannot access telephony message database to getOrCreateThreadId,
    //so if return false(permission not granted), stop receive/send messages actions
    public static boolean isSmsPermissionGranted() {
        try {
            MmsSmsUtils.Threads.getOrCreateThreadId(HSApplication.getContext(), String.valueOf(987089));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
