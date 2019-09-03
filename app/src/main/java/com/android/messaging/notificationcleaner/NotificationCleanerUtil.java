package com.android.messaging.notificationcleaner;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.android.messaging.BugleFiles;
import com.android.messaging.R;
import com.android.messaging.datamodel.NotificationServiceV18;
import com.android.messaging.notificationcleaner.data.BlockedNotificationDBHelper;
import com.android.messaging.notificationcleaner.data.BlockedNotificationInfo;
import com.android.messaging.notificationcleaner.data.NotificationCleanerProvider;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Permissions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

public class NotificationCleanerUtil {

    private static final String PREF_KEY_NOTIFICATION_CLEANER_EVER_OPENED = "PREF_KEY_NOTIFICATION_CLEANER_EVER_OPENED";
    public static final String PREF_KEY_NOTIFICATION_BLOCKED_ACTIVITY_ILLUSTRATED_PAGE_SHOWED = "PREF_KEY_NOTIFICATION_BLOCKED_ACTIVITY_ILLUSTRATED_PAGE_SHOWED";
    public static final String PREF_KEY_HAS_INSERTED_FAKE_NOTIFICATIONS = "PREF_KEY_HAS_INSERTED_FAKE_NOTIFICATIONS";

    public static final int NOTIFICATION_STATE_NOT_SHOWED = 0;
    public static final int NOTIFICATION_STATE_SHOWING = 1;
    public static final int NOTIFICATION_STATE_SHOWED = 2;

    public static final String FAKE_NOTIFICATION_PACKAGE_NAME_1 = "com.fakeinfo.1";
    public static final String FAKE_NOTIFICATION_PACKAGE_NAME_2 = "com.fakeinfo.2";

    public static boolean isNotificationOrganizerEnabled() {
        boolean isNotificationAccessGranted = Permissions.isNotificationAccessGranted();
        boolean isNotificationOrganizerSwitchOn = NotificationCleanerProvider.isNotificationOrganizerSwitchOn();
        return isNotificationAccessGranted && isNotificationOrganizerSwitchOn;
    }

    public static void setNotificationCleanerEverOpened() {
        Preferences.get(BugleFiles.NOTIFICATION_CLEANER_PREFS).putBoolean(PREF_KEY_NOTIFICATION_CLEANER_EVER_OPENED, true);
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

    public static boolean hasNotificationCleanerEverOpened() {
        return Preferences.get(BugleFiles.NOTIFICATION_CLEANER_PREFS).getBoolean(PREF_KEY_NOTIFICATION_CLEANER_EVER_OPENED, false);
    }

    public static int getNotificationBlockedActivityIllustratePageShowingState() {
        return Preferences.get(BugleFiles.NOTIFICATION_CLEANER_PREFS).getInt(PREF_KEY_NOTIFICATION_BLOCKED_ACTIVITY_ILLUSTRATED_PAGE_SHOWED, NOTIFICATION_STATE_NOT_SHOWED);
    }

    public static void setNotificationBlockedActivityIllustratePageShowingState(int state) {
        Preferences.get(BugleFiles.NOTIFICATION_CLEANER_PREFS).putInt(PREF_KEY_NOTIFICATION_BLOCKED_ACTIVITY_ILLUSTRATED_PAGE_SHOWED, state);
    }

    public static void autoOpenNotificationCleanerIfNeeded() {
        if (!hasNotificationCleanerEverOpened()
                && Permissions.isNotificationAccessGranted()) {
            Intent broadcastReceiverIntent = new Intent(NotificationServiceV18.ACTION_NOTIFICATION_CLEANER_AUTO_OPEN);
            broadcastReceiverIntent.setPackage(HSApplication.getContext().getPackageName());
            HSApplication.getContext().sendBroadcast(broadcastReceiverIntent);
        }
    }

    public static void insertFakeNotificationsIfNeeded() {
        if (Preferences.get(BugleFiles.NOTIFICATION_CLEANER_PREFS).getBoolean(PREF_KEY_HAS_INSERTED_FAKE_NOTIFICATIONS, false)) {
            return;
        }

        Preferences.get(BugleFiles.NOTIFICATION_CLEANER_PREFS).putBoolean(PREF_KEY_HAS_INSERTED_FAKE_NOTIFICATIONS, true);

        Threads.postOnThreadPoolExecutor(() -> {
            BlockedNotificationInfo fakeNotification2 = new BlockedNotificationInfo();
            fakeNotification2.packageName = NotificationCleanerUtil.FAKE_NOTIFICATION_PACKAGE_NAME_2;
            fakeNotification2.title = HSApplication.getContext().getString(R.string.notification_cleaner_fake_notification_title_2);
            fakeNotification2.text = HSApplication.getContext().getString(R.string.notification_cleaner_fake_notification_body_2);
            fakeNotification2.postTime = System.currentTimeMillis();

            ContentValues contentValues = new ContentValues();
            contentValues.put(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_PACKAGE_NAME, fakeNotification2.packageName);
            contentValues.put(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_TITLE, fakeNotification2.title);
            contentValues.put(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_TEXT, fakeNotification2.text);
            contentValues.put(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_POST_TIME, fakeNotification2.postTime);
            Uri newUri = HSApplication.getContext().getContentResolver().insert(
                    NotificationCleanerProvider.createBlockNotificationContentUri(HSApplication.getContext()), contentValues);
            HSLog.d(NotificationCleanerUtil.class.getSimpleName(), "insert fake notification2 result is " + ContentUris.parseId(newUri));

            BlockedNotificationInfo fakeNotification1 = new BlockedNotificationInfo();
            fakeNotification1.packageName = NotificationCleanerUtil.FAKE_NOTIFICATION_PACKAGE_NAME_1;
            fakeNotification1.title = HSApplication.getContext().getString(R.string.notification_cleaner_fake_notification_title_1);
            fakeNotification1.text = HSApplication.getContext().getString(R.string.notification_cleaner_fake_notification_body_1);
            fakeNotification1.postTime = System.currentTimeMillis();

            contentValues = new ContentValues();
            contentValues.put(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_PACKAGE_NAME, fakeNotification1.packageName);
            contentValues.put(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_TITLE, fakeNotification1.title);
            contentValues.put(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_TEXT, fakeNotification1.text);
            contentValues.put(BlockedNotificationDBHelper.COLUMN_NOTIFICATION_POST_TIME, fakeNotification1.postTime);
            newUri = HSApplication.getContext().getContentResolver().insert(
                    NotificationCleanerProvider.createBlockNotificationContentUri(HSApplication.getContext()), contentValues);
            HSLog.d(NotificationCleanerUtil.class.getSimpleName(), "insert fake notification1 result is " + ContentUris.parseId(newUri));
        });
    }
}
