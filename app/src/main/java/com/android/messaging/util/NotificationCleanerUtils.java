package com.android.messaging.util;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

public class NotificationCleanerUtils {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    public static boolean isNotificationAccessGranted(Context context) {
        final String flat = Settings.Secure.getString(context.getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);

        if (TextUtils.isEmpty(flat)) {
            return false;
        }

        for (String name : flat.split(":")) {
            final ComponentName componentName = ComponentName.unflattenFromString(name);

            if (componentName != null
                && TextUtils.equals(context.getPackageName(), componentName.getPackageName())) {

                return true;
            }
        }

        return false;
    }
}
