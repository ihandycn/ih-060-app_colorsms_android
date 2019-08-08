package com.android.messaging.scheduledmessage;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.text.format.DateUtils;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.data.MessageData;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Threads;

public class MessageScheduleManager {

    public static void resetAllScheduledTask() {
        Threads.postOnThreadPoolExecutor(() -> {
            DatabaseWrapper db = DataModel.get().getDatabase();
            Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE,
                    new String[]{
                            DatabaseHelper.MessageColumns._ID,
                            DatabaseHelper.MessageColumns.SCHEDULED_TIME
                    },
                    DatabaseHelper.MessageColumns.SCHEDULED_TIME + " != 0", null,
                    null, null, DatabaseHelper.MessageColumns.SCHEDULED_TIME);

            if (cursor.getCount() == 0) {
                return;
            }

            while (cursor.moveToNext()) {
                int messageId = cursor.getInt(0);
                long scheduledTime = cursor.getLong(1);
                cancelScheduledTask(messageId);
                if (scheduledTime <= System.currentTimeMillis() + 3000) {
                    //send message
                    if (scheduledTime < System.currentTimeMillis() - 3 * DateUtils.MINUTE_IN_MILLIS) {
                        markMessageFailed(db, String.valueOf(messageId));
                    } else {
                        SendScheduledMessageAction.sendMessage(String.valueOf(messageId));
                    }
                } else {
                    addScheduledTask(messageId, scheduledTime);
                }
            }
        });
    }

    private static void markMessageFailed(DatabaseWrapper db, String messageId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.MessageColumns.STATUS, MessageData.BUGLE_STATUS_OUTGOING_FAILED);
        values.put(DatabaseHelper.MessageColumns.SCHEDULED_TIME, 0);
        db.update(DatabaseHelper.MESSAGES_TABLE, values,
                DatabaseHelper.MessageColumns._ID + "=?", new String[]{messageId});
    }

    public static void addScheduledTask(int messageId, long scheduledTime) {
        AlarmManager alarmManager = (AlarmManager) HSApplication.getContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = HandleScheduledMessageAction.getPendingIntentForHandleScheduledMessage(messageId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledTime - 100, intent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduledTime - 100, intent);
        }
    }

    public static void cancelScheduledTask(int messageId) {
        AlarmManager alarmManager = (AlarmManager) HSApplication.getContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = HandleScheduledMessageAction.getPendingIntentForHandleScheduledMessage(messageId);
        alarmManager.cancel(intent);
    }
}
