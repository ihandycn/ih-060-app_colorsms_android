package com.android.messaging.scheduledmessage;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.action.Action;
import com.android.messaging.datamodel.action.ActionService;
import com.android.messaging.datamodel.data.MessageData;
import com.ihs.app.framework.HSApplication;

public class HandleScheduledMessageAction extends Action implements Parcelable {
    public static PendingIntent getPendingIntentForHandleScheduledMessage(long messageId) {
        final Action action = new HandleScheduledMessageAction(messageId);
        return ActionService.makeStartActionPendingIntent(HSApplication.getContext(), action, (int) messageId, false);
    }

    private HandleScheduledMessageAction(long messageId) {
        super();
        actionParameters.putLong("message_id", messageId);
    }

    @Override
    protected Object executeAction() {
        long alarmMessageId = actionParameters.getLong("message_id");
        DatabaseWrapper db = DataModel.get().getDatabase();
        Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE,
                new String[]{
                        DatabaseHelper.MessageColumns._ID,
                        DatabaseHelper.MessageColumns.SCHEDULED_TIME
                },
                DatabaseHelper.MessageColumns.SCHEDULED_TIME + " !=0"
                        + " AND " + DatabaseHelper.MessageColumns.STATUS +
                        "=" + MessageData.BUGLE_STATUS_OUTGOING_SCHEDULED,
                null, null, null, DatabaseHelper.MessageColumns.SCHEDULED_TIME);
        while (cursor.moveToNext()) {
            long messageId = cursor.getLong(0);
            long scheduledTime = cursor.getLong(1);
            if (scheduledTime <= System.currentTimeMillis() + 3000) {
                //send message
                if (scheduledTime < System.currentTimeMillis() - 3 * DateUtils.MINUTE_IN_MILLIS) {
                    markMessageFailed(db, String.valueOf(messageId));
                } else {
                    if (alarmMessageId == messageId) {
                        SendScheduledMessageAction.sendMessage(String.valueOf(messageId));
                    }
                }
            } else {
                return 0;
            }
        }
        return 0;
    }

    private void markMessageFailed(DatabaseWrapper db, String messageId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.MessageColumns.STATUS, MessageData.BUGLE_STATUS_OUTGOING_SCHEDULED_FAILED);
        values.put(DatabaseHelper.MessageColumns.SCHEDULED_TIME, 0);
        db.update(DatabaseHelper.MESSAGES_TABLE, values,
                DatabaseHelper.MessageColumns._ID + "=?", new String[]{messageId});
    }

    private HandleScheduledMessageAction(final Parcel in) {
        super(in);
    }

    public static final Creator<HandleScheduledMessageAction> CREATOR
            = new Creator<HandleScheduledMessageAction>() {
        @Override
        public HandleScheduledMessageAction createFromParcel(final Parcel in) {
            return new HandleScheduledMessageAction(in);
        }

        @Override
        public HandleScheduledMessageAction[] newArray(final int size) {
            return new HandleScheduledMessageAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
