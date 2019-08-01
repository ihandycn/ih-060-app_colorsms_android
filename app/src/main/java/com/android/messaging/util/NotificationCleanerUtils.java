package com.android.messaging.util;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;

public class NotificationCleanerUtils {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String PREF_KEY_LAUNCHER_SETTINGS_NOTIFICATION_OPENED = "PREF_KEY_LAUNCHER_SETTINGS_NOTIFICATION_OPENED";
    private static final String PREF_KEY_NOTIFICATION_CLEANER_OPENED = "NOTIFICATION_CLEANER_OPENED";
    private static final String PREF_KEY_JUNK_CLEANER_SETTINGS_NOTIFICATION_OPENED = "PREF_KEY_JUNK_CLEANER_SETTINGS_NOTIFICATION_OPENED";

    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    public static final String EXTRA_IS_AUTHORIZATION_SUCCESS = "EXTRA_IS_AUTHORIZATION_SUCCESS";

    public static final String PREF_KEY_NOTIFICATION_BLOCKED_ACTIVITY_FAKE_INFO_ON_SHOW = "PREF_KEY_NOTIFICATION_BLOCKED_ACTIVITY_FAKE_INFO_ON_SHOW";
    public static final String PREF_KEY_NOTIFICATION_BLOCKED_ACTIVITY_FAKE_INFO_LOGGED = "PREF_KEY_NOTIFICATION_BLOCKED_ACTIVITY_FAKE_INFO_LOGGED";

    public static final String PREF_KEY_NOTIFICATION_CLEANER_SETTINGS_EVER_SWITCHED = "PREF_KEY_NOTIFICATION_CLEANER_SETTINGS_EVER_SWITCHED";

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

    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        final int width = drawable.getIntrinsicWidth();
        final int height = drawable.getIntrinsicHeight();

        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);

        drawable.setBounds(0, 0, width, height);
        drawable.draw(new Canvas(bitmap));

        return bitmap;
    }
}
