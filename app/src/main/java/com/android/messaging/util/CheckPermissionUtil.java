package com.android.messaging.util;

import com.ihs.app.framework.HSApplication;
import com.superapps.util.RuntimePermissions;

public class CheckPermissionUtil {
    public static boolean isSmsPermissionGranted() {
        return RuntimePermissions.checkSelfPermission(HSApplication.getContext(), "android.permission-group.SMS") == RuntimePermissions.PERMISSION_GRANTED;
    }

    public static void requestPermissionIfNeed() {
        if (!isSmsPermissionGranted()) {
            //RuntimePermissions.requestPermissions();
        }
    }
}
