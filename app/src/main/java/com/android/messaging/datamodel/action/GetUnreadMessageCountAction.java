package com.android.messaging.datamodel.action;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseHelper.MessageColumns;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.sms.UnreadMessageManager;

import static com.android.messaging.datamodel.data.MessageData.BUGLE_STATUS_INCOMING_COMPLETE;

public class GetUnreadMessageCountAction extends Action implements Parcelable {

    public static void refreshUnreadMessageCount() {
        new GetUnreadMessageCountAction().start();
    }

    public GetUnreadMessageCountAction() {

    }

    @Override
    protected Object executeAction() {
        final DatabaseWrapper db = DataModel.get().getDatabase();

        final Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM "
                        + DatabaseHelper.MESSAGES_TABLE + ","
                        + DatabaseHelper.PARTICIPANTS_TABLE
                        + " WHERE " + DatabaseHelper.MESSAGES_TABLE + "." + MessageColumns.SENDER_PARTICIPANT_ID
                        + "=" + DatabaseHelper.PARTICIPANTS_TABLE + "." + DatabaseHelper.ParticipantColumns._ID
                        + " AND " + MessageColumns.STATUS + "=\"" + BUGLE_STATUS_INCOMING_COMPLETE + "\" "
                        + " AND " + MessageColumns.READ + "!=1 "
                        + " AND " + DatabaseHelper.ParticipantColumns.BLOCKED + " = 0"
                , null);
        if (cursor != null && cursor.moveToFirst()) {
            UnreadMessageManager.getInstance().setUnreadMessageCount(cursor.getInt(0));
            cursor.close();
        } else {
            UnreadMessageManager.getInstance().setUnreadMessageCount(0);
        }
        return null;
    }

    public GetUnreadMessageCountAction(final Parcel in) {
        super(in);
    }

    public static final Creator<GetUnreadMessageCountAction> CREATOR
            = new Creator<GetUnreadMessageCountAction>() {
        @Override
        public GetUnreadMessageCountAction createFromParcel(final Parcel in) {
            return new GetUnreadMessageCountAction(in);
        }

        @Override
        public GetUnreadMessageCountAction[] newArray(final int size) {
            return new GetUnreadMessageCountAction[size];
        }
    };

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        writeActionToParcel(parcel, flags);
    }
}
