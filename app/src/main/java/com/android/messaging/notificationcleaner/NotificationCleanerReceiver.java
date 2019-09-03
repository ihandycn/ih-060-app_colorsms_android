package com.android.messaging.notificationcleaner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.android.messaging.notificationcleaner.data.BlockedNotificationDBHelper;
import com.android.messaging.notificationcleaner.data.NotificationCleanerProvider;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.PostOnNextFrameReceiver;
import com.superapps.util.Threads;

public class NotificationCleanerReceiver extends PostOnNextFrameReceiver {

    @Override
    protected void onPostReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        if (TextUtils.isEmpty(intent.getAction())) {
            return;
        }

        switch (intent.getAction()) {
            case Intent.ACTION_PACKAGE_REMOVED:

                if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    break;
                }

                Uri uri = intent.getData();
                if (uri == null) {
                    break;
                }

                final String packageName = uri.getSchemeSpecificPart();
                if (TextUtils.isEmpty(packageName)) {
                    break;
                }

                Threads.postOnThreadPoolExecutor(() -> {
                    HSApplication.getContext().getContentResolver()
                            .delete(NotificationCleanerProvider.createBlockNotificationContentUri(HSApplication.getContext()),
                                    BlockedNotificationDBHelper.COLUMN_NOTIFICATION_PACKAGE_NAME + "=?", new String[]{packageName});
                    NotificationBarUtil.checkToUpdateBlockedNotification();
                });
                break;
            default:
                break;
        }
    }
}
