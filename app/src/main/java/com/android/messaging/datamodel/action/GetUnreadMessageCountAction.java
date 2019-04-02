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

        // Update local db
        db.beginTransaction();
        try {
            final Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE, new String[]{MessageColumns.READ},
                    MessageColumns.STATUS + "=\"" + BUGLE_STATUS_INCOMING_COMPLETE + "\" AND "
                            + MessageColumns.READ + "!=?", new String[]{"1"},
                    null, null, null, null);
            UnreadMessageManager.getInstance().setUnreadMessageCount(cursor.getCount());
            cursor.close();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
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
