package com.android.messaging.notificationcleaner.resultpage.util;

import android.app.Activity;
import android.content.Intent;

import com.android.messaging.R;
import com.android.messaging.notificationcleaner.resultpage.ResultPageActivity;
import com.superapps.util.Navigations;

public class ResultTransitionUtils {

    public static void startForNotificationCleaner(Activity activity, int clearNotificationsCount) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, ResultPageActivity.class);
        intent.putExtra(ResultPageActivity.EXTRA_KEY_CLEAR_NOTIFICATIONS_COUNT, clearNotificationsCount);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Navigations.startActivitySafely(activity, intent);
        activity.overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
    }
}
